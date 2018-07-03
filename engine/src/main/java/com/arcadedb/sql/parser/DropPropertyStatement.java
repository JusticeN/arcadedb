/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. ODropPropertyStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.InternalResultSet;
import com.arcadedb.sql.executor.ResultSet;

import java.util.Map;

public class DropPropertyStatement extends ODDLStatement {

  protected Identifier typeName;
  protected Identifier propertyName;
  protected boolean    ifExists = false;
  protected boolean    force = false;

  public DropPropertyStatement(int id) {
    super(id);
  }

  public DropPropertyStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override public ResultSet executeDDL(CommandContext ctx) {
    InternalResultSet rs = new InternalResultSet();
    final Database database = ctx.getDatabase();

    throw new UnsupportedOperationException();
//    final OClassImpl sourceClass = (OClassImpl) database.getMetadata().getSchema().getClass(className.getStringValue());
//    if (sourceClass == null)
//      throw new PCommandExecutionException("Source class '" + className + "' not found");
//
//    if (sourceClass.getProperty(propertyName.getStringValue()) == null) {
//      if(ifExists){
//        return rs;
//      }
//      throw new PCommandExecutionException("Property '" + propertyName + "' not found on class " + className);
//    }
//    final List<OIndex<?>> indexes = relatedIndexes(propertyName.getStringValue(), database);
//    if (!indexes.isEmpty()) {
//      if (force) {
//        for (final OIndex<?> index : indexes) {
//          index.delete();
//          OResultInternal result = new OResultInternal();
//          result.setProperty("operation", "cascade drop index");
//          result.setProperty("indexName", index.getName());
//          rs.add(result);
//        }
//      } else {
//        final StringBuilder indexNames = new StringBuilder();
//
//        boolean first = true;
//        for (final OIndex<?> index : sourceClass.getClassInvolvedIndexes(propertyName.getStringValue())) {
//          if (!first) {
//            indexNames.append(", ");
//          } else {
//            first = false;
//          }
//          indexNames.append(index.getName());
//        }
//
//        throw new PCommandExecutionException("Property used in indexes (" + indexNames.toString()
//            + "). Please drop these indexes before removing property or use FORCE parameter.");
//      }
//    }
//
//    // REMOVE THE PROPERTY
//    sourceClass.dropProperty(propertyName.getStringValue());
//
//    OResultInternal result = new OResultInternal();
//    result.setProperty("operation", "drop property");
//    result.setProperty("className", className.getStringValue());
//    result.setProperty("propertyname", propertyName.getStringValue());
//    rs.add(result);
//    return rs;
  }

//  private List<OIndex<?>> relatedIndexes(final String fieldName, ODatabase database) {
//    final List<OIndex<?>> result = new ArrayList<OIndex<?>>();
//    for (final OIndex<?> oIndex : database.getMetadata().getIndexManager().getClassIndexes(className.getStringValue())) {
//      if (OCollections.indexOf(oIndex.getDefinition().getFields(), fieldName, new OCaseInsentiveComparator()) > -1) {
//        result.add(oIndex);
//      }
//    }
//
//    return result;
//  }

  @Override public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("DROP PROPERTY ");
    typeName.toString(params, builder);
    builder.append(".");
    propertyName.toString(params, builder);
    if(ifExists){
      builder.append(" IF EXISTS");
    }
    if(force){
      builder.append(" FORCE");
    }
  }

  @Override public DropPropertyStatement copy() {
    DropPropertyStatement result = new DropPropertyStatement(-1);
    result.typeName = typeName == null ? null : typeName.copy();
    result.propertyName = propertyName == null ? null : propertyName.copy();
    result.force = force;
    result.ifExists = ifExists;
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DropPropertyStatement that = (DropPropertyStatement) o;

    if (force != that.force)
      return false;
    if(ifExists!=that.ifExists){
      return false;
    }
    if (typeName != null ? !typeName.equals(that.typeName) : that.typeName != null)
      return false;
    return propertyName != null ? propertyName.equals(that.propertyName) : that.propertyName == null;
  }

  @Override public int hashCode() {
    int result = typeName != null ? typeName.hashCode() : 0;
    result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
    result = 31 * result + (force ? 1 : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=6a9b4b1694dc36caf2b801218faebe42 (do not edit this line) */
