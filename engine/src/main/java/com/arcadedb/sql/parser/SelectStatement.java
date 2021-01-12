/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

/* Generated By:JJTree: Do not edit this line. OSelectStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
/*


 */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.exception.CommandSQLParsingException;
import com.arcadedb.sql.executor.*;

import java.util.HashMap;
import java.util.Map;

public class SelectStatement extends Statement {

  protected FromClause target;

  protected Projection projection;

  protected WhereClause whereClause;

  protected GroupBy groupBy;

  protected OrderBy orderBy;

  protected Unwind unwind;

  protected Skip skip;

  protected Limit limit;

  protected Object lockRecord = null;

  protected FetchPlan fetchPlan;

  protected LetClause letClause;

  protected Timeout timeout;

  protected Boolean parallel;

  protected Boolean noCache;

  public SelectStatement(int id) {
    super(id);
  }

  public SelectStatement(SqlParser p, int id) {
    super(p, id);
  }

  public Projection getProjection() {
    return projection;
  }

  public void setProjection(Projection projection) {
    this.projection = projection;
  }

  public FromClause getTarget() {
    return target;
  }

  public void setTarget(FromClause target) {
    this.target = target;
  }

  public WhereClause getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(WhereClause whereClause) {
    this.whereClause = whereClause;
  }

  public GroupBy getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(GroupBy groupBy) {
    this.groupBy = groupBy;
  }

  public OrderBy getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(OrderBy orderBy) {
    this.orderBy = orderBy;
  }

  public Skip getSkip() {
    return skip;
  }

  public void setSkip(Skip skip) {
    this.skip = skip;
  }

  public Limit getLimit() {
    return limit;
  }

  public void setLimit(Limit limit) {
    this.limit = limit;
  }

  public Object getLockRecord() {
    return lockRecord;
  }

  public void setLockRecord(Object lockRecord) {
    this.lockRecord = lockRecord;
  }

  public FetchPlan getFetchPlan() {
    return fetchPlan;
  }

  public void setFetchPlan(FetchPlan fetchPlan) {
    this.fetchPlan = fetchPlan;
  }

  public LetClause getLetClause() {
    return letClause;
  }

