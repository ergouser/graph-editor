package com.ergotech.grapheditor.model.command;

public class AddCommand<T> extends AbstractCommand {
  private final Object owner;
  private final ModelListSupplier<T> listSupplier;
  private final T element;

  /** Convenience method to create the remove command. */
  public static <S> AddCommand<S> create(Object owner, ModelListSupplier<S> listSupplier, S element) {
      return new AddCommand<>(owner, listSupplier, element);
  }

  public AddCommand(Object owner, ModelListSupplier<T> listSupplier, T element) {
      this.owner = owner;
      this.listSupplier = listSupplier;
      this.element = element;
  }

  @Override
  public void execute() {
      if (!executed && canExecute()) {
          listSupplier.getList(owner).add(element);
          executed = true;
      }
  }

  @Override
  public void undo() {
      if (executed && canUndo()) {
          listSupplier.getList(owner).remove(element);
          executed = false;
      }
  }

}
