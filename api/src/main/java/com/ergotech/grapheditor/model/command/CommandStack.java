package com.ergotech.grapheditor.model.command;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * The BasicCommandStack class manages the execution, undoing, and redoing of commands.
 * It uses JavaFX properties to integrate seamlessly with JavaFX UI components.
 */
public class CommandStack {

  /** The singleton command stack. */
  protected static Map<Object,CommandStack> commandStacks = new HashMap<>();

  /** Return the command stack for the current editing domain.  This is likely a GModel. */
  public static CommandStack getCommandStack(Object model) {
    CommandStack basicCommandStack = commandStacks.get(model);
    if ( basicCommandStack == null ) {
      basicCommandStack = new CommandStack();
      commandStacks.put(model, basicCommandStack);
    }
    return basicCommandStack;
  }
  /**
   * The list of executed commands.
   */
  protected final ObservableList<Command> commands;

  /**
   * The index of the last executed command in the commands list.
   */
  protected final IntegerProperty top = new SimpleIntegerProperty(-1);

  /**
   * Indicates whether an undo operation can be performed.
   */
  protected final BooleanProperty canUndo = new SimpleBooleanProperty(false);

  /**
   * Indicates whether a redo operation can be performed.
   */
  protected final BooleanProperty canRedo = new SimpleBooleanProperty(false);

  private boolean stackChangeNotificationsSuspended = false;

  /*The command stack listener. May be null.*/
  protected CommandStackListener listener;

  ListChangeListener<Command> listChangeListener = change -> {
    if (!stackChangeNotificationsSuspended && listener != null ) {
      while (change.next()) {
        if (change.wasRemoved()) {
          for (Command removedCmd : change.getRemoved()) {
            removedCmd.dispose();
          }
        }
        if (change.wasAdded() || change.wasRemoved()) {
          // Create an event and pass it to the listener
          EventObject event = new EventObject(change);
          listener.commandStackChanged(event);
        }
        if (change.wasUpdated()) {
          // some command’s executedProperty changed
          updateCanUndoRedo();
        }
      }
    }
  };
  
  private final ObservableList<Runnable> topRefreshListeners = FXCollections.observableArrayList();

  /**
   * Constructs a CommandStack and sets up listeners for property changes.
   */
  protected CommandStack() {
    commands = FXCollections.observableArrayList(cmd -> new Observable[]{cmd.executedProperty()});

    // Update canUndo and canRedo whenever 'top' changes fire a refresh to the listeners.
    top.addListener((observable, oldValue, newValue) -> {
      updateCanUndoRedo();
      fireTopRefresh();
    });
    commands.addListener(listChangeListener);
  }

  /** Emulate the commandStackChanged call from the commands observable list.
   * 
   * @param listener
   */
  public void addCommandStackListener(CommandStackListener listener) {
    this.listener = listener;
  }

  /** Remove the listener
   * 
   * @param listener unused but provided for compatibility
   */
  public void removeCommandStackListener(CommandStackListener listener) {
    this.listener = null;
  }

  /**
   * Suspend notifications to the listeners.
   */
  public void suspendStackChangeNotifications() {
    stackChangeNotificationsSuspended = true;
  }

  /**
   * Resume notifications to the listeners.
   */
  public void resumeStackChangeNotifications() {
    stackChangeNotificationsSuspended = false;
  }
  
  /** Add a listener to be notified when the top of the stack changes or when a refresh is forced. */
  public void addTopRefreshListener(Runnable listener) {
    topRefreshListeners.add(listener);
  }

  /** Remove a listener from the top refresh notifications. */
  public void removeTopRefreshListener(Runnable listener) {
    topRefreshListeners.remove(listener);
  }

  /** Notify listeners that the top of the stack has changed or that a refresh is needed. */
  public void fireTopRefresh() {
    for (Runnable listener : topRefreshListeners) {
      listener.run();
    }
  }

