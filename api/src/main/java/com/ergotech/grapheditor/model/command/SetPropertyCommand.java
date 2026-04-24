package com.ergotech.grapheditor.model.command;

import java.beans.PropertyChangeListener;
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
  private final PropertyChangeListener propertyChangeListener;
  private final BiFunction<Object, PropertyChangeListener, Runnable> propertyListenerBinder;
  private final VetoableChangeListener vetoableChangeListener;
  private final BiFunction<Object, VetoableChangeListener, Runnable> vetoableListenerBinder;

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
      PropertyChangeListener propertyChangeListener,
      BiFunction<? super T, ? super PropertyChangeListener, Runnable> propertyBinder,
      VetoableChangeListener vetoableChangeListener,
      BiFunction<? super T, ? super VetoableChangeListener, Runnable> vetoablePropertyBinder) {

    @SuppressWarnings("unchecked")
    BiFunction<Object, PropertyChangeListener, Runnable> pBinder =
        (BiFunction<Object, PropertyChangeListener, Runnable>) (BiFunction<?, ?, ?>) propertyBinder;
    @SuppressWarnings("unchecked")
    BiFunction<Object, VetoableChangeListener, Runnable> vBinder =
        (BiFunction<Object, VetoableChangeListener, Runnable>) (BiFunction<?, ?, ?>) vetoablePropertyBinder;

    return new SetPropertyCommand<>(property, newValue, listenerTarget, propertyChangeListener, pBinder, vetoableChangeListener, vBinder);
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
      PropertyChangeListener propertyChangeListener,
      BiFunction<? super T, ? super PropertyChangeListener, Runnable> propertyBinder,
      VetoableChangeListener vetoableChangeListener,
      BiFunction<? super T, ? super VetoableChangeListener, Runnable> vetoablePropertyBinder) {

    try {
      @SuppressWarnings("unchecked")
      JavaBeanObjectProperty<V> property = JavaBeanObjectPropertyBuilder
          .create()
          .bean(object)
          .name(attributeName)
          .build();

      @SuppressWarnings("unchecked")
      BiFunction<Object, PropertyChangeListener, Runnable> pBinder =
          (BiFunction<Object, PropertyChangeListener, Runnable>) (BiFunction<?, ?, ?>) propertyBinder;
      @SuppressWarnings("unchecked")
      BiFunction<Object, VetoableChangeListener, Runnable> vBinder =
          (BiFunction<Object, VetoableChangeListener, Runnable>) (BiFunction<?, ?, ?>) vetoablePropertyBinder;

      return new SetPropertyCommand<>(property, newValue, object, propertyChangeListener, pBinder, vetoableChangeListener, vBinder);

    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to create SetPropertyCommand: unable to access JavaBean property", e);
    }
  }

  public SetPropertyCommand(Property<V> property, V newValue) {
    this(property, newValue, null, null, null, null, null);
  }

  private SetPropertyCommand(
      Property<V> property,
      V newValue,
      Object listenerTarget,
      PropertyChangeListener propertyChangeListener,
      BiFunction<Object, PropertyChangeListener, Runnable> propertyListenerBinder,
      VetoableChangeListener vetoableChangeListener,
      BiFunction<Object, VetoableChangeListener, Runnable> vetoableListenerBinder) {

    this.property = Objects.requireNonNull(property, "property");
    this.newValue = newValue;
    this.listenerTarget = listenerTarget;
    this.propertyChangeListener = propertyChangeListener;
    this.propertyListenerBinder = propertyListenerBinder;
    this.vetoableChangeListener = vetoableChangeListener;
    this.vetoableListenerBinder = vetoableListenerBinder;
  }

  @Override
  public void execute() throws Exception {
    if (!canExecute()) {
      return;
    }

    oldValue = property.getValue();

    // Add listener -> mutate -> remove listener
    // Add listener -> mutate -> remove listener
    Runnable unbindProperty = bindPropertyListenerIfConfigured();
    Runnable unbindVeto = bindVetoListenerIfConfigured();
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
      if (unbindProperty != null) {
        unbindProperty.run();
      }
      if (unbindVeto != null) {
        unbindVeto.run();
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
    Runnable unbindProperty = bindPropertyListenerIfConfigured();
    Runnable unbindVeto = bindVetoListenerIfConfigured();
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
     if (unbindProperty != null) {
       unbindProperty.run();
     }
     if (unbindVeto != null) {
       unbindVeto.run();
     }
    }
  }

  private Runnable bindVetoListenerIfConfigured() {
    if (vetoableListenerBinder == null || listenerTarget == null || vetoableChangeListener == null) {
      return null;
    }
    Runnable unbind = vetoableListenerBinder.apply(listenerTarget, vetoableChangeListener);
    return (unbind != null) ? unbind : () -> {};
  }

  private Runnable bindPropertyListenerIfConfigured() {
    if (propertyListenerBinder == null || listenerTarget == null || propertyChangeListener == null) {
      return null;
    }
    Runnable unbind = propertyListenerBinder.apply(listenerTarget, propertyChangeListener);
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
