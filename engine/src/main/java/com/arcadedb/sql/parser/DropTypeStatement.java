/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. ODropClassStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.graph.Edge;
import com.arcadedb.graph.Vertex;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.InternalResultSet;
import com.arcadedb.sql.executor.ResultSet;

import java.util.Map;

public class DropTypeStatement extends ODDLStatement {

  public Identifier name;
  public boolean    ifExists = false;
  public boolean    unsafe   = false;

  public DropTypeStatement(int id) {
    super(id);
  }

  public DropTypeStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public ResultSet executeDDL(CommandContext ctx) {
    Schema schema = ctx.getDatabase().getSchema();
    DocumentType clazz = schema.getType(name.getStringValue());
    if (clazz == null) {
      if (ifExists) {
        return new InternalResultSet();
      }
      throw new CommandExecutionException("Type '" + name.getStringValue() + "' does not exist");
    }

    if (!unsafe && ctx.getDatabase().countType(clazz.getName(), false) > 0) {
      //check vertex or edge
      if (clazz.getType() == Vertex.RECORD_TYPE) {
        throw new CommandExecutionException("'DROP TYPE' command cannot drop type '" + name.getStringValue()
            + "' because it contains Vertices. Use 'DELETE VERTEX' command first to avoid broken edges in a database, or apply the 'UNSAFE' keyword to force it");
      } else if (clazz.getType() == Edge.RECORD_TYPE) {
        // FOUND EDGE CLASS
        throw new CommandExecutionException("'DROP TYPE' command cannot drop type '" + name.getStringValue()
            + "' because it contains Edges. Use 'DELETE EDGE' command first to avoid broken vertices in a database, or apply the 'UNSAFE' keyword to force it");
      }
    }

    throw new UnsupportedOperationException();
//    schema.dropClass(name.getStringValue());
//
//    OInternalResultSet rs = new OInternalResultSet();
//    OResultInternal result = new OResultInternal();
//    result.setProperty("operation", "drop class");
//    result.setProperty("className", name.getStringValue());
//    rs.add(result);
//    return rs;
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("DROP TYPE ");
    name.toString(params, builder);
    if (ifExists) {
      builder.append(" IF EXISTS");
    }
    if (unsafe) {
      builder.append(" UNSAFE");
    }
  }

  @Override
  public DropTypeStatement copy() {
    DropTypeStatement result = new DropTypeStatement(-1);
    result.name = name == null ? null : name.copy();
    result.ifExists = ifExists;
    result.unsafe = unsafe;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DropTypeStatement that = (DropTypeStatement) o;

    if (unsafe != that.unsafe)
      return false;
    if (ifExists != that.ifExists)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (unsafe ? 1 : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=8c475e1225074f68be37fce610987d54 (do not edit this line) */
