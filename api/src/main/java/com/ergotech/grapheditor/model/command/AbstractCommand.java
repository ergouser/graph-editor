package com.ergotech.grapheditor.model.command;

public abstract class AbstractCommand implements Command {
  protected boolean executed = false;

  @Override
  public boolean canExecute() {
      return !executed;
  }

  @Override
  public boolean canUndo() {
      return executed;
  }

  @Override
  public String toString() {
    return "AbstractCommand [executed=" + executed + "]";
  }
  
}
