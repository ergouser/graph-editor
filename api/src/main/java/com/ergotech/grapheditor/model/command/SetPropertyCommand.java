package com.ergotech.grapheditor.model.command;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Objects;
import java.util.function.BiFunction;

import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;

public class SetPropertyCommand<V> extends AbstractCommand {
  private final Property<V> property;
  private V newValue; // can be updated during undo() to support redo() after undo()
  private V oldValue;

  // Optional, for temporary binding during execute/undo
  private final Object listenerTarget;
  private final VetoableChangeListener listener;
  private final BiFunction<Object, VetoableChangeListener, Runnable> listenerBinder;

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
      VetoableChangeListener listener,
      BiFunction<? super T, ? super VetoableChangeListener, Runnable> binder) {

    @SuppressWarnings("unchecked")
    BiFunction<Object, VetoableChangeListener, Runnable> erased =
        (BiFunction<Object, VetoableChangeListener, Runnable>) (BiFunction<?, ?, ?>) binder;

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
      VetoableChangeListener listener,
      BiFunction<? super T, ? super VetoableChangeListener, Runnable> binder) {

    try {
      @SuppressWarnings("unchecked")
      JavaBeanObjectProperty<V> property = JavaBeanObjectPropertyBuilder
          .create()
          .bean(object)
          .name(attributeName)
          .build();

      @SuppressWarnings("unchecked")
      BiFunction<Object, VetoableChangeListener, Runnable> erased =
          (BiFunction<Object, VetoableChangeListener, Runnable>) (BiFunction<?, ?, ?>) binder;

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
      VetoableChangeListener listener,
      BiFunction<Object, VetoableChangeListener, Runnable> listenerBinder) {

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
    } catch (RuntimeException e) { // probably a PropertyVetoException wrapped in a RuntimeException, but we want to be safe about catching only veto-related exceptions
      if ( e.getCause() instanceof PropertyVetoException) {
        // Log the veto exception for debugging purposes
        //System.err.println("Property change was vetoed: " + e.getCause().getMessage());
      } else {
        // If it's not a veto exception, rethrow it as it's unexpected
        throw e;
      }
      // If the change was vetoed, we should not mark the command as executed and should not update oldValue.
      // The command can be retried with a different newValue.
      setExecuted(false);
      throw e; // rethrow to allow handling by caller to prevent the command from being added to the stack
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

    // Save the current value in case of redo, but restore the old value
    newValue = property.getValue();

    // Add listener -> mutate -> remove listener
    Runnable unbind = bindListenerIfConfigured();
    try {
      property.setValue(oldValue);
      setExecuted(false);
    } catch (RuntimeException e) { // probably a PropertyVetoException wrapped in a RuntimeException, but we want to be safe about catching only veto-related exceptions
      if ( e.getCause() instanceof PropertyVetoException) {
        // Log the veto exception for debugging purposes
        //System.err.println("Property change was vetoed: " + e.getCause().getMessage());
        // this is likely to be "name" so we could mutate the command with a new name and retry, 
        // but for now we will just leave it to the caller to handle the exception and decide what to do 
        // (e.g. show an error message and let the user retry with a different value - althouth it's "undo" so the user 
        // might not bea able to provide a new value, but this is a consequence of the veto and the fact that we want to support redo after undo)
      } else {
        // If it's not a veto exception, rethrow it as it's unexpected
        throw e;
      }
      // If the change was vetoed, we should not mark the command as executed and should not update oldValue.
      // The command can be retried with a different newValue.
      setExecuted(true); // undo did not succeed, so we are still in the executed state
      throw e; // rethrow to allow handling by caller to prevent the command from being added to the stack
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
