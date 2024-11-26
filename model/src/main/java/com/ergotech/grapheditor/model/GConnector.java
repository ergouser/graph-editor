package com.ergotech.grapheditor.model;

import java.util.List;

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
 *   <li>{@link #xProperty()} - The x-coordinate of the connector</li>
 *   <li>{@link #yProperty()} - The y-coordinate of the connector</li>
 *   <li>{@link #connectionDetachedOnDragProperty()} - Indicates whether connections are detached when the connector is dragged</li>
 * </ul>
 */
public interface GConnector extends Selectable {

    /**
     * Returns the parent node to which this connector belongs.
     *
     * @return the parent GNode.
     */
    public GNode getParent();

    /**
     * Sets the parent node to which this connector belongs.
     *
     * @param parent the parent GNode to set.
     */
    public void setParent(GNode parent);

    /**
     * Gets the list of connections associated with this connector.
     *
     * @return the connections as an ObservableList.
     */
    public List<GConnection> getConnections();
    /**
     * Adds a connection to the connector.
     *
     * @param connection the connection to add.
     */
    public void addConnection(GConnection connection);

    /**
     * Removes a connection from the connector.
     *
     * @param connection the connection to remove.
     */
    public void removeConnection(GConnection connection);

    /**
     * Returns whether connections are detached when the connector is dragged.
     *
     * @return true if connections are detached on drag, false otherwise.
     */
    public boolean isConnectionDetachedOnDrag();
    /**
     * Sets whether connections are detached when the connector is dragged.
     *
     * @param value true to detach connections on drag, false otherwise.
     */
    public void setConnectionDetachedOnDrag(boolean value);
}
