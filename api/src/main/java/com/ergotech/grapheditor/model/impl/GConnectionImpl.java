package com.ergotech.grapheditor.model.impl;

import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

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
public class GConnectionImpl extends SelectableType implements GConnection {

  private final ObjectProperty<GConnectorPort> source = new SimpleObjectProperty<>(this, "source");

  private final ObjectProperty<GConnectorPort> target = new SimpleObjectProperty<>(this, "target");

  //private final ObservableList<GJoint> joints = FXCollections.observableArrayList();

  private final BooleanProperty bidirectional = new SimpleBooleanProperty(this, "bidirectional", false);

  // Listener references
  private ChangeListener<GConnectorPort> sourceListener;

  private ChangeListener<GConnectorPort> targetListener;

  private ChangeListener<String> typeListener;

  private ChangeListener<Boolean> bidirectionalListener;


  public GConnectionImpl() {
    super();
  }

  // Listener management methods
  public void addListeners(ChangeListener<GConnectorPort> sourceListener, ChangeListener<GConnectorPort> targetListener,
      ChangeListener<String> typeListener, ChangeListener<Boolean> bidirectionalListener) {
    removeListeners(); // if there are any.
    // Store strong references
    this.sourceListener = sourceListener;
    this.targetListener = targetListener;
    this.typeListener = typeListener;
    this.bidirectionalListener = bidirectionalListener;

    // Attach listeners using WeakListeners
    sourceProperty().addListener(sourceListener);
    targetProperty().addListener(targetListener);
    //typeProperty().addListener(typeListener);
    bidirectionalProperty().addListener(bidirectionalListener);
  }

  public void removeListeners() {
    if ( sourceListener != null ) { // assume that all are null or none are null...
      sourceProperty().removeListener(sourceListener);
      targetProperty().removeListener(targetListener);
      //typeProperty().removeListener(typeListener);
      bidirectionalProperty().removeListener(bidirectionalListener);
    }
  }

  /**
   * Gets the source of the connection. The source is the starting node for the connection.
   *
   * @return the source of the connection as an ObjectProperty.
   */
  public ObjectProperty<GConnectorPort> sourceProperty() {
    return source;
  }

  /**
   * Returns the value of the 'Source' attribute.
   *
   * @return the source node of the connection.
   */
  @Override
  public GConnectorPort getSourcePort() {
    return source.get();
  }

  /**
   * Sets the value of the 'Source' attribute.
   *
   * @param source
   *          the source node to set.
   */
  @Override
  public void setSourcePort(GConnectorPort source) {
    this.source.set(source);
  }

  /**
   * Gets the target of the connection. The target is the ending node for the connection.
   *
   * @return the target of the connection as an ObjectProperty.
   */
  public ObjectProperty<GConnectorPort> targetProperty() {
    return target;
  }

  /**
   * Returns the value of the 'Target' attribute.
   *
   * @return the target node of the connection.
   */
  @Override
  public GConnectorPort getTargetPort() {
    return target.get();
  }

  /**
   * Sets the value of the 'Target' attribute.
   *
   * @param target
   *          the target node to set.
   */
  @Override
  public void setTargetPort(GConnectorPort target) {
    this.target.set(target);
  }

//  /**
//   * Gets the list of joints (control points) along the connection. Joints can represent intermediate control points
//   * that define the path of the connection.
//   *
//   * @return the list of joints as an ObservableList.
//   */
//  @Override
//  public List<GJoint> getJoints() {
//    return joints;
//  }
//
  //  /**
  //   * Adds a joint to the connection. This method also sets the connection property of the joint to this connection.
  //   *
  //   * @param joint
  //   *          the joint to add.
  //   */
  //  public void addJoint(GJoint joint) {
  //    if (!joints.contains(joint)) {
  //      joints.add(joint);
  //      joint.setConnection(this);
  //    }
  //  }
  //
  //  /**
  //   * Removes a joint from the connection. This method also clears the connection property of the joint.
  //   *
  //   * @param joint
  //   *          the joint to remove.
  //   */
  //  public void removeJoint(GJoint joint) {
  //    if (joints.contains(joint)) {
  //      joints.remove(joint);
  //      joint.setConnection(null);
  //    }
  //  }
  //
  /**
   * Gets the bidirectional property. A bidirectional connection means that the relationship can flow in both
   * directions.
   *
   * @return the bidirectional property as a BooleanProperty.
   */
  public BooleanProperty bidirectionalProperty() {
    return bidirectional;
  }

  /**
   * Returns whether the connection is bidirectional.
   *
   * @return true if the connection is bidirectional, false otherwise.
   */
  @Override
 public boolean isBidirectional() {
    return bidirectional.get();
  }

  /**
   * Sets whether the connection is bidirectional.
   *
   * @param bidirectional
   *          true if the connection is bidirectional, false otherwise.
   */
  @Override
 public void setBidirectional(boolean bidirectional) {
    this.bidirectional.set(bidirectional);
  }

  @Override
  public String toString() {
    return "GConnection [id=" + getId() + ", source=" + source.getName() + ", target=" + target.getName() 
    + ", bidirectional=" + isBidirectional() + "]";
  }

}
