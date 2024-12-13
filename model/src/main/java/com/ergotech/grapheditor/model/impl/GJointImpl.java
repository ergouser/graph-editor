package com.ergotech.grapheditor.model.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.ergotech.grapheditor.model.GJoint;

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

  //private final ObjectProperty<GConnection> connection = new SimpleObjectProperty<>(this, "connection");
  private static AtomicInteger uniqueId = new AtomicInteger(100);

  public GJointImpl() {
    super();
    setId("Joint" + uniqueId.incrementAndGet());
  }

//  /**
//   * Gets the connection to which this joint belongs.
//   *
//   * @return the connection as an ObjectProperty.
//   */
//  public ObjectProperty<GConnection> connectionProperty() {
//    return connection;
//  }
//
//  /**
//   * Returns the connection to which this joint belongs.
//   *
//   * @return the GConnection this joint is part of.
//   */
//  @Override
//  public GConnection getConnection() {
//    return connection.get();
//  }
//
//  /**
//   * Sets the connection to which this joint belongs.
//   *
//   * @param connection
//   *          the GConnection to set.
//   */
//  @Override
//  public void setConnection(GConnection connection) {
//    this.connection.set(connection);
//  }

  @Override
  public String toString() {
    //return "GJoint [id=" + getId() + ", connection=" + getConnection() + "]";
    return "GJoint [id=" + getId() + "]";
  }

}
