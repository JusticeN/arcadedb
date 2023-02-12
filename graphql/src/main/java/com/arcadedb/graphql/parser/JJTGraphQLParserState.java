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
/* Generated by: ParserGeneratorCC: Do not edit this line. JJTGraphQLParserState.java Version 1.1.3 */
package com.arcadedb.graphql.parser;

import java.io.*;
import java.util.*;

public class JJTGraphQLParserState implements Serializable {
  private final List<Node>    nodes;
  private final List<Integer> marks;
  /* number of nodes on stack */
  private       int           sp;
  /* current mark */
  private       int           mk;

  public JJTGraphQLParserState() {
    nodes = new ArrayList<>();
    marks = new ArrayList<>();
    sp = 0;
    mk = 0;
  }

  /* Pushes a node on to the stack. */
  public void pushNode(final Node n) {
    nodes.add(n);
    ++sp;
  }

  /* Returns the node on the top of the stack, and remove it from the
     stack.  */
  public Node popNode() {
    --sp;
    if (sp < mk) {
      mk = marks.remove(marks.size() - 1);
    }
    return nodes.remove(nodes.size() - 1);
  }

  /* Returns the number of children on the stack in the current node
     scope. */
  public int nodeArity() {
    return sp - mk;
  }

  /* Parameter is currently unused. */
  public void clearNodeScope(@SuppressWarnings("unused") final Node n) {
    while (sp > mk) {
      popNode();
    }
    mk = marks.remove(marks.size() - 1);
  }

  public void openNodeScope(final Node n) {
    marks.add(mk);
    mk = sp;
    n.jjtOpen();
  }

  /* A conditional node is constructed if its condition is true.  All
     the nodes that have been pushed since the node was opened are
     made children of the conditional node, which is then pushed
     on to the stack.  If the condition is false the node is not
     constructed and they are left on the stack. */
  public void closeNodeScope(final Node n, final boolean condition) {
    if (condition) {
      int a = nodeArity();
      mk = marks.remove(marks.size() - 1);
      while (a-- > 0) {
        final Node c = popNode();
        c.jjtSetParent(n);
        n.jjtAddChild(c, a);
      }
      n.jjtClose();
      pushNode(n);
    } else {
      mk = marks.remove(marks.size() - 1);
    }
  }
}
/* ParserGeneratorCC - OriginalChecksum=8f16c6ca694424614c1ce21fa9f6818f (do not edit this line) */
