package com.ergotech.grapheditor.model.impl;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GJoint;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

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
 * </ul>
 */
public class GJointImpl extends SelectableType implements GJoint {

  private final ObjectProperty<GConnection> connection = new SimpleObjectProperty<>(this, "connection");

  public GJointImpl() {
    super();
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
  @Override
  public void setConnection(GConnection connection) {
    this.connection.set(connection);
  }

  @Override
  public String toString() {
    return "GJoint [id=" + getId() + ", connection=" + getConnection() + "]";
  }

}
