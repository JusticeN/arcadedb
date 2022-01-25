/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
/* Generated By:JJTree: Do not edit this line. ODropClusterStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.schema.DocumentType;

import java.util.Map;

public class DropBucketStatement extends DDLStatement {
  protected Identifier name;
  protected PInteger   id;
  protected boolean    ifExists = false;

  public DropBucketStatement(int id) {
    super(id);
  }

  public DropBucketStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public ResultSet executeDDL(CommandContext ctx) {
    Database database = ctx.getDatabase();
    // CHECK IF ANY USERTYPE IS USING IT
    final int bucketId;
    if (id != null) {
      bucketId = id.getValue().intValue();
    } else {
      bucketId = database.getSchema().getBucketByName(name.getStringValue()).getId();
      if (bucketId < 0) {
        if (ifExists) {
          return new InternalResultSet();
        } else {
          throw new CommandExecutionException("Bucket not found: " + name);
        }
      }
    }
    for (DocumentType iClass : database.getSchema().getTypes()) {
//      for (int i : iClass.getClusterIds()) {
//        if (i == bucketId) {
//          // IN USE
//          throw new PCommandExecutionException(
//              "Cannot drop bucket " + bucketId + " because it's used by class " + iClass.getName());
//        }
//      }
      //TODO
    }

    // REMOVE CACHE OF COMMAND RESULTS IF ACTIVE
    String bucketName = database.getSchema().getBucketById(bucketId).getName();
    if (bucketName == null) {
      if (ifExists) {
        return new InternalResultSet();
      } else {
        throw new CommandExecutionException("Bucket not found: " + bucketId);
      }
    }

//    ((OMetadataInternal) database.getMetadata()).getCommandCache().invalidateResultsOfCluster(bucketName);

//    database.dropCluster(bucketId, true);
//
//    OInternalResultSet rs = new OInternalResultSet();
//    OResultInternal result = new OResultInternal();
//    result.setProperty("operation", "drop cluster");
//    result.setProperty("bucketName", name == null ? null : name.getStringValue());
//    result.setProperty("bucketId", id == null ? null : id.getValue());
//    rs.add(result);
//    return rs;

    throw new UnsupportedOperationException();
  }

  @Override
  public void toString(Map<String, Object> params, StringBuilder builder) {
    builder.append("DROP BUCKET ");
    if (name != null) {
      name.toString(params, builder);
    } else {
      id.toString(params, builder);
    }
    if (ifExists) {
      builder.append(" IF EXISTS");
    }
  }

  @Override
  public DropBucketStatement copy() {
    DropBucketStatement result = new DropBucketStatement(-1);
    result.name = name == null ? null : name.copy();
    result.id = id == null ? null : id.copy();
    result.ifExists = this.ifExists;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DropBucketStatement that = (DropBucketStatement) o;

    if (ifExists != that.ifExists)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    return id != null ? id.equals(that.id) : that.id == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (ifExists ? 1 : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=239ffe92e79e1d5c82976ed9814583ec (do not edit this line) */
