package com.ergotech.grapheditor.model;

import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GNode;

/**
 * Represents the model of a graph in the graph editor.
 * <p>
 * This interface provides methods to manage the nodes and connections
 * within the graph model, as well as properties related to the model's
 * type and content dimensions.
 * </p>
 */
public interface GModel {

    // Nodes

    /**
     * Gets the list of nodes in the model.
     *
     * @return a list of {@link GNode} instances contained in the model.
     */
    List<GNode> getNodes();

    /**
     * Adds a node to the model.
     *
     * @param node the {@link GNode} instance to add.
     */
    void addNode(GNode node);

    /**
     * Removes a node from the model.
     *
     * @param node the {@link GNode} instance to remove.
     */
    void removeNode(GNode node);

    // Connections

    /**
     * Gets the list of connections in the model.
     *
     * @return a list of {@link GConnection} instances contained in the model.
     */
    List<GConnection> getConnections();

    /**
     * Adds a connection to the model.
     *
     * @param connection the {@link GConnection} instance to add.
     */
    void addConnection(GConnection connection);

    /**
     * Removes a connection from the model.
     *
     * @param connection the {@link GConnection} instance to remove.
     */
    void removeConnection(GConnection connection);

    /**
     * Gets the type of the model.
     *
     * @return the type of the model as a {@link String}.
     */
    String getType();

    /**
     * Sets the type of the model.
     *
     * @param value the type to set for the model.
     */
    void setType(String value);

    /**
     * Gets the content width of the model.
     *
     * @return the content width as a double.
     */
    double getContentWidth();

    /**
     * Sets the content width of the model.
     *
     * @param value the content width to set.
     */
    void setContentWidth(double value);

    /**
     * Gets the content height of the model.
     *
     * @return the content height as a double.
     */
    double getContentHeight();

    /**
     * Sets the content height of the model.
     *
     * @param value the content height to set.
     */
    void setContentHeight(double value);

    /** Return the graph factory. */
    GraphFactory getGraphFactory();

}
