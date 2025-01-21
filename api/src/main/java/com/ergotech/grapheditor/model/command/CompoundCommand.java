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

    public void appendAndExecute(Command command) {
      append(command);
      if (command.canExecute()) {
        command.execute();
      }
    }
    @Override
    public void execute() {
        if (!isExecuted()) {
            for (Command command : commands) {
                if (command.canExecute()) {
                    command.execute();
                }
            }
            setExecuted(true);
        }
    }

    @Override
    public void undo() {
        if (isExecuted()) {
            for (int i = commands.size() - 1; i >= 0; i--) {
                Command command = commands.get(i);
                if (command.canUndo()) {
                    command.undo();
                }
            }
            setExecuted(false);
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
