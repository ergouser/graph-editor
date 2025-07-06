/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults;

import com.ergotech.grapheditor.model.GJoint;

import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.utils.DraggableBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;

/**
 * The default joint skin.
 *
 * <p>
 * Pretty much just a {@link DraggableBox} with some hover and pressed effects.
 * </p>
 */
public class DefaultJointSkin extends GJointSkin {

  // define the pseudo-class for temporary.
  private static final PseudoClass TEMPORARY_PSEUDO_CLASS = PseudoClass.getPseudoClass("temporary");

  private static final String STYLE_CLASS = "default-joint";

  private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

  private static final double SIZE = 12;

  private static final Point2D SNAP_OFFSET = new Point2D(-5, -5);

  /** Sets the joint to be a temporary joint. */
  protected final BooleanProperty temporaryProperty = new SimpleBooleanProperty(this, "temporary", false);

  /**
   * Creates a new default join instance.
   *
   * @param joint
   *          the {@link GJoint} the skin is being created for
   */
  public DefaultJointSkin(final GJoint joint) {
    super(joint);

    // whenever joint.temporary changes, flip the pseudo-class
    temporaryProperty.addListener((obs, wasTemp, isNowTemp) -> {
      getRoot().pseudoClassStateChanged(TEMPORARY_PSEUDO_CLASS, isNowTemp);
    });

    getRoot().resize(SIZE, SIZE);
    getRoot().getStyleClass().setAll(STYLE_CLASS);

    getRoot().setPickOnBounds(false);
    getRoot().setSnapToGridOffset(SNAP_OFFSET);
  }

  @Override
  protected void selectionChanged(boolean isSelected) {
    getRoot().pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, isSelected);
    if (isSelected) {
      getRoot().toFront();
    }
  }

  @Override
  public double getWidth() {
    return SIZE;
  }

  @Override
  public double getHeight() {
    return SIZE;
  }

  @Override
  public String toString() {
    return "DefaultJointSkin [getX()=" + getX() + ", getY()=" + getY() + ", isSelected()=" + isSelected() + ", isTemporary()=" + isTemporary()
        + ", getItem()=" + getItem() + "]";
  }

  /** Return true if the skin is temporary. */
  public boolean isTemporary () {
    return temporaryProperty.get();
  }
  
  /**Set the skin to be temporary. */
  public void setTemporary (boolean temporary ) {
    temporaryProperty.set(temporary);
  }
}
