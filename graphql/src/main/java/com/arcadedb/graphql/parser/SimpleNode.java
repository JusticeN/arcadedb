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
/* Generated by: JJTree: Do not edit this line. SimpleNode.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=false,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

import java.util.*;
import java.util.stream.*;

public class SimpleNode implements Node {
  protected Node   parent;
  protected Node[] children;
  protected int    id;
  protected Object value;
  protected Token  firstToken;
  protected Token  lastToken;

  public SimpleNode(final int i) {
    id = i;
  }

  public void jjtOpen() {
    // NO ACTIONS
  }

  public void jjtClose() {
    // NO ACTIONS
  }

  public void jjtSetParent(final Node n) {
    parent = n;
  }

  public Node jjtGetParent() {
    return parent;
  }

  public void jjtAddChild(final Node n, final int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      final Node[] c = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(final int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return children == null ? 0 : children.length;
  }

  public void jjtSetValue(final Object aValue) {
    value = aValue;
  }

  public Object jjtGetValue() {
    return value;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */
  @Override
  public String toString() {
    return GraphQLParserTreeConstants.jjtNodeName[id];
  }

  public String treeToString(final String prefix, final Class... excludes) {
    StringBuilder buffer = new StringBuilder(prefix + this);
    if (children != null) {
      final Set<Class> set = Arrays.stream(excludes).collect(Collectors.toSet());

      for (int i = 0; i < children.length; ++i) {
        final SimpleNode n = (SimpleNode) children[i];
        if (n != null) {
          if (set.contains(n.getClass()))
            continue;
          buffer.append("\n").append(n.treeToString(prefix + " "));
        }
      }
    }
    return buffer.toString();
  }

  public int getId() {
    return id;
  }

  public void jjtSetFirstToken(final Token token) {
    this.firstToken = token;
  }

  public void jjtSetLastToken(final Token token) {
    this.lastToken = token;
  }

  public Node[] getChildren() {
    return children;
  }
}

/* ParserGeneratorCC - OriginalChecksum=a932dd6e6d33187f6cbda5ebe13d5edb (do not edit this line) */
