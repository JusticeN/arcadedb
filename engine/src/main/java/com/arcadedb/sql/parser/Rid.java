/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. ORid.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.database.RID;
import com.arcadedb.sql.executor.BasicCommandContext;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.Map;

public class Rid extends SimpleNode {
  protected PInteger bucket;
  protected PInteger position;

  protected Expression expression;
  protected boolean    legacy;

  public Rid(int id) {
    super(id);
  }

  public Rid(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString(String prefix) {
    return "#" + bucket.getValue() + ":" + position.getValue();
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (legacy) {
      builder.append("#" + bucket.getValue() + ":" + position.getValue());
    } else {
      builder.append("{\"@rid\":");
      expression.toString(params, builder);
      builder.append("}");
    }
  }

  public RID toRecordId(final Result target, final CommandContext ctx) {
    if (legacy) {
      return new RID(ctx.getDatabase(), bucket.value.intValue(), position.value.longValue());
    } else {
      Object result = expression.execute(target, ctx);
      if (result == null) {
        return null;
      }
      if (result instanceof Identifiable) {
        return ((Identifiable) result).getIdentity();
      }
      if (result instanceof String) {
        throw new UnsupportedOperationException();
      }
      return null;
    }
  }

  public RID toRecordId(Identifiable target, CommandContext ctx) {
    if (legacy) {
      return new RID(ctx.getDatabase(), bucket.value.intValue(), position.value.longValue());
    } else {
      Object result = expression.execute(target, ctx);
      if (result == null) {
        return null;
      }
      if (result instanceof Identifiable) {
        return ((Identifiable) result).getIdentity();
      }
      if (result instanceof String) {
        throw new UnsupportedOperationException();
      }
      return null;
    }
  }

  public Rid copy() {
    Rid result = new Rid(-1);
    result.bucket = bucket == null ? null : bucket.copy();
    result.position = position == null ? null : position.copy();
    result.expression = expression == null ? null : expression.copy();
    result.legacy = legacy;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Rid oRid = (Rid) o;

    if (bucket != null ? !bucket.equals(oRid.bucket) : oRid.bucket != null)
      return false;
    if (position != null ? !position.equals(oRid.position) : oRid.position != null)
      return false;
    if (expression != null ? !expression.equals(oRid.expression) : oRid.expression != null)
      return false;
    return legacy == oRid.legacy;
  }

  @Override
  public int hashCode() {
    int result = bucket != null ? bucket.hashCode() : 0;
    result = 31 * result + (position != null ? position.hashCode() : 0);
    result = 31 * result + (expression != null ? expression.hashCode() : 0);
    return result;
  }

  public void setBucket(PInteger bucket) {
    this.bucket = bucket;
  }

  public void setPosition(PInteger position) {
    this.position = position;
  }

  public void setLegacy(boolean b) {
    this.legacy = b;
  }

  public PInteger getBucket() {
    if (expression != null) {
      RID rid = toRecordId((Result) null, new BasicCommandContext());
      if (rid == null) {
        PInteger result = new PInteger(-1);
        result.setValue(rid.getBucketId());
        return result;
      }
    }
    return bucket;
  }

  public PInteger getPosition() {
    if (expression != null) {
      RID rid = toRecordId((Result) null, new BasicCommandContext());
      if (rid == null) {
        PInteger result = new PInteger(-1);
        result.setValue(rid.getPosition());
        return result;
      }
    }
    return position;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if(bucket !=null){
      result.setProperty("bucket", bucket.serialize());
    }
    if(position!=null){
      result.setProperty("position", position.serialize());
    }
    if(expression!=null){
      result.setProperty("expression", expression.serialize());
    }
    result.setProperty("legacy", legacy);
    return result;
  }

  public void deserialize(Result fromResult) {
    if(fromResult.getProperty("bucket")!=null){
      bucket = new PInteger(-1);
      bucket.deserialize(fromResult.getProperty("bucket"));
    }
    if(fromResult.getProperty("position")!=null){
      position = new PInteger(-1);
      position.deserialize(fromResult.getProperty("position"));
    }
    if(fromResult.getProperty("expression")!=null){
      expression = new Expression(-1);
      expression.deserialize(fromResult.getProperty("expression"));
    }
    legacy = fromResult.getProperty("legacy");
  }
}
/* JavaCC - OriginalChecksum=c2c6d67d7722e29212e438574698d7cd (do not edit this line) */
