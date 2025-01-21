package com.ergotech.grapheditor.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.GraphFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GModelImpl implements GModel {
  // Observable Lists for nodes and connections
  private final ObservableList<GNode> nodes = FXCollections.observableArrayList();
  private final ObservableList<GConnection> connections = FXCollections.observableArrayList();

  // Properties
  private final StringProperty type = new SimpleStringProperty(this, "type");
//  private final DoubleProperty contentWidth = new SimpleDoubleProperty(this, "contentWidth", 3000);
//  private final DoubleProperty contentHeight = new SimpleDoubleProperty(this, "contentHeight", 2250);

  private static final Map<Class<?>, Supplier<?>> FACTORY_MAP = new HashMap<>();

  protected static GraphFactory graphFactory;

  static {
    FACTORY_MAP.put(GNode.class, GNodeImpl::new);
    FACTORY_MAP.put(GConnection.class, GConnectionImpl::new);
    FACTORY_MAP.put(GConnectorPort.class, GConnectorPortImpl::new);
    FACTORY_MAP.put(GJoint.class, GJointImpl::new);
    graphFactory = new GraphFactory(FACTORY_MAP);
  }

  // Constructor
  public GModelImpl() {
    // Optionally, add listeners to the nodes and connections lists
  }

  // Nodes
  @Override
  public ObservableList<GNode> getNodes() {
    return nodes;
  }

  @Override
  public void addNode(GNode node) {
    nodes.add(node);
    // If bidirectional, set the parent model in the node
    // node.setParentModel(this);
  }

  @Override
  public void removeNode(GNode node) {
    nodes.remove(node);
    // If bidirectional, remove the parent model reference
    // node.setParentModel(null);
  }

  // Connections
  @Override
  public ObservableList<GConnection> getConnections() {
    return connections;
  }

  @Override
  public void addConnection(GConnection connection) {
    connections.add(connection);
    // If bidirectional, set the parent model in the connection
    // connection.setParentModel(this);
  }

  @Override
  public void removeConnection(GConnection connection) {
    connections.remove(connection);
    // If bidirectional, remove the parent model reference
    // connection.setParentModel(null);
  }

  // Type Property
  public StringProperty typeProperty() {
    return type;
  }

  @Override
  public String getType() {
    return type.get();
  }

  @Override
  public void setType(String value) {
    type.set(value);
  }

//  // Content Width Property
//  public DoubleProperty contentWidthProperty() {
//    return contentWidth;
//  }
//
//  @Override
//  public double getContentWidth() {
//    return contentWidth.get();
//  }
//
//  @Override
//  public void setContentWidth(double value) {
//    contentWidth.set(value);
//  }
//
//  // Content Height Property
//  public DoubleProperty contentHeightProperty() {
//    return contentHeight;
//  }
//
//  @Override
//  public double getContentHeight() {
//    return contentHeight.get();
//  }
//
//  @Override
//  public void setContentHeight(double value) {
//    contentHeight.set(value);
//  }

  /** Return the graph factory. */
  @Override
  public GraphFactory getGraphFactory() {
    return GModelImpl.graphFactory;
  }

}
