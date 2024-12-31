/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import java.util.List;

import com.ergotech.grapheditor.model.GConnectorPort;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Side;

/**
 * The tail-skin class for a {@link GConnectorSkin}. Responsible for visualizing the tails that extend temporarily from
 * connectors during a drag gesture in the graph editor.
 *
 * <p>
 * A custom tail skin must extend this class. It <b>must</b> also provide a constructor taking exactly one
 * {@link GConnectorSkin} parameter.
 * </p>
 *
 * <p>
 * Tail skins can have similar logic to connection skins, but they do not have to worry about positionable joints.
 * </p>
 */
public abstract class GTailSkin extends GSkin<GConnectorPort> {

  ObjectProperty<Side> side  = new SimpleObjectProperty<>(this, "side");

  /**
     * Creates a new {@link GTailSkin}.
     *
     * @param connector the {@link GConnectorSkin} that the tail will extend from
     */
    public GTailSkin(final GConnectorPort connector) {
        super(connector);
    }

    /**
     * Updates the position of the tail according to the specified start and end points.
     *
     * <p>
     * This method will be called when a 'fresh' tail is created from an unoccupied connector.
     * </p>
     *
     * @param start a {@link Point2D} containing the start x and y values
     * @param end a {@link Point2D} containing the end x and y values
     */
    public abstract void draw(Point2D start, Point2D end);

    /**
     * Updates the position of the tail according to the specified start and end points.
     *
     * <p>
     * This method will be called when a tail is snapped to the target connector that the mouse is hovering over.
     * </p>
     *
     * @param start a {@link Point2D} containing the start x and y values
     * @param end a {@link Point2D} containing the end x and y values
     * @param target the target connector that the tail is snapping to
     * @param valid {@code true} if the connection is valid, {@code false} if invalid
     */
    public abstract void draw(Point2D start, Point2D end, GConnectorSkin target, boolean valid);

    /**
     * Updates the position of the tail according to the specified start points, end points, and joint positions.
     *
     * <p>
     * This method will be called when an existing connection is repositioned. The tail skin may use the position of the
     * old connection to decide how to position itself, or it may ignore this information.
     * </p>
     *
     * @param start a {@link Point2D} containing the start x and y values
     * @param end a {@link Point2D} containing the end x and y values
     * @param jointPositions the positions of the joints at the time the connection was removed
     */
    public abstract void draw(Point2D start, Point2D end, List<Point2D> jointPositions);

    /**
     * Updates the position of the tail according to the specified start points, end points, and joint positions.
     *
     * <p>
     * This method will be called when an existing connection is repositioned and the stail is snapped to a target
     * connector.
     * </p>
     *
     * @param start a {@link Point2D} containing the start x and y values
     * @param end a {@link Point2D} containing the end x and y values
     * @param jointPositions 
     * @param target the target connector that the tail is snapping to
     * @param valid {@code true} if the connection is valid, {@code false} if invalid
     */
    public abstract void draw(Point2D start, Point2D end, List<Point2D> jointPositions, GConnectorSkin target, boolean valid);

    /**
     * Allocates a list of joint positions for a new connection.
     *
     * <p>
     * When the tail is 'converted' into a connection during a successful drag-drop gesture, this method will be called
     * so that the new connection's joint positions can be based on the final position of the tail.
     * </p>
     *
     * @return a list of {@code Point2D} objects containing x and y values for a newly-created connection
     */
    public abstract List<Point2D> allocateJointPositions();
    
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
