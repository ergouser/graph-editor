package com.ergotech.grapheditor.model.command;

import java.util.Collection;

public class RemoveCommand<T> extends AbstractCommand {
  private final Object owner;
  private final ModelListSupplier<T> listSupplier;
  private final T element;
  private boolean executed = false;

  /** Convenience method to create the remove command. */
  public static <S> RemoveCommand<S> create(Object owner, ModelListSupplier<S> listSupplier, S element) {
    return new RemoveCommand<>(owner, listSupplier, element);
  }

  public RemoveCommand(Object owner, ModelListSupplier<T> listSupplier, T element) {
    this.owner = owner;
    this.listSupplier = listSupplier;
    this.element = element;
  }

  @Override
  public void execute() {
    if (!executed && canExecute()) {
      listSupplier.getList(owner).remove(element);
      executed = true;
    }
  }

  @Override
  public void undo() {
    if (executed && canUndo()) {
      @SuppressWarnings("unchecked")
      Collection<T> list = (Collection<T>) listSupplier.getList(owner);
      list.add(element);
      executed = false;
    }
  }
}

