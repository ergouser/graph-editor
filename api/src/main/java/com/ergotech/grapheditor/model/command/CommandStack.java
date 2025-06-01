package com.ergotech.grapheditor.model.command;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import com.ergotech.grapheditor.model.GModel;

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
  protected static Map<GModel,CommandStack> commandStacks = new HashMap<>();

  /** Return the command stack (currently a singleton). */
  public static CommandStack getCommandStack(GModel model) {
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
  protected final ObservableList<Command> commands = FXCollections.observableArrayList();

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
  CommandStackListener listener;

  ListChangeListener<Command> listChangeListener = change -> {
    if (!stackChangeNotificationsSuspended && listener != null ) {
      while (change.next()) {
      }
      // You can add more specific conditions here if needed
      if (change.wasAdded() || change.wasRemoved()) {
        // Create an event and pass it to the listener
        EventObject event = new EventObject(change);
        listener.commandStackChanged(event);
      }

    }
  };

  /**
   * Constructs a CommandStack and sets up listeners for property changes.
   */
  protected CommandStack() {
    // Update canUndo and canRedo whenever 'top' changes
    top.addListener((observable, oldValue, newValue) -> {
      updateCanUndoRedo();
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
      top.set(top.get() - 1); 
      command.undo();
    }
  }

  /**
   * Redoes the last undone command.
   * @throws Exception 
   */
  public void redo() throws Exception {
    if (canRedo()) {
      top.set(top.get() + 1);
      Command command = commands.get(top.get());
      command.execute();
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
    Command command = null;
    canUndo.set(top.get() >= 0 && (command=commands.get(top.get())).canUndo());
    //    if ( command != null ) {
    //      System.out.println (top.get() + " Command undo " + command.canUndo() + " " + command.canExecute());
    //      command = null;
    //    }
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
