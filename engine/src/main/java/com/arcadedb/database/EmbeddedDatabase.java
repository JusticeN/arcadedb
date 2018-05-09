/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.database;

import com.arcadedb.GlobalConfiguration;
import com.arcadedb.Profiler;
import com.arcadedb.database.async.DatabaseAsyncExecutor;
import com.arcadedb.engine.*;
import com.arcadedb.engine.Dictionary;
import com.arcadedb.exception.*;
import com.arcadedb.exception.ConcurrentModificationException;
import com.arcadedb.graph.*;
import com.arcadedb.index.Index;
import com.arcadedb.index.IndexLSM;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.SchemaImpl;
import com.arcadedb.schema.VertexType;
import com.arcadedb.serializer.BinarySerializer;
import com.arcadedb.sql.executor.ResultSet;
import com.arcadedb.sql.executor.SQLEngine;
import com.arcadedb.sql.parser.Statement;
import com.arcadedb.utility.FileUtils;
import com.arcadedb.utility.LogManager;
import com.arcadedb.utility.MultiIterator;
import com.arcadedb.utility.RWLockContext;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

public class EmbeddedDatabase extends RWLockContext implements Database, DatabaseInternal {
  protected final String                name;
  protected final PaginatedFile.MODE    mode;
  protected final String                databasePath;
  protected final FileManager           fileManager;
  protected final PageManager           pageManager;
  protected final BinarySerializer      serializer     = new BinarySerializer();
  protected final RecordFactory         recordFactory  = new RecordFactory();
  protected final SchemaImpl            schema;
  protected final GraphEngine           graphEngine    = new GraphEngine();
  protected final TransactionManager    transactionManager;
  protected       DatabaseAsyncExecutor asynch         = null;
  private         boolean               readYourWrites = true;

  protected          boolean autoTransaction = false;
  protected volatile boolean open            = false;

  protected static final Set<String>                               SUPPORTED_FILE_EXT = new HashSet<String>(
      Arrays.asList(Dictionary.DICT_EXT, Bucket.BUCKET_EXT, IndexLSM.INDEX_EXT));
  private                File                                      lockFile;
  private                Map<CALLBACK_EVENT, List<Callable<Void>>> callbacks;

