package com.ergotech.grapheditor.model.command;

/**
 * The Command interface represents an executable action with undo and redo capabilities.
 */
public interface Command {
    /**
     * Executes the command.
     */
    void execute();

    /**
     * Undoes the command.
     */
    void undo();

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
