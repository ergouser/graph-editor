/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults.connection;

import java.util.List;

import javax.sound.sampled.Port;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnection;

import io.github.eckig.grapheditor.GConnectorStyle;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.connections.RectangularConnections;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;

/**
 * The default connection skin.
 *
 * <p>
 * Extension of {@link SimpleConnectionSkin} that provides a mechanism for creating and removing joints.
 * </p>
 */
public class BezierConnectionSkin extends SimpleConnectionSkin {

  protected static final double STUB_LENGTH = 100.0;

  private static final Logger LOGGER = LoggerFactory.getLogger(BezierConnectionSkin.class);

  private final JointCreator jointCreator;
  private final JointCleaner jointCleaner;
  private final JointAlignmentManager jointAlignmentManager;
  private final CursorOffsetCalculator cursorOffsetCalculator;

  protected static final String BEZIER_STYLE_CLASS = "bezier-connection";

  protected static final String BEZIER_STYLE_CLASS_BACKGROUND = "bezier-connection-background";

  /** The default style‐class for the connection based on the last value. */
  protected final StringProperty defaultStyle = new SimpleStringProperty("connection-novalue");

  /**
   * Creates a new default connection skin instance.
   *
   * @param connection the {@link GConnection} the skin is being created for
   */
  public BezierConnectionSkin(final GConnection connection) {

    super(connection);
    setGeometry(Geometry.BEZIER);
    performChecks();

    cursorOffsetCalculator = new CursorOffsetCalculator(connection, path, backgroundPath, connectionSegments);
    jointCreator = new JointCreator(this, cursorOffsetCalculator);
    jointCleaner = new JointCleaner(this);
    jointAlignmentManager = new JointAlignmentManager(connection);

    jointCreator.addJointCreationHandler(root);
    backgroundPath.getStyleClass().clear();
    path.getStyleClass().clear();
    backgroundPath.getStyleClass().setAll(BEZIER_STYLE_CLASS_BACKGROUND);
    // Apply the initial style‐class:
    path.getStyleClass().setAll(defaultStyle.get());

    // Whenever defaultStyle changes, remove the old style‐class and add the new one:
    defaultStyle.addListener((obs, oldStyle, newStyle) -> {
      if (!isSelected()) {  // don't update the style 
        if (oldStyle != null && path.getStyleClass().contains(oldStyle)) {
          path.getStyleClass().remove(oldStyle);
        }
        if (newStyle != null && !path.getStyleClass().contains(newStyle)) {
          path.getStyleClass().add(newStyle);
        }
      }
    });
  }

  @Override
  public void setGraphEditor(final GraphEditor graphEditor) {

    super.setGraphEditor(graphEditor);

    jointCreator.setGraphEditor(graphEditor);
    jointCleaner.setGraphEditor(graphEditor);
    jointAlignmentManager.setSkinLookup(graphEditor.getSkinLookup());
  }

  @Override
  public void setJointSkins(final List<GJointSkin> jointSkins) {

    super.setJointSkins(jointSkins);

    jointCleaner.addCleaningHandlers(jointSkins);
    jointAlignmentManager.addAlignmentHandlers(jointSkins);
  }

  /**
   * Checks that the connection has the correct values to be displayed using this skin.
   */
  protected void performChecks() {
    final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();

    if ( skinLookup != null ) {
      if (!RectangularConnections.checkJointCount(this, skinLookup)) {
        LOGGER.error("Joint count not compatible with source and target connector types.");
      }
    }
  }

