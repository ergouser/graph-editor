package com.ergotech.grapheditor.model.command;

import javafx.collections.ObservableList;

@FunctionalInterface
public interface ModelListSupplier<T> {
    ObservableList<T> getList(Object owner);
}