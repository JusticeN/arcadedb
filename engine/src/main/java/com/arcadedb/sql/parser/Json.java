/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OJson.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.ModifiableDocument;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.*;
import java.util.stream.Collectors;

public class Json extends SimpleNode {

  protected List<JsonItem> items = new ArrayList<JsonItem>();

  public Json(int id) {
    super(id);
  }

  public Json(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor. *
   */
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("{");
    boolean first = true;
    for (JsonItem item : items) {
      if (!first) {
        builder.append(", ");
      }
      item.toString(params, builder);

      first = false;
    }
    builder.append("}");
  }

  public Document toDocument(final Identifiable source, final CommandContext ctx) {
    final String className = getClassNameForDocument(ctx, source);
    final ModifiableDocument doc;
    if (className != null) {
      doc = ctx.getDatabase().newDocument(className);
    } else {
      doc = ctx.getDatabase().newDocument(null);
    }
    for (JsonItem item : items) {
      String name = item.getLeftValue();
      if (name == null) {
        continue;
      }
      Object value;
      if (item.right.value instanceof Json) {
        value = ((Json) item.right.value).toDocument(source, ctx);
      } else {
        value = item.right.execute(source, ctx);
      }
      doc.set(name, value);
    }

    return doc;
  }

  public Map<String, Object> toMap(final Identifiable source, final CommandContext ctx) {
    final Map<String, Object> doc = new HashMap<String, Object>();
    for (JsonItem item : items) {
      final String name = item.getLeftValue();
      if (name == null) {
        continue;
      }
      final Object value = item.right.execute(source, ctx);
      doc.put(name, value);
    }

    return doc;
  }

  public Map<String, Object> toMap(final Result source, final CommandContext ctx) {
    final Map<String, Object> doc = new HashMap<String, Object>();
    for (JsonItem item : items) {
      final String name = item.getLeftValue();
      if (name == null) {
        continue;
      }
      final Object value = item.right.execute(source, ctx);
      doc.put(name, value);
    }

    return doc;
  }

  private String getClassNameForDocument(final CommandContext ctx, final Identifiable record) {
    if (record != null) {
      final Document doc = (Document) record.getRecord();
      if (doc != null)
        return doc.getType();
    }

    for (JsonItem item : items) {
      final String left = item.getLeftValue();
      if (left != null && left.toLowerCase(Locale.ENGLISH).equals("@type")) {
        return "" + item.right.execute((Result) null, ctx);
      }
    }

    return null;
  }

  public boolean needsAliases(final Set<String> aliases) {
    for (JsonItem item : items) {
      if (item.needsAliases(aliases)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAggregate() {
    for (JsonItem item : items) {
      if (item.isAggregate()) {
        return true;
      }
    }
    return false;
  }

  public Json splitForAggregation(AggregateProjectionSplit aggregateSplit) {
    if (isAggregate()) {
      Json result = new Json(-1);
      for (JsonItem item : items) {
        result.items.add(item.splitForAggregation(aggregateSplit));
      }
      return result;
    } else {
      return this;
    }
  }

  public Json copy() {
    Json result = new Json(-1);
    result.items = items.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Json oJson = (Json) o;

    return items != null ? items.equals(oJson.items) : oJson.items == null;
  }

  @Override
  public int hashCode() {
    return items != null ? items.hashCode() : 0;
  }

  public void extractSubQueries(SubQueryCollector collector) {
    for (JsonItem item : items) {
      item.extractSubQueries(collector);
    }
  }

  public boolean refersToParent() {
    for (JsonItem item : items) {
      if (item.refersToParent()) {
        return true;
      }
    }
    return false;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if (items != null) {
      result.setProperty("items", items.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    return result;
  }

  public void deserialize(Result fromResult) {

    if (fromResult.getProperty("items") != null) {
      List<Result> ser = fromResult.getProperty("items");
      items = new ArrayList<>();
      for (Result r : ser) {
        JsonItem exp = new JsonItem();
        exp.deserialize(r);
        items.add(exp);
      }
    }
  }

  public boolean isCacheable() {
    return false;//TODO optimize
  }
}
/* JavaCC - OriginalChecksum=3beec9f6db486de944498588b51a505d (do not edit this line) */
