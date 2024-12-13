/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import com.ergotech.grapheditor.model.GJoint;

import io.github.eckig.grapheditor.utils.DraggableBox;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The skin class for a {@link GJoint}. Responsible for visualizing joints in the graph editor.
 *
 * <p>
 * A custom joint skin must extend this class. It <b>must</b> also provide a constructor taking exactly one
 * {@link GJoint} parameter.
 * </p>
 *
 * <p>
 * The root JavaFX node of this skin is a {@link DraggableBox}.
 * </p>
 */
public abstract class GJointSkin extends GSkin<GJoint> {

  protected final DoubleProperty x = new SimpleDoubleProperty(this, "x", 0);

  protected final DoubleProperty y = new SimpleDoubleProperty(this, "y", 0);

  protected final DoubleProperty width = new SimpleDoubleProperty(this, "width", 12);

  protected final DoubleProperty height = new SimpleDoubleProperty(this, "height", 12);

  protected final StringProperty type = new SimpleStringProperty(this, "type");

  private final DraggableBox root = new DraggableBox(EditorElement.JOINT) {

    @Override
    public final void positionMoved() {
      super.positionMoved();
      GJointSkin.this.impl_positionMoved();
    }
  };

  /**
   * Creates a new {@link GJointSkin}.
   *
   * @param joint
   *          the {@link GJoint} represented by the skin
   */
  public GJointSkin(final GJoint joint) {
    super(joint);

    // add listeners to the properties so that that root (DraggableBox) will be updated
    // whenever any of the properties change.
    x.addListener((observable, oldValue, newValue) -> updateLayout());
    y.addListener((observable, oldValue, newValue) -> updateLayout());
    width.addListener((observable, oldValue, newValue) -> updateLayout());
    height.addListener((observable, oldValue, newValue) -> updateLayout());

    updateLayout();

  }

  /**
   * Gets the root JavaFX node of the skin.
   *
   * @return a {@link DraggableBox} containing the skin's root JavaFX node
   */
  @Override
  public DraggableBox getRoot() {
    return root;
  }

  /**
   * Initializes the joint skin.
   *
   * <p>
   * The skin's layout values are loaded from the {@link GJoint} at this point.
   * </p>
   */
  public void initialize() {
    // managed by the listener - updateLayout();
  }

  /**
   * Initializes the joint skin.
   *
   * <p>
   * The skin's layout values are loaded from the {@link GJoint} at this point.
   * </p>
   */
  public void updateLayout() {
    getRoot().setLayoutX(getX() - getWidth() / 2);
    getRoot().setLayoutY(getY() - getHeight() / 2);
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

  /**
   * Returns the {@code DoubleProperty} representing the width. This property can be used to observe changes to the
   * width or bind it to another property.
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
   * @param value
   *          the new width value.
   */
  public void setWidth(double value) {
    width.set(value);
  }

  /**
   * Returns the {@code DoubleProperty} representing the height. This property can be used to observe changes to the
   * height or bind it to another property.
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
   * @param value
   *          the new height value.
   */
  public void setHeight(double value) {
    height.set(value);
  }


  /**
   * Gets the type of the connection.
   *
   * @return the type of the connection as a StringProperty.
   */
  public StringProperty typeProperty() {
    return type;
  }

  /**
   * Returns the value of the 'Type' attribute.
   *
   * @return the type of the connection.
   */
  public String getType() {
    return type.get();
  }

  /**
   * Sets the value of the 'Type' attribute.
   *
   * @param value the new value of the type.
   */
  public void setType(String value) {
    type.set(value);
  }

  @Override
  public String toString() {
    return "GJointSkin [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", item=" + item + "]";
  }

}
