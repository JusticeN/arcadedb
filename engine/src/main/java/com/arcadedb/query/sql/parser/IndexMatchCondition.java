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
/* Generated By:JJTree: Do not edit this line. OIndexMatchCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;

import java.util.*;
import java.util.stream.*;

public class IndexMatchCondition extends BooleanExpression {

  protected BinaryCompareOperator operator;
  protected Boolean               between;

  protected List<Expression> leftExpressions;
  protected List<Expression> rightExpressions;

  public IndexMatchCondition(final int id) {
    super(id);
  }

  @Override
  public Boolean evaluate(final Identifiable currentRecord, final CommandContext context) {
    throw new UnsupportedOperationException("TODO Implement IndexMatch!!!");//TODO
  }

  @Override
  public Boolean evaluate(final Result currentRecord, final CommandContext context) {
    throw new UnsupportedOperationException("TODO Implement IndexMatch!!!");//TODO
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("KEY ");
    if (operator != null) {
      builder.append(operator);
      builder.append(" [");
      boolean first = true;
      for (final Expression x : leftExpressions) {
        if (!first) {
          builder.append(", ");
        }
        x.toString(params, builder);
        first = false;
      }
      builder.append("]");
    } else if (Boolean.TRUE.equals(between)) {
      builder.append(" BETWEEN [");
      boolean first = true;
      for (final Expression x : leftExpressions) {
        if (!first) {
          builder.append(", ");
        }
        x.toString(params, builder);
        first = false;
      }
      builder.append("] AND [");
      first = true;
      for (final Expression x : rightExpressions) {
        if (!first) {
          builder.append(", ");
        }
        x.toString(params, builder);
        first = false;
      }
      builder.append("]");
    }
  }

  @Override
  public IndexMatchCondition copy() {
    final IndexMatchCondition result = new IndexMatchCondition(-1);
    result.operator = operator == null ? null : operator.copy();
    result.between = between;
    result.leftExpressions = leftExpressions == null ? null : leftExpressions.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.rightExpressions = rightExpressions == null ? null : rightExpressions.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public void extractSubQueries(final SubQueryCollector collector) {
    if (leftExpressions != null) {
      for (final Expression exp : leftExpressions) {
        exp.extractSubQueries(collector);
      }
    }
    if (rightExpressions != null) {
      for (final Expression exp : rightExpressions) {
        exp.extractSubQueries(collector);
      }
    }
  }

  @Override
  public List<String> getMatchPatternInvolvedAliases() {
    return null;
  }

  @Override
  protected Object[] getIdentityElements() {
    return new Object[] { operator, between, leftExpressions, rightExpressions };
  }

  @Override
  protected SimpleNode[] getCacheableElements() {
    return Stream.concat(Arrays.stream(leftExpressions.toArray()), Arrays.stream(rightExpressions.toArray())).toArray(SimpleNode[]::new);
  }
}
/* JavaCC - OriginalChecksum=702e9ab959e87b043b519844a7d31224 (do not edit this line) */
