/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OSleepStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.InternalResultSet;
import com.arcadedb.sql.executor.ResultInternal;
import com.arcadedb.sql.executor.ResultSet;

import java.util.Map;

public class SleepStatement extends SimpleExecStatement {

  protected PInteger millis;

  public SleepStatement(int id) {
    super(id);
  }

  public SleepStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override public ResultSet executeSimple(CommandContext ctx) {

    InternalResultSet result = new InternalResultSet();
    ResultInternal item = new ResultInternal();
    item.setProperty("operation", "sleep");
    try {
      Thread.sleep(millis.getValue().intValue());
      item.setProperty("result", "OK");
      item.setProperty("millis", millis.getValue().intValue());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      item.setProperty("result", "failure");
      item.setProperty("errorType", e.getClass().getSimpleName());
      item.setProperty("errorMessage", e.getMessage());
    }
    result.add(item);
    return result;

  }

  @Override public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("SLEEP ");
    millis.toString(params, builder);
  }

  @Override public SleepStatement copy() {
    SleepStatement result = new SleepStatement(-1);
    result.millis = millis == null ? null : millis.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SleepStatement that = (SleepStatement) o;

    return millis != null ? millis.equals(that.millis) : that.millis == null;
  }

  @Override public int hashCode() {
    return millis != null ? millis.hashCode() : 0;
  }
}
/* JavaCC - OriginalChecksum=2ea765ee266d4215414908b0e09c0779 (do not edit this line) */
