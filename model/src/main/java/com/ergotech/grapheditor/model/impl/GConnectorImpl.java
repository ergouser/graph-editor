package com.ergotech.grapheditor.model.impl;

import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnector;
import com.ergotech.grapheditor.model.GNode;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A representation of the model object '<em><b>GConnector</b></em>'.
 *
 * A `GConnector` represents a connection point on a `GNode`. It can be connected
 * to other connectors via `GConnection` objects. Connectors have positional properties
 * and can determine whether a connection should be detached when dragged.
 *
 * <p>
 * Features of the `GConnector` class:
 * </p>
 * <ul>
 *   <li>{@link #idProperty()} - The unique identifier of the connector</li>
 *   <li>{@link #typeProperty()} - The type of the connector</li>
 *   <li>{@link #parentProperty()} - The parent node to which this connector belongs</li>
 *   <li>{@link #connectionsProperty()} - The list of connections associated with this connector</li>
 *   <li>{@link #connectionDetachedOnDragProperty()} - Indicates whether connections are detached when the connector is dragged</li>
 * </ul>
 */
public class GConnectorImpl extends SelectableType implements GConnector {

    // Properties
    private final ObjectProperty<GNode> parent = new SimpleObjectProperty<>(this, "parent");
    private final ObservableList<GConnection> connections = FXCollections.observableArrayList();
    private final BooleanProperty connectionDetachedOnDrag = new SimpleBooleanProperty(this, "connectionDetachedOnDrag", true);

    /**
     * Gets the parent node to which this connector belongs.
     *
     * @return the parent node as an ObjectProperty.
     */
    public ObjectProperty<GNode> parentProperty() {
        return parent;
    }

    /**
     * Returns the parent node to which this connector belongs.
     *
     * @return the parent GNode.
     */
    public GNode getParent() {
        return parent.get();
    }

    /**
     * Sets the parent node to which this connector belongs.
     *
     * @param parent the parent GNode to set.
     */
    public void setParent(GNode parent) {
        this.parent.set(parent);
    }

    /**
     * Gets the list of connections associated with this connector.
     *
     * @return the connections as an ObservableList.
     */
    public List<GConnection> getConnections() {
        return connections;
    }

    /**
     * Adds a connection to the connector.
     *
     * @param connection the connection to add.
     */
    public void addConnection(GConnection connection) {
        if (!connections.contains(connection)) {
            connections.add(connection);
        }
    }

    /**
     * Removes a connection from the connector.
     *
     * @param connection the connection to remove.
     */
    public void removeConnection(GConnection connection) {
        connections.remove(connection);
    }

    /**
     * Gets the property indicating whether connections are detached when the connector is dragged.
     *
     * @return the connectionDetachedOnDrag property as a BooleanProperty.
     */
    public BooleanProperty connectionDetachedOnDragProperty() {
        return connectionDetachedOnDrag;
    }

    /**
     * Returns whether connections are detached when the connector is dragged.
     *
     * @return true if connections are detached on drag, false otherwise.
     */
    public boolean isConnectionDetachedOnDrag() {
        return connectionDetachedOnDrag.get();
    }

    /**
     * Sets whether connections are detached when the connector is dragged.
     *
     * @param value true to detach connections on drag, false otherwise.
     */
    public void setConnectionDetachedOnDrag(boolean value) {
        connectionDetachedOnDrag.set(value);
    }

    // Optional: Override toString for better debugging
    @Override
    public String toString() {
        return "GConnector{" +
                "id=" + getId() +
                ", type=" + getType() +
                ", parent=" + getParent() +
                 ", connectionDetachedOnDrag=" + isConnectionDetachedOnDrag() +
                '}';
    }
}
