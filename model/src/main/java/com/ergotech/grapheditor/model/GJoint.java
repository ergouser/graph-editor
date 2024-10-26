package com.ergotech.grapheditor.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

/**
 * A representation of the model object '<em><b>GJoint</b></em>'.
 * 
 * A `GJoint` represents a point in a connection, typically used in graphical editors for visualizing curves or links
 * between nodes.
 * 
 * <p>
 * Features of the `GJoint` class:
 * </p>
 * <ul>
 * <li>{@link #idProperty()} - The unique identifier of the joint</li>
 * <li>{@link #typeProperty()} - The type of the joint</li>
 * <li>{@link #connectionProperty()} - The connection to which this joint belongs</li>
 * <li>{@link #xProperty()} - The x-coordinate of the joint</li>
 * <li>{@link #yProperty()} - The y-coordinate of the joint</li>
 * </ul>
 */
public class GJoint extends SelectableType {

  // Properties
  private final StringProperty id = new SimpleStringProperty(this, "id");

  private final ObjectProperty<GConnection> connection = new SimpleObjectProperty<>(this, "connection");

  private final DoubleProperty x = new SimpleDoubleProperty(this, "x", 0);

  private final DoubleProperty y = new SimpleDoubleProperty(this, "y", 0);

  // Listener references
  private ChangeListener<Number> xListener;

  private ChangeListener<Number> yListener;

  public GJoint() {
    super();
  }

  // Listener management method
  public void addListeners(ChangeListener<Number> xListener, ChangeListener<Number> yListener) {
    removeListeners(); // if there are any.
    // Store strong references
    this.xListener = xListener;
    this.yListener = yListener;

    // Attach listeners
    xProperty().addListener(xListener);
    yProperty().addListener(yListener);
  }

  public void removeListeners () {
    // Remove listeners 
    if ( xListener != null ) { // assume that all are null or none are null...
      xProperty().removeListener(xListener);
      yProperty().removeListener(yListener);
    }
  }

  /**
   * Gets the unique identifier of the joint.
   *
   * @return the id of the joint as a StringProperty.
   */
  public StringProperty idProperty() {
    return id;
  }

  /**
   * Returns the value of the 'Id' attribute.
   *
   * @return the id of the joint.
   */
  public String getId() {
    return id.get();
  }

  /**
   * Sets the value of the 'Id' attribute.
   *
   * @param value
   *          the new value of the id.
   */
  public void setId(String value) {
    id.set(value);
  }

  /**
   * Gets the connection to which this joint belongs.
   *
   * @return the connection as an ObjectProperty.
   */
  public ObjectProperty<GConnection> connectionProperty() {
    return connection;
  }

  /**
   * Returns the connection to which this joint belongs.
   *
   * @return the GConnection this joint is part of.
   */
  public GConnection getConnection() {
    return connection.get();
  }

  /**
   * Sets the connection to which this joint belongs.
   *
   * @param connection
   *          the GConnection to set.
   */
  public void setConnection(GConnection connection) {
    this.connection.set(connection);
  }

  /**
   * Gets the x-coordinate of the joint.
   *
   * @return the x-coordinate as a DoubleProperty.
   */
  public DoubleProperty xProperty() {
    return x;
  }

  /**
   * Returns the x-coordinate of the joint.
   *
   * @return the x-coordinate of the joint.
   */
  public double getX() {
    return x.get();
  }

  /**
   * Sets the x-coordinate of the joint.
   *
   * @param value
   *          the new value of the x-coordinate.
   */
  public void setX(double value) {
    x.set(value);
  }

  /**
   * Gets the y-coordinate of the joint.
   *
   * @return the y-coordinate as a DoubleProperty.
   */
  public DoubleProperty yProperty() {
    return y;
  }

  /**
   * Returns the y-coordinate of the joint.
   *
   * @return the y-coordinate of the joint.
   */
  public double getY() {
    return y.get();
  }

  /**
   * Sets the y-coordinate of the joint.
   *
   * @param value
   *          the new value of the y-coordinate.
   */
  public void setY(double value) {
    y.set(value);
  }

  @Override
  public String toString() {
    return "GJoint [id=" + id.get() + ", connection=" + connection.get() + ", x=" + x.get() + ", y=" + y.get() + "]";
  }
  
}
