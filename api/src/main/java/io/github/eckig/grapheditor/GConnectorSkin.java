/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.impl.GConnectorPortImpl;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;

/**
 * The skin class for a {@link GConnectorPortImpl}. Responsible for visualizing connectors in the graph editor.
 *
 * <p>
 * A custom connector skin must extend this class. It <b>must</b> also provide a constructor taking exactly one
 * {@link GConnectorPortImpl} parameter.
 * </p>
 *
 * <p>
 * The root JavaFX node must be created by the skin implementation and returned in the {@link #getRoot()} method.
 * </p>
 */
public abstract class GConnectorSkin extends GSkin<GConnectorPort> {

  protected final DoubleProperty x = new SimpleDoubleProperty(this, "x", 0);
  protected final DoubleProperty y = new SimpleDoubleProperty(this, "y", 0);
  protected final DoubleProperty width = new SimpleDoubleProperty(this, "width", 151);
  protected final DoubleProperty height = new SimpleDoubleProperty(this, "height", 101);

  protected final BooleanProperty connectionDetachedOnDrag = new SimpleBooleanProperty(false);
  ObjectProperty<Side> side  = new SimpleObjectProperty<>(this, "side");

  /**
   * Creates a new {@link GConnectorSkin}.
   */
  public GConnectorSkin() {}

  /**
   * Creates a new {@link GConnectorSkin}.
   *
   * @param connector the {@link GConnectorPortImpl} represented by the skin
   */
  public GConnectorSkin(final GConnectorPort connector) {
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

  /**
   * Returns the connection point for this connector skin based on its side.
   * 
   * If the side is:
   * - LEFT: returns center-left
   * - RIGHT: returns center-right
   * - TOP: returns top-center
   * - BOTTOM: returns bottom-center
   *
   * @return the connection point as a {@link Point2D}
   */
  public Point2D getConnectionPoint() {
      // Grab the connector’s rendered bounds
//      Bounds b = getRoot().getBoundsInLocal();
//      double x0 = b.getMinX();
//      double x1 = b.getMaxX();
//      double y0 = b.getMinY();
//      double y1 = b.getMaxY();
//      double midX = (x0 + x1) / 2.0;
//      double midY = (y0 + y1) / 2.0;
//      midY = getHeight();
//      switch (getSide()) {
//        case LEFT:
//          return new Point2D(x0, midY);
//        case RIGHT:
//          return new Point2D(x1, midY);
//        case TOP:
//          return new Point2D(midX, y0);
//        case BOTTOM:
//          return new Point2D(midX, y1);
//        default:
//          // center
//          return new Point2D(midX, midY);
//      }
    double x = getX();
    double y = getY();
    double width = getWidth();
    double height = getHeight();

    switch (getSide()) {
      case LEFT:
        return new Point2D(x, y + height / 2.0);
      case RIGHT:
        return new Point2D(x + width, y + height / 2.0);
      case TOP:
        return new Point2D(x + width / 2.0, y);
      case BOTTOM:
        return new Point2D(x + width / 2.0, y + height);
      default:
        // Default to center if side is null or unknown
        return new Point2D(x + width / 2.0, y + height / 2.0);
    }
  }

  /**
   * Returns the connection point in scene coordinates.
   *
   * This is useful when performing hit tests or drawing connections that span multiple nodes.
   *
   * @return the connection point in scene coordinates as a {@link Point2D}
   */
 // public Point2D getConnectionPointInScene() {
//    Node root = getRoot();
//    if (root == null) {
//      return getConnectionPoint(); // Fallback to local coordinates if root is not yet available
//    }
//    return root.localToScene(getConnectionPoint());
//    Point2D localPt = getConnectionPoint();
//    return getRoot().localToScene(localPt);
//  }
  public Point2D getConnectionPointInScene() {
    // Since getConnectionPoint() is already in **parent** coords,
    // we can convert it up to scene with one call:
    return getRoot().localToScene(getConnectionPoint());
//        .subtract(
//        getRoot().getLayoutX(), getRoot().getLayoutY()));
}
  /**
   * Returns the connection point in screen coordinates.
   *
   * This is useful for placing popups, tooltips, or drag visuals relative to the screen.
   *
   * @return the connection point in screen coordinates as a {@link Point2D}
   */
  public Point2D getConnectionPointInScreen() {
    Node root = getRoot();
    if (root == null) {
      return getConnectionPoint(); // Fallback to local coordinates if root is not yet available
    }
    return root.localToScreen(getConnectionPoint());
  }

  /**
   * Returns the connection point of this connector skin converted to the local coordinates of the specified node.
   *
   * @param targetNode the node to which the connection point should be converted
   * @return the connection point relative to the given node's local coordinate space
   */
  public Point2D getConnectionPointInLocalOf(Node target) {
    return target.sceneToLocal(getConnectionPointInScene());
}
// public Point2D getConnectionPointInLocalOf(Node targetNode) {
//    if (targetNode == null || getRoot() == null) {
//      return getConnectionPoint(); // fallback
//    }
//    return targetNode.sceneToLocal(getConnectionPointInScene());
//  }

}
