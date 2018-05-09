/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.sql.function.math;

import com.arcadedb.database.Identifiable;
import com.arcadedb.schema.Type;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.MultiValue;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Compute the average value for a field. Uses the context to save the last average number. When different Number class are used,
 * take the class with most precision.
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
public class SQLFunctionAverage extends SQLFunctionMathAbstract {
  public static final String NAME = "avg";

  private Number sum;
  private int    total = 0;

  public SQLFunctionAverage() {
    super(NAME, 1, -1);
  }

  public Object execute( Object iThis, Identifiable iCurrentRecord, Object iCurrentResult,
      final Object[] iParams, CommandContext iContext) {
    if (iParams.length == 1) {
      if (iParams[0] instanceof Number)
        sum((Number) iParams[0]);
      else if (MultiValue.isMultiValue(iParams[0]))
        for (Object n : MultiValue.getMultiValueIterable(iParams[0]))
          sum((Number) n);

    } else {
      sum = null;
      for (int i = 0; i < iParams.length; ++i)
        sum((Number) iParams[i]);
    }

    return getResult();
  }

  protected void sum(Number value) {
    if (value != null) {
      total++;
      if (sum == null)
        // FIRST TIME
        sum = value;
      else
        sum = Type.increment(sum, value);
    }
  }

  public String getSyntax() {
    return "avg(<field> [,<field>*])";
  }

  @Override
  public Object getResult() {
    return computeAverage(sum, total);
  }

  @Override
  public boolean aggregateResults() {
    return configuredParameters.length == 1;
  }

  private Object computeAverage(Number iSum, int iTotal) {
    if (iSum instanceof Integer)
      return iSum.intValue() / iTotal;
    else if (iSum instanceof Long)
      return iSum.longValue() / iTotal;
    else if (iSum instanceof Float)
      return iSum.floatValue() / iTotal;
    else if (iSum instanceof Double)
      return iSum.doubleValue() / iTotal;
    else if (iSum instanceof BigDecimal)
      return ((BigDecimal) iSum).divide(new BigDecimal(iTotal), RoundingMode.HALF_UP);

    return null;
  }
}
