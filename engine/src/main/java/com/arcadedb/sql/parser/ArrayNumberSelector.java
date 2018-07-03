/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OArrayNumberSelector.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.Map;
import java.util.Set;

public class ArrayNumberSelector extends SimpleNode {
  InputParameter inputValue;
  MathExpression expressionValue;

  Integer integer;

  public ArrayNumberSelector(int id) {
    super(id);
  }

  public ArrayNumberSelector(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (inputValue != null) {
      inputValue.toString(params, builder);
    } else if (expressionValue != null) {
      expressionValue.toString(params, builder);
    } else if (integer != null) {
      builder.append(integer);
    }
  }

  public Integer getValue(Identifiable iCurrentRecord, Object iResult, CommandContext ctx) {
    Object result = null;
    if (inputValue != null) {
      result = inputValue.getValue(ctx.getInputParameters());
    } else if (expressionValue != null) {
      result = expressionValue.execute(iCurrentRecord, ctx);
    } else if (integer != null) {
      result = integer;
    }

    if (result == null) {
      return null;
    }
    if (result instanceof Number) {
      return ((Number) result).intValue();
    }
    return null;
  }

  public Integer getValue(Result iCurrentRecord, Object iResult, CommandContext ctx) {
    Object result = null;
    if (inputValue != null) {
      result = inputValue.getValue(ctx.getInputParameters());
    } else if (expressionValue != null) {
      result = expressionValue.execute(iCurrentRecord, ctx);
    } else if (integer != null) {
      result = integer;
    }

    if (result == null) {
      return null;
    }
    if (result instanceof Number) {
      return ((Number) result).intValue();
    }
    return null;
  }

  public boolean needsAliases(Set<String> aliases) {
    if (expressionValue != null) {
      return expressionValue.needsAliases(aliases);
    }
    return false;
  }

  public ArrayNumberSelector copy() {
    ArrayNumberSelector result = new ArrayNumberSelector(-1);
    result.inputValue = inputValue == null ? null : inputValue.copy();
    result.expressionValue = expressionValue == null ? null : expressionValue.copy();
    result.integer = integer;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ArrayNumberSelector that = (ArrayNumberSelector) o;

    if (inputValue != null ? !inputValue.equals(that.inputValue) : that.inputValue != null)
      return false;
    if (expressionValue != null ? !expressionValue.equals(that.expressionValue) : that.expressionValue != null)
      return false;
    return integer != null ? integer.equals(that.integer) : that.integer == null;
  }

  @Override
  public int hashCode() {
    int result = inputValue != null ? inputValue.hashCode() : 0;
    result = 31 * result + (expressionValue != null ? expressionValue.hashCode() : 0);
    result = 31 * result + (integer != null ? integer.hashCode() : 0);
    return result;
  }

  public void extractSubQueries(SubQueryCollector collector) {
    if (expressionValue != null) {
      expressionValue.extractSubQueries(collector);
    }
  }

  public boolean refersToParent() {
    return expressionValue != null && expressionValue.refersToParent();
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if (inputValue != null) {
      result.setProperty("inputValue", inputValue.serialize());
    }
    if (expressionValue != null) {
      result.setProperty("expressionValue", expressionValue.serialize());
    }
    result.setProperty("integer", integer);
    return result;
  }

  public void deserialize(Result fromResult) {
    if (fromResult.getProperty("inputValue") != null) {
      inputValue = InputParameter.deserializeFromOResult(fromResult.getProperty("inputValue"));
    }
    if (fromResult.getProperty("toSelector") != null) {
      expressionValue = new MathExpression(-1);
      expressionValue.deserialize(fromResult.getProperty("expressionValue"));
    }
    integer = fromResult.getProperty("integer");
  }
}
/* JavaCC - OriginalChecksum=5b2e495391ede3ccdc6c25aa63c8e591 (do not edit this line) */
