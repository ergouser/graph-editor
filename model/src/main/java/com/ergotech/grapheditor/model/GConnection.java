package com.ergotech.grapheditor.model;

import java.util.List;

/**
 * A representation of the model object '<em><b>GConnection</b></em>'.
 * 
 * A `GConnection` represents a connection between two nodes (or other elements) in a graphical editor. It contains a
 * list of `GJoint` objects, which represent intermediate control points along the connection.
 * 
 * <p>
 * Features of the `GConnection` class:
 * </p>
 * <ul>
 * <li>{@link #idProperty()} - The unique identifier of the connection</li>
 * <li>{@link #typeProperty()} - The type of the connection</li>
 * <li>{@link #sourceProperty()} - The source of the connection</li>
 * <li>{@link #targetProperty()} - The target of the connection</li>
 * <li>{@link #jointsProperty()} - The list of joints (control points) along the connection</li>
 * <li>{@link #bidirectionalProperty()} - Indicates whether the connection is bidirectional</li>
 * </ul>
 */
public interface GConnection extends Selectable {


  /**
   * Returns the value of the 'Source' attribute.
   *
   * @return the source node of the connection.
   */
  public GConnectorPort getSourcePort();

  /**
   * Sets the value of the 'Source' attribute.
   *
   * @param source
   *          the source node to set.
   */
  public void setSourcePort(GConnectorPort source);

  /**
   * Returns the value of the 'Target' attribute.
   *
   * @return the target node of the connection.
   */
  public GConnectorPort getTargetPort();

  /**
   * Sets the value of the 'Target' attribute.
   *
   * @param target
   *          the target node to set.
   */
  public void setTargetPort(GConnectorPort target);

  /**
   * Gets the list of joints (control points) along the connection. Joints can represent intermediate control points
   * that define the path of the connection.  IS THIS AN ATTRIBUTE OF THE CONNECTION OR THE SKIN?????
   *
   * @return the list of joints as an ObservableList.
   */
  public List<? extends GJoint> getJoints();

  //  /**
  //   * Adds a joint to the connection. This method also sets the connection property of the joint to this connection.
  //   *
  //   * @param joint
  //   *          the joint to add.
  //   */
  //  public void addJoint(GJoint joint);
  //
  //  /**
  //   * Removes a joint from the connection. This method also clears the connection property of the joint.
  //   *
  //   * @param joint
  //   *          the joint to remove.
  //   */
  //  public void removeJoint(GJoint joint);
  //
  /**
   * Returns whether the connection is bidirectional.
   *
   * @return true if the connection is bidirectional, false otherwise.
   */
  public boolean isBidirectional();

  /**
   * Sets whether the connection is bidirectional.
   *
   * @param bidirectional
   *          true if the connection is bidirectional, false otherwise.
   */
  public void setBidirectional(boolean bidirectional);

}
