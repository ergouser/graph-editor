/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.Selectable;

import javafx.collections.ObservableSet;

/**
 * Provides actions related to selections in the graph editor.
 */
public interface SelectionManager {

    /**
     * Gets the list of currently selected nodes.
     *
     * <p>
     * This list is read-only. Nodes should be selected via {@link #select(Selectable)}.
     * </p>
     *
     * @return the unmodifiable list of currently selected nodes
     */
    List<GSkin<?>> getSelectedNodes();
    
    /**
     * Gets the list of currently selected connections.
     *
     * <p>
     * This list is read-only. Connections should be selected via {@link #select(Selectable)}.
     * </p>
     *
     * @return the unmodifiable list of currently selected connections
     */
    List<GSkin<?>> getSelectedConnections();

    /**
     * Gets the list of currently selected joints.
     *
     * <p>
     * This list is read-only. Joints should be selected via {@link #select(Selectable)}.
     * </p>
     *
     * @return the unmodifiable list of currently selected joints
     */
    List<GSkin<?>> getSelectedJoints();
    
    /**
     * Convenience method to inform if the given object is currently selected. Is
     * functionally equivalent to calling
     * <code>getSelectedItems().contains(object)</code>.
     * 
     * @param object
     * @return {@code true} if the given index is selected, {@code false} otherwise.
     */
    boolean isSelected(GSkin<?>  object);
    
    /**
     * Convenience method to inform if the given object is currently selected. Is
     * functionally equivalent to calling
     * <code>getSelectedItems().contains(object)</code>.
     * 
     * @param object
     * @return {@code true} if the given index is selected, {@code false} otherwise.
     */
    boolean isSelected(Selectable  object);
    
    /**
     * Gets the {@link ObservableSet} of currently-selected items.
     *
     * <p>
     * This set is read-only. Items should be selected via {@link #select(Selectable)}.
     * </p>
     *
     * @return the set of selected items
     */
    ObservableSet<GSkin<?> > getSelectedItems();
    
    /**
     * This method will attempt to select the given object.
     *
     * @param skin The object to attempt to select in the underlying data model.
     */
    void select(final GSkin<?> skin);
    
    /**
     * Selects all selectable elements (nodes, joints, and connections) in the graph editor.
     */
    void selectAll();
    
    /**
     * This method will clear the selection of the given object.
     * If the given object is not selected, nothing will happen.
     *
     * @param object The selected item to deselect.
     */
    void clearSelection(final GSkin<?> skin);

    /**
     * Clears the selection, i.e. de-selects all elements.
     */
    void clearSelection();
}
