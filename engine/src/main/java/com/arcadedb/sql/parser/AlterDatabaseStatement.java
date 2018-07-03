/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OAlterDatabaseStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.ResultSet;

import java.util.Map;

public class AlterDatabaseStatement extends ODDLStatement {

  Identifier customPropertyName;
  Expression customPropertyValue;

  Identifier settingName;
  Expression settingValue;

  public AlterDatabaseStatement(int id) {
    super(id);
  }

  public AlterDatabaseStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override public ResultSet executeDDL(CommandContext ctx) {
//    OInternalResultSet result = new OInternalResultSet();
//    if (customPropertyName == null) {
//      result.add(executeSimpleAlter(settingName, settingValue, ctx));
//    } else {
//      result.add(executeCustomAlter(customPropertyName, customPropertyValue, ctx));
//    }
//    return result;
    throw new UnsupportedOperationException();
  }

//  private OResult executeCustomAlter(OIdentifier customPropertyName, OExpression customPropertyValue, OCommandContext ctx) {
//    ODatabaseDocumentInternal db = (ODatabaseDocumentInternal) ctx.getDatabase();
//    db.checkSecurity(ORule.ResourceGeneric.DATABASE, ORole.PERMISSION_UPDATE);
//    List<OStorageEntryConfiguration> oldValues = (List<OStorageEntryConfiguration>) db.get(ODatabase.ATTRIBUTES.CUSTOM);
//    String oldValue = null;
//    if (oldValues != null) {
//      for (OStorageEntryConfiguration entry : oldValues) {
//        if (entry.name.equals(customPropertyName.getStringValue())) {
//          oldValue = entry.value;
//          break;
//        }
//      }
//    }
//    Object finalValue = customPropertyValue.execute((PIdentifiable) null, ctx);
//    db.setCustom(customPropertyName.getStringValue(), finalValue);
//
//    OResultInternal result = new OResultInternal();
//    result.setProperty("operation", "alter database");
//    result.setProperty("customAttribute", customPropertyName.getStringValue());
//    result.setProperty("oldValue", oldValue);
//    result.setProperty("newValue", finalValue);
//    return result;
//  }
//
//  private OResult executeSimpleAlter(OIdentifier settingName, OExpression settingValue, OCommandContext ctx) {
//    ODatabase.ATTRIBUTES attribute = ODatabase.ATTRIBUTES.valueOf(settingName.getStringValue().toUpperCase(Locale.ENGLISH));
//    ODatabaseDocumentInternal db = (ODatabaseDocumentInternal) ctx.getDatabase();
//    db.checkSecurity(ORule.ResourceGeneric.DATABASE, ORole.PERMISSION_UPDATE);
//    Object oldValue = db.get(attribute);
//    Object finalValue = settingValue.execute((PIdentifiable) null, ctx);
//    db.setInternal(attribute, finalValue);
//
//    OResultInternal result = new OResultInternal();
//    result.setProperty("operation", "alter database");
//    result.setProperty("attribute", settingName.getStringValue());
//    result.setProperty("oldValue", oldValue);
//    result.setProperty("newValue", finalValue);
//    return result;
//  }

  @Override public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("ALTER DATABASE ");

    if (customPropertyName != null) {
      builder.append("CUSTOM ");
      customPropertyName.toString(params, builder);
      builder.append(" = ");
      customPropertyValue.toString(params, builder);
    } else {
      settingName.toString(params, builder);
      builder.append(" ");
      settingValue.toString(params, builder);
    }
  }

  @Override public AlterDatabaseStatement copy() {
    AlterDatabaseStatement result = new AlterDatabaseStatement(-1);
    result.customPropertyName = customPropertyName == null ? null : customPropertyName.copy();
    result.customPropertyValue = customPropertyValue == null ? null : customPropertyValue.copy();
    result.settingName = settingName == null ? null : settingName.copy();
    result.settingValue = settingValue == null ? null : settingValue.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    AlterDatabaseStatement that = (AlterDatabaseStatement) o;

    if (customPropertyName != null ? !customPropertyName.equals(that.customPropertyName) : that.customPropertyName != null)
      return false;
    if (customPropertyValue != null ? !customPropertyValue.equals(that.customPropertyValue) : that.customPropertyValue != null)
      return false;
    if (settingName != null ? !settingName.equals(that.settingName) : that.settingName != null)
      return false;
    return settingValue != null ? settingValue.equals(that.settingValue) : that.settingValue == null;
  }

  @Override public int hashCode() {
    int result = customPropertyName != null ? customPropertyName.hashCode() : 0;
    result = 31 * result + (customPropertyValue != null ? customPropertyValue.hashCode() : 0);
    result = 31 * result + (settingName != null ? settingName.hashCode() : 0);
    result = 31 * result + (settingValue != null ? settingValue.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=8fec57db8dd2a3b52aaa52dec7367cd4 (do not edit this line) */
