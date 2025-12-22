package com.ergotech.grapheditor.model.command;

import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.function.BiFunction;

import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;

public class SetPropertyCommand<V> extends AbstractCommand {
  private final Property<V> property;
  private final V newValue;
  private V oldValue;

  // Optional, for temporary binding during execute/undo
  private final Object listenerTarget;
  private final PropertyChangeListener listener;
  private final BiFunction<Object, PropertyChangeListener, Runnable> listenerBinder;

  /** Convenience method to create the set property command. */
  public static <S> SetPropertyCommand<S> create(Property<S> property, S newValue) {
    return new SetPropertyCommand<>(property, newValue);
  }

  /**
   * Convenience method to create the command with a temporary add/remove of a listener
   * during execute() and undo().
   *
   * binder must: add the listener and return an unbind Runnable that removes it.
   */
  public static <S, T> SetPropertyCommand<S> create(
      Property<S> property,
      S newValue,
      T listenerTarget,
      PropertyChangeListener listener,
      BiFunction<? super T, ? super PropertyChangeListener, Runnable> binder) {

    @SuppressWarnings("unchecked")
    BiFunction<Object, PropertyChangeListener, Runnable> erased =
        (BiFunction<Object, PropertyChangeListener, Runnable>) (BiFunction<?, ?, ?>) binder;

    return new SetPropertyCommand<>(property, newValue, listenerTarget, listener, erased);
  }

  /**
   * Creates a {@link SetPropertyCommand} for a specified object and attribute.
   */
  public static <T, V> SetPropertyCommand<V> create(T object, String attributeName, V newValue) {
    try {
      @SuppressWarnings("unchecked")
      JavaBeanObjectProperty<V> property = JavaBeanObjectPropertyBuilder
          .create()
          .bean(object)
          .name(attributeName)
          .build();

      return new SetPropertyCommand<>(property, newValue);

    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to create SetPropertyCommand: unable to access JavaBean property", e);
    }
  }

  /**
   * Creates a {@link SetPropertyCommand} for a specified object and attribute, with temporary
   * listener add/remove during execute() and undo().
   */
  public static <T, V> SetPropertyCommand<V> create(
      T object,
      String attributeName,
      V newValue,
      PropertyChangeListener listener,
      BiFunction<? super T, ? super PropertyChangeListener, Runnable> binder) {

    try {
      @SuppressWarnings("unchecked")
      JavaBeanObjectProperty<V> property = JavaBeanObjectPropertyBuilder
          .create()
          .bean(object)
          .name(attributeName)
          .build();

      @SuppressWarnings("unchecked")
      BiFunction<Object, PropertyChangeListener, Runnable> erased =
          (BiFunction<Object, PropertyChangeListener, Runnable>) (BiFunction<?, ?, ?>) binder;

      return new SetPropertyCommand<>(property, newValue, object, listener, erased);

    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to create SetPropertyCommand: unable to access JavaBean property", e);
    }
  }

  public SetPropertyCommand(Property<V> property, V newValue) {
    this(property, newValue, null, null, null);
  }

  private SetPropertyCommand(
      Property<V> property,
      V newValue,
      Object listenerTarget,
      PropertyChangeListener listener,
      BiFunction<Object, PropertyChangeListener, Runnable> listenerBinder) {

    this.property = Objects.requireNonNull(property, "property");
    this.newValue = newValue;
    this.listenerTarget = listenerTarget;
    this.listener = listener;
    this.listenerBinder = listenerBinder;
  }

  @Override
  public void execute() throws Exception {
    if (!canExecute()) {
      return;
    }

    oldValue = property.getValue();

    // Add listener -> mutate -> remove listener
    Runnable unbind = bindListenerIfConfigured();
    try {
      property.setValue(newValue);
      setExecuted(true);
    } finally {
      if (unbind != null) {
        unbind.run();
      }
    }
  }

  @Override
  public void undo() throws Exception {
    if (!canUndo()) {
      return;
    }

    // Add listener -> mutate -> remove listener
    Runnable unbind = bindListenerIfConfigured();
    try {
      property.setValue(oldValue);
      setExecuted(false);
    } finally {
      if (unbind != null) {
        unbind.run();
      }
    }
  }

  private Runnable bindListenerIfConfigured() {
    if (listenerBinder == null || listenerTarget == null || listener == null) {
      return null;
    }
    Runnable unbind = listenerBinder.apply(listenerTarget, listener);
    return (unbind != null) ? unbind : () -> {};
  }

  @Override
  public boolean canExecute() {
    return !isExecuted();
  }

  @Override
  public boolean canUndo() {
    return isExecuted();
  }

  @Override
  public String toString() {
    return "SetPropertyCommand [property=" + property + ", newValue=" + newValue + ", oldValue=" + oldValue
        + ", executed=" + isExecuted() + "]";
  }
}
