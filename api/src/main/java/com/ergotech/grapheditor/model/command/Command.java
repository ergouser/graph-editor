package com.ergotech.grapheditor.model.command;

/**
 * The Command interface represents an executable action with undo and redo capabilities.
 */
public interface Command {
    /**
     * Executes the command.
     * 
     * @throws Exception thrown if the command execution fails for any reason
     * one example might be a command to change a property that throws a PropertyVetoException
     * but the Command can fail for any reason required by the implementation
    */ 
    void execute() throws Exception;

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
}
