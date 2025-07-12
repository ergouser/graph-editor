package com.ergotech.grapheditor.model.command;

import java.util.function.Consumer;

public class SetCommand<T> extends AbstractCommand {
  private final Consumer<T> setter;

  private final T oldValue;

  private final T newValue;

  /** Convenience method to create the set dimension command. */
  public static <S> SetCommand<S> create(S oldValue, Consumer<S> setter, S newValue) {
    return new SetCommand<>(oldValue, setter, newValue);
  }

  /**
   * @param oldValue
   *          the current value
   * @param setter
   *          a Consumer that applies a new value
   * @param newValue
   *          the value to set on execute()
   */
  public SetCommand(T oldValue, Consumer<T> setter, T newValue) {
    this.setter = setter;
    this.newValue = newValue;
    this.oldValue = oldValue;
  }

  @Override
  public void execute() throws Exception {
    if (!isExecuted() && canExecute()) {
      setter.accept(newValue);
      setExecuted(true);
    }
  }

  @Override
  public void undo() throws Exception {
    if (isExecuted() && canUndo()) {
      setter.accept(oldValue);
    }
  }

  @Override
  public String toString() {
    return "SetHeightCommand [executed=" + isExecuted() + "]";
  }

}
