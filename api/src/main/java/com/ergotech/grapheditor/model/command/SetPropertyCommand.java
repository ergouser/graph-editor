package com.ergotech.grapheditor.model.command;

import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;

public class SetPropertyCommand<V> extends AbstractCommand {
  private final Property<V> property;
  private final V newValue;
  private V oldValue;

  /** Convenience method to create the set property command. */
  public static <S> SetPropertyCommand<S> create(Property<S> property, S newValue) {
    return new SetPropertyCommand<>(property, newValue);
  }
  //public class CommandFactory {

    /**
     * Creates a {@link SetPropertyCommand} for a specified object and attribute.
     * <p>
     * This method uses JavaFX's {@link JavaBeanObjectProperty} to create a {@link Property}
     * that is directly tied to the JavaBean-style property (getter/setter methods) of the object.
     * The returned {@link SetPropertyCommand} can be used to update the property to a new value.
     * </p>
     *
     * @param <T> the type of the object containing the attribute
     * @param <V> the type of the attribute value
     * @param object the object instance containing the attribute to be updated
     * @param attributeName the name of the attribute (e.g., "Width", "Height")
     * @param newValue the new value to set for the attribute
     * @return a {@link SetPropertyCommand} that can be executed to update the object's attribute to the specified value
     * @throws RuntimeException if there is an issue creating the command, such as if the JavaBean property cannot be accessed
     */
    public static <T, V> SetPropertyCommand<V> create(T object, String attributeName, V newValue) {
      try {
        // Use JavaFX's JavaBeanObjectProperty to link to the JavaBean-style property
        @SuppressWarnings("unchecked")
        JavaBeanObjectProperty<V> property = JavaBeanObjectPropertyBuilder
            .create()
            .bean(object)
            .name(attributeName)
            .build();

        // Return the SetPropertyCommand
        return new SetPropertyCommand<>(property, newValue);

      } catch (NoSuchMethodException e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to create SetPropertyCommand: unable to access JavaBean property", e);
      }
    }
  //}

  public SetPropertyCommand(Property<V> property, V newValue) {
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute() throws Exception {
    if (canExecute()) {
      oldValue = property.getValue();
      property.setValue(newValue);
      setExecuted(true);
    }
  }

  @Override
  public void undo() throws Exception {
    if (canUndo()) {
      property.setValue(oldValue);
      setExecuted(false);
    }
  }

  @Override
  public boolean canExecute() {
      return !isExecuted() ;
  }

  @Override
  public boolean canUndo() {
      return isExecuted() ;
  }


  @Override
  public String toString() {
    return "SetPropertyCommand [property=" + property + ", newValue=" + newValue + ", oldValue=" + oldValue
        + ", executed=" + isExecuted() + "]";
  }
  
}