  /**
   * Checks the position of the first and last joints and makes sure they are
   * aligned with their adjacent connectors by adding horizontal “stubs.”
   *
   * <p>
   * The stub on the source side is always to the left of the source port;
   * the stub on the target side is always to the right of the target port.
   * By default each stub is 100 pixels long, but if the straight‐line distance
   * between the two connector ports is less than 300 px, each stub is set to
   * one‐third of that distance instead.
   * </p>
   *
   * @param points
   *          all points that the connection should pass through
   *          (both connector and joint positions)
   */
  @Override
  protected void checkFirstAndLastJoints(final Point2D[] points) {
    // We need at least two “end” joints: one immediately after the source
    // port and one immediately before the target port.
    if (points.length < 3 || jointSkins == null || jointSkins.size() < 2) {
      return;
    }

    // Coordinates of the source and target connector ports:
    final Point2D sourcePt = points[0];
    final Point2D targetPt = points[points.length - 1];

    // Compute the Euclidean distance between the two connector ports:
    final double portDistance = sourcePt.distance(targetPt);

    // Stub length logic: default to 100 px, but if the ports are closer than 300 px,
    // use one‐third of the port‐to‐port distance.
    final double stubLength = (portDistance < STUB_LENGTH*3) ? (portDistance / 3.0) : STUB_LENGTH;

    // === First (source) stub: always to the right of the source port ===
    // New location for the first “joint point”:
    final double firstStubX = sourcePt.getX() + stubLength;
    final double firstStubY = sourcePt.getY();
    // Update the points array at index 1 (the first joint’s coordinate):
    points[1] = new Point2D(firstStubX, firstStubY);

    // Adjust the corresponding GJointSkin node so that it is centered on (firstStubX, firstStubY):
    final GJointSkin firstJointSkin = jointSkins.get(0);
    final double firstSkinWidth = firstJointSkin.getWidth();
    final double firstSkinHeight = firstJointSkin.getHeight();
    // Position = (stubX - width/2, stubY - height/2), then “move on pixel” for sharp rendering:
    final double firstLayoutX = GeometryUtils.moveOnPixel(firstStubX - (firstSkinWidth / 2));
    final double firstLayoutY = GeometryUtils.moveOnPixel(firstStubY - (firstSkinHeight / 2));
    firstJointSkin.getRoot().setLayoutX(firstLayoutX);
    firstJointSkin.getRoot().setLayoutY(firstLayoutY);

    // === Last (target) stub: always to the left of the target port ===
    final double lastStubX = targetPt.getX() - stubLength;
    final double lastStubY = targetPt.getY();
    // Update the points array at index (points.length - 2) for the last joint:
    points[points.length - 2] = new Point2D(lastStubX, lastStubY);

    // Adjust the corresponding GJointSkin node for the last joint:
    final GJointSkin lastJointSkin = jointSkins.get(jointSkins.size() - 1);
    final double lastSkinWidth = lastJointSkin.getWidth();
    final double lastSkinHeight = lastJointSkin.getHeight();
    final double lastLayoutX = GeometryUtils.moveOnPixel(lastStubX - (lastSkinWidth / 2));
    final double lastLayoutY = GeometryUtils.moveOnPixel(lastStubY - (lastSkinHeight / 2));
    lastJointSkin.getRoot().setLayoutX(lastLayoutX);
    lastJointSkin.getRoot().setLayoutY(lastLayoutY);
  }

  /**
   * Draws all segments of the connection using Bezier curves.
   *
   * @param points
   *          all points that the connection should pass through (both connector and joint positions)
   * @param intersections
   *          all intersection-points of this connection with other connections
   */
  protected void drawAllSegments(final Point2D[] points, final double[][] intersections) {
    final double startX = points[0].getX();
    final double startY = points[0].getY();

    // Starting point
    final MoveTo moveTo = new MoveTo(GeometryUtils.moveOffPixel(startX), GeometryUtils.moveOffPixel(startY));

    connectionSegments.clear();
    path.getElements().clear();
    path.getElements().add(moveTo);

    // Loop through points and create cubic Bezier curves
    for (int i = 0; i < points.length - 1; i++) {
      final Point2D start = points[i];
      final Point2D end = points[i + 1];

      // Control points for the Bezier curve
      final double controlX1 = (start.getX() + end.getX()) / 2;
      final double controlY1 = start.getY();
      final double controlX2 = (start.getX() + end.getX()) / 2;
      final double controlY2 = end.getY();

      final CubicCurveTo cubicCurveTo = new CubicCurveTo(
          GeometryUtils.moveOffPixel(controlX1), GeometryUtils.moveOffPixel(controlY1),
          GeometryUtils.moveOffPixel(controlX2), GeometryUtils.moveOffPixel(controlY2),
          GeometryUtils.moveOffPixel(end.getX()), GeometryUtils.moveOffPixel(end.getY())
          );

      // Add the curve to the path
      path.getElements().add(cubicCurveTo);
    }

    // Copy elements to background path
    backgroundPath.getElements().clear();
    backgroundPath.getElements().addAll(path.getElements());
  }

  @Override
  protected void selectionChanged(boolean isSelected) {
    super.selectionChanged(isSelected);
    if (isSelected) {
      path.getStyleClass().remove(getDefaultStyle());
      path.getStyleClass().add("bezier-connection-selected");
    } else {
      path.getStyleClass().remove("bezier-connection-selected");
      path.getStyleClass().add(getDefaultStyle());
   }
  }

  /** Call this to change the style at runtime: */
  public void setDefaultStyle(String cssClassName) {
    defaultStyle.set(cssClassName);
  }

  public String getDefaultStyle() {
    return defaultStyle.get();
  }

  public StringProperty defaultStyleProperty() {
    return defaultStyle;
  }

  @Override
  public String toString() {
    return "BezierConnectionSkin [isSelected()=" + isSelected() + ", getItem()=" + getItem() + "]";
  }

}
