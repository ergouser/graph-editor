/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnector;
import com.ergotech.grapheditor.model.GConnector.Direction;

import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GConnectorStyle;
import io.github.eckig.grapheditor.core.connectors.DefaultConnectorTypes;
import io.github.eckig.grapheditor.core.skins.defaults.utils.AnimatedColor;
import io.github.eckig.grapheditor.core.skins.defaults.utils.ColorAnimationUtils;
import javafx.css.PseudoClass;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

/**
 * The default connector skin.
 *
 * <p>
 * A connector that uses this skin must have one of the 8 types defined in {@link DefaultConnectorTypes}. If the
 * connector does not have one of these types, it will be set to <b>left-input</b>.
 * </p>
 */
public class DefaultConnectorSkin extends GConnectorSkin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectorSkin.class);

    private static final String STYLE_CLASS_BASE = "default-connector";

    private static final PseudoClass PSEUDO_CLASS_ALLOWED = PseudoClass.getPseudoClass("allowed");
    private static final PseudoClass PSEUDO_CLASS_FORBIDDEN = PseudoClass.getPseudoClass("forbidden");

    private static final String ALLOWED = "-animated-color-allowed";
    private static final String FORBIDDEN = "-animated-color-forbidden";

    private static final double SIZE = 25;

    private final Pane root = new Pane();
    private final Polygon polygon = new Polygon();

    private final AnimatedColor animatedColorAllowed;
    private final AnimatedColor animatedColorForbidden;

    /**
     * Creates a new default connector skin instance.
     *
     * @param connector the {@link GConnector} the skin is being created for
     */
    public DefaultConnectorSkin(final GConnector connector) {

        super(connector);

        performChecks();

        root.setManaged(false);
        root.resize(SIZE, SIZE);
        root.setPickOnBounds(false);

        polygon.setManaged(false);
        
        // by default, if the connector is an output, it's on the right side, an input on the left.
        switch (connector.getDirection()) {
          case INPUT: 
            setSide(Side.LEFT);
            break;
          case OUTPUT:
          case BIDIRECTIONAL:
            setSide(Side.RIGHT);
            break;
        }
        
        String connectorStyleClass = getSide().name() + "-" + connector.getDirection().name();
        polygon.getStyleClass().addAll(STYLE_CLASS_BASE, connectorStyleClass);

        drawTriangleConnector(getSide(), connector.getDirection(), polygon);

        root.getChildren().add(polygon);

        animatedColorAllowed = new AnimatedColor(ALLOWED, Color.WHITE, Color.MEDIUMSEAGREEN, Duration.millis(500));
        animatedColorForbidden = new AnimatedColor(FORBIDDEN, Color.WHITE, Color.TOMATO, Duration.millis(500));
    }

    @Override
    public Node getRoot() {
        return root;
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
    public void applyStyle(final GConnectorStyle style) {

        switch (style) {

        case DEFAULT:
            ColorAnimationUtils.removeAnimation(polygon);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            break;

        case DRAG_OVER_ALLOWED:
            ColorAnimationUtils.animateColor(polygon, animatedColorAllowed);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, true);
            break;

        case DRAG_OVER_FORBIDDEN:
            ColorAnimationUtils.animateColor(polygon, animatedColorForbidden);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, true);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            break;
        }
    }

    /**
     * Draws the given polygon to have a triangular shape.
     *
     * @param direction the connector type
     * @param polygon the polygon to be drawn
     */
    public static void drawTriangleConnector(final Side side, final Direction direction, final Polygon polygon) {
      switch (side) {
          case TOP:
              // TOP: INPUT was vertical(false), OUTPUT was vertical(true)
              if (direction == Direction.INPUT) {
                  drawVertical(false, polygon);
              } else if (direction == Direction.OUTPUT) {
                  drawVertical(true, polygon);
              } else {
                  // Possibly handle BIDIRECTIONAL if needed
              }
              break;

          case BOTTOM:
              // BOTTOM: INPUT was vertical(true), OUTPUT was vertical(false)
              if (direction == Direction.INPUT) {
                  drawVertical(true, polygon);
              } else if (direction == Direction.OUTPUT) {
                  drawVertical(false, polygon);
              } else {
                  // Possibly handle BIDIRECTIONAL
              }
              break;

          case RIGHT:
              // RIGHT: INPUT was horizontal(false), OUTPUT was horizontal(true)
              if (direction == Direction.INPUT) {
                  drawHorizontal(false, polygon);
              } else if (direction == Direction.OUTPUT) {
                  drawHorizontal(true, polygon);
              } else {
                  // Possibly handle BIDIRECTIONAL
              }
              break;

          case LEFT:
              // LEFT: INPUT was horizontal(true), OUTPUT was horizontal(false)
              if (direction == Direction.INPUT) {
                  drawHorizontal(true, polygon);
              } else if (direction == Direction.OUTPUT) {
                  drawHorizontal(false, polygon);
              } else {
                  // Possibly handle BIDIRECTIONAL
              }
              break;

          default:
              // Handle OTHER or unexpected sides
              break;
      }
  }

    /**
     * Draws the polygon for a horizontal orientation, pointing right or left.
     *
     * @param pointingRight {@code true} to point right, {@code false} to point left
     * @param polygon the polygon to be drawn
     */
    private static void drawHorizontal(final boolean pointingRight, final Polygon polygon) {

        if (pointingRight) {
            polygon.getPoints().addAll(new Double[] { 0D, 0D, SIZE, SIZE / 2, 0D, SIZE });
        } else {
            polygon.getPoints().addAll(new Double[] { SIZE, 0D, SIZE, SIZE, 0D, SIZE / 2 });
        }
    }

    /**
     * Draws the polygon for a vertical orientation, pointing up or down.
     *
     * @param pointingUp {@code true} to point up, {@code false} to point down
     * @param polygon the polygon to be drawn
     */
    private static void drawVertical(final boolean pointingUp, final Polygon polygon) {

        if (pointingUp) {
            polygon.getPoints().addAll(new Double[] { SIZE / 2, 0D, SIZE, SIZE, 0D, SIZE });
        } else {
            polygon.getPoints().addAll(new Double[] { 0D, 0D, SIZE, 0D, SIZE / 2, SIZE });
        }
    }

    /**
     * Checks that the connector has the correct values to be displayed using this skin.
     */
    private void performChecks() {
      // the "types" (direction and side) are no enums so cannot be invalid
      // if (!DefaultConnectorTypes.isValid(getItem().getType()))
      // {
      // LOGGER.error("Connector type '{}' not recognized, setting to 'left-input'.", getItem().getType());
      // getItem().setType(DefaultConnectorTypes.LEFT_INPUT);
      // }
    }

    @Override
    protected void selectionChanged(boolean isSelected) {
        // Not implemented
    }

    @Override
    public String toString() {
      return "DefaultConnectorSkin [polygon=" + polygon + ", isSelected()=" + isSelected() + ", getItem()=" + getItem()
          + "]";
    }
    
}
