package com.ergotech.grapheditor.model.command;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SetHeightCommand extends AbstractCommand {
  private final Consumer<Double> setter;

  private final double oldHeight;

  private final double newHeight;

  /** Convenience method to create the remove command. */
  public static SetHeightCommand create(Supplier<Double> getter, Consumer<Double> setter, double newHeight) {
    return new SetHeightCommand(getter, setter, newHeight);
  }

  /**
   * @param getter
   *          a Supplier that returns the current height
   * @param setter
   *          a Consumer that applies a new height
   * @param newHeight
   *          the height to set on execute()
   */
  public SetHeightCommand(Supplier<Double> getter, Consumer<Double> setter, double newHeight) {
    this.setter = setter;
    this.newHeight = newHeight;
    this.oldHeight = getter.get();
  }

  @Override
  public void execute() throws Exception {
    if (!isExecuted() && canExecute()) {
      setter.accept(newHeight);
      setExecuted(true);
    }
  }

  @Override
  public void undo() throws Exception {
    if (isExecuted() && canUndo()) {
      setter.accept(oldHeight);
    }
  }

  @Override
  public String toString() {
    return "SetHeightCommand [executed=" + isExecuted() + "]";
  }

}
