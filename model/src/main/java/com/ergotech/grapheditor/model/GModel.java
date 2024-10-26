package com.ergotech.grapheditor.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GModel {
    // Observable Lists for nodes and connections
    private final ObservableList<GNode> nodes = FXCollections.observableArrayList();
    private final ObservableList<GConnection> connections = FXCollections.observableArrayList();

    // Properties
    private final StringProperty type = new SimpleStringProperty(this, "type");
    private final DoubleProperty contentWidth = new SimpleDoubleProperty(this, "contentWidth", 3000);
    private final DoubleProperty contentHeight = new SimpleDoubleProperty(this, "contentHeight", 2250);

    // Constructor
    public GModel() {
        // Optionally, add listeners to the nodes and connections lists
    }

    // Nodes
    public ObservableList<GNode> getNodes() {
        return nodes;
    }

    public void addNode(GNode node) {
        nodes.add(node);
        // If bidirectional, set the parent model in the node
        // node.setParentModel(this);
    }

    public void removeNode(GNode node) {
        nodes.remove(node);
        // If bidirectional, remove the parent model reference
        // node.setParentModel(null);
    }

    // Connections
    public ObservableList<GConnection> getConnections() {
        return connections;
    }

    public void addConnection(GConnection connection) {
        connections.add(connection);
        // If bidirectional, set the parent model in the connection
        // connection.setParentModel(this);
    }

    public void removeConnection(GConnection connection) {
        connections.remove(connection);
        // If bidirectional, remove the parent model reference
        // connection.setParentModel(null);
    }

    // Type Property
    public StringProperty typeProperty() {
        return type;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String value) {
        type.set(value);
    }

    // Content Width Property
    public DoubleProperty contentWidthProperty() {
        return contentWidth;
    }

    public double getContentWidth() {
        return contentWidth.get();
    }

    public void setContentWidth(double value) {
        contentWidth.set(value);
    }

    // Content Height Property
    public DoubleProperty contentHeightProperty() {
        return contentHeight;
    }

    public double getContentHeight() {
        return contentHeight.get();
    }

    public void setContentHeight(double value) {
        contentHeight.set(value);
    }
}
