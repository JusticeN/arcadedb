/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OCreateVertexStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.sql.executor.*;

import java.util.HashMap;
import java.util.Map;

public class CreateVertexStatement extends Statement {

  Identifier targetType;
  Identifier targetBucketName;
  Bucket     targetBucket;
  Projection returnStatement;
  InsertBody insertBody;

  public CreateVertexStatement(int id) {
    super(id);
  }

  public CreateVertexStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override public ResultSet execute(Database db, Map params, CommandContext parentCtx) {
    BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    InsertExecutionPlan executionPlan = (InsertExecutionPlan) createExecutionPlan(ctx, false);
    executionPlan.executeInternal(targetType.getStringValue());
    return new LocalResultSet(executionPlan);
  }

  @Override public ResultSet execute(Database db, Object[] args, CommandContext parentCtx) {
    BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    Map<Object, Object> params = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        params.put(i, args[i]);
      }
    }
    ctx.setInputParameters(params);
    InsertExecutionPlan executionPlan = (InsertExecutionPlan) createExecutionPlan(ctx, false);
    executionPlan.executeInternal(targetType.getStringValue());
    return new LocalResultSet(executionPlan);
  }

  @Override public InternalExecutionPlan createExecutionPlan(CommandContext ctx, boolean enableProfiling) {
    OCreateVertexExecutionPlanner planner = new OCreateVertexExecutionPlanner(this);
    return planner.createExecutionPlan(ctx, enableProfiling);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {

    builder.append("CREATE VERTEX ");
    if (targetType != null) {
      targetType.toString(params, builder);
      if (targetBucketName != null) {
        builder.append(" BUCKET ");
        targetBucketName.toString(params, builder);
      }
    }
    if (targetBucket != null) {
      targetBucket.toString(params, builder);
    }
    if (returnStatement != null) {
      builder.append(" RETURN ");
      returnStatement.toString(params, builder);
    }
    if (insertBody != null) {
      if (targetType != null || targetBucket != null || returnStatement != null) {
        builder.append(" ");
      }
      insertBody.toString(params, builder);
    }
  }

  @Override public CreateVertexStatement copy() {
    CreateVertexStatement result = null;
    try {
      result = getClass().getConstructor(Integer.TYPE).newInstance(-1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    result.targetType = targetType == null ? null : targetType.copy();
    result.targetBucketName = targetBucketName == null ? null : targetBucketName.copy();
    result.targetBucket = targetBucket == null ? null : targetBucket.copy();
    result.returnStatement = returnStatement == null ? null : returnStatement.copy();
    result.insertBody = insertBody == null ? null : insertBody.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    CreateVertexStatement that = (CreateVertexStatement) o;

    if (targetType != null ? !targetType.equals(that.targetType) : that.targetType != null)
      return false;
    if (targetBucketName != null ? !targetBucketName.equals(that.targetBucketName) : that.targetBucketName != null)
      return false;
    if (targetBucket != null ? !targetBucket.equals(that.targetBucket) : that.targetBucket != null)
      return false;
    if (returnStatement != null ? !returnStatement.equals(that.returnStatement) : that.returnStatement != null)
      return false;
    return insertBody != null ? insertBody.equals(that.insertBody) : that.insertBody == null;
  }

  @Override public int hashCode() {
    int result = targetType != null ? targetType.hashCode() : 0;
    result = 31 * result + (targetBucketName != null ? targetBucketName.hashCode() : 0);
    result = 31 * result + (targetBucket != null ? targetBucket.hashCode() : 0);
    result = 31 * result + (returnStatement != null ? returnStatement.hashCode() : 0);
    result = 31 * result + (insertBody != null ? insertBody.hashCode() : 0);
    return result;
  }

  public Identifier getTargetType() {
    return targetType;
  }

  public void setTargetType(Identifier targetType) {
    this.targetType = targetType;
  }

  public Identifier getTargetBucketName() {
    return targetBucketName;
  }

  public void setTargetBucketName(Identifier targetBucketName) {
    this.targetBucketName = targetBucketName;
  }

  public Bucket getTargetBucket() {
    return targetBucket;
  }

  public void setTargetBucket(Bucket targetBucket) {
    this.targetBucket = targetBucket;
  }

  public Projection getReturnStatement() {
    return returnStatement;
  }

  public void setReturnStatement(Projection returnStatement) {
    this.returnStatement = returnStatement;
  }

  public InsertBody getInsertBody() {
    return insertBody;
  }

  public void setInsertBody(InsertBody insertBody) {
    this.insertBody = insertBody;
  }
}
/* JavaCC - OriginalChecksum=0ac3d3f09a76b9924a17fd05bc293863 (do not edit this line) */
