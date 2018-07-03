/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.mongodbw;

import com.arcadedb.database.Database;
import com.arcadedb.database.RID;
import com.arcadedb.server.ArcadeDBServer;
import com.arcadedb.sql.executor.Result;
import de.bwaldvogel.mongo.MongoCollection;
import de.bwaldvogel.mongo.backend.Index;
import de.bwaldvogel.mongo.backend.Utils;
import de.bwaldvogel.mongo.bson.Document;
import de.bwaldvogel.mongo.bson.ObjectId;
import de.bwaldvogel.mongo.exception.MongoServerException;

import java.util.*;

public class MongoDBCollectionWrapper implements MongoCollection<Long> {
  private final ArcadeDBServer    server;
  private final Database          database;
  private final int               collectionId;
  private final String            collectionName;
  private final List<Index<Long>> indexes = new ArrayList<>();

  private static class ProjectingIterable implements Iterable<Document> {
    private Iterable<Document> iterable;
    private Document           fieldSelector;
    private String             idField;

    ProjectingIterable(Iterable<Document> iterable, Document fieldSelector, String idField) {
      this.iterable = iterable;
      this.fieldSelector = fieldSelector;
      this.idField = idField;
    }

    public Iterator<Document> iterator() {
      return new ProjectingIterator(this.iterable.iterator(), this.fieldSelector, this.idField);
    }
  }

  private static class ProjectingIterator implements Iterator<Document> {
    private Iterator<Document> iterator;
    private Document           fieldSelector;
    private String             idField;

    ProjectingIterator(Iterator<Document> iterator, Document fieldSelector, String idField) {
      this.iterator = iterator;
      this.fieldSelector = fieldSelector;
      this.idField = idField;
    }

    public boolean hasNext() {
      return this.iterator.hasNext();
    }

    public Document next() {
      Document document = this.iterator.next();
      return projectDocument(document, this.fieldSelector, this.idField);
    }

    public void remove() {
      this.iterator.remove();
    }
  }

  protected MongoDBCollectionWrapper(final ArcadeDBServer server, final Database database, final String collectionName) {
    this.server = server;
    this.database = database;
    this.collectionName = collectionName;
    this.collectionId = database.getSchema().getType(collectionName).getBuckets(false).get(0).getId();
  }

  protected Document getDocument(final Long aLong) {
    final com.arcadedb.database.Document record = (com.arcadedb.database.Document) database
        .lookupByRID(new RID(database, collectionId, aLong), true);

    final Document result = new Document();

    for (String p : record.getPropertyNames())
      result.put(p, record.get(p));

    return result;
  }

  @Override
  public String getDatabaseName() {
    return database.getName();
  }

  @Override
  public String getFullName() {
    return null;
  }

  @Override
  public String getCollectionName() {
    return collectionName;
  }

  @Override
  public void addIndex(Index<Long> index) {

  }

  @Override
  public void addDocument(Document document) {

  }

  @Override
  public void removeDocument(Document document) {

  }

  @Override
  public Iterable<Document> handleQuery(Document queryObject, int numberToSkip, int numberToReturn, Document fieldSelector)
      throws MongoServerException {
    if (numberToReturn < 0)
      numberToReturn = -numberToReturn;

    Document query;
    Document orderBy;
    if (queryObject.containsKey("query")) {
      query = (Document) queryObject.get("query");
      orderBy = (Document) queryObject.get("orderby");
    } else if (queryObject.containsKey("$query")) {
      query = (Document) queryObject.get("$query");
      orderBy = (Document) queryObject.get("$orderby");
    } else {
      query = queryObject;
      orderBy = null;
    }

    if (this.count() == 0)
      return Collections.emptyList();

    final Iterable<Document> objs = this.queryDocuments(query, orderBy, numberToSkip, numberToReturn);

    return (fieldSelector != null && !fieldSelector.keySet().isEmpty() ? new ProjectingIterable(objs, fieldSelector, "") : objs);
  }

  @Override
  public int insertDocuments(final List<Document> list) {
    database.begin();

    int total = 0;
    for (Document d : list) {
      final com.arcadedb.database.ModifiableDocument record = database.newDocument(collectionName);

      for (Map.Entry<String, Object> p : d.entrySet()) {
        final Object value = p.getValue();
        if (value instanceof ObjectId) {
          final byte[] var2 = ((ObjectId) value).toByteArray();
          int var3 = var2.length;

          String s = "";
          for (int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            s = s + String.format("%02x", b);
          }

          record.set(p.getKey(), s);
        } else
          record.set(p.getKey(), value);
      }

      record.save();
      ++total;
    }

    database.commit();

    return total;
  }

