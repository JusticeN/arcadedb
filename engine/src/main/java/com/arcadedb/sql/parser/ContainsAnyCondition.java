/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OContainsAnyCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.MultiValue;
import com.arcadedb.sql.executor.Result;

import java.util.*;

public class ContainsAnyCondition extends BooleanExpression {

  protected Expression left;

  protected Expression right;

  protected OrBlock rightBlock;

  public ContainsAnyCondition(int id) {
    super(id);
  }

  public ContainsAnyCondition(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public boolean execute(Object left, Object right) {
    if (left instanceof Collection) {
      if (right instanceof Iterable) {
        right = ((Iterable) right).iterator();
      }
      if (right instanceof Iterator) {
        Iterator iterator = (Iterator) right;
        while (iterator.hasNext()) {
          Object next = iterator.next();
          if (((Collection) left).contains(next)) {
            return true;
          }
        }
      }
      return false;
    }
    if (left instanceof Iterable) {
      left = ((Iterable) left).iterator();
    }
    if (left instanceof Iterator) {
      if (!(right instanceof Iterable)) {
        right = Collections.singleton(right);
      }
      right = ((Iterable) right).iterator();

      Iterator leftIterator = (Iterator) left;
      Iterator rightIterator = (Iterator) right;
      while (rightIterator.hasNext()) {
        Object leftItem = rightIterator.next();
        while (leftIterator.hasNext()) {
          Object rightItem = leftIterator.next();
          if (leftItem != null && leftItem.equals(rightItem)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean evaluate(Identifiable currentRecord, CommandContext ctx) {
    Object leftValue = left.execute(currentRecord, ctx);
    if (right != null) {
      Object rightValue = right.execute(currentRecord, ctx);
      return execute(leftValue, rightValue);
    } else {
      if (!MultiValue.isMultiValue(leftValue)) {
        return false;
      }
      Iterator<Object> iter = MultiValue.getMultiValueIterator(leftValue);
      while (iter.hasNext()) {
        Object item = iter.next();
        if (item instanceof Identifiable) {
          if (!rightBlock.evaluate((Identifiable) item, ctx)) {
            return false;
          }
        } else if (item instanceof Result) {
          if (!rightBlock.evaluate((Result) item, ctx)) {
            return false;
          }
        } else {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  public boolean evaluate(Result currentRecord, CommandContext ctx) {
    Object leftValue = left.execute(currentRecord, ctx);
    if (right != null) {
      Object rightValue = right.execute(currentRecord, ctx);
      return execute(leftValue, rightValue);
    } else {
      if (!MultiValue.isMultiValue(leftValue)) {
        return false;
      }
      Iterator<Object> iter = MultiValue.getMultiValueIterator(leftValue);
      while (iter.hasNext()) {
        Object item = iter.next();
        if (item instanceof Identifiable) {
          if (!rightBlock.evaluate((Identifiable) item, ctx)) {
            return false;
          }
        } else if (item instanceof Result) {
          if (!rightBlock.evaluate((Result) item, ctx)) {
            return false;
          }
        } else {
          return false;
        }
      }
      return true;
    }

  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    left.toString(params, builder);
    builder.append(" CONTAINSANY ");
    if (right != null) {
      right.toString(params, builder);
    } else if (rightBlock != null) {
      builder.append("(");
      rightBlock.toString(params, builder);
      builder.append(")");
    }
  }

  public Expression getLeft() {
    return left;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public Expression getRight() {
    return right;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  @Override
  public boolean supportsBasicCalculation() {
    if (left != null && !left.supportsBasicCalculation()) {
      return false;
    }
    if (right != null && !right.supportsBasicCalculation()) {
      return false;
    }
    return rightBlock == null || rightBlock.supportsBasicCalculation();
  }

  @Override
  protected int getNumberOfExternalCalculations() {
    int total = 0;
    if (left != null && !left.supportsBasicCalculation()) {
      total++;
    }
    if (right != null && !right.supportsBasicCalculation()) {
      total++;
    }
    if (rightBlock != null && !rightBlock.supportsBasicCalculation()) {
      total++;
    }
    return total;
  }

  @Override
  protected List<Object> getExternalCalculationConditions() {
    List<Object> result = new ArrayList<Object>();
    if (left != null && !left.supportsBasicCalculation()) {
      result.add(left);
    }
    if (right != null && !right.supportsBasicCalculation()) {
      result.add(right);
    }
    if (rightBlock != null) {
      result.addAll(rightBlock.getExternalCalculationConditions());
    }
    return result;
  }

  @Override
  public boolean needsAliases(Set<String> aliases) {
    if (left.needsAliases(aliases)) {
      return true;
    }

    if (right != null && right.needsAliases(aliases)) {
      return true;
    }
    return rightBlock != null && rightBlock.needsAliases(aliases);
  }

  @Override
  public ContainsAnyCondition copy() {
    ContainsAnyCondition result = new ContainsAnyCondition(-1);
    result.left = left.copy();
    result.right = right == null ? null : right.copy();
    result.rightBlock = rightBlock == null ? null : rightBlock.copy();
    return result;
  }

  @Override
  public void extractSubQueries(SubQueryCollector collector) {
    left.extractSubQueries(collector);
    if (right != null) {
      right.extractSubQueries(collector);
    }
    if (rightBlock != null) {
      rightBlock.extractSubQueries(collector);
    }
  }

  @Override
  public boolean refersToParent() {
    if (left != null && left.refersToParent()) {
      return true;
    }
    if (right != null && right.refersToParent()) {
      return true;
    }
    return rightBlock != null && rightBlock.refersToParent();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ContainsAnyCondition that = (ContainsAnyCondition) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    if (right != null ? !right.equals(that.right) : that.right != null)
      return false;
    return rightBlock != null ? rightBlock.equals(that.rightBlock) : that.rightBlock == null;
  }

  @Override
  public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (right != null ? right.hashCode() : 0);
    result = 31 * result + (rightBlock != null ? rightBlock.hashCode() : 0);
    return result;
  }

  @Override
  public List<String> getMatchPatternInvolvedAliases() {
    List<String> leftX = left == null ? null : left.getMatchPatternInvolvedAliases();
    List<String> rightX = right == null ? null : right.getMatchPatternInvolvedAliases();
    List<String> rightBlockX = rightBlock == null ? null : rightBlock.getMatchPatternInvolvedAliases();

    List<String> result = new ArrayList<String>();
    if (leftX != null) {
      result.addAll(leftX);
    }
    if (rightX != null) {
      result.addAll(rightX);
    }
    if (rightBlockX != null) {
      result.addAll(rightBlockX);
    }

    return result.size() == 0 ? null : result;
  }

  @Override
  public boolean isCacheable() {
    if (left != null && !left.isCacheable()) {
      return false;
    }

    if (right != null && !right.isCacheable()) {
      return false;
    }

    return rightBlock == null || rightBlock.isCacheable();
  }
}
/* JavaCC - OriginalChecksum=7992ab9e8e812c6d9358ede8b67b4506 (do not edit this line) */
