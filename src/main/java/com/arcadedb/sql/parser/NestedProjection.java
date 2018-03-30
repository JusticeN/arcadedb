/* Generated By:JJTree: Do not edit this line. OExpansion.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.PDocument;
import com.arcadedb.sql.executor.OCommandContext;
import com.arcadedb.sql.executor.OResult;
import com.arcadedb.sql.executor.OResultInternal;

import java.util.*;
import java.util.stream.Collectors;

public class NestedProjection extends SimpleNode {
  protected List<NestedProjectionItem> includeItems = new ArrayList<>();
  protected List<NestedProjectionItem> excludeItems = new ArrayList<>();
  protected NestedProjectionItem starItem;
  private   PInteger              recursion; //not used for now

  public NestedProjection(int id) {
    super(id);
  }

  public NestedProjection(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * @param expression
   * @param input
   * @param ctx
   */
  public Object apply(Expression expression, Object input, OCommandContext ctx) {
    if (input instanceof OResult) {
      return apply(expression, (OResult) input, ctx, recursion == null ? 0 : recursion.getValue().intValue());
    }
    if (input instanceof PDocument) {
      return apply(expression, (PDocument) input, ctx, recursion == null ? 0 : recursion.getValue().intValue());
    }
    if (input instanceof Map) {
      return apply(expression, (Map) input, ctx, recursion == null ? 0 : recursion.getValue().intValue());
    }
    if (input instanceof Collection) {
      return ((Collection) input).stream().map(x -> apply(expression, x, ctx)).collect(Collectors.toList());
    }
    Iterator iter = null;
    if (input instanceof Iterable) {
      iter = ((Iterable) input).iterator();
    }
    if (input instanceof Iterator) {
      iter = (Iterator) input;
    }
    if (iter != null) {
      List result = new ArrayList();
      while (iter.hasNext()) {
        result.add(apply(expression, iter.next(), ctx));
      }
      return result;
    }
    return input;
  }

  private Object apply(Expression expression, OResult elem, OCommandContext ctx, int recursion) {
    OResultInternal result = new OResultInternal();
    if (starItem != null || includeItems.size() == 0) {
      for (String property : elem.getPropertyNames()) {
        if (isExclude(property)) {
          continue;
        }
        result.setProperty(property, convert(tryExpand(expression, property, elem.getProperty(property), ctx, recursion)));
      }
    }
    if (includeItems.size() > 0) {
      //TODO manage wildcards!
      for (NestedProjectionItem item : includeItems) {
        String alias = item.alias != null ? item.alias.getStringValue() : item.expression.getDefaultAlias().getStringValue();
        Object value = item.expression.execute(elem, ctx);
        if (item.expansion != null) {
          value = item.expand(expression, alias, value, ctx, recursion - 1);
        }
        result.setProperty(alias, convert(value));
      }
    }
    return result;
  }

  private boolean isExclude(String propertyName) {
    for (NestedProjectionItem item : excludeItems) {
      if (item.matches(propertyName)) {
        return true;
      }
    }
    return false;
  }

  private Object tryExpand(Expression rootExpr, String propName, Object propValue, OCommandContext ctx, int recursion) {
    for (NestedProjectionItem item : includeItems) {
      if (item.matches(propName) && item.expansion != null) {
        return item.expand(rootExpr, propName, propValue, ctx, recursion);
      }
    }
    return propValue;
  }

  private Object apply(Expression expression, PDocument input, OCommandContext ctx, int recursion) {
    PDocument elem = input;
    OResultInternal result = new OResultInternal();
    if (starItem != null || includeItems.size() == 0) {
      for (String property : elem.getPropertyNames()) {
        if (isExclude(property)) {
          continue;
        }
        result.setProperty(property, convert(tryExpand(expression, property, elem.get(property), ctx, recursion)));
      }
    }
    if (includeItems.size() > 0) {
      //TODO manage wildcards!
      for (NestedProjectionItem item : includeItems) {
        String alias = item.alias != null ? item.alias.getStringValue() : item.expression.getDefaultAlias().getStringValue();
        Object value = item.expression.execute(elem, ctx);
        if (item.expansion != null) {
          value = item.expand(expression, alias, value, ctx, recursion - 1);
        }
        result.setProperty(alias, convert(value));
      }
    }
    return result;
  }

  private Object apply(Expression expression, Map<String, Object> input, OCommandContext ctx, int recursion) {
    OResultInternal result = new OResultInternal();

    if (starItem != null || includeItems.size() == 0) {
      for (String property : input.keySet()) {
        if (isExclude(property)) {
          continue;
        }
        result.setProperty(property, convert(tryExpand(expression, property, input.get(property), ctx, recursion)));
      }
    }
    if (includeItems.size() > 0) {
      //TODO manage wildcards!
      for (NestedProjectionItem item : includeItems) {
        String alias = item.alias != null ? item.alias.getStringValue() : item.expression.getDefaultAlias().getStringValue();
        OResultInternal elem = new OResultInternal();
        input.entrySet().forEach(x -> elem.setProperty(x.getKey(), x.getValue()));
        Object value = item.expression.execute(elem, ctx);
        if (item.expansion != null) {
          value = item.expand(expression, alias, value, ctx, recursion - 1);
        }
        result.setProperty(alias, convert(value));
      }
    }
    return result;
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append(":{");
    boolean first = true;
    if (starItem != null) {
      starItem.toString(params, builder);
      first = false;
    }
    for (NestedProjectionItem item : includeItems) {
      if (!first) {
        builder.append(", ");
      }
      item.toString(params, builder);
      first = false;
    }
    for (NestedProjectionItem item : excludeItems) {
      if (!first) {
        builder.append(", ");
      }
      item.toString(params, builder);
      first = false;
    }

    builder.append("}");
    if (recursion != null) {
      builder.append("[");
      recursion.toString(params, builder);
      builder.append("]");
    }
  }

  public NestedProjection copy() {
    NestedProjection result = new NestedProjection(-1);
    result.includeItems = includeItems.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.excludeItems = excludeItems.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.starItem = starItem == null ? null : starItem.copy();
    result.recursion = recursion == null ? null : recursion.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    NestedProjection that = (NestedProjection) o;

    if (includeItems != null ? !includeItems.equals(that.includeItems) : that.includeItems != null)
      return false;
    if (excludeItems != null ? !excludeItems.equals(that.excludeItems) : that.excludeItems != null)
      return false;
    if (starItem != null ? !starItem.equals(that.starItem) : that.starItem != null)
      return false;
    return recursion != null ? recursion.equals(that.recursion) : that.recursion == null;
  }

  @Override
  public int hashCode() {
    int result = includeItems != null ? includeItems.hashCode() : 0;
    result = 31 * result + (excludeItems != null ? excludeItems.hashCode() : 0);
    result = 31 * result + (starItem != null ? starItem.hashCode() : 0);
    result = 31 * result + (recursion != null ? recursion.hashCode() : 0);
    return result;
  }

  private Object convert(Object value) {
//    if (value instanceof ORidBag) {
//      List result = new ArrayList();
//      ((ORidBag) value).forEach(x -> result.add(x));
//      return result;
//    }
    return value;
  }

  public OResult serialize() {
    OResultInternal result = new OResultInternal();
    if (includeItems != null) {
      result.setProperty("includeItems", includeItems.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    if (excludeItems != null) {
      result.setProperty("excludeItems", excludeItems.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    if (starItem != null) {
      result.setProperty("starItem", starItem.serialize());
    }
    result.setProperty("recursion", recursion);
    return result;
  }

  public void deserialize(OResult fromResult) {
    if (fromResult.getProperty("includeItems") != null) {
      includeItems = new ArrayList<>();
      List<OResult> ser = fromResult.getProperty("includeItems");
      for (OResult x : ser) {
        NestedProjectionItem item = new NestedProjectionItem(-1);
        item.deserialize(x);
        includeItems.add(item);
      }
    }
    if (fromResult.getProperty("excludeItems") != null) {
      excludeItems = new ArrayList<>();
      List<OResult> ser = fromResult.getProperty("excludeItems");
      for (OResult x : ser) {
        NestedProjectionItem item = new NestedProjectionItem(-1);
        item.deserialize(x);
        excludeItems.add(item);
      }
    }
    if (fromResult.getProperty("starItem") != null) {
      starItem = new NestedProjectionItem(-1);
      starItem.deserialize(fromResult.getProperty("starItem"));
    }
    recursion = fromResult.getProperty("recursion");

  }
}
/* JavaCC - OriginalChecksum=a7faf9beb3c058e28999b17cb43b26f6 (do not edit this line) */