  /**
   * Executes the given command and adds it to the command stack.
   *
   * @param command the command to execute.
   * @throws Exception 
   */
  public void execute(Command command) throws Exception {
    //System.out.println (top.get() + " Command execute " + command.canUndo() + " " + command.canExecute());
    if (command.canExecute()) {
      command.execute();
      // don't add the command to the undo stack if the command did not execute

      // Remove any commands above the current top
      if (commands.size() > top.get() + 1) {
        commands.subList(top.get() + 1, commands.size()).clear();
      }

      // when a command is added, all the commands eligible for redo must be
      // removed.  They become inaccessible after this command is added.
      commands.add(command);
      top.set(top.get() + 1);
    }
  }

  /**
   * Undoes the last executed command.
   * @throws Exception 
   */
  public void undo() throws Exception {
    if (canUndo()) {
      Command command = commands.get(top.get());
      // move the  stack position even if the undo fails
      // otherwise further undos would be inaccessible.
      try {
      command.undo();
      } finally {
        top.set(top.get() - 1); 
      }
    }
  }

  /**
   * Redoes the last undone command.
   * @throws Exception 
   */
  public void redo() throws Exception {
    if (canRedo()) {
      Command command = commands.get(top.get()+1);
      try {
        command.redo();
      } finally {
        top.set(top.get() + 1);
      }
    }
  }

  /**
   * Determines if an undo operation can be performed.
   *
   * @return true if undo is available; false otherwise.
   */
  public boolean canUndo() {
    return canUndo.get();
  }

  public void flush() {
    commands.clear();
    canUndo.set(false);
    canRedo.set(false);
    top.set(-1);
  }

  /**
   * Returns the property representing the ability to undo.
   *
   * @return the canUndo property.
   */
  public ReadOnlyBooleanProperty canUndoProperty() {
    return canUndo;
  }

  /**
   * Determines if a redo operation can be performed.
   *
   * @return true if redo is available; false otherwise.
   */
  public boolean canRedo() {
    return canRedo.get();
  }

  /**
   * Returns the property representing the ability to redo.
   *
   * @return the canRedo property.
   */
  public ReadOnlyBooleanProperty canRedoProperty() {
    return canRedo;
  }

  /**
   * Updates the canUndo and canRedo properties based on the current state.
   */
  private void updateCanUndoRedo() {
    // this will be called because there has been a change to executed before the command has been added to the stack.
    Command command = null;
    if ( commands.size() > 0 && top.get() >= 0 ) { 
      boolean enoughCommands = top.get() >= 0;
      boolean wasExecuted = commands.get(top.get()).canUndo();
    }
    canUndo.set(top.get() >= 0 && (command=commands.get(top.get())).canUndo());
    //    if ( command != null ) {
    //      System.out.println (top.get() + " Command undo " + command.canUndo() + " " + command.canExecute());
    //      command = null;
    //    }
    if ( commands.size() > 0 && top.get() >= 0 ) { 
      boolean enoughCommands = top.get() + 1 < commands.size();
      if ( enoughCommands ) {
        command = commands.get(top.get() + 1);
        boolean isExecutable = command.canExecute();
      }
    }
    canRedo.set(top.get() + 1 < commands.size() && (command=commands.get(top.get() + 1)).canExecute());  // why +1 ?? 
    //canRedo.set(top.get() >= 0 && (command=commands.get(top.get())).canExecute());
    //System.out.println (top.get() + " Command redo " + canRedo.get() + " " + canUndo.get());
  }

  /**
   * Returns an unmodifiable view of the command list.
   *
   * @return the list of commands.
   */
  public ObservableList<Command> getCommands() {
    return FXCollections.unmodifiableObservableList(commands);
  }

  /**
   * Returns the property representing the top index.
   *
   * @return the top property.
   */
  public ReadOnlyIntegerProperty topProperty() {
    return top;
  }

}
