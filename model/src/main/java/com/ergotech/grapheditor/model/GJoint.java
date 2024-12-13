package com.ergotech.grapheditor.model;

/**
 * A representation of the model object '<em><b>GJoint</b></em>'.
 * 
 * A `GJoint` represents a point in a connection, typically used in graphical editors for visualizing curves or links
 * between nodes.  As a visual attribute of the connection it is maintained as an attribute of the GConnectionSkin
 * not the GConnector.
 * 
 * The GJoint exists to support the GJointSkin.
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
public interface GJoint extends Selectable {

//  /**
//   * Returns the connection to which this joint belongs.
//   *
//   * @return the GConnection this joint is part of.
//   */
//  public GConnection getConnection();
//  /**
//   * Sets the connection to which this joint belongs.
//   *
//   * @param connection
//   *          the GConnection to set.
//   */
//  public void setConnection(GConnection connection);

  
}