  protected EmbeddedDatabase(final String path, final PaginatedFile.MODE mode,
      final Map<CALLBACK_EVENT, List<Callable<Void>>> callbacks) {
    try {
      this.mode = mode;
      this.callbacks = callbacks;
      if (path.endsWith("/"))
        databasePath = path.substring(0, path.length() - 1);
      else
        databasePath = path;

      final int lastSeparatorPos = path.lastIndexOf("/");
      if (lastSeparatorPos > -1)
        name = path.substring(lastSeparatorPos + 1);
      else
        name = path;

      DatabaseContext.INSTANCE.init(this);

      fileManager = new FileManager(path, mode, SUPPORTED_FILE_EXT);
      transactionManager = new TransactionManager(this);
      pageManager = new PageManager(fileManager, transactionManager);

      open = true;

      try {
        schema = new SchemaImpl(this, databasePath, mode);

        if (fileManager.getFiles().isEmpty())
          schema.create(mode);
        else
          schema.load(mode);

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
      // RECOVERY
      LogManager.instance().warn(this, "Database '%s' was not closed properly last time", name);

      if (mode == PaginatedFile.MODE.READ_ONLY)
        throw new DatabaseMetadataException("Database needs recovery but has been open in read only mode");

      executeCallbacks(CALLBACK_EVENT.DB_NOT_CLOSED);

      transactionManager.checkIntegrity();
    } else
      lockFile.createNewFile();
  }

  @Override
  public void drop() {
    super.executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        checkDatabaseIsOpen();
        if (mode == PaginatedFile.MODE.READ_ONLY)
          throw new DatabaseIsReadOnlyException("Cannot drop database");

        close();
        FileUtils.deleteRecursively(new File(databasePath));
        return null;
      }
    });
  }

  @Override
  public void close() {
    if (asynch != null) {
      // EXECUTE OUTSIDE LOCK
      asynch.waitCompletion();
      asynch.close();
    }

    super.executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        if (!open)
          return null;

        if (asynch != null)
          asynch.close();

        if (getTransaction().isActive())
          // ROLLBACK ANY PENDING OPERATION
          getTransaction().rollback();

        try {
          schema.close();
          pageManager.close();
          fileManager.close();
          transactionManager.close();

          lockFile.delete();

          DatabaseContext.INSTANCE.init(EmbeddedDatabase.this);

        } finally {
          open = false;
          Profiler.INSTANCE.unregisterDatabase(EmbeddedDatabase.this);
        }
        return null;
      }
    });
  }

  public DatabaseAsyncExecutor asynch() {
    if (asynch == null) {
      super.executeInWriteLock(new Callable<Object>() {
        @Override
        public Object call() {
          asynch = new DatabaseAsyncExecutor(EmbeddedDatabase.this);
          return null;
        }
      });
    }
    return asynch;
  }

  @Override
  public String getDatabasePath() {
    return databasePath;
  }

  public TransactionContext getTransaction() {
    return DatabaseContext.INSTANCE.get().transaction;
  }

  @Override
  public void begin() {
    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        checkDatabaseIsOpen();
        getTransaction().begin();
        return null;
      }
    });
  }

  @Override
  public void commit() {
    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        checkTransactionIsActive();
        getTransaction().commit();
        return null;
      }
    });
  }

  @Override
  public void rollback() {
    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        checkTransactionIsActive();
        getTransaction().rollback();
        return null;
      }
    });
  }

  @Override
  public long countBucket(final String bucketName) {
    return (Long) super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        checkDatabaseIsOpen();
        return schema.getBucketByName(bucketName).count();
      }
    });
  }

  @Override
  public long countType(final String typeName, final boolean polymorphic) {
    return (Long) super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        checkDatabaseIsOpen();
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
    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {

        checkDatabaseIsOpen();
        try {
          final DocumentType type = schema.getType(typeName);

          for (Bucket b : type.getBuckets(polymorphic)) {
            b.scan(new RawRecordCallback() {
              @Override
              public boolean onRecord(final RID rid, final Binary view) {
                final Document record = (Document) recordFactory.newImmutableRecord(EmbeddedDatabase.this, typeName, rid, view);
                return callback.onRecord(record);
              }
            });
          }
        } catch (IOException e) {
          throw new DatabaseOperationException("Error on executing scan of type '" + schema.getType(typeName) + "'", e);
        }
        return null;
      }
    });
  }

  @Override
  public void scanBucket(final String bucketName, final RecordCallback callback) {
    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {

        checkDatabaseIsOpen();
        try {
          final String type = schema.getTypeNameByBucketId(schema.getBucketByName(bucketName).getId());
          schema.getBucketByName(bucketName).scan(new RawRecordCallback() {
            @Override
            public boolean onRecord(final RID rid, final Binary view) {
              final Record record = recordFactory.newImmutableRecord(EmbeddedDatabase.this, type, rid, view);
              return callback.onRecord(record);
            }
          });
        } catch (IOException e) {
          throw new DatabaseOperationException("Error on executing scan of bucket '" + bucketName + "'", e);
        }
        return null;
      }
    });
  }

  @Override
  public Iterator<Record> iterateType(final String typeName, final boolean polymorphic) {
    return (Iterator<Record>) super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {

        checkDatabaseIsOpen();
        try {
          final DocumentType type = schema.getType(typeName);

          final MultiIterator iter = new MultiIterator();

          for (Bucket b : type.getBuckets(polymorphic))
            iter.add(b.iterator());

          return iter;

        } catch (IOException e) {
          throw new DatabaseOperationException("Error on executing scan of type '" + schema.getType(typeName) + "'", e);
        }
      }
    });
  }

  @Override
  public Iterator<Record> iterateBucket(final String bucketName) {
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
    return (Record) super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {

        checkDatabaseIsOpen();

        // CHECK IN TX CACHE FIRST
        final TransactionContext tx = getTransaction();
        Record record = tx.getRecordFromCache(rid);
        if (record != null)
          return record;

        final DocumentType type = schema.getTypeByBucketId(rid.getBucketId());

        if (loadContent) {
          final Binary buffer = schema.getBucketById(rid.getBucketId()).getRecord(rid);
          record = recordFactory.newImmutableRecord(EmbeddedDatabase.this, type != null ? type.getName() : null, rid, buffer);
          tx.updateRecordInCache(record);
          return record;
        }

        if (type != null)
          record = recordFactory.newImmutableRecord(EmbeddedDatabase.this, type.getName(), rid, type.getType());
        else
          record = recordFactory.newImmutableRecord(EmbeddedDatabase.this, null, rid, Document.RECORD_TYPE);

        tx.updateRecordInCache(record);

        return record;
      }
    });
  }

  @Override
  public Cursor<RID> lookupByKey(final String type, final String[] properties, final Object[] keys) {
    return (Cursor<RID>) super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {

        checkDatabaseIsOpen();
        final DocumentType t = schema.getType(type);

        final List<DocumentType.IndexMetadata> metadata = t.getIndexMetadataByProperties(properties);
        if (metadata == null || metadata.isEmpty())
          throw new IllegalArgumentException(
              "No index has been created on type '" + type + "' properties " + Arrays.toString(properties));

        final List<RID> result = new ArrayList<>();
        for (DocumentType.IndexMetadata m : metadata)
          result.addAll(m.index.get(keys));

        return new CursorCollection<RID>(result);
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
  public void setReadYourWrites(boolean readYourWrites) {
    this.readYourWrites = readYourWrites;
  }

  @Override
  public void createRecord(final ModifiableDocument record) {
    if (record.getIdentity() != null)
      throw new IllegalArgumentException("Cannot create record " + record.getIdentity() + " because it is already persistent");

    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {

        checkTransactionIsActive();
        if (mode == PaginatedFile.MODE.READ_ONLY)
          throw new DatabaseIsReadOnlyException("Cannot create a new record");

        final DocumentType type = schema.getType(record.getType());

        // NEW
        final Bucket bucket = type.getBucketToSave(false);
        record.setIdentity(bucket.createRecord(record));
        indexDocument(record, type, bucket);

        getTransaction().updateRecordInCache(record);

        return null;
      }
    });
  }

  @Override
  public void createRecord(final Record record, final String bucketName) {
    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        createRecordNoLock(record, bucketName);
        return null;
      }
    });
  }

  @Override
  public void createRecordNoLock(final Record record, final String bucketName) {
    if (record.getIdentity() != null)
      throw new IllegalArgumentException("Cannot create record " + record.getIdentity() + " because it is already persistent");

    checkTransactionIsActive();
    if (mode == PaginatedFile.MODE.READ_ONLY)
      throw new DatabaseIsReadOnlyException("Cannot create a new record");

    final Bucket bucket = schema.getBucketByName(bucketName);

    ((RecordInternal) record).setIdentity(bucket.createRecord(record));

    getTransaction().updateRecordInCache(record);
  }

  @Override
  public void updateRecord(final Record record) {
    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        updateRecordNoLock(record);
        return null;
      }
    });
  }

  @Override
  public void updateRecordNoLock(final Record record) {
    if (record.getIdentity() == null)
      throw new IllegalArgumentException("Cannot update the record because it is not persistent");

    checkTransactionIsActive();
    if (mode == PaginatedFile.MODE.READ_ONLY)
      throw new DatabaseIsReadOnlyException("Cannot update a record");

    schema.getBucketById(record.getIdentity().getBucketId()).updateRecord(record);

    getTransaction().updateRecordInCache(record);
  }

  @Override
  public void deleteRecord(final Record record) {
    if (record.getIdentity() == null)
      throw new IllegalArgumentException("Cannot delete a non persistent record");

    super.executeInReadLock(new Callable<Object>() {
      @Override
      public Object call() {
        checkTransactionIsActive();
        if (mode == PaginatedFile.MODE.READ_ONLY)
          throw new DatabaseIsReadOnlyException("Cannot delete record " + record.getIdentity());

        final Bucket bucket = schema.getBucketById(record.getIdentity().getBucketId());

        if (record instanceof Edge) {
          graphEngine.deleteEdge((Edge) record);
        } else if (record instanceof Vertex) {
          graphEngine.deleteVertex((VertexInternal) record);
        } else
          bucket.deleteRecord(record.getIdentity());

        getTransaction().removeRecordFromCache(record);

        return null;
      }
    });
  }

  @Override
  public boolean isTransactionActive() {
    return getTransaction().isActive();
  }

  @Override
  public void transaction(final PTransaction txBlock) {
    transaction(txBlock, GlobalConfiguration.MVCC_RETRIES.getValueAsInteger());
  }

  @Override
  public void transaction(final PTransaction txBlock, final int retries) {
    if (txBlock == null)
      throw new IllegalArgumentException("Transaction block is null");

    ConcurrentModificationException lastException = null;

    for (int retry = 0; retry < retries; ++retry) {
      try {
        begin();
        txBlock.execute(this);
        commit();

        // OK
        return;

      } catch (ConcurrentModificationException e) {
        // RETRY
        lastException = e;
        continue;
      } catch (Exception e) {
        if (getTransaction().isActive())
          rollback();
        throw e;
      }
    }
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
    return pageManager;
  }

  @Override
  public ModifiableDocument newDocument(final String typeName) {
    if (typeName == null)
      throw new IllegalArgumentException("Type is null");

    final DocumentType type = schema.getType(typeName);
    if (!type.getClass().equals(DocumentType.class))
      throw new IllegalArgumentException("Cannot create a document of type '" + typeName + "' because is not a document type");

    return new ModifiableDocument(this, typeName, null);
  }

  @Override
  public ModifiableVertex newVertex(final String typeName) {
    if (typeName == null)
      throw new IllegalArgumentException("Type is null");

    final DocumentType type = schema.getType(typeName);
    if (!type.getClass().equals(VertexType.class))
      throw new IllegalArgumentException("Cannot create a vertex of type '" + typeName + "' because is not a vertex type");

    return new ModifiableVertex(this, typeName, null);
  }

  public Edge newEdgeByKeys(final String sourceVertexType, final String[] sourceVertexKey, final Object[] sourceVertexValue,
      final String destinationVertexType, final String[] destinationVertexKey, final Object[] destinationVertexValue,
      final boolean createVertexIfNotExist, final String edgeType, final boolean bidirectional, final Object... properties) {
    if (sourceVertexKey == null)
      throw new IllegalArgumentException("Source vertex key is null");

    if (sourceVertexKey.length != sourceVertexValue.length)
      throw new IllegalArgumentException("Source vertex key and value arrays have different sizes");

    if (destinationVertexKey == null)
      throw new IllegalArgumentException("Destination vertex key is null");

    if (destinationVertexKey.length != destinationVertexValue.length)
      throw new IllegalArgumentException("Destination vertex key and value arrays have different sizes");

    final Iterator<RID> v1Result = lookupByKey(sourceVertexType, sourceVertexKey, sourceVertexValue);

    Vertex sourceVertex;
    if (!v1Result.hasNext()) {
      if (createVertexIfNotExist) {
        sourceVertex = newVertex(sourceVertexType);
        for (int i = 0; i < sourceVertexKey.length; ++i)
          ((ModifiableVertex) sourceVertex).set(sourceVertexKey[i], sourceVertexValue[i]);
      } else
        throw new IllegalArgumentException(
            "Cannot find source vertex with key " + Arrays.toString(sourceVertexKey) + "=" + Arrays.toString(sourceVertexValue));
    } else
      sourceVertex = (Vertex) v1Result.next().getRecord();

    final Iterator<RID> v2Result = lookupByKey(destinationVertexType, destinationVertexKey, destinationVertexValue);
    Vertex destinationVertex;
    if (!v2Result.hasNext()) {
      if (createVertexIfNotExist) {
        destinationVertex = newVertex(destinationVertexType);
        for (int i = 0; i < destinationVertexKey.length; ++i)
          ((ModifiableVertex) destinationVertex).set(destinationVertexKey[i], destinationVertexValue[i]);
      } else
        throw new IllegalArgumentException(
            "Cannot find destination vertex with key " + Arrays.toString(destinationVertexKey) + "=" + Arrays
                .toString(destinationVertexValue));
    } else
      destinationVertex = (Vertex) v2Result.next().getRecord();

    return sourceVertex.newEdge(edgeType, destinationVertex, bidirectional, properties);
  }

  @Override
  public void setAutoTransaction(final boolean autoTransaction) {
    this.autoTransaction = autoTransaction;
  }

  @Override
  public FileManager getFileManager() {
    return fileManager;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void checkTransactionIsActive() {
    checkDatabaseIsOpen();
    if (autoTransaction && !isTransactionActive())
      begin();
    else if (!getTransaction().isActive())
      throw new DatabaseOperationException("Transaction not begun");
  }

  @Override
  public void indexDocument(final ModifiableDocument record, final DocumentType type, final Bucket bucket) {
    // INDEX THE RECORD
    final List<DocumentType.IndexMetadata> metadata = type.getIndexMetadataByBucketId(bucket.getId());
    if (metadata != null) {
      for (DocumentType.IndexMetadata entry : metadata) {
        final Index index = entry.index;
        final String[] keyNames = entry.propertyNames;
        final Object[] keyValues = new Object[keyNames.length];
        for (int i = 0; i < keyNames.length; ++i) {
          keyValues[i] = record.get(keyNames[i]);
        }

        index.put(keyValues, record.getIdentity());
      }
    }
  }

  /**
   * Test only API.
   */
  @Override
  public void kill() {
    if (asynch != null)
      asynch.kill();

    if (getTransaction().isActive())
      // ROLLBACK ANY PENDING OPERATION
      getTransaction().kill();

    try {
      schema.close();
      pageManager.kill();
      fileManager.close();
      transactionManager.kill();

    } finally {
      open = false;
      Profiler.INSTANCE.unregisterDatabase(EmbeddedDatabase.this);
    }
  }

  @Override
  public ResultSet query(String query, Map<String, Object> args) {
    final Statement statement = SQLEngine.parse(query, this);
    if (!statement.isIdempotent()) {
      throw new CommandExecutionException("Cannot execute query on non idempotent statement: " + query);
    }
    final ResultSet original = statement.execute(this, args);
    return original;
  }

  @Override
  public ResultSet command(String query, Map<String, Object> args) {
    final Statement statement = SQLEngine.parse(query, this);
    final ResultSet original = statement.execute(this, args);
    return original;
  }

  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final EmbeddedDatabase pDatabase = (EmbeddedDatabase) o;

    return databasePath != null ? databasePath.equals(pDatabase.databasePath) : pDatabase.databasePath == null;
  }

  public DatabaseContext.PDatabaseContextTL getContext() {
    return DatabaseContext.INSTANCE.get();
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
  public String toString() {
    return name;
  }

  protected void checkDatabaseIsOpen() {
    if (!open)
      throw new DatabaseIsClosedException(name);

    if (DatabaseContext.INSTANCE.get() == null)
      DatabaseContext.INSTANCE.init(this);
  }
}
