/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ergotech.grapheditor.model.GConnection;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.connections.RectangularConnections;
import io.github.eckig.grapheditor.core.skins.defaults.connection.segment.ConnectionSegment;
import io.github.eckig.grapheditor.core.skins.defaults.connection.segment.DetouredConnectionSegment;
import io.github.eckig.grapheditor.core.skins.defaults.connection.segment.GappedConnectionSegment;
import io.github.eckig.grapheditor.core.skins.defaults.tail.RectangularPathCreator;
import io.github.eckig.grapheditor.utils.DraggableBox;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * A simple rectangular connection skin.
 *
 * <p>
 * Shows a rectangular connection shape based on the positions of its joints. Shows a graphical effect at points where
 * the connection intersects other connections.
 * </p>
 */
public class SimpleConnectionSkin extends GConnectionSkin {

  /**
   * Property key to show detours at intersections.
   *
   * <p>
   * By default small gaps are drawn points where the connection passes <b>under</b> other connections. However it is
   * also possible to draw detours (small semicircles) at points where the connection passes <b>over</b> others.
   * </p>
   *
   * <p>
   * To activate this functionality, add this key to the graph editor's custom properties with the value "true". Do
   * <b>NOT</b> mix 'detoured' and 'gapped' connection skins in the same graph, it will look bad.
   * </p>
   */
  public static final String SHOW_DETOURS_KEY = "default-connection-skin-show-detours";

  protected final Group root = new Group();

  protected final Path path = new Path();

  protected final Path backgroundPath = new Path();

  protected final List<ConnectionSegment> connectionSegments = new ArrayList<>();

  private static final String STYLE_CLASS = "default-connection";

  private static final String STYLE_CLASS_BACKGROUND = "default-connection-background";

  /** The default style‐class for the connection based on the last value. */
  protected final StringProperty defaultStyle = new SimpleStringProperty("connection-novalue");

  /**
   * Creates a new se connection skin instance.
   *
   * @param connection
   *          the {@link GConnection} the skin is being created for
   */
  public SimpleConnectionSkin(final GConnection connection) {

    super(connection);

    root.setManaged(false);

    // Background path is invisible and used only to capture hover events.
    root.getChildren().add(backgroundPath);
    root.getChildren().add(path);

    path.setMouseTransparent(true);

    backgroundPath.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);
    path.getStyleClass().setAll(STYLE_CLASS);
  }

  public SimpleConnectionSkin() {
    super();
    root.setManaged(false);

    // Background path is invisible and used only to capture hover events.
    root.getChildren().add(backgroundPath);
    root.getChildren().add(path);

    path.setMouseTransparent(true);

    backgroundPath.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);
    path.getStyleClass().setAll(STYLE_CLASS);
 }

  @Override
  public Node getRoot() {
    return root;
  }

