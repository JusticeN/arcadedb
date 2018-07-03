/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OAlterClusterStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.InternalResultSet;
import com.arcadedb.sql.executor.ResultSet;

import java.util.Map;

public class AlterBucketStatement extends ODDLStatement {

  protected Identifier name;
  protected boolean    starred = false;
  protected Identifier attributeName;
  protected Expression attributeValue;

  public AlterBucketStatement(int id) {
    super(id);
  }

  public AlterBucketStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("ALTER BUCKET ");
    name.toString(params, builder);
    if (starred) {
      builder.append("*");
    }
    builder.append(" ");
    attributeName.toString(params, builder);
    builder.append(" ");
    attributeValue.toString(params, builder);
  }

  @Override
  public AlterBucketStatement copy() {
    AlterBucketStatement result = new AlterBucketStatement(-1);
    result.name = name == null ? null : name.copy();
    result.attributeName = attributeName == null ? null : attributeName.copy();
    result.starred = starred;
    result.attributeValue = attributeValue == null ? null : attributeValue.copy();
    return result;
  }

  @Override
  public ResultSet executeDDL(CommandContext ctx) {
    InternalResultSet result = new InternalResultSet();
//    List<com.orientechnologies.orient.core.storage.OCluster> clustersToUpdate = getClusters(ctx);
//    Object finalValue = attributeValue.execute((PIdentifiable) null, ctx);
//
//    com.orientechnologies.orient.core.storage.OCluster.ATTRIBUTES attribute;
//    try {
//      attribute = OCluster.ATTRIBUTES.valueOf(attributeName.getStringValue());
//    } catch (IllegalArgumentException e) {
//      throw OException.wrapException(new PCommandExecutionException(
//          "Unknown class attribute '" + attributeName + "'. Supported attributes are: " + Arrays
//              .toString(OCluster.ATTRIBUTES.values())), e);
//    }
//
//    for (com.orientechnologies.orient.core.storage.OCluster cluster : clustersToUpdate) {
//      if (attributeName.getStringValue().equalsIgnoreCase("status") || attributeName.getStringValue().equalsIgnoreCase("name"))
//        // REMOVE CACHE OF COMMAND RESULTS IF ACTIVE
//        getDatabase().getMetadata().getCommandCache().invalidateResultsOfCluster(cluster.getName());
//      try {
//        cluster.set(attribute, finalValue);
//      } catch (IOException e) {
//        OException.wrapException(new PCommandExecutionException("Cannot execute alter cluster"), e);
//      }
//      OResultInternal resultItem = new OResultInternal();
//      resultItem.setProperty("cluster", cluster.getName());
//      result.add(resultItem);
//    }

//    return result;
    throw new UnsupportedOperationException();
  }

//  private OCluster.ATTRIBUTES getClusterAttribute(OIdentifier attributeName) {
//    return null;
//  }
//
//  private List<com.orientechnologies.orient.core.storage.OCluster> getClusters(OCommandContext ctx) {
//    OStorage storage = ((ODatabaseDocumentInternal) ctx.getDatabase()).getStorage();
//    if (starred) {
//      List<com.orientechnologies.orient.core.storage.OCluster> result = new ArrayList<>();
//      for (String clusterName : storage.getClusterNames()) {
//        if (clusterName.startsWith(name.getStringValue())) {
//          result.add(storage.getClusterByName(clusterName));
//        }
//      }
//      return result;
//    } else {
//      int clusterId = ctx.getDatabase().getClusterIdByName(name.getStringValue());
//      if (clusterId <= 0) {
//        throw new PCommandExecutionException("Cannot find cluster " + name);
//      }
//      com.orientechnologies.orient.core.storage.OCluster cluster = storage.getClusterById(clusterId);
//      return Collections.singletonList(cluster);
//    }
//  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    AlterBucketStatement that = (AlterBucketStatement) o;

    if (starred != that.starred)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (attributeName != null ? !attributeName.equals(that.attributeName) : that.attributeName != null)
      return false;
    return attributeValue != null ? attributeValue.equals(that.attributeValue) : that.attributeValue == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (attributeName != null ? attributeName.hashCode() : 0);
    result = 31 * result + (starred ? 1 : 0);
    result = 31 * result + (attributeValue != null ? attributeValue.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=ed78ea0f1a05b0963db625ed1f338bd6 (do not edit this line) */
