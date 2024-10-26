package com.ergotech.grapheditor.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class GNode extends SelectableType {
  // Properties
  private final StringProperty id = new SimpleStringProperty(this, "id");

  private final DoubleProperty x = new SimpleDoubleProperty(this, "x", 0);

  private final DoubleProperty y = new SimpleDoubleProperty(this, "y", 0);

  private final DoubleProperty width = new SimpleDoubleProperty(this, "width", 151);

  private final DoubleProperty height = new SimpleDoubleProperty(this, "height", 101);

  // Observable List for connectors
  private final ObservableList<GConnector> connectors = FXCollections.observableArrayList();

  // Listener references
  private ChangeListener<Number> xListener;

  private ChangeListener<Number> yListener;

  private ChangeListener<Number> widthListener;

  private ChangeListener<Number> heightListener;

  private ChangeListener<String> typeListener;

  private ListChangeListener<GConnector> connectorsListener;

  // Constructor
  public GNode() {
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
  public void addListeners(ChangeListener<Number> xListener, ChangeListener<Number> yListener,
      ChangeListener<Number> widthListener, ChangeListener<Number> heightListener, ChangeListener<String> typeListener,
      ListChangeListener<GConnector> connectorsListener) {
    removeListeners(); // if there are any.
    // Store strong references
    this.xListener = xListener;
    this.yListener = yListener;
    this.widthListener = widthListener;
    this.heightListener = heightListener;
    this.typeListener = typeListener;
    this.connectorsListener = connectorsListener;

    // Attach listeners using WeakListeners
    xProperty().addListener(xListener);
    yProperty().addListener(yListener);
    widthProperty().addListener(widthListener);
    heightProperty().addListener(heightListener);
    typeProperty().addListener(typeListener);
    getConnectors().addListener(connectorsListener);
  }

  public void removeListeners() {
    if ( xListener != null ) { // assume that all are null or none are null...
      xProperty().removeListener(xListener);
      yProperty().removeListener(yListener);
      widthProperty().removeListener(widthListener);
      heightProperty().removeListener(heightListener);
      typeProperty().removeListener(typeListener);
      getConnectors().removeListener(connectorsListener);
    }
  }

  // ID Property
  public StringProperty idProperty() {
    return id;
  }

  public String getId() {
    return id.get();
  }

  public void setId(String value) {
    id.set(value);
  }

  // X Property
  public DoubleProperty xProperty() {
    return x;
  }

  public double getX() {
    return x.get();
  }

  public void setX(double value) {
    x.set(value);
  }

  // Y Property
  public DoubleProperty yProperty() {
    return y;
  }

  public double getY() {
    return y.get();
  }

  public void setY(double value) {
    y.set(value);
  }

  // Width Property
  public DoubleProperty widthProperty() {
    return width;
  }

  public double getWidth() {
    return width.get();
  }

  public void setWidth(double value) {
    width.set(value);
  }

  // Height Property
  public DoubleProperty heightProperty() {
    return height;
  }

  public double getHeight() {
    return height.get();
  }

  public void setHeight(double value) {
    height.set(value);
  }

  // Connectors
  public ObservableList<GConnector> getConnectors() {
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
    return "GNode [id=" + id.get() + ", x=" + x.get() + ", y=" + y.get() + ", width=" + width.get() + ", height=" + height.get() + ", connectors="
        + connectors.size() + "]";
  }
  
}
