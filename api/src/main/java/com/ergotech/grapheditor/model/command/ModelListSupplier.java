package com.ergotech.grapheditor.model.command;

import java.util.Collection;

@FunctionalInterface
public interface ModelListSupplier<T> {
    Collection<? extends T> getList(Object owner);
}