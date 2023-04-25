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
/* Generated By:JJTree: Do not edit this line. OBetweenCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.schema.Type;

import java.util.*;

public class BetweenCondition extends BooleanExpression {

  protected Expression first;
  protected Expression second;
  protected Expression third;

  public BetweenCondition(final int id) {
    super(id);
  }

  @Override
  public Boolean evaluate(final Identifiable currentRecord, final CommandContext context) {
    final Object firstValue = first.execute(currentRecord, context);
    if (firstValue == null) {
      return false;
    }

    Object secondValue = second.execute(currentRecord, context);
    if (secondValue == null) {
      return false;
    }

    secondValue = Type.convert(context.getDatabase(), secondValue, firstValue.getClass());

    Object thirdValue = third.execute(currentRecord, context);
    if (thirdValue == null) {
      return false;
    }

    thirdValue = Type.convert(context.getDatabase(), thirdValue, firstValue.getClass());

    final int leftResult = ((Comparable<Object>) firstValue).compareTo(secondValue);
    final int rightResult = ((Comparable<Object>) firstValue).compareTo(thirdValue);

    return leftResult >= 0 && rightResult <= 0;
  }

  @Override
  public Boolean evaluate(final Result currentRecord, final CommandContext context) {
    final Object firstValue = first.execute(currentRecord, context);
    if (firstValue == null) {
      return false;
    }

    Object secondValue = second.execute(currentRecord, context);
    if (secondValue == null) {
      return false;
    }

    secondValue = Type.convert(context.getDatabase(), secondValue, firstValue.getClass());

    Object thirdValue = third.execute(currentRecord, context);
    if (thirdValue == null) {
      return false;
    }
    thirdValue = Type.convert(context.getDatabase(), thirdValue, firstValue.getClass());

    final int leftResult = ((Comparable<Object>) firstValue).compareTo(secondValue);
    final int rightResult = ((Comparable<Object>) firstValue).compareTo(thirdValue);

    return leftResult >= 0 && rightResult <= 0;
  }

  public Expression getFirst() {
    return first;
  }

  public Expression getSecond() {
    return second;
  }

  public Expression getThird() {
    return third;
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    first.toString(params, builder);
    builder.append(" BETWEEN ");
    second.toString(params, builder);
    builder.append(" AND ");
    third.toString(params, builder);
  }

  @Override
  public BooleanExpression copy() {
    final BetweenCondition result = new BetweenCondition(-1);
    result.first = first.copy();
    result.second = second.copy();
    result.third = third.copy();
    return result;
  }

  @Override
  public void extractSubQueries(final SubQueryCollector collector) {
    first.extractSubQueries(collector);
    second.extractSubQueries(collector);
    third.extractSubQueries(collector);
  }

  @Override
  protected Object[] getIdentityElements() {
    return getCacheableElements();
  }

  @Override
  public List<String> getMatchPatternInvolvedAliases() {
    final List<String> result = new ArrayList<>();
    List<String> x = first.getMatchPatternInvolvedAliases();
    if (x != null) {
      result.addAll(x);
    }
    x = second.getMatchPatternInvolvedAliases();
    if (x != null) {
      result.addAll(x);
    }
    x = third.getMatchPatternInvolvedAliases();
    if (x != null) {
      result.addAll(x);
    }

    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  @Override
  protected Expression[] getCacheableElements() {
    return new Expression[] { first, second, third };
  }
}
/* JavaCC - OriginalChecksum=f94f4779c4a6c6d09539446045ceca89 (do not edit this line) */
