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
/* Generated By:JJTree: Do not edit this line. OParenthesisBlock.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;

import java.util.*;

public class ParenthesisBlock extends BooleanExpression {
  BooleanExpression subElement;

  public ParenthesisBlock(final int id) {
    super(id);
  }

  @Override
  public Boolean evaluate(final Identifiable currentRecord, final CommandContext context) {
    return subElement.evaluate(currentRecord, context);
  }

  @Override
  public Boolean evaluate(final Result currentRecord, final CommandContext context) {
    return subElement.evaluate(currentRecord, context);
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("(");
    subElement.toString(params, builder);
    builder.append(" )");
  }

  @Override
  public List<AndBlock> flatten() {
    return subElement.flatten();
  }

  @Override
  public ParenthesisBlock copy() {
    final ParenthesisBlock result = new ParenthesisBlock(-1);
    result.subElement = subElement.copy();
    return result;
  }

  @Override
  public void extractSubQueries(final SubQueryCollector collector) {
    this.subElement.extractSubQueries(collector);
  }

  @Override
  protected Object[] getIdentityElements() {
    return new Object[] { subElement };
  }

  @Override
  public List<String> getMatchPatternInvolvedAliases() {
    return subElement.getMatchPatternInvolvedAliases();
  }

  @Override
  protected SimpleNode[] getCacheableElements() {
    return new SimpleNode[] { subElement };
  }

  @Override
  public boolean isAlwaysTrue() {
    return subElement.isAlwaysTrue();
  }
}
/* JavaCC - OriginalChecksum=9a16b6cf7d051382acb94c45067631a9 (do not edit this line) */
