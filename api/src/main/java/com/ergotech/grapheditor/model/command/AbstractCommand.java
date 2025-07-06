package com.ergotech.grapheditor.model.command;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class AbstractCommand implements Command {
  private final BooleanProperty executed = new SimpleBooleanProperty(this, "executed", false);

  /** The executed property. */
  public final BooleanProperty executedProperty() {
      return executed;
  }

  /** Returns true if the command has been executed (and not undone).  */
  public final boolean isExecuted() {
      return executed.get();
  }

  /** Set the executed property. */
  public final void setExecuted(boolean value) {
      executed.set(value);
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
    return "AbstractCommand [executed=" + isExecuted()  + "]";
  }
  
}
