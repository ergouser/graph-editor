package com.ergotech.grapheditor.model.impl;

import java.util.Collection;

import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GNode;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class GNodeImpl extends SelectableType implements GNode {
  
  // Observable List for connectors
  private final ObservableList<GConnectorPort> connectors = FXCollections.observableArrayList();

  private ChangeListener<String> typeListener;

  private ListChangeListener<GConnectorPort> connectorsListener;

  // Constructor
  public GNodeImpl() {
    // Listen for changes in the connectors list to manage parent references
    connectors.addListener((ListChangeListener<GConnectorPort>) change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          for (GConnectorPort connector : change.getAddedSubList()) {
            connector.setParent(this);
          }
        }
        if (change.wasRemoved()) {
          for (GConnectorPort connector : change.getRemoved()) {
            if (connector.getParent() == this) {
              connector.setParent(null);
            }
          }
        }
      }
    });
  }

  // Listener management methods
  public void addListeners(ChangeListener<String> typeListener,
      ListChangeListener<GConnectorPort> connectorsListener) {
    removeListeners(); // if there are any.
    // Store strong references
    this.typeListener = typeListener;
    this.connectorsListener = connectorsListener;

    // Attach listeners using WeakListeners
    //typeProperty().addListener(typeListener);
    ((ObservableList<GConnectorPort>)getConnectorPorts()).addListener(connectorsListener);
  }

  public void removeListeners() {
    if ( typeListener != null ) { // assume that all are null or none are null...
      //typeProperty().removeListener(typeListener);
      ((ObservableList<GConnectorPort>)getConnectorPorts()).removeListener(connectorsListener);
    }
  }

   // Connectors
  public Collection<GConnectorPort> getConnectorPorts() {
    return connectors;
  }

  // Methods to add and remove connectors
  public void addConnectorPort(GConnectorPort connector) {
    connectors.add(connector);
  }

  public void removeConnectorPort(GConnectorPort connector) {
    connectors.remove(connector);
  }

  @Override
  public String toString() {
    return "GNode [id=" + getId() + ", connectors=" + connectors.size() + "]";
  }
  
}
