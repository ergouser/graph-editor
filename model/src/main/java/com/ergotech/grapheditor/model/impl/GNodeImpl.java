package com.ergotech.grapheditor.model.impl;

import java.util.List;

import com.ergotech.grapheditor.model.GConnector;
import com.ergotech.grapheditor.model.GNode;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class GNodeImpl extends SelectableType implements GNode {
  
  // Observable List for connectors
  private final ObservableList<GConnector> connectors = FXCollections.observableArrayList();

  private ChangeListener<String> typeListener;

  private ListChangeListener<GConnector> connectorsListener;

  // Constructor
  public GNodeImpl() {
    // Listen for changes in the connectors list to manage parent references
    connectors.addListener((ListChangeListener<GConnector>) change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          for (GConnector connector : change.getAddedSubList()) {
            connector.setParent(this);
          }
        }
        if (change.wasRemoved()) {
          for (GConnector connector : change.getRemoved()) {
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
      ListChangeListener<GConnector> connectorsListener) {
    removeListeners(); // if there are any.
    // Store strong references
    this.typeListener = typeListener;
    this.connectorsListener = connectorsListener;

    // Attach listeners using WeakListeners
    //typeProperty().addListener(typeListener);
    ((ObservableList<GConnector>)getConnectors()).addListener(connectorsListener);
  }

  public void removeListeners() {
    if ( typeListener != null ) { // assume that all are null or none are null...
      //typeProperty().removeListener(typeListener);
      ((ObservableList<GConnector>)getConnectors()).removeListener(connectorsListener);
    }
  }

   // Connectors
  public List<GConnector> getConnectors() {
    return connectors;
  }

  // Methods to add and remove connectors
  public void addConnector(GConnector connector) {
    connectors.add(connector);
  }

  public void removeConnector(GConnector connector) {
    connectors.remove(connector);
  }

  @Override
  public String toString() {
    return "GNode [id=" + getId() + ", connectors=" + connectors.size() + "]";
  }
  
}