  @Override
  public Document updateDocuments(final Document document, final Document document1, boolean b, boolean b1) {
    return null;
  }

  @Override
  public int deleteDocuments(final Document document, final int limit) {
    return 0;
  }

  @Override
  public Document handleDistinct(Document document) {
    return null;
  }

  @Override
  public Document getStats() {
    return null;
  }

  @Override
  public Document validate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Document findAndModify(Document document) {
    return null;
  }

  @Override
  public int count(Document document, int i, int i1) {
    return (int) database.countType(collectionName, false);
  }

  @Override
  public int count() {
    return (int) database.countType(getCollectionName(), false);
  }

  @Override
  public int getNumIndexes() {
    return 0;
  }

  @Override
  public void drop() {
    database.getSchema().dropType(collectionName);
  }

  @Override
  public void renameTo(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  private Iterable<Document> queryDocuments(Document query, Document orderBy, int numberToSkip, int numberToReturn) {
    final List<Document> result = new ArrayList<>();

    Iterator it;

    if (query.isEmpty()) {
      // SCAN
      it = database.iterateType(collectionName, false);
    } else {
      // EXECUTE A SQL QUERY
      final StringBuilder sql = new StringBuilder("select from " + collectionName + " where ");

      buildExpression(sql, query);

      it = database.query(sql.toString());
    }

    fillResultSet(numberToSkip, numberToReturn, result, it);

    return result;
  }

  private void buildExpression(final StringBuilder buffer, final Document query) {
    for (Map.Entry<String, Object> entry : query.entrySet()) {
      final Object key = entry.getKey();
      final Object value = entry.getValue();

      if (key instanceof String && ((String) key).startsWith("$"))
        buildExpression(buffer, (String) key, value);
      else if (value instanceof Document) {
        buildAnd(buffer, key, value);
      } else if (value instanceof List) {
        if (key.equals("$or")) {
          buildOr(buffer, (List) value);
        } else
          throw new IllegalArgumentException("Invalid operator " + key);
      } else {
        buffer.append(entry.getKey());
        buffer.append(" = ");
        buildValue(buffer, value);
      }
    }
  }

  private void buildAnd(final StringBuilder sql, final Object key, final Object value) {
    int expressionCount = 0;

    sql.append("(");

    if (value instanceof List) {
      for (Document o : (List<Document>) value) {
        if (expressionCount++ > 0)
          sql.append(" AND ");

        buildExpression(sql, o);
      }
    } else if (value instanceof Document) {
      for (Map.Entry<String, Object> subEntry : ((Document) value).entrySet()) {
        final String subKey = subEntry.getKey();
        final Object subValue = subEntry.getValue();

        if (expressionCount++ > 0)
          sql.append(" AND ");

        if (key != null)
          sql.append(key);

        buildExpression(sql, subKey, subValue);

      }
    }

    sql.append(")");
  }

  private void buildExpression(final StringBuilder sql, final String key, final Object value) {
    if (key.equals("$in")) {
      if (value instanceof Collection) {
        sql.append(" IN ");
        buildCollection(sql, (Collection) value);
      } else
        throw new IllegalArgumentException("Operator $in was expecting a collection");
    } else if (key.equals("$nin")) {
      if (value instanceof Collection) {
        sql.append(" NOT IN ");
        buildCollection(sql, (Collection) value);
      } else
        throw new IllegalArgumentException("Operator $in was expecting a collection");
    } else if (key.equals("$eq")) {
      sql.append(" = ");
      buildValue(sql, value);
    } else if (key.equals("$ne")) {
      sql.append(" <> ");
      buildValue(sql, value);
    } else if (key.equals("$lt")) {
      sql.append(" < ");
      buildValue(sql, value);
    } else if (key.equals("$lte")) {
      sql.append(" <= ");
      buildValue(sql, value);
    } else if (key.equals("$gt")) {
      sql.append(" > ");
      buildValue(sql, value);
    } else if (key.equals("$gte")) {
      sql.append(" >= ");
      buildValue(sql, value);
    } else if (key.equals("$exists")) {
      sql.append(" IS DEFINED ");
    } else if (key.equals("$size")) {
      sql.append(".size() = ");
      buildValue(sql, value);
    } else if (key.equals("$or")) {
      buildOr(sql, (List) value);
    } else if (key.equals("$and")) {
      buildAnd(sql, key, value);
    } else if (key.equals("$not")) {
      sql.append(" NOT ");
      buildExpression(sql, (Document) value);
    } else
      throw new IllegalArgumentException("Unknown operator " + key);
  }

  private void buildOr(final StringBuilder buffer, final List list) {
    buffer.append("(");

    int i = 0;
    for (Object o : list) {
      if (i++ > 0)
        buffer.append(" OR ");

      if (o instanceof Document) {
        buildExpression(buffer, (Document) o);
      }
    }

    buffer.append(")");
  }

  private void buildCollection(final StringBuilder buffer, final Collection coll) {
    int i = 0;
    buffer.append('[');
    for (Iterator it = coll.iterator(); it.hasNext(); ) {
      if (i++ > 0)
        buffer.append(',');

      buildValue(buffer, it.next());
    }
    buffer.append(']');
  }

  private void buildValue(final StringBuilder buffer, final Object value) {
    if (value instanceof String) {
      buffer.append('\'');
      buffer.append(value);
      buffer.append('\'');
    } else
      buffer.append(value);
  }

  private void fillResultSet(final int numberToSkip, final int numberToReturn, final List<Document> result, final Iterator it) {
    for (int i = 0; it.hasNext(); ++i) {
      if (numberToSkip > 0 && i < numberToSkip - 1)
        continue;

      final Object next = it.next();

      if (next instanceof com.arcadedb.database.Document)
        result.add(convertDocumentToMongoDB((com.arcadedb.database.Document) next));
      else if (next instanceof Result)
        result.add(convertDocumentToMongoDB((Result) next));
      else
        throw new IllegalArgumentException("Object not supported");

      if (numberToReturn > 0 && result.size() >= numberToReturn)
        break;
    }
  }

  private Document convertDocumentToMongoDB(final com.arcadedb.database.Document doc) {
    final Document result = new Document();

    for (String p : doc.getPropertyNames()) {
      final Object value = doc.get(p);

      if ("_id".equals(p)) {
        result.put(p, getObjectId((String) value));
      } else
        result.put(p, value);
    }

    return result;
  }

  private Document convertDocumentToMongoDB(final Result doc) {
    final Document result = new Document();

    for (String p : doc.getPropertyNames()) {
      final Object value = doc.getProperty(p);

      if ("_id".equals(p)) {
        result.put(p, getObjectId((String) value));
      } else
        result.put(p, value);
    }

    return result;
  }

  private ObjectId getObjectId(final String s) {
    final byte[] buffer = new byte[s.length() / 2];
    for (int i = 0; i < s.length(); i += 2) {
      buffer[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return new ObjectId(buffer);
  }

  private static Document projectDocument(Document document, Document fields, String idField) {
    if (document == null) {
      return null;
    } else {
      Document newDocument = new Document();
      Iterator var4;
      String key;
      if (onlyExclusions(fields)) {
        newDocument.putAll(document);
        var4 = fields.keySet().iterator();

        while (var4.hasNext()) {
          key = (String) var4.next();
          newDocument.remove(key);
        }
      } else {
        var4 = fields.keySet().iterator();

        while (var4.hasNext()) {
          key = (String) var4.next();
          if (Utils.isTrue(fields.get(key))) {
            projectField(document, newDocument, key);
          }
        }
      }

      if (!fields.containsKey(idField)) {
        newDocument.put(idField, document.get(idField));
      }

      return newDocument;
    }
  }

  private static boolean onlyExclusions(final Document fields) {
    final Iterator var1 = fields.keySet().iterator();

    String key;
    do {
      if (!var1.hasNext()) {
        return true;
      }

      key = (String) var1.next();
    } while (!Utils.isTrue(fields.get(key)));

    return false;
  }

  private static void projectField(final Document document, final Document newDocument, final String key) {
    if (document != null) {
      final int dotPos = key.indexOf(46);
      if (dotPos > 0) {
        String mainKey = key.substring(0, dotPos);
        String subKey = key.substring(dotPos + 1);
        Object object = document.get(mainKey);
        if (object instanceof Document) {
          if (!newDocument.containsKey(mainKey)) {
            newDocument.put(mainKey, new Document());
          }

          projectField((Document) object, (Document) newDocument.get(mainKey), subKey);
        }
      } else {
        newDocument.put(key, document.get(key));
      }

    }
  }
}
