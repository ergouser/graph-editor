package com.ergotech.grapheditor.model.command;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CompoundCommand implements Command {

  private final ObservableList<Command> commands = FXCollections.observableArrayList();
  private final BooleanProperty executed = new SimpleBooleanProperty(false);

  public void append(Command command) {
    if (!isExecuted()) {
      commands.add(command);
    }
  }

  public ObservableList<Command> getCommandList() {
    return commands;
  }

  /** Append the command and execute it.  The command will not be added to the Compound command if
   * execution fails.
   * 
   * @param command the command to execute
   * @throws Exception thrown if the execution fails.  The command will not be appended to the Compound command if the execution fails.
   */
  public void appendAndExecute(Command command) throws Exception {
    if (command.canExecute()) {
      command.execute();
    }
    append(command);
  }
  
  /** Execute all commands.  If any command fails the rest of the commands will still
   * be executed.  The exception reported by the first failed command will be thrown
   * after completion of the execution.  The compound command will be marked as 
   * executed even if all the commands fail. 
   * 
   * @throws Exception thrown if any command execution fails.  The exception will be the first failure reported.
   */
  @Override
  public void execute() throws Exception {
    Exception firstException = null;
    if (!isExecuted()) {
      for (Command command : commands) {
        if (command.canExecute()) {
          try {
            command.execute();
          } catch ( Exception e ) {
            if (firstException == null ) {
              firstException = e;
            }
          }
        }
      }
      setExecuted(true);
    }
    if ( firstException != null ) {
      throw firstException;
    }
  }

  /** Execute all undos.  If any undo fails the rest of the undos will still
   * be executed.  The exception reported by the first failed undo will be thrown
   * after completion of the execution.  The compound undo will be marked as 
   * not executed even if all the undos fail (that behvior is the same as if all undos
   * succeeded).
   * 
   * @throws Exception thrown if any command execution fails.  The exception will be the first failure reported.
   */
 @Override
  public void undo() throws Exception {
    Exception firstException = null;
    if (isExecuted()) {
      for (int i = commands.size() - 1; i >= 0; i--) {
        Command command = commands.get(i);
        if (command.canUndo()) {
          try {
            command.undo();
          } catch ( Exception e ) {
            if (firstException == null ) {
              firstException = e;
            }
          }
        }
      }
      setExecuted(false);
    }
    if ( firstException != null ) {
      throw firstException;
    }
  }

  @Override
  public boolean canExecute() {
    return !isExecuted() && commands.stream().allMatch(Command::canExecute);
  }

  @Override
  public boolean canUndo() {
    return isExecuted() && commands.stream().allMatch(Command::canUndo);
  }

  public boolean isExecuted() {
    return executed.get();
  }

  public void setExecuted(boolean value) {
    executed.set(value);
  }

  public BooleanProperty executedProperty() {
    return executed;
  }

  public boolean isEmpty() {
    return commands.isEmpty();
  }

  @Override
  public String toString() {
    return "CompoundCommand [commands=" + commands + ", executed=" + executed + "]";
  }

}