  public void setLetClause(LetClause letClause) {
    this.letClause = letClause;
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {

    builder.append("SELECT");
    if (projection != null) {
      builder.append(" ");
      projection.toString(params, builder);
    }
    if (target != null) {
      builder.append(" FROM ");
      target.toString(params, builder);
    }

    if (letClause != null) {
      builder.append(" ");
      letClause.toString(params, builder);
    }

    if (whereClause != null) {
      builder.append(" WHERE ");
      whereClause.toString(params, builder);
    }

    if (groupBy != null) {
      builder.append(" ");
      groupBy.toString(params, builder);
    }

    if (orderBy != null) {
      builder.append(" ");
      orderBy.toString(params, builder);
    }

    if (unwind != null) {
      builder.append(" ");
      unwind.toString(params, builder);
    }

    if (skip != null) {
      skip.toString(params, builder);
    }

    if (limit != null) {
      limit.toString(params, builder);
    }

//    if (lockRecord != null) {
//      builder.append(" LOCK ");
//      switch (lockRecord) {
//      case DEFAULT:
//        builder.append("DEFAULT");
//        break;
//      case EXCLUSIVE_LOCK:
//        builder.append("RECORD");
//        break;
//      case SHARED_LOCK:
//        builder.append("SHARED");
//        break;
//      case NONE:
//        builder.append("NONE");
//        break;
//      }
//    }

    if (fetchPlan != null) {
      builder.append(" ");
      fetchPlan.toString(params, builder);
    }

    if (timeout != null) {
      timeout.toString(params, builder);
    }

    if (Boolean.TRUE.equals(parallel)) {
      builder.append(" PARALLEL");
    }

    if (Boolean.TRUE.equals(noCache)) {
      builder.append(" NOCACHE");
    }
  }

  public void validate() throws CommandSQLParsingException {
    if (projection != null) {
      projection.validate();
      if (projection.isExpand() && groupBy != null) {
        throw new CommandSQLParsingException("expand() cannot be used together with GROUP BY");
      }
    }
  }

  @Override
  public boolean executinPlanCanBeCached() {
    if (originalStatement == null) {
      return false;
    }
    if (this.target != null && !this.target.isCacheable()) {
      return false;
    }

    if (this.letClause != null && !this.letClause.isCacheable()) {
      return false;
    }

    if (projection != null && !this.projection.isCacheable()) {
      return false;
    }

    return whereClause == null || whereClause.isCacheable();
  }

  @Override
  public ResultSet execute(final Database db, final Object[] args, final CommandContext parentCtx) {
    final BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    final Map<Object, Object> params = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        params.put(i, args[i]);
      }
    }
    ctx.setInputParameters(params);
    final InternalExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    final LocalResultSet result = new LocalResultSet(executionPlan);
    return result;
  }

  @Override
  public ResultSet execute(final Database db, final Map params, final CommandContext parentCtx) {
    final BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    final InternalExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    final LocalResultSet result = new LocalResultSet(executionPlan);
    return result;
  }

  public InternalExecutionPlan createExecutionPlan(CommandContext ctx, boolean enableProfiling) {
    OSelectExecutionPlanner planner = new OSelectExecutionPlanner(this);
    return planner.createExecutionPlan(ctx, enableProfiling);
  }

  @Override
  public SelectStatement copy() {
    SelectStatement result = null;
    try {
      result = getClass().getConstructor(Integer.TYPE).newInstance(-1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    result.originalStatement = originalStatement;
    result.target = target == null ? null : target.copy();
    result.projection = projection == null ? null : projection.copy();
    result.whereClause = whereClause == null ? null : whereClause.copy();
    result.groupBy = groupBy == null ? null : groupBy.copy();
    result.orderBy = orderBy == null ? null : orderBy.copy();
    result.unwind = unwind == null ? null : unwind.copy();
    result.skip = skip == null ? null : skip.copy();
    result.limit = limit == null ? null : limit.copy();
    result.lockRecord = lockRecord;
    result.fetchPlan = fetchPlan == null ? null : fetchPlan.copy();
    result.letClause = letClause == null ? null : letClause.copy();
    result.timeout = timeout == null ? null : timeout.copy();
    result.parallel = parallel;
    result.noCache = noCache;

    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SelectStatement that = (SelectStatement) o;

    if (target != null ? !target.equals(that.target) : that.target != null)
      return false;
    if (projection != null ? !projection.equals(that.projection) : that.projection != null)
      return false;
    if (whereClause != null ? !whereClause.equals(that.whereClause) : that.whereClause != null)
      return false;
    if (groupBy != null ? !groupBy.equals(that.groupBy) : that.groupBy != null)
      return false;
    if (orderBy != null ? !orderBy.equals(that.orderBy) : that.orderBy != null)
      return false;
    if (unwind != null ? !unwind.equals(that.unwind) : that.unwind != null)
      return false;
    if (skip != null ? !skip.equals(that.skip) : that.skip != null)
      return false;
    if (limit != null ? !limit.equals(that.limit) : that.limit != null)
      return false;
    if (lockRecord != that.lockRecord)
      return false;
    if (fetchPlan != null ? !fetchPlan.equals(that.fetchPlan) : that.fetchPlan != null)
      return false;
    if (letClause != null ? !letClause.equals(that.letClause) : that.letClause != null)
      return false;
    if (timeout != null ? !timeout.equals(that.timeout) : that.timeout != null)
      return false;
    if (parallel != null ? !parallel.equals(that.parallel) : that.parallel != null)
      return false;
    return noCache != null ? noCache.equals(that.noCache) : that.noCache == null;
  }

  @Override
  public int hashCode() {
    int result = target != null ? target.hashCode() : 0;
    result = 31 * result + (projection != null ? projection.hashCode() : 0);
    result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0);
    result = 31 * result + (groupBy != null ? groupBy.hashCode() : 0);
    result = 31 * result + (orderBy != null ? orderBy.hashCode() : 0);
    result = 31 * result + (unwind != null ? unwind.hashCode() : 0);
    result = 31 * result + (skip != null ? skip.hashCode() : 0);
    result = 31 * result + (limit != null ? limit.hashCode() : 0);
    result = 31 * result + (lockRecord != null ? lockRecord.hashCode() : 0);
    result = 31 * result + (fetchPlan != null ? fetchPlan.hashCode() : 0);
    result = 31 * result + (letClause != null ? letClause.hashCode() : 0);
    result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
    result = 31 * result + (parallel != null ? parallel.hashCode() : 0);
    result = 31 * result + (noCache != null ? noCache.hashCode() : 0);
    return result;
  }

  @Override
  public boolean refersToParent() {
    //no FROM, if a subquery refers to parent it does not make sense, so that reference will be just ignored

    if (projection != null && projection.refersToParent()) {
      return true;
    }
    if (whereClause != null && whereClause.refersToParent()) {
      return true;
    }
    if (groupBy != null && groupBy.refersToParent()) {
      return true;
    }
    if (orderBy != null && orderBy.refersToParent()) {
      return true;
    }
    return letClause != null && letClause.refersToParent();
  }

  public Unwind getUnwind() {
    return unwind;
  }

  @Override
  public boolean isIdempotent() {
    return true;
  }

  public void setUnwind(Unwind unwind) {
    this.unwind = unwind;
  }

  public Timeout getTimeout() {
    return timeout;
  }

  public void setTimeout(Timeout timeout) {
    this.timeout = timeout;
  }

  public void setParallel(Boolean parallel) {
    this.parallel = parallel;
  }

  public void setNoCache(Boolean noCache) {
    this.noCache = noCache;
  }

  public Result serialize() {
    ResultInternal result = (ResultInternal) super.serialize();
    if (target != null) {
      result.setProperty("target", target.serialize());
    }
    if (projection != null) {
      result.setProperty("projection", projection.serialize());
    }
    if (whereClause != null) {
      result.setProperty("whereClause", whereClause.serialize());
    }
    if (groupBy != null) {
      result.setProperty("groupBy", groupBy.serialize());
    }
    if (orderBy != null) {
      result.setProperty("orderBy", orderBy.serialize());
    }
    if (unwind != null) {
      result.setProperty("unwind", unwind.serialize());
    }
    if (skip != null) {
      result.setProperty("skip", skip.serialize());
    }
    if (limit != null) {
      result.setProperty("limit", limit.serialize());
    }
    if (lockRecord != null) {
      result.setProperty("lockRecord", lockRecord.toString());
    }
    if (fetchPlan != null) {
      result.setProperty("fetchPlan", fetchPlan.serialize());
    }
    if (letClause != null) {
      result.setProperty("letClause", letClause.serialize());
    }
    if (timeout != null) {
      result.setProperty("timeout", timeout.serialize());
    }
    result.setProperty("parallel", parallel);
    result.setProperty("noCache", noCache);
    return result;
  }

  public void deserialize(Result fromResult) {
    if (fromResult.getProperty("target") != null) {
      target = new FromClause(-1);
      target.deserialize(fromResult.getProperty("target"));
    }
    if (fromResult.getProperty("projection") != null) {
      projection = new Projection(-1);
      projection.deserialize(fromResult.getProperty("projection"));
    }
    if (fromResult.getProperty("whereClause") != null) {
      whereClause = new WhereClause(-1);
      whereClause.deserialize(fromResult.getProperty("whereClause"));
    }
    if (fromResult.getProperty("groupBy") != null) {
      groupBy = new GroupBy(-1);
      groupBy.deserialize(fromResult.getProperty("groupBy"));
    }
    if (fromResult.getProperty("orderBy") != null) {
      orderBy = new OrderBy(-1);
      orderBy.deserialize(fromResult.getProperty("orderBy"));
    }
    if (fromResult.getProperty("unwind") != null) {
      unwind = new Unwind(-1);
      unwind.deserialize(fromResult.getProperty("unwind"));
    }
    if (fromResult.getProperty("skip") != null) {
      skip = new Skip(-1);
      skip.deserialize(fromResult.getProperty("skip"));
    }
    if (fromResult.getProperty("limit") != null) {
      limit = new Limit(-1);
      limit.deserialize(fromResult.getProperty("limit"));
    }
    if (fromResult.getProperty("lockRecord") != null) {
      lockRecord = fromResult.getProperty("lockRecord");//TODO
    }
    if (fromResult.getProperty("fetchPlan") != null) {
      fetchPlan = new FetchPlan(-1);
      fetchPlan.deserialize(fromResult.getProperty("fetchPlan"));
    }
    if (fromResult.getProperty("letClause") != null) {
      letClause = new LetClause(-1);
      letClause.deserialize(fromResult.getProperty("letClause"));
    }
    if (fromResult.getProperty("timeout") != null) {
      timeout = new Timeout(-1);
      timeout.deserialize(fromResult.getProperty("timeout"));
    }

    parallel = fromResult.getProperty("parallel");
    noCache = fromResult.getProperty("noCache");
  }
}
/* JavaCC - OriginalChecksum=b26959b9726a8cf35d6283eca931da6b (do not edit this line) */