//  @Override
  public List<GJointSkin> getJointSkins() {
    final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();
    GConnectionSkin connectionSkin = skinLookup.lookupConnection(getItem());
    List<GJointSkin> jointSkins = connectionSkin.getJoints().stream()
      .map(joint -> skinLookup.lookupJoint(joint))
      .collect(Collectors.toList());
    return jointSkins;
}

  public void setJointSkins(final List<GJointSkin> jointSkins) {

//    if (this.jointSkins != null) {
//      removeOldRectangularConstraints();
//    }
//
//    this.jointSkins = jointSkins;
//
//    addRectangularConstraints();
  }

  @Override
  public Point2D[] update() {
    final Point2D[] points = super.update();
    if ( points != null ) {
      checkFirstAndLastJoints(points);
    }
    return points;
  }

  @Override
  public void draw(final Map<GConnectionSkin, Point2D[]> allPoints) {
    super.draw(allPoints);

    // If we are showing detours, get all intersections with connections *behind* this one. Otherwise in front.
    final double[][] intersections = IntersectionFinder.find(this, allPoints, checkShowDetours());

    final Point2D[] points = allPoints == null ? null : allPoints.get(this);
    if (points != null) {
      drawAllSegments(points, intersections);
    } else {
      connectionSegments.clear();
      path.getElements().clear();
    }
  }

  /**
   * Removes the old rectangular constraints on the connection's list of joint skins.
   */
  protected void removeOldRectangularConstraints() {
    final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();
    List<GJointSkin> jointSkins = getJointSkins();
    for (int i = 0; i < jointSkins.size() - 1; i++) {
      final DraggableBox thisJoint = jointSkins.get(i).getRoot();
      final DraggableBox nextJoint = jointSkins.get(i + 1).getRoot();

      if (RectangularConnections.isSegmentHorizontal(this, skinLookup, i)) {
        thisJoint.bindLayoutX(null);
        nextJoint.bindLayoutX(null);
      } else {
        thisJoint.bindLayoutY(null);
        nextJoint.bindLayoutY(null);
      }
    }
  }

  /**
   * Adds constraints to the connection's joints in order to keep the connection rectangular in shape.
   */
  protected void addRectangularConstraints() {
    // Our rectangular connection logic assumes an even number of joints.
    final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();
    List<GJointSkin> jointSkins = getJointSkins();
    for (int i = 0; i < jointSkins.size() - 1; i++) {
      final DraggableBox thisJoint = jointSkins.get(i).getRoot();
      final DraggableBox nextJoint = jointSkins.get(i + 1).getRoot();

      if (RectangularConnections.isSegmentHorizontal(this, skinLookup, i)) {
        thisJoint.bindLayoutX(nextJoint);
        nextJoint.bindLayoutX(thisJoint);
      } else {
        thisJoint.bindLayoutY(nextJoint);
        nextJoint.bindLayoutY(thisJoint);
      }
    }
  }

  /**
   * Checks the position of the first and last joints and makes sure they are aligned with their adjacent connectors.
   *
   * @param points
   *          all points that the connection should pass through (both connector and joint positions)
   */
  protected void checkFirstAndLastJoints(final Point2D[] points) {
    final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();
    if ( points.length > 3 ) {
      alignJoint(points, RectangularConnections.isSegmentHorizontal(this, skinLookup, 0), true);
      alignJoint(points, RectangularConnections.isSegmentHorizontal(this, skinLookup, points.length - 2), false);
    }
  }

  /**
   * Aligns the first or last joint to have the same vertical or horizontal position as the start or end point.
   *
   * @param points
   *          the list of points in this connection
   * @param vertical
   *          {@code true} to align in the vertical (y) direction, {@code false} for horizontal (x)
   * @param start
   *          {@code true} to align the first joint to the start, {@code false} for the last joint to the end
   */
  protected void alignJoint(final Point2D[] points, final boolean vertical, final boolean start) {
    final int targetPositionIndex = start ? 0 : points.length - 1;
    final int jointPositionIndex = start ? 1 : points.length - 2;
    List<GJointSkin> jointSkins = getJointSkins();
    final GJointSkin jointSkin = jointSkins.get(start ? 0 : jointSkins.size() - 1);
    List<Point2D> calculatedPath = RectangularPathCreator.createPath(points[0], points[points.length-1], Side.RIGHT, Side.LEFT);
    // the calculated path could have more, or less points than the points array...  This should be managed better.
    // likely by returning the new "points"
    // for now, we'll just ignore this
    // Also need to cover the case where the user repositioned the joints, but since that doesn't currently work, that's also ignored
    for ( int counter = 1 ; counter < points.length-1 ; counter++ ) {
      points[counter] = calculatedPath.get(counter-1);
    }
    if (vertical) {
      final double newJointY = points[targetPositionIndex].getY();
      final double newJointLayoutY = GeometryUtils.moveOnPixel(newJointY - jointSkin.getHeight() / 2);
      jointSkin.getRoot().setLayoutY(newJointLayoutY);

      final double currentX = points[jointPositionIndex].getX();
      points[jointPositionIndex] = new Point2D(currentX, newJointY);
    } else {
      final double newJointX = points[targetPositionIndex].getX();
      final double newJointLayoutX = GeometryUtils.moveOnPixel(newJointX - jointSkin.getWidth() / 2);
      jointSkin.getRoot().setLayoutX(newJointLayoutX);

      final double currentY = points[jointPositionIndex].getY();
      points[jointPositionIndex] = new Point2D(newJointX, currentY);
    }
  }

  /**
   * Draws all segments of the connection.
   *
   * @param points
   *          all points that the connection should pass through (both connector and joint positions)
   * @param intersections
   *          all intersection-points of this connection with other connections
   */
  protected void drawAllSegments(final Point2D[] points, final double[][] intersections) {
    final double startX = points[0].getX();
    final double startY = points[0].getY();

    final MoveTo moveTo = new MoveTo(GeometryUtils.moveOffPixel(startX), GeometryUtils.moveOffPixel(startY));

    connectionSegments.clear();
    path.getElements().clear();
    path.getElements().add(moveTo);

    for (int i = 0; i < points.length - 1; i++) {
      final Point2D start = points[i];
      final Point2D end = points[i + 1];

      final double[] segmentIntersections = intersections != null ? intersections[i] : null;
      final ConnectionSegment segment;

      if (checkShowDetours()) {
        segment = new DetouredConnectionSegment(start, end, segmentIntersections);
      } else {
        segment = new GappedConnectionSegment(start, end, segmentIntersections);
      }

      segment.draw();

      connectionSegments.add(segment);
      path.getElements().addAll(segment.getPathElements());
    }

    backgroundPath.getElements().clear();
    backgroundPath.getElements().addAll(path.getElements());
  }

  /**
   * Checks whether the custom property has been set to show detours instead of gaps when connections intersect.
   *
   * @return {@code true} if the custom property to show detours has been set
   */
  protected boolean checkShowDetours() {
    boolean showDetours = false;

    final String value = getGraphEditor().getProperties().getCustomProperties().get(SHOW_DETOURS_KEY);
    if (Boolean.toString(true).equals(value)) {
      showDetours = true;
    }

    return showDetours;
  }
  
  @Override
  protected Node getStylableNode() {
    return path;
  }

  @Override
  protected void selectionChanged(boolean isSelected) {
    if (isSelected) {
      getStyleClass().remove(getDefaultStyle());
      getStyleClass().add("rectangular-connection-selected");
    } else {
      getStyleClass().remove("rectangular-connection-selected");
      if (getDefaultStyle() != null && !getStyleClass().contains(getDefaultStyle())) {
        getStyleClass().add(getDefaultStyle());
      }
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



}
