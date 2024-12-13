/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import com.ergotech.grapheditor.model.GConnector;
import com.ergotech.grapheditor.model.impl.GConnectorImpl;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Side;

/**
 * The skin class for a {@link GConnectorImpl}. Responsible for visualizing connectors in the graph editor.
 *
 * <p>
 * A custom connector skin must extend this class. It <b>must</b> also provide a constructor taking exactly one
 * {@link GConnectorImpl} parameter.
 * </p>
 *
 * <p>
 * The root JavaFX node must be created by the skin implementation and returned in the {@link #getRoot()} method.
 * </p>
 */
public abstract class GConnectorSkin extends GSkin<GConnector> {

  protected final DoubleProperty x = new SimpleDoubleProperty(this, "x", 0);
  protected final DoubleProperty y = new SimpleDoubleProperty(this, "y", 0);
  protected final DoubleProperty width = new SimpleDoubleProperty(this, "width", 151);
  protected final DoubleProperty height = new SimpleDoubleProperty(this, "height", 101);

  protected final BooleanProperty connectionDetachedOnDrag = new SimpleBooleanProperty(false);
  ObjectProperty<Side> side  = new SimpleObjectProperty<>(this, "side");

  /**
   * Creates a new {@link GConnectorSkin}.
   *
   * @param connector the {@link GConnectorImpl} represented by the skin
   */
  public GConnectorSkin(final GConnector connector) {
    super(connector);
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
   * @param value the new value of the x-coordinate.
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
   * @param value the new value of the y-coordinate.
   */
  public void setY(double value) {
    y.set(value);
  }

  /**
   * Returns the {@code DoubleProperty} representing the width.
   * This property can be used to observe changes to the width or bind it to another property.
   *
   * @return the {@code DoubleProperty} for the width.
   */
  public DoubleProperty widthProperty() {
    return width;
  }

  /**
   * Gets the current value of the width.
   *
   * @return the current width value.
   */
  public double getWidth() {
    return width.get();
  }

  /**
   * Sets the value of the width.
   *
   * @param value the new width value.
   */
  public void setWidth(double value) {
    width.set(value);
  }

  /**
   * Returns the {@code DoubleProperty} representing the height.
   * This property can be used to observe changes to the height or bind it to another property.
   *
   * @return the {@code DoubleProperty} for the height.
   */
  public DoubleProperty heightProperty() {
    return height;
  }

  /**
   * Gets the current value of the height.
   *
   * @return the current height value.
   */
  public double getHeight() {
    return height.get();
  }

  /**
   * Sets the value of the height.
   *
   * @param value the new height value.
   */
  public void setHeight(double value) {
    height.set(value);
  }

  /**
   * Returns the property that indicates whether connections are detached when the connector is dragged.
   *
   * @return property maintaining connection detached on drag value
   */
  public BooleanProperty connectionDetachedOnDragProperty() {
    return connectionDetachedOnDrag;
  }
  
  /**
   * Applys the specified style to the connector.
   *
   * <p>
   * This is called by the library during various mouse events. For example when a connector is dragged over another
   * connector in an attempt to create a new connection.
   * </p>
   *
   * @param style the {@link GConnectorStyle} to apply
   */
  public abstract void applyStyle(GConnectorStyle style);

  /**
   * Returns whether connections are detached when the connector is dragged.
   *
   * @return true if connections are detached on drag, false otherwise.
   */
  public boolean isConnectionDetachedOnDrag() {
    return connectionDetachedOnDrag.get(); 
  }
  /**
   * Sets whether connections are detached when the connector is dragged.
   *
   * @param value true to detach connections on drag, false otherwise.
   */
  public void setConnectionDetachedOnDrag(boolean connectionDetachedOnDrag) {
    this.connectionDetachedOnDrag.set(connectionDetachedOnDrag);
  }

  /**
   * Gets the side property of this skin.
   *
   * @return the side property of the skin.
   */
  public ObjectProperty<Side> sideProperty() {
    return side;
  }

  /**
   * Gets the side property of this skin.
   * This is a convenience method for accessing the value of the side property.
   *
   * @return the side of the skin.
   */
  public Side getSide() {
    return sideProperty().get();
  }

  /**
   * Sets the side of this skin.
   * This is a convenience method for setting the value of the side property.
   *
   * @param side the new side of the skin.
   */
  public void setSide(Side side) {
    sideProperty().set(side);
  }


}
