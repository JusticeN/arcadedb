/*
 * Copyright 2021 Arcade Data Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.arcadedb.database;

import com.arcadedb.ContextConfiguration;
import com.arcadedb.GlobalConfiguration;
import com.arcadedb.Profiler;
import com.arcadedb.database.async.DatabaseAsyncExecutor;
import com.arcadedb.database.async.ErrorCallback;
import com.arcadedb.database.async.OkCallback;
import com.arcadedb.engine.Bucket;
import com.arcadedb.engine.Dictionary;
import com.arcadedb.engine.*;
import com.arcadedb.exception.*;
import com.arcadedb.graph.*;
import com.arcadedb.index.Index;
import com.arcadedb.index.IndexCursor;
import com.arcadedb.index.TypeIndex;
import com.arcadedb.index.lsm.LSMTreeIndexCompacted;
import com.arcadedb.index.lsm.LSMTreeIndexMutable;
import com.arcadedb.log.LogManager;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.SchemaImpl;
import com.arcadedb.schema.VertexType;
import com.arcadedb.serializer.BinarySerializer;
import com.arcadedb.sql.executor.*;
import com.arcadedb.sql.parser.*;
import com.arcadedb.utility.FileUtils;
import com.arcadedb.utility.LockException;
import com.arcadedb.utility.MultiIterator;
import com.arcadedb.utility.RWLockContext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class EmbeddedDatabase extends RWLockContext implements DatabaseInternal {
  protected static final Set<String>                               SUPPORTED_FILE_EXT      = new HashSet<String>(Arrays
      .asList(Dictionary.DICT_EXT, Bucket.BUCKET_EXT, LSMTreeIndexMutable.NOTUNIQUE_INDEX_EXT, LSMTreeIndexMutable.UNIQUE_INDEX_EXT,
          LSMTreeIndexCompacted.NOTUNIQUE_INDEX_EXT, LSMTreeIndexCompacted.UNIQUE_INDEX_EXT));
  public final           AtomicLong                                indexCompactions        = new AtomicLong();
  protected final        String                                    name;
  protected final        PaginatedFile.MODE                        mode;
  protected final        ContextConfiguration                      configuration;
  protected final        String                                    databasePath;
  protected final        BinarySerializer                          serializer              = new BinarySerializer();
  protected final        RecordFactory                             recordFactory           = new RecordFactory();
  protected final        GraphEngine                               graphEngine             = new GraphEngine();
  protected final        WALFileFactory                            walFactory;
  protected final        DocumentIndexer                           indexer;
  // STATISTICS
  private final          AtomicLong                                statsTxCommits          = new AtomicLong();
  private final          AtomicLong                                statsTxRollbacks        = new AtomicLong();
  private final          AtomicLong                                statsCreateRecord       = new AtomicLong();
  private final          AtomicLong                                statsReadRecord         = new AtomicLong();
  private final          AtomicLong                                statsUpdateRecord       = new AtomicLong();
  private final          AtomicLong                                statsDeleteRecord       = new AtomicLong();
  private final          AtomicLong                                statsQueries            = new AtomicLong();
  private final          AtomicLong                                statsCommands           = new AtomicLong();
  private final          AtomicLong                                statsScanType           = new AtomicLong();
  private final          AtomicLong                                statsScanBucket         = new AtomicLong();
  private final          AtomicLong                                statsIterateType        = new AtomicLong();
  private final          AtomicLong                                statsIterateBucket      = new AtomicLong();
  private final          AtomicLong                                statsCountType          = new AtomicLong();
  private final          AtomicLong                                statsCountBucket        = new AtomicLong();
  protected              FileManager                               fileManager;
  protected              PageManager                               pageManager;
  protected              SchemaImpl                                schema;
  protected              TransactionManager                        transactionManager;
  protected volatile     DatabaseAsyncExecutor                     async                   = null;
  protected              Lock                                      asyncLock               = new ReentrantLock();
  protected              boolean                                   autoTransaction         = false;
  protected volatile     boolean                                   open                    = false;
  private                boolean                                   readYourWrites          = true;
  private                File                                      lockFile;
  private                FileLock                                  lockFileIO;
  private                Map<CALLBACK_EVENT, List<Callable<Void>>> callbacks;
  private                StatementCache                            statementCache;
  private                ExecutionPlanCache                        executionPlanCache;
  private                DatabaseInternal                          wrappedDatabaseInstance = this;

  protected EmbeddedDatabase(final String path, final PaginatedFile.MODE mode, final ContextConfiguration configuration,
      final Map<CALLBACK_EVENT, List<Callable<Void>>> callbacks) {
    try {
      this.mode = mode;
      this.configuration = configuration;
      this.callbacks = callbacks;
      this.walFactory = mode == PaginatedFile.MODE.READ_WRITE ? new WALFileFactoryEmbedded() : null;
      this.statementCache = new StatementCache(this, configuration.getValueAsInteger(GlobalConfiguration.SQL_STATEMENT_CACHE));
      this.executionPlanCache = new ExecutionPlanCache(this, configuration.getValueAsInteger(GlobalConfiguration.SQL_STATEMENT_CACHE));

      if (path.endsWith("/"))
        databasePath = path.substring(0, path.length() - 1);
      else
        databasePath = path;

      final int lastSeparatorPos = path.lastIndexOf("/");
      if (lastSeparatorPos > -1)
        name = path.substring(lastSeparatorPos + 1);
      else
        name = path;

      indexer = new DocumentIndexer(this);

    } catch (Exception e) {
      if (e instanceof DatabaseOperationException)
        throw (DatabaseOperationException) e;

      throw new DatabaseOperationException("Error on creating new database instance", e);
    }
  }

  protected void open() {
    if (!new File(databasePath).exists())
      throw new DatabaseOperationException("Database '" + databasePath + "' not exists");

    final File file = new File(databasePath + "/configuration.json");
    if (file.exists()) {
      try {
        final String content = FileUtils.readFileAsString(file, "UTF8");
        configuration.reset();
        configuration.fromJSON(content);
      } catch (IOException e) {
        LogManager.instance().log(this, Level.SEVERE, "Error on loading configuration from file '%s'", e, file);
      }
    }

    openInternal();
  }

  protected void create() {
    if (new File(databasePath + "/" + SchemaImpl.SCHEMA_FILE_NAME).exists() || new File(databasePath + "/" + SchemaImpl.SCHEMA_PREV_FILE_NAME).exists())
      throw new DatabaseOperationException("Database '" + databasePath + "' already exists");

    openInternal();

    schema.saveConfiguration();

    String cfgFileName = databasePath + "/configuration.json";
    try {
      FileUtils.writeFile(new File(cfgFileName), configuration.toJSON());
    } catch (IOException e) {
      LogManager.instance().log(this, Level.SEVERE, "Error on saving configuration to file '%s'", e, cfgFileName);
    }
  }

  private void openInternal() {
    try {
      DatabaseContext.INSTANCE.init(this);

      fileManager = new FileManager(databasePath, mode, SUPPORTED_FILE_EXT);
      transactionManager = new TransactionManager(wrappedDatabaseInstance);
      pageManager = new PageManager(fileManager, transactionManager, configuration);

      open = true;

      try {
        schema = new SchemaImpl(wrappedDatabaseInstance, databasePath, mode);

        if (fileManager.getFiles().isEmpty())
          schema.create(mode);
        else
          schema.load(mode);

        if (mode == PaginatedFile.MODE.READ_WRITE)
          checkForRecovery();

        Profiler.INSTANCE.registerDatabase(this);

      } catch (RuntimeException e) {
        open = false;
        pageManager.close();
        throw e;
      } catch (Exception e) {
        open = false;
        pageManager.close();
        throw new DatabaseOperationException("Error on creating new database instance", e);
      }
    } catch (Exception e) {
      open = false;

      if (e instanceof DatabaseOperationException)
        throw (DatabaseOperationException) e;

      throw new DatabaseOperationException("Error on creating new database instance", e);
    }
  }

  private void checkForRecovery() throws IOException {
    lockFile = new File(databasePath + "/database.lck");

    if (lockFile.exists()) {
      lockDatabase();

      // RECOVERY
      LogManager.instance().log(this, Level.WARNING, "Database '%s' was not closed properly last time", null, name);

      if (mode == PaginatedFile.MODE.READ_ONLY)
        throw new DatabaseMetadataException("Database needs recovery but has been open in read only mode");

      executeCallbacks(CALLBACK_EVENT.DB_NOT_CLOSED);

      transactionManager.checkIntegrity();
    } else {
      if (mode == PaginatedFile.MODE.READ_WRITE) {
        lockFile.createNewFile();
        lockDatabase();
      } else
        lockFile = null;
    }
  }

  @Override
  public void drop() {
    checkDatabaseIsOpen();
    if (mode == PaginatedFile.MODE.READ_ONLY)
      throw new DatabaseIsReadOnlyException("Cannot drop database");

    close();

    executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() {
        FileUtils.deleteRecursively(new File(databasePath));
        return null;
      }
    });
  }

  @Override
  public void close() {
    if (async != null) {
      // EXECUTE OUTSIDE LOCK
      async.waitCompletion();
      async.close();
    }

    executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() {
        if (!open)
          return null;

        open = false;

        if (async != null)
          async.close();

        final DatabaseContext.DatabaseContextTL dbContext = DatabaseContext.INSTANCE.removeContext(databasePath);
        if (dbContext != null && !dbContext.transactions.isEmpty()) {
          // ROLLBACK ALL THE TX FROM LAST TO FIRST
          for (int i = dbContext.transactions.size() - 1; i > -1; --i) {
            final TransactionContext tx = dbContext.transactions.get(i);
            if (tx.isActive())
              // ROLLBACK ANY PENDING OPERATION
              tx.rollback();
          }
          dbContext.transactions.clear();
        }

        try {
          schema.close();
          pageManager.close();
          fileManager.close();
          transactionManager.close();
          statementCache.clear();

          if (lockFile != null) {
            try {
              lockFileIO.release();
            } catch (IOException e) {
              // IGNORE IT
            }
            lockFile.delete();
          }

        } finally {
          Profiler.INSTANCE.unregisterDatabase(EmbeddedDatabase.this);
        }
        return null;
      }
    });
  }

  public DatabaseAsyncExecutor async() {
    if (async == null) {
      asyncLock.lock();
      try {
        if (async == null)
          async = new DatabaseAsyncExecutor(wrappedDatabaseInstance);
      } finally {
        asyncLock.unlock();
      }
    }
    return async;
  }

  @Override
  public Map<String, Object> getStats() {
    final Map<String, Object> map = new HashMap<>();
    map.put("txCommits", statsTxCommits.get());
    map.put("txRollbacks", statsTxRollbacks.get());
    map.put("createRecord", statsCreateRecord.get());
    map.put("readRecord", statsReadRecord.get());
    map.put("updateRecord", statsUpdateRecord.get());
    map.put("deleteRecord", statsDeleteRecord.get());
    map.put("queries", statsQueries.get());
    map.put("commands", statsCommands.get());
    map.put("scanType", statsScanType.get());
    map.put("scanBucket", statsScanBucket.get());
    map.put("iterateType", statsIterateType.get());
    map.put("iterateBucket", statsIterateBucket.get());
    map.put("countType", statsCountType.get());
    map.put("countBucket", statsCountBucket.get());
    map.put("indexCompactions", indexCompactions.get());
    return map;
  }

  @Override
  public String getDatabasePath() {
    return databasePath;
  }

  public TransactionContext getTransaction() {
    final DatabaseContext.DatabaseContextTL dbContext = DatabaseContext.INSTANCE.getContext(databasePath);
    if (dbContext != null) {
      final TransactionContext tx = dbContext.getLastTransaction();
      if (tx != null) {
        final DatabaseInternal txDb = tx.getDatabase();
        if (txDb == null) {
          tx.rollback();
          throw new InvalidDatabaseInstanceException("Invalid transactional context (db is null)");
        }
        if (txDb.getEmbedded() != this) {
          try {
            DatabaseContext.INSTANCE.init(this);
          } catch (Exception e) {
            // IGNORE IT
          }
          throw new InvalidDatabaseInstanceException("Invalid transactional context (different db)");
        }
        return tx;
      }
    }
    return null;
  }

  @Override
  public void begin() {
    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        checkDatabaseIsOpen();

        // FORCE THE RESET OF TL
        final DatabaseContext.DatabaseContextTL current = DatabaseContext.INSTANCE.getContext(EmbeddedDatabase.this.getDatabasePath());
        TransactionContext tx = current.getLastTransaction();
        if (tx.isActive()) {
          // CREATE A NESTED TX
          tx = new TransactionContext(getWrappedDatabaseInstance());
          current.pushTransaction(tx);
        }

        tx.begin();

        return null;
      }
    });
  }

  public void incrementStatsTxCommits() {
    statsTxCommits.incrementAndGet();
  }

  @Override
  public void commit() {
    statsTxCommits.incrementAndGet();

    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        checkTransactionIsActive(false);

        final DatabaseContext.DatabaseContextTL current = DatabaseContext.INSTANCE.getContext(EmbeddedDatabase.this.getDatabasePath());
        current.popIfNotLastTransaction().commit();

        return null;
      }
    });
  }

  @Override
  public void rollback() {
    statsTxRollbacks.incrementAndGet();

    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        try {
          checkTransactionIsActive(false);

          final DatabaseContext.DatabaseContextTL current = DatabaseContext.INSTANCE.getContext(EmbeddedDatabase.this.getDatabasePath());
          current.popIfNotLastTransaction().rollback();

        } catch (TransactionException e) {
          // ALREADY ROLLBACKED
        }
        return null;
      }
    });
  }

  @Override
  public void rollbackAllNested() {
    statsTxRollbacks.incrementAndGet();

    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        final DatabaseContext.DatabaseContextTL current = DatabaseContext.INSTANCE.getContext(EmbeddedDatabase.this.getDatabasePath());

        while (true) {
          try {
            if (!isTransactionActive())
              break;

            current.popIfNotLastTransaction().rollback();

          } catch (InvalidDatabaseInstanceException e) {
            current.popIfNotLastTransaction().rollback();

          } catch (TransactionException e) {
            // ALREADY ROLLBACKED
          }
        }
        return null;
      }
    });
  }

  @Override
  public long countBucket(final String bucketName) {
    statsCountBucket.incrementAndGet();

    return (Long) executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        return schema.getBucketByName(bucketName).count();
      }
    });
  }

  @Override
  public long countType(final String typeName, final boolean polymorphic) {
    statsCountType.incrementAndGet();

    return (Long) executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        final DocumentType type = schema.getType(typeName);

        long total = 0;
        for (Bucket b : type.getBuckets(polymorphic))
          total += b.count();

        return total;
      }
    });
  }

  @Override
  public void scanType(final String typeName, final boolean polymorphic, final DocumentCallback callback) {
    statsScanType.incrementAndGet();

    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        boolean success = false;
        final boolean implicitTransaction = checkTransactionIsActive(autoTransaction);
        try {
          final DocumentType type = schema.getType(typeName);

          final AtomicBoolean continueScan = new AtomicBoolean(true);

          for (Bucket b : type.getBuckets(polymorphic)) {
            b.scan(new RawRecordCallback() {
              @Override
              public boolean onRecord(final RID rid, final Binary view) {
                final Document record = (Document) recordFactory.newImmutableRecord(wrappedDatabaseInstance, typeName, rid, view);
                continueScan.set(callback.onRecord(record));
                return continueScan.get();
              }
            });

            if (!continueScan.get())
              break;
          }

          success = true;

        } finally {
          if (implicitTransaction)
            if (success)
              wrappedDatabaseInstance.commit();
            else
              wrappedDatabaseInstance.rollback();
        }
        return null;
      }
    });
  }

  @Override
  public void scanBucket(final String bucketName, final RecordCallback callback) {
    statsScanBucket.incrementAndGet();

    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {

        checkDatabaseIsOpen();

        final String type = schema.getTypeNameByBucketId(schema.getBucketByName(bucketName).getId());
        schema.getBucketByName(bucketName).scan(new RawRecordCallback() {
          @Override
          public boolean onRecord(final RID rid, final Binary view) {
            final Record record = recordFactory.newImmutableRecord(wrappedDatabaseInstance, type, rid, view);
            return callback.onRecord(record);
          }
        });
        return null;
      }
    });
  }

  @Override
  public Iterator<Record> iterateType(final String typeName, final boolean polymorphic) {
    statsIterateType.incrementAndGet();

    return (Iterator<Record>) executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {

        checkDatabaseIsOpen();

        final DocumentType type = schema.getType(typeName);

        final MultiIterator iter = new MultiIterator();

        for (Bucket b : type.getBuckets(polymorphic))
          iter.add(b.iterator());

        return iter;
      }
    });
  }

  @Override
  public Iterator<Record> iterateBucket(final String bucketName) {
    statsIterateBucket.incrementAndGet();

    readLock();
    try {

      checkDatabaseIsOpen();
      try {
        final Bucket bucket = schema.getBucketByName(bucketName);
        return bucket.iterator();
      } catch (Exception e) {
        throw new DatabaseOperationException("Error on executing scan of bucket '" + bucketName + "'", e);
      }

    } finally {
      readUnlock();
    }
  }

  @Override
  public Record lookupByRID(final RID rid, final boolean loadContent) {
    if (rid == null)
      throw new IllegalArgumentException("record is null");

    statsReadRecord.incrementAndGet();

    return (Record) executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {

        checkDatabaseIsOpen();

        // CHECK IN TX CACHE FIRST
        final TransactionContext tx = getTransaction();
        Record record = tx.getRecordFromCache(rid);
        if (record != null)
          return record;

        final DocumentType type = schema.getTypeByBucketId(rid.getBucketId());

        if (loadContent || type == null) {
          final Binary buffer = schema.getBucketById(rid.getBucketId()).getRecord(rid);
          record = recordFactory.newImmutableRecord(wrappedDatabaseInstance, type != null ? type.getName() : null, rid, buffer.copy());
          return record;
        }

        record = recordFactory.newImmutableRecord(wrappedDatabaseInstance, type.getName(), rid, type.getType());

        return record;
      }
    });
  }

  @Override
  public IndexCursor lookupByKey(final String type, final String[] properties, final Object[] keys) {
    statsReadRecord.incrementAndGet();

    return (IndexCursor) executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {

        checkDatabaseIsOpen();
        final DocumentType t = schema.getType(type);

        final TypeIndex idx = t.getPolymorphicIndexByProperties(properties);
        if (idx == null)
          throw new IllegalArgumentException("No index has been created on type '" + type + "' properties " + Arrays.toString(properties));

        return idx.get(keys);
      }
    });
  }

  @Override
  public void registerCallback(final CALLBACK_EVENT event, final Callable<Void> callback) {
    List<Callable<Void>> callbacks = this.callbacks.get(event);
    if (callbacks == null) {
      callbacks = new ArrayList<Callable<Void>>();
      this.callbacks.put(event, callbacks);
    }
    callbacks.add(callback);
  }

  @Override
  public void unregisterCallback(final CALLBACK_EVENT event, final Callable<Void> callback) {
    List<Callable<Void>> callbacks = this.callbacks.get(event);
    if (callbacks != null) {
      callbacks.remove(callback);
      if (callbacks.isEmpty())
        this.callbacks.remove(event);
    }
  }

  @Override
  public GraphEngine getGraphEngine() {
    return graphEngine;
  }

  @Override
  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  @Override
  public boolean isReadYourWrites() {
    return readYourWrites;
  }

  @Override
  public void setReadYourWrites(final boolean readYourWrites) {
    this.readYourWrites = readYourWrites;
  }

  @Override
  public void createRecord(final MutableDocument record) {
    if (record.getIdentity() != null)
      throw new IllegalArgumentException("Cannot create record " + record.getIdentity() + " because it is already persistent");

    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        if (mode == PaginatedFile.MODE.READ_ONLY)
          throw new DatabaseIsReadOnlyException("Cannot create a new record because the database is open in read-only mode");

        boolean success = false;
        final boolean implicitTransaction = checkTransactionIsActive(autoTransaction);

        try {
          final DocumentType type = schema.getType(record.getType());

          final Bucket bucket = type.getBucketToSave(DatabaseContext.INSTANCE.getContext(databasePath).asyncMode);
          record.setIdentity(bucket.createRecord(record));
          indexer.createDocument(record, type, bucket);

          getTransaction().updateRecordInCache(record);

          success = true;

        } finally {
          if (implicitTransaction) {
            if (success)
              wrappedDatabaseInstance.commit();
            else
              wrappedDatabaseInstance.rollback();
          }
        }

        return null;
      }
    });
  }

  @Override
  public void createRecord(final Record record, final String bucketName) {
    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        createRecordNoLock(record, bucketName);
        return null;
      }
    });
  }

  @Override
  public void createRecordNoLock(final Record record, final String bucketName) {
    if (record.getIdentity() != null)
      throw new IllegalArgumentException("Cannot create record " + record.getIdentity() + " because it is already persistent");

    if (mode == PaginatedFile.MODE.READ_ONLY)
      throw new DatabaseIsReadOnlyException("Cannot create a new record");

    boolean success = false;
    final boolean implicitTransaction = checkTransactionIsActive(autoTransaction);

    try {
      final Bucket bucket = schema.getBucketByName(bucketName);
      ((RecordInternal) record).setIdentity(bucket.createRecord(record));
      getTransaction().updateRecordInCache(record);
      success = true;

    } finally {
      if (implicitTransaction) {
        if (success)
          wrappedDatabaseInstance.commit();
        else
          wrappedDatabaseInstance.rollback();
      }
    }
  }

  @Override
  public void updateRecord(final Record record) {
    if (record.getIdentity() == null)
      throw new IllegalArgumentException("Cannot update the record because it is not persistent");

    if (mode == PaginatedFile.MODE.READ_ONLY)
      throw new DatabaseIsReadOnlyException("Cannot update a record");

    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        markRecordForUpdateAtCommit(record);
        return null;
      }
    });
  }

  private void markRecordForUpdateAtCommit(final Record record) {
    if (isTransactionActive()) {
      // MARK THE RECORD FOR UPDATE IN TX AND DEFER THE SERIALIZATION AT COMMIT TIME. THIS SPEEDS UP CASES WHEN THE SAME RECORDS ARE UPDATE MULTIPLE TIME INSIDE
      // THE SAME TX. THE MOST CLASSIC EXAMPLE IS INSERTING EDGES: THE RECORD CHUNK IS UPDATED EVERYTIME A NEW EDGE IS CREATED IN THE SAME CHUNK.
      getTransaction().addUpdatedRecord(record);
    } else
      updateRecordNoLock(record);
  }

  @Override
  public void updateRecordNoLock(final Record record) {
    final List<Index> indexes = record instanceof Document ? indexer.getInvolvedIndexes((Document) record) : Collections.emptyList();

    if (!indexes.isEmpty()) {
      // UPDATE THE INDEXES TOO
      final Binary originalBuffer = ((RecordInternal) record).getBuffer();
      if (originalBuffer == null)
        throw new IllegalStateException("Cannot read original buffer for indexing");
      originalBuffer.rewind();
      final Document originalRecord = (Document) recordFactory.newImmutableRecord(this, ((Document) record).getType(), record.getIdentity(), originalBuffer);

      schema.getBucketById(record.getIdentity().getBucketId()).updateRecord(record);

      indexer.updateDocument(originalRecord, (Document) record, indexes);
    } else
      // NO INDEXES
      schema.getBucketById(record.getIdentity().getBucketId()).updateRecord(record);

    getTransaction().updateRecordInCache(record);
    getTransaction().removeImmutableRecordsOfSamePage(record.getIdentity());
  }

  @Override
  public void deleteRecord(final Record record) {
    if (record.getIdentity() == null)
      throw new IllegalArgumentException("Cannot delete a non persistent record");

    executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        if (mode == PaginatedFile.MODE.READ_ONLY)
          throw new DatabaseIsReadOnlyException("Cannot delete record " + record.getIdentity());

        boolean success = false;
        final boolean implicitTransaction = checkTransactionIsActive(autoTransaction);

        try {
          final Bucket bucket = schema.getBucketById(record.getIdentity().getBucketId());

          if (record instanceof Document)
            indexer.deleteDocument((Document) record);

          if (record instanceof Edge) {
            graphEngine.deleteEdge((Edge) record);
          } else if (record instanceof Vertex) {
            graphEngine.deleteVertex((VertexInternal) record);
          } else
            bucket.deleteRecord(record.getIdentity());

          getTransaction().removeRecordFromCache(record);
          getTransaction().removeImmutableRecordsOfSamePage(record.getIdentity());

          success = true;

        } finally {
          if (implicitTransaction) {
            if (success)
              wrappedDatabaseInstance.commit();
            else
              wrappedDatabaseInstance.rollback();
          }
        }
        return null;
      }
    });
  }

  @Override
  public boolean isTransactionActive() {
    final Transaction tx = getTransaction();
    return tx != null && tx.isActive();
  }

  @Override
  public void transaction(final TransactionScope txBlock) {
    transaction(txBlock, true, configuration.getValueAsInteger(GlobalConfiguration.TX_RETRIES));
  }

  @Override
  public boolean transaction(final TransactionScope txBlock, final boolean joinCurrentTx) {
    return transaction(txBlock, joinCurrentTx, configuration.getValueAsInteger(GlobalConfiguration.TX_RETRIES));
  }

  @Override
  public boolean transaction(final TransactionScope txBlock, final boolean joinCurrentTx, int retries) {
    return transaction(txBlock, joinCurrentTx, retries, null, null);
  }

  @Override
  public boolean transaction(final TransactionScope txBlock, final boolean joinCurrentTx, int retries, final OkCallback ok, final ErrorCallback error) {
    if (txBlock == null)
      throw new IllegalArgumentException("Transaction block is null");

    NeedRetryException lastException = null;

    if (retries < 1)
      retries = 1;

    for (int retry = 0; retry < retries; ++retry) {
      boolean txJoined = false;

      try {
        if (joinCurrentTx && wrappedDatabaseInstance.isTransactionActive())
          txJoined = true;
        else
          wrappedDatabaseInstance.begin();

        txBlock.execute(wrappedDatabaseInstance);

        if (!txJoined)
          wrappedDatabaseInstance.commit();

        if (ok != null)
          ok.call();

        // OK
        return !joinCurrentTx;

      } catch (NeedRetryException e) {
        // RETRY
        lastException = e;
        continue;
      } catch (Exception e) {
        final TransactionContext tx = getTransaction();
        if (tx != null && tx.isActive())
          rollback();

        if (error != null)
          error.call(e);

        throw e;
      }
    }

    if (error != null)
      error.call(lastException);

    throw lastException;
  }

  @Override
  public RecordFactory getRecordFactory() {
    return recordFactory;
  }

  @Override
  public Schema getSchema() {
    checkDatabaseIsOpen();
    return schema;
  }

  @Override
  public BinarySerializer getSerializer() {
    return serializer;
  }

  @Override
  public PageManager getPageManager() {
    checkDatabaseIsOpen();
    return pageManager;
  }

  @Override
  public MutableDocument newDocument(final String typeName) {
    if (typeName == null)
      throw new IllegalArgumentException("Type is null");

    final DocumentType type = schema.getType(typeName);
    if (!type.getClass().equals(DocumentType.class))
      throw new IllegalArgumentException("Cannot create a document of type '" + typeName + "' because is not a document type");

    statsCreateRecord.incrementAndGet();

    return new MutableDocument(wrappedDatabaseInstance, typeName, null);
  }

  @Override
  public MutableEmbeddedDocument newEmbeddedDocument(final String typeName) {
    if (typeName == null)
      throw new IllegalArgumentException("Type is null");

    final DocumentType type = schema.getType(typeName);
    if (!type.getClass().equals(DocumentType.class))
      throw new IllegalArgumentException("Cannot create an embedded document of type '" + typeName + "' because is not a document type");

    return new MutableEmbeddedDocument(wrappedDatabaseInstance, typeName);
  }

  @Override
  public MutableVertex newVertex(final String typeName) {
    if (typeName == null)
      throw new IllegalArgumentException("Type is null");

    final DocumentType type = schema.getType(typeName);
    if (!type.getClass().equals(VertexType.class))
      throw new IllegalArgumentException("Cannot create a vertex of type '" + typeName + "' because is not a vertex type");

    statsCreateRecord.incrementAndGet();

    return new MutableVertex(wrappedDatabaseInstance, typeName, null);
  }

  public Edge newEdgeByKeys(final String sourceVertexType, final String[] sourceVertexKey, final Object[] sourceVertexValue, final String destinationVertexType,
      final String[] destinationVertexKey, final Object[] destinationVertexValue, final boolean createVertexIfNotExist, final String edgeType,
      final boolean bidirectional, final Object... properties) {
    if (sourceVertexKey == null)
      throw new IllegalArgumentException("Source vertex key is null");

    if (sourceVertexKey.length != sourceVertexValue.length)
      throw new IllegalArgumentException("Source vertex key and value arrays have different sizes");

    if (destinationVertexKey == null)
      throw new IllegalArgumentException("Destination vertex key is null");

    if (destinationVertexKey.length != destinationVertexValue.length)
      throw new IllegalArgumentException("Destination vertex key and value arrays have different sizes");

    final Iterator<Identifiable> v1Result = lookupByKey(sourceVertexType, sourceVertexKey, sourceVertexValue);

    Vertex sourceVertex;
    if (!v1Result.hasNext()) {
      if (createVertexIfNotExist) {
        sourceVertex = newVertex(sourceVertexType);
        for (int i = 0; i < sourceVertexKey.length; ++i)
          ((MutableVertex) sourceVertex).set(sourceVertexKey[i], sourceVertexValue[i]);
        ((MutableVertex) sourceVertex).save();
      } else
        throw new IllegalArgumentException("Cannot find source vertex with key " + Arrays.toString(sourceVertexKey) + "=" + Arrays.toString(sourceVertexValue));
    } else
      sourceVertex = v1Result.next().getIdentity().getVertex();

    final Iterator<Identifiable> v2Result = lookupByKey(destinationVertexType, destinationVertexKey, destinationVertexValue);
    Vertex destinationVertex;
    if (!v2Result.hasNext()) {
      if (createVertexIfNotExist) {
        destinationVertex = newVertex(destinationVertexType);
        for (int i = 0; i < destinationVertexKey.length; ++i)
          ((MutableVertex) destinationVertex).set(destinationVertexKey[i], destinationVertexValue[i]);
        ((MutableVertex) destinationVertex).save();
      } else
        throw new IllegalArgumentException(
            "Cannot find destination vertex with key " + Arrays.toString(destinationVertexKey) + "=" + Arrays.toString(destinationVertexValue));
    } else
      destinationVertex = v2Result.next().getIdentity().getVertex();

    statsCreateRecord.incrementAndGet();

    return sourceVertex.newEdge(edgeType, destinationVertex, bidirectional, properties);
  }

  public Edge newEdgeByKeys(final Vertex sourceVertex, final String destinationVertexType, final String[] destinationVertexKey,
      final Object[] destinationVertexValue, final boolean createVertexIfNotExist, final String edgeType, final boolean bidirectional,
      final Object... properties) {
    if (sourceVertex == null)
      throw new IllegalArgumentException("Source vertex is null");

    if (destinationVertexKey == null)
      throw new IllegalArgumentException("Destination vertex key is null");

    if (destinationVertexKey.length != destinationVertexValue.length)
      throw new IllegalArgumentException("Destination vertex key and value arrays have different sizes");

    final Iterator<Identifiable> v2Result = lookupByKey(destinationVertexType, destinationVertexKey, destinationVertexValue);
    Vertex destinationVertex;
    if (!v2Result.hasNext()) {
      if (createVertexIfNotExist) {
        destinationVertex = newVertex(destinationVertexType);
        for (int i = 0; i < destinationVertexKey.length; ++i)
          ((MutableVertex) destinationVertex).set(destinationVertexKey[i], destinationVertexValue[i]);
        ((MutableVertex) destinationVertex).save();
      } else
        throw new IllegalArgumentException(
            "Cannot find destination vertex with key " + Arrays.toString(destinationVertexKey) + "=" + Arrays.toString(destinationVertexValue));
    } else
      destinationVertex = v2Result.next().getIdentity().getVertex();

    statsCreateRecord.incrementAndGet();

    return sourceVertex.newEdge(edgeType, destinationVertex, bidirectional, properties);
  }

  @Override
  public boolean isAutoTransaction() {
    return autoTransaction;
  }

  @Override
  public void setAutoTransaction(final boolean autoTransaction) {
    this.autoTransaction = autoTransaction;
  }

  @Override
  public FileManager getFileManager() {
    checkDatabaseIsOpen();
    return fileManager;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public PaginatedFile.MODE getMode() {
    return mode;
  }

  @Override
  public boolean checkTransactionIsActive(boolean createTx) {
    checkDatabaseIsOpen();

    if (!isTransactionActive()) {
      if (createTx) {
        wrappedDatabaseInstance.begin();
        return true;
      }
      throw new DatabaseOperationException("Transaction not begun");
    }

    return false;
  }

  /**
   * Test only API.
   */
  @Override
  public void kill() {
    if (async != null)
      async.kill();

    if (getTransaction().isActive())
      // ROLLBACK ANY PENDING OPERATION
      getTransaction().kill();

    try {
      schema.close();
      pageManager.kill();
      fileManager.close();
      transactionManager.kill();

      if (lockFile != null) {
        try {
          lockFileIO.release();
        } catch (IOException e) {
          // IGNORE IT
        }
      }

    } finally {
      open = false;
      Profiler.INSTANCE.unregisterDatabase(EmbeddedDatabase.this);
    }
  }

  @Override
  public DocumentIndexer getIndexer() {
    return indexer;
  }

  @Override
  public ResultSet command(final String language, final String query, final Object... parameters) {
    checkDatabaseIsOpen();

    statsCommands.incrementAndGet();

    final Statement statement = SQLEngine.parse(query, wrappedDatabaseInstance);
    final ResultSet original = statement.execute(wrappedDatabaseInstance, parameters);
    return original;
  }

  @Override
  public ResultSet command(final String language, final String query, final Map<String, Object> parameters) {
    checkDatabaseIsOpen();

    statsCommands.incrementAndGet();

    final Statement statement = SQLEngine.parse(query, wrappedDatabaseInstance);
    final ResultSet original = statement.execute(wrappedDatabaseInstance, parameters);
    return original;
  }

  @Override
  public ResultSet execute(String language, String script, Map<Object, Object> params) {
    checkDatabaseIsOpen();

    BasicCommandContext context = new BasicCommandContext();
    context.setDatabase(this);

    context.setInputParameters(params);

    final List<Statement> statements = SQLEngine.parseScript(script, wrappedDatabaseInstance);
    ResultSet original;
    try {
      original = executeInternal(statements, context);
    } finally {

    }
    LocalResultSetLifecycleDecorator result = new LocalResultSetLifecycleDecorator(original);
    return result;
  }

  @Override
  public ResultSet execute(String language, String script, Object... args) {
    checkDatabaseIsOpen();

    BasicCommandContext context = new BasicCommandContext();
    context.setDatabase(this);

    Map<Object, Object> params = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        params.put(i, args[i]);
      }
    }

    context.setInputParameters(params);

    final List<Statement> statements = SQLEngine.parseScript(script, wrappedDatabaseInstance);
    ResultSet original;
    try {
      original = executeInternal(statements, context);
    } finally {

    }
    LocalResultSetLifecycleDecorator result = new LocalResultSetLifecycleDecorator(original);

    return result;
  }

  private ResultSet executeInternal(List<Statement> statements, CommandContext scriptContext) {
    ScriptExecutionPlan plan = new ScriptExecutionPlan(scriptContext);

    List<Statement> lastRetryBlock = new ArrayList<>();
    int nestedTxLevel = 0;

    for (Statement stm : statements) {
      if (stm instanceof BeginStatement) {
        nestedTxLevel++;
      }

      if (nestedTxLevel <= 0) {
        InternalExecutionPlan sub = stm.createExecutionPlan(scriptContext);
        plan.chain(sub, false);
      } else {
        lastRetryBlock.add(stm);
      }

      if (stm instanceof CommitStatement && nestedTxLevel > 0) {
        nestedTxLevel--;
        if (nestedTxLevel == 0) {

          for (Statement statement : lastRetryBlock) {
            InternalExecutionPlan sub = statement.createExecutionPlan(scriptContext);
            plan.chain(sub, false);
          }
          lastRetryBlock = new ArrayList<>();
        }
      }

    }

    final LocalResultSet original = new LocalResultSet(plan);
    return original;

  }

  @Override
  public ResultSet query(final String language, final String query, final Object... parameters) {
    checkDatabaseIsOpen();

    statsQueries.incrementAndGet();

    final Statement statement = SQLEngine.parse(query, wrappedDatabaseInstance);
    if (!statement.isIdempotent())
      throw new IllegalArgumentException("Query '" + query + "' is not idempotent");
    final ResultSet original = statement.execute(wrappedDatabaseInstance, parameters);
    return original;
  }

  @Override
  public ResultSet query(final String language, final String query, final Map<String, Object> parameters) {
    checkDatabaseIsOpen();

    statsQueries.incrementAndGet();

    final Statement statement = SQLEngine.parse(query, wrappedDatabaseInstance);
    if (!statement.isIdempotent())
      throw new IllegalArgumentException("Query '" + query + "' is not idempotent");
    final ResultSet original = statement.execute(wrappedDatabaseInstance, parameters);
    return original;
  }

  /**
   * Returns true if two databases are the same.
   */
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final EmbeddedDatabase pDatabase = (EmbeddedDatabase) o;

    return databasePath != null ? databasePath.equals(pDatabase.databasePath) : pDatabase.databasePath == null;
  }

  public DatabaseContext.DatabaseContextTL getContext() {
    return DatabaseContext.INSTANCE.getContext(databasePath);
  }

  /**
   * Executes a callback in an shared lock.
   */
  @Override
  public <RET extends Object> RET executeInReadLock(final Callable<RET> callable) {
    readLock();
    try {

      return callable.call();

    } catch (ClosedChannelException e) {
      LogManager.instance().log(this, Level.SEVERE, "Database '%s' has some files that are closed", e, name);
      close();
      throw new DatabaseOperationException("Database '" + name + "' has some files that are closed", e);

    } catch (RuntimeException e) {
      throw e;

    } catch (Throwable e) {
      throw new DatabaseOperationException("Error during read lock", e);

    } finally {
      readUnlock();
    }
  }

  /**
   * Executes a callback in an exclusive lock.
   */
  @Override
  public <RET extends Object> RET executeInWriteLock(final Callable<RET> callable) {
    writeLock();
    try {

      return callable.call();

    } catch (ClosedChannelException e) {
      LogManager.instance().log(this, Level.SEVERE, "Database '%s' has some files that are closed", e, name);
      close();
      throw new DatabaseOperationException("Database '" + name + "' has some files that are closed", e);

    } catch (RuntimeException e) {
      throw e;

    } catch (Throwable e) {
      throw new DatabaseOperationException("Error during write lock", e);

    } finally {
      writeUnlock();
    }
  }

  @Override
  public StatementCache getStatementCache() {
    return statementCache;
  }

  @Override
  public ExecutionPlanCache getExecutionPlanCache() {
    return executionPlanCache;
  }

  @Override
  public WALFileFactory getWALFileFactory() {
    return walFactory;
  }

  @Override
  public int hashCode() {
    return databasePath != null ? databasePath.hashCode() : 0;
  }

  @Override
  public void executeCallbacks(final CALLBACK_EVENT event) throws IOException {
    final List<Callable<Void>> callbacks = this.callbacks.get(event);
    if (callbacks != null && !callbacks.isEmpty()) {
      for (Callable<Void> cb : callbacks) {
        try {
          cb.call();
        } catch (RuntimeException | IOException e) {
          throw e;
        } catch (Exception e) {
          throw new IOException("Error on executing test callback EVENT=" + event, e);
        }
      }
    }
  }

  @Override
  public DatabaseInternal getEmbedded() {
    return this;
  }

  @Override
  public ContextConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public DatabaseInternal getWrappedDatabaseInstance() {
    return wrappedDatabaseInstance;
  }

  public void setWrappedDatabaseInstance(final DatabaseInternal wrappedDatabaseInstance) {
    this.wrappedDatabaseInstance = wrappedDatabaseInstance;
  }

  protected void checkDatabaseIsOpen() {
    if (!open)
      throw new DatabaseIsClosedException(name);

    if (DatabaseContext.INSTANCE.getContext(databasePath) == null)
      DatabaseContext.INSTANCE.init(this);
  }

  private void lockDatabase() throws IOException {
    try {
      lockFileIO = new RandomAccessFile(lockFile, "rw").getChannel().tryLock();

      if (lockFileIO == null)
        throw new LockException("Database '" + name + "' is locked by another process (path=" + new File(databasePath).getAbsolutePath() + ")");

    } catch (Exception e) {
      // IGNORE HERE
      throw new LockException("Database '" + name + "' is locked by another process (path=" + new File(databasePath).getAbsolutePath() + ")", e);
    }
  }
}
