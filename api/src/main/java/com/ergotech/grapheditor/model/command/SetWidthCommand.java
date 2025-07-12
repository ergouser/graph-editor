package com.ergotech.grapheditor.model.command;

import java.util.function.Consumer;

public class SetWidthCommand extends AbstractCommand {
  private final Consumer<Double> setter;

  private final double oldWidth;

  private final double newWidth;

  /** Convenience method to create the remove command. */
  public static SetWidthCommand create(double oldWidth, Consumer<Double> setter, double newWidth) {
    return new SetWidthCommand(oldWidth, setter, newWidth);
  }

  /**
   * @param getter
   *          a Supplier that returns the current width
   * @param setter
   *          a Consumer that applies a new width
   * @param newWidth
   *          the width to set on execute()
   */
  public SetWidthCommand(double oldWidth, Consumer<Double> setter, double newWidth) {
    this.setter = setter;
    this.newWidth = newWidth;
    this.oldWidth = oldWidth;
  }

  @Override
  public void execute() throws Exception {
    if (!isExecuted() && canExecute()) {
      setter.accept(newWidth);
      setExecuted(true);
    }
  }

  @Override
  public void undo() throws Exception {
    if (isExecuted() && canUndo()) {
      setter.accept(oldWidth);
    }
  }

  @Override
  public String toString() {
    return "SetWidthCommand [executed=" + isExecuted() + "]";
  }

}
