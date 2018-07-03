/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OUpdateIncrementItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import java.util.Map;

public class UpdateIncrementItem extends SimpleNode {
  protected Identifier left;
  protected Modifier   leftModifier;
  protected Expression right;

  public UpdateIncrementItem(int id) {
    super(id);
  }

  public UpdateIncrementItem(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    left.toString(params, builder);
    if (leftModifier != null) {
      leftModifier.toString(params, builder);
    }
    builder.append(" = ");
    right.toString(params, builder);
  }

  public UpdateIncrementItem copy() {
    UpdateIncrementItem result = new UpdateIncrementItem(-1);
    result.left = left == null ? null : left.copy();
    result.leftModifier = leftModifier == null ? null : leftModifier.copy();
    result.right = right == null ? null : right.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    UpdateIncrementItem that = (UpdateIncrementItem) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    if (leftModifier != null ? !leftModifier.equals(that.leftModifier) : that.leftModifier != null)
      return false;
    return right != null ? right.equals(that.right) : that.right == null;
  }

  @Override public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (leftModifier != null ? leftModifier.hashCode() : 0);
    result = 31 * result + (right != null ? right.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=94dd82febb904e4e31130bdcbbb48fe3 (do not edit this line) */
