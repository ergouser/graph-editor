package com.ergotech.grapheditor.model.command;

import java.util.List;

@FunctionalInterface
public interface ModelListSupplier<T> {
    List<T> getList(Object owner);
}