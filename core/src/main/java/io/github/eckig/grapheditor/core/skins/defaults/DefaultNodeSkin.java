/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnector;
import com.ergotech.grapheditor.model.GConnector.Direction;
import com.ergotech.grapheditor.model.GNode;

import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.core.connectors.DefaultConnectorTypes;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import io.github.eckig.grapheditor.utils.ResizableBox;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

/**
 * The default node skin. Uses a {@link ResizableBox}.
 *
 * <p>
 * If a node uses this skin its connectors must have one of the 8 types defined in {@link DefaultConnectorTypes}. If a
 * connector does not have one of these types, it will be set to <b>left-input</b>.
 * </p>
 *
 * <p>
 * Connectors are evenly spaced along the sides of the node according to their type.
 * </p>
 */
public class DefaultNodeSkin extends GNodeSkin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNodeSkin.class);

    private static final String STYLE_CLASS_BORDER = "default-node-border";
    private static final String STYLE_CLASS_BACKGROUND = "default-node-background";
    private static final String STYLE_CLASS_SELECTION_HALO = "default-node-selection-halo";

    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    private static final double HALO_OFFSET = 5;
    private static final double HALO_CORNER_SIZE = 10;

    private static final double MINOR_POSITIVE_OFFSET = 2;
    private static final double MINOR_NEGATIVE_OFFSET = -3;

    private static final double MIN_WIDTH = 41;
    private static final double MIN_HEIGHT = 41;

    private final Rectangle selectionHalo = new Rectangle();

    private final List<GConnectorSkin> connectorSkins = new ArrayList<>();
    
    // Border and background are separated into 2 rectangles so they can have different effects applied to them.
    private final Rectangle border = new Rectangle();
    private final Rectangle background = new Rectangle();

    /**
     * Creates a new default node skin instance.
     *
     * @param node the {@link GNode} the skin is being created for
     */
    public DefaultNodeSkin(final GNode node) {

        super(node);

        performChecks();

        background.widthProperty().bind(border.widthProperty().subtract(border.strokeWidthProperty().multiply(2)));
        background.heightProperty().bind(border.heightProperty().subtract(border.strokeWidthProperty().multiply(2)));

        border.widthProperty().bind(getRoot().widthProperty());
        border.heightProperty().bind(getRoot().heightProperty());

        border.getStyleClass().setAll(STYLE_CLASS_BORDER);
        background.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);

        getRoot().getChildren().addAll(border, background);
        getRoot().setMinSize(MIN_WIDTH, MIN_HEIGHT);

        background.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::filterMouseDragged);

        addSelectionHalo();
    }

    @Override
    public void setConnectorSkins(final List<GConnectorSkin> newConnectorSkins) {

        removeAllConnectors();

        connectorSkins.clear();

        if (newConnectorSkins != null) {
            for (final GConnectorSkin connectorSkin : newConnectorSkins) {
                connectorSkins.add(connectorSkin);

                // Add the connectorSkin's root to the scene graph
                getRoot().getChildren().add(connectorSkin.getRoot());
            }
        }

        layoutConnectors();
    }

    @Override
    public void layoutConnectors() {
        layoutAllConnectors();
        layoutSelectionHalo();
    }

    @Override
    public Point2D getConnectorPosition(final GConnectorSkin connectorSkin) {

        final Node connectorRoot = connectorSkin.getRoot();

        final Side side = connectorSkin.getSide();

        // The following logic is required because the connectors are offset slightly from the node edges.
        final double x, y;
        if (side.equals(Side.LEFT)) {
            x = 0;
            y = connectorRoot.getLayoutY() + connectorSkin.getHeight() / 2;
        } else if (side.equals(Side.RIGHT)) {
            x = getRoot().getWidth();
            y = connectorRoot.getLayoutY() + connectorSkin.getHeight() / 2;
        } else if (side.equals(Side.TOP)) {
            x = connectorRoot.getLayoutX() + connectorSkin.getWidth() / 2;
            y = 0;
        } else {
            x = connectorRoot.getLayoutX() + connectorSkin.getWidth() / 2;
            y = getRoot().getHeight();
        }

        return new Point2D(x, y);
    }

    /**
     * Checks that the node and its connectors have the correct values to be displayed using this skin.
     */
    private void performChecks() {
      // the "types" (direction and side) are no enums so cannot be invalid
//      for (final GConnector connector : getItem().getConnectors()) {
//        if (!DefaultConnectorTypes.isValid(connector.getType())) {
//          LOGGER.error("Connector type '{}' not recognized, setting to 'left-input'.", connector.getType());
//          connector.setType(DefaultConnectorTypes.LEFT_INPUT);
//        }
//      }
    }

    /**
     * Lays out the node's connectors.
     */
    private void layoutAllConnectors() {
        layoutConnectors(getTopConnectorSkins(), false, 0);
        layoutConnectors(getRightConnectorSkins(), true, getRoot().getWidth());
        layoutConnectors(getBottomConnectorSkins(), false, getRoot().getHeight());
        layoutConnectors(getLeftConnectorSkins(), true, 0);
    }

    /**
     * Lays out the given connector skins in a horizontal or vertical direction at the given offset.
     *
     * @param connectorSkins the skins to lay out
     * @param vertical {@code true} to lay out vertically, {@code false} to lay out horizontally
     * @param offset the offset in the other dimension that the skins are layed out in
     */
    private void layoutConnectors(final List<GConnectorSkin> connectorSkins, final boolean vertical, final double offset) {

        final int count = connectorSkins.size();

        for (int i = 0; i < count; i++) {

            final GConnectorSkin skin = connectorSkins.get(i);
            final Node root = skin.getRoot();

            if (vertical) {

                final double offsetY = getRoot().getHeight() / (count + 1);
                final double offsetX = getMinorOffsetX(skin.getItem());

                root.setLayoutX(GeometryUtils.moveOnPixel(offset - skin.getWidth() / 2 + offsetX));
                root.setLayoutY(GeometryUtils.moveOnPixel((i + 1) * offsetY - skin.getHeight() / 2));

            } else {

                final double offsetX = getRoot().getWidth() / (count + 1);
                final double offsetY = getMinorOffsetY(skin.getItem());

                root.setLayoutX(GeometryUtils.moveOnPixel((i + 1) * offsetX - skin.getWidth() / 2));
                root.setLayoutY(GeometryUtils.moveOnPixel(offset - skin.getHeight() / 2 + offsetY));
            }
        }
    }

    /**
     * Adds the selection halo and initializes some of its values.
     */
    private void addSelectionHalo() {

        getRoot().getChildren().add(selectionHalo);

        selectionHalo.setManaged(false);
        selectionHalo.setMouseTransparent(false);
        selectionHalo.setVisible(false);

        selectionHalo.setLayoutX(-HALO_OFFSET);
        selectionHalo.setLayoutY(-HALO_OFFSET);

        selectionHalo.getStyleClass().add(STYLE_CLASS_SELECTION_HALO);
    }

    /**
     * Lays out the selection halo based on the current width and height of the node skin region.
     */
    private void layoutSelectionHalo() {

        if (selectionHalo.isVisible()) {

            selectionHalo.setWidth(border.getWidth() + 2 * HALO_OFFSET);
            selectionHalo.setHeight(border.getHeight() + 2 * HALO_OFFSET);

            final double cornerLength = 2 * HALO_CORNER_SIZE;
            final double xGap = border.getWidth() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;
            final double yGap = border.getHeight() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;

            selectionHalo.setStrokeDashOffset(HALO_CORNER_SIZE);
            selectionHalo.getStrokeDashArray().setAll(cornerLength, yGap, cornerLength, xGap);
        }
    }

    @Override
    protected void selectionChanged(boolean isSelected) {
        if (isSelected) {
            selectionHalo.setVisible(true);
            layoutSelectionHalo();
            background.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
            getRoot().toFront();
        } else {
            selectionHalo.setVisible(false);
            background.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
        }
    }

    /**
     * Removes all connectors from the list of children.
     */
    private void removeAllConnectors() {

        connectorSkins.stream().forEach(skin -> getRoot().getChildren().remove(skin.getRoot()));
    }

    /**
     * Gets a minor x-offset of a few pixels in order that the connector's area is distributed more evenly on either
     * side of the node border.
     *
     * @param connector the connector to be positioned
     * @return an x-offset of a few pixels
     */
    private double getMinorOffsetX(final GConnector connector) {

      GConnectorSkin connectorSkin = connectorSkins.stream()
          .filter(skin -> skin.getItem() == connector)
          .findFirst()
          .orElse(null); // null if no match is found

      if ( connectorSkin != null ) { // the connector should always have a corresponding skin so this should never be the case.
        final Side side = connectorSkin.getSide();
        final Direction direction = connector.getDirection();

        if ((side == Side.LEFT && direction == Direction.INPUT)
            || (side == Side.RIGHT && direction == Direction.OUTPUT)) {
          return MINOR_POSITIVE_OFFSET;
        } else {
          return MINOR_NEGATIVE_OFFSET;
        }
      }
      return 0;
    }

    /**
     * Gets a minor y-offset of a few pixels in order that the connector's area is distributed more evenly on either
     * side of the node border.
     *
     * @param connector the connector to be positioned
     * @return a y-offset of a few pixels
     */
    private double getMinorOffsetY(final GConnector connector) {

      GConnectorSkin connectorSkin = connectorSkins.stream()
          .filter(skin -> skin.getItem() == connector)
          .findFirst()
          .orElse(null); // null if no match is found

      if ( connectorSkin != null ) { // the connector should always have a corresponding skin so this should never be the case.
        final Side side = connectorSkin.getSide();
        final Direction direction = connector.getDirection();

        if ((side == Side.TOP && direction == Direction.INPUT)
            || (side == Side.BOTTOM && direction == Direction.OUTPUT)) {
          return MINOR_POSITIVE_OFFSET;
        } else {
          return MINOR_NEGATIVE_OFFSET;
        }
      }
      return 0;
    }

    /**
     * Stops the node being dragged if it isn't selected.
     *
     * @param event a mouse-dragged event on the node
     */
    private void filterMouseDragged(final MouseEvent event) {
        if (event.isPrimaryButtonDown() && !isSelected()) {
            event.consume();
        }
    }

    @Override
    public String toString() {
      return "DefaultNodeSkin [getX()=" + getX() + ", getY()=" + getY() + ", getWidth()=" + getWidth()
          + ", getHeight()=" + getHeight() + ", isSelected()=" + isSelected() + ", getItem()=" + getItem() + "]";
    }
 
    public List<GConnectorSkin> getTopConnectorSkins() {
      return connectorSkins.stream()
          .filter(skin -> skin.getSide() == Side.TOP)
          .toList();
  }

  public List<GConnectorSkin> getRightConnectorSkins() {
      return connectorSkins.stream()
          .filter(skin -> skin.getSide() == Side.RIGHT)
          .toList();
  }

  public List<GConnectorSkin> getBottomConnectorSkins() {
      return connectorSkins.stream()
          .filter(skin -> skin.getSide() == Side.BOTTOM)
          .toList();
  }

  public List<GConnectorSkin> getLeftConnectorSkins() {
      return connectorSkins.stream()
          .filter(skin -> skin.getSide() == Side.LEFT)
          .toList();
  }
}
