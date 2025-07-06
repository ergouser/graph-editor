package com.ergotech.grapheditor.model.command;

import javafx.beans.property.BooleanProperty;

/**
 * The Command interface represents an executable action with undo and redo capabilities.
 */
public interface Command {


  /** Return an observable property for executed. */
   BooleanProperty executedProperty();

    /**
   * Executes the command.
   * 
   * @throws Exception thrown if the command execution fails for any reason
   * one example might be a command to change a property that throws a PropertyVetoException
   * but the Command can fail for any reason required by the implementation
   */ 
  void execute() throws Exception;
  /**
   * Redo the command.  This default to calling execute()
   * 
   * @throws Exception thrown if the command execution fails for any reason
   * one example might be a command to change a property that throws a PropertyVetoException
   * but the Command can fail for any reason required by the implementation
   */ 
  default void redo() throws Exception {
    execute();
  }

  /**
   * Undoes the command.
   *      
   * @throws Exception thrown if the command execution fails for any reason
   * one example might be a command to change a property that throws a PropertyVetoException
   * but the Command can fail for any reason required by the implementation
   * This may occur on undo even if the original execute succeeded.
   * 
   */
  void undo() throws Exception;

  /**
   * Determines if the command can be executed.
   *
   * @return true if the command can be executed; false otherwise.
   */
  boolean canExecute();

  /**
   * Determines if the command can be undone.
   *
   * @return true if the command can be undone; false otherwise.
   */
  boolean canUndo();
  
  /** Dispose of the commmand.  By default this does nothing
   * but is available, for cleanup of the command elements
   */
  default void dispose() {
    // does nothing.
  }
}
