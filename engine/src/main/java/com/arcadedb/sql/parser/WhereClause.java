/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OWhereClause.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.database.Identifiable;
import com.arcadedb.index.Index;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.*;
import java.util.stream.Collectors;

public class WhereClause extends SimpleNode {
  protected BooleanExpression baseExpression;

  protected List<AndBlock> flattened;

  public WhereClause(int id) {
    super(id);
  }

  public WhereClause(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor. *
   */
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public boolean matchesFilters(Identifiable currentRecord, CommandContext ctx) {
    if (baseExpression == null) {
      return true;
    }
    return baseExpression.evaluate(currentRecord, ctx);
  }

  public boolean matchesFilters(Result currentRecord, CommandContext ctx) {
    if (baseExpression == null) {
      return true;
    }
    return baseExpression.evaluate(currentRecord, ctx);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (baseExpression == null) {
      return;
    }
    baseExpression.toString(params, builder);
  }

  /**
   * estimates how many items of this class will be returned applying this filter
   *
   * @param oClass
   *
   * @return an estimation of the number of records of this class returned applying this filter, 0 if and only if sure that no
   * records are returned
   */
  public long estimate(DocumentType oClass, long threshold, CommandContext ctx) {
//    long count = oClass.count();
//    if (count > 1) {
//      count = count / 2;
//    }
//    if (count < threshold) {
//      return count;
//    }

//    long indexesCount = 0l;
//    List<OAndBlock> flattenedConditions = flatten();
//    Collection<List<PType.IndexMetadata>> indexes = oClass.getAllIndexesMetadata();
//    for (OAndBlock condition : flattenedConditions) {
//
//      List<OBinaryCondition> indexedFunctConditions = condition
//          .getIndexedFunctionConditions(oClass, (PDatabase) ctx.getDatabase());
//
//      long conditionEstimation = Long.MAX_VALUE;
//
//      if (indexedFunctConditions != null) {
//        for (OBinaryCondition cond : indexedFunctConditions) {
//          OFromClause from = new OFromClause(-1);
//          OFromItem item = new OFromItem(-1);
//          from.item = item;
//          from.item.setIdentifier(new OIdentifier(oClass.getName()));
//          long newCount = cond.estimateIndexed(from, ctx);
//          if (newCount < conditionEstimation) {
//            conditionEstimation = newCount;
//          }
//        }
//      } else {
//        Map<String, Object> conditions = getEqualityOperations(condition, ctx);

        //TODO!!!
//        for (PIndex index : indexes) {
//          List<String> indexedFields = index.getDefinition().getFields();
//          int nMatchingKeys = 0;
//          for (String indexedField : indexedFields) {
//            if (conditions.containsKey(indexedField)) {
//              nMatchingKeys++;
//            } else {
//              break;
//            }
//          }
//          if (nMatchingKeys > 0) {
//            long newCount = estimateFromIndex(index, conditions, nMatchingKeys);
//            if (newCount < conditionEstimation) {
//              conditionEstimation = newCount;
//            }
//          }
//        }
//      }
//      if (conditionEstimation > count) {
//        return count;
//      }
//      indexesCount += conditionEstimation;
//    }
//    return Math.min(indexesCount, count);
      throw new UnsupportedOperationException("TODO");
  }

  private long estimateFromIndex(Index index, Map<String, Object> conditions, int nMatchingKeys) {
    if (nMatchingKeys < 1) {
      throw new IllegalArgumentException("Cannot estimate from an index with zero keys");
    }
    //TODO
//    OIndexDefinition definition = index.getDefinition();
//    List<String> definitionFields = definition.getFields();
//    Object key = null;
//    if (definition instanceof OPropertyIndexDefinition) {
//      key = convert(conditions.get(definitionFields.get(0)), definition.getTypes()[0]);
//    } else if (definition instanceof OCompositeIndexDefinition) {
//      key = new OCompositeKey();
//      for (int i = 0; i < nMatchingKeys; i++) {
//        Object keyValue = convert(conditions.get(definitionFields.get(i)), definition.getTypes()[i]);
//        ((OCompositeKey) key).addKey(keyValue);
//      }
//    }
//    if (key != null) {
//      Object result = null;
//      if (conditions.size() == definitionFields.size()) {
//        result = index.get(key);
//      } else if (index.supportsOrderedIterations()) {
//        result = index.iterateEntriesBetween(key, true, key, true, true);
//      }
//      if (result instanceof PIdentifiable) {
//        return 1;
//      }
//      if (result instanceof Collection) {
//        return ((Collection) result).size();
//      }
//      if (result instanceof OSizeable) {
//        return ((OSizeable) result).size();
//      }
//      if (result instanceof Iterable) {
//        result = ((Iterable) result).iterator();
//      }
//      if (result instanceof Iterator) {
//        int i = 0;
//        while (((Iterator) result).hasNext()) {
//          ((Iterator) result).next();
//          i++;
//        }
//        return i;
//      }
//    }
    return Long.MAX_VALUE;
  }

  public Iterable fetchFromIndexes(DocumentType oClass, CommandContext ctx) {

      //TODO
      throw new UnsupportedOperationException("TODO");
//    List<OAndBlock> flattenedConditions = flatten();
//    if (flattenedConditions == null || flattenedConditions.size() == 0) {
//      return null;
//    }
//    Set<OIndex<?>> indexes = oClass.getIndexes();
//    List<OIndex> bestIndexes = new ArrayList<OIndex>();
//    List<Map<String, Object>> indexConditions = new ArrayList<Map<String, Object>>();
//    for (OAndBlock condition : flattenedConditions) {
//      Map<String, Object> conditions = getEqualityOperations(condition, ctx);
//      long conditionEstimation = Long.MAX_VALUE;
//      OIndex bestIndex = null;
//      Map<String, Object> bestCondition = null;
//
//      for (OIndex index : indexes) {
//        List<String> indexedFields = index.getDefinition().getFields();
//        int nMatchingKeys = 0;
//        for (String indexedField : indexedFields) {
//          if (conditions.containsKey(indexedField)) {
//            nMatchingKeys++;
//          } else {
//            break;
//          }
//        }
//        if (nMatchingKeys > 0) {
//          long newCount = estimateFromIndex(index, conditions, nMatchingKeys);
//          if (newCount >= 0 && newCount <= conditionEstimation) {
//            conditionEstimation = newCount;
//            bestIndex = index;
//            bestCondition = conditions;
//          }
//        }
//      }
//      if (bestIndex == null) {
//        return null;
//      }
//      bestIndexes.add(bestIndex);
//      indexConditions.add(bestCondition);
//    }
//    OMultiCollectionIterator result = new OMultiCollectionIterator();
//
//    for (int i = 0; i < bestIndexes.size(); i++) {
//      OIndex index = bestIndexes.get(i);
//      Map<String, Object> condition = indexConditions.get(i);
//      result.add(fetchFromIndex(index, indexConditions.get(i)));
//    }
//    return result;
  }

  private Iterable fetchFromIndex(DocumentType index, Map<String, Object> conditions) {
      //TODO
//    OIndexDefinition definition = index.getDefinition();
//    List<String> definitionFields = definition.getFields();
//    Object key = null;
//    if (definition instanceof OPropertyIndexDefinition) {
//      key = convert(conditions.get(definitionFields.get(0)), definition.getTypes()[0]);
//    } else if (definition instanceof OCompositeIndexDefinition) {
//      key = new OCompositeKey();
//      for (int i = 0; i < definitionFields.size(); i++) {
//        String keyName = definitionFields.get(i);
//        if (!conditions.containsKey(keyName)) {
//          break;
//        }
//        Object keyValue = convert(conditions.get(keyName), definition.getTypes()[i]);
//        ((OCompositeKey) key).addKey(conditions.get(keyName));
//      }
//    }
//    if (key != null) {
//      final Object result = index.get(key);
//      if (result == null) {
//        return Collections.EMPTY_LIST;
//      }
//      if (result instanceof Iterable) {
//        return (Iterable) result;
//      }
//      if (result instanceof Iterator) {
//        return new Iterable() {
//          @Override
//          public Iterator iterator() {
//            return (Iterator) result;
//          }
//        };
//      }
//      return Collections.singleton(result);
//    }
    return null;
  }

  private Object convert(Object o, DocumentType oType) {
    return o;
    //TODO
//    return PType.convert(o, oType.getDefaultJavaType());
  }

  private Map<String, Object> getEqualityOperations(AndBlock condition, CommandContext ctx) {
    Map<String, Object> result = new HashMap<String, Object>();
    for (BooleanExpression expression : condition.subBlocks) {
      if (expression instanceof BinaryCondition) {
        BinaryCondition b = (BinaryCondition) expression;
        if (b.operator instanceof EqualsCompareOperator) {
          if (b.left.isBaseIdentifier() && b.right.isEarlyCalculated()) {
            result.put(b.left.toString(), b.right.execute((Result) null, ctx));
          }
        }
      }
    }
    return result;
  }

  public List<AndBlock> flatten() {
    if (this.baseExpression == null) {
      return Collections.EMPTY_LIST;
    }
    if (flattened == null) {
      flattened = this.baseExpression.flatten();
    }
    // TODO remove false conditions (contraddictions)
    return flattened;

  }

  public List<BinaryCondition> getIndexedFunctionConditions(DocumentType iSchemaClass, Database database) {
    if (baseExpression == null) {
      return null;
    }
    return this.baseExpression.getIndexedFunctionConditions(iSchemaClass, database);
  }

  public boolean needsAliases(Set<String> aliases) {
    return this.baseExpression.needsAliases(aliases);
  }

  public void setBaseExpression(BooleanExpression baseExpression) {
    this.baseExpression = baseExpression;
  }

  public WhereClause copy() {
    WhereClause result = new WhereClause(-1);
    result.baseExpression = baseExpression.copy();
    result.flattened = flattened == null ? null : flattened.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    WhereClause that = (WhereClause) o;

    if (baseExpression != null ? !baseExpression.equals(that.baseExpression) : that.baseExpression != null)
      return false;
    return flattened != null ? flattened.equals(that.flattened) : that.flattened == null;
  }

  @Override
  public int hashCode() {
    int result = baseExpression != null ? baseExpression.hashCode() : 0;
    result = 31 * result + (flattened != null ? flattened.hashCode() : 0);
    return result;
  }

  public void extractSubQueries(SubQueryCollector collector) {
    if (baseExpression != null) {
      baseExpression.extractSubQueries(collector);
    }
    flattened = null;
  }

  public boolean refersToParent() {
    return baseExpression != null && baseExpression.refersToParent();
  }

  public BooleanExpression getBaseExpression() {
    return baseExpression;
  }

  public List<AndBlock> getFlattened() {
    return flattened;
  }

  public void setFlattened(List<AndBlock> flattened) {
    this.flattened = flattened;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if (baseExpression != null) {
      result.setProperty("baseExpression", baseExpression.serialize());
    }
    if (flattened != null) {
      result.setProperty("flattened", flattened.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    return result;
  }

  public void deserialize(Result fromResult) {
    if (fromResult.getProperty("baseExpression") != null) {
      baseExpression = BooleanExpression.deserializeFromOResult(fromResult.getProperty("baseExpression"));
    }
    if (fromResult.getProperty("flattened") != null) {
      List<Result> ser = fromResult.getProperty("flattened");
      flattened = new ArrayList<>();
      for (Result r : ser) {
        AndBlock block = new AndBlock(-1);
        block.deserialize(r);
        flattened.add(block);
      }
    }
  }

  public boolean isCacheable() {
    return baseExpression.isCacheable();
  }
}
/* JavaCC - OriginalChecksum=e8015d01ce1ab2bc337062e9e3f2603e (do not edit this line) */
