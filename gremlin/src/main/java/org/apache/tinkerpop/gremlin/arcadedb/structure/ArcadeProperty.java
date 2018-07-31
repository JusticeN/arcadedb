package org.apache.tinkerpop.gremlin.arcadedb.structure;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.util.NoSuchElementException;

/**
 * Created by Enrico Risa on 30/07/2018.
 */
public class ArcadeProperty<T> implements Property<T> {

  protected final ArcadeElement element;
  protected final String        key;
  protected final ArcadeGraph   graph;
  protected       T             value;
  protected       boolean       removed = false;

  protected ArcadeProperty(final ArcadeElement element, final String key, final T value) {
    this.element = element;
    this.key = key;
    this.value = value;
    this.graph = (ArcadeGraph) element.graph();
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public T value() throws NoSuchElementException {
    return value;
  }

  @Override
  public boolean isPresent() {
    return value != null;
  }

  @Override
  public Element element() {
    return element;
  }

  @Override
  public void remove() {
    if (this.removed)
      return;
    this.graph.tx().readWrite();
    element.baseElement.remove(key);
    element.baseElement.save();
    this.removed = true;
  }
}
