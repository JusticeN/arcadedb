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
/* Generated by: JJTree: Do not edit this line. Argument.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

public class Argument extends SimpleNode {
  protected Name              name;
  protected ValueWithVariable valueWithVariable;

  public Argument(final int id) {
    super(id);
  }


  public String getName() {
    return name != null ? name.value : null;
  }

  public ValueWithVariable getValueWithVariable() {
    return valueWithVariable;
  }

  @Override
  public String toString() {
    return "Argument{" + name + " = " + valueWithVariable + '}';
  }
}
/* ParserGeneratorCC - OriginalChecksum=5812dd8322af831327adf8100aeb4693 (do not edit this line) */
