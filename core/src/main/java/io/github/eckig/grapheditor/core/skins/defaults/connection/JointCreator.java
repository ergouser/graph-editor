/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import com.ergotech.grapheditor.model.GJoint;

import io.github.eckig.grapheditor.EditorElement;
import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectionSkin.Geometry;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.connections.RectangularConnections;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultJointSkin;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

/**
 * Responsible for creating joints when a click + drag gesture occurs on a connection.
 */
public class JointCreator {

  private static final String STYLE_CLASS_HOVER_EFFECT = "default-connection-hover-effect";

  private static final int HOVER_EFFECT_SIZE = 12;

  private final GConnectionSkin connectionSkin;

  private final CursorOffsetCalculator offsetCalculator;

  private GraphEditor graphEditor;

  private final Rectangle hoverEffect = new Rectangle(HOVER_EFFECT_SIZE, HOVER_EFFECT_SIZE);

  private GJointSkin temporarySelectedJointSkin;

  private double newJointX;

  private double newJointY;

  // private List<GJoint> temporaryJoints;
  private List<Point2D> oldJointPositions;

  /**
   * Creates a new joint creator. One instance should exist for each default connection skin instance.
   *
   * @param connection
   *          the connection the joint creator is creating joints in
   * @param offsetCalculator
   *          used to determine where to put new joints based on the cursor position
   */
  public JointCreator(final GConnectionSkin connectionSkin, final CursorOffsetCalculator offsetCalculator) {

    this.connectionSkin = connectionSkin;
    this.offsetCalculator = offsetCalculator;

    hoverEffect.getStyleClass().addAll(STYLE_CLASS_HOVER_EFFECT);
    hoverEffect.setPickOnBounds(true);
    hoverEffect.setVisible(false);
  }

  /**
   * Sets the graph editor instance currently in use.
   *
   * @param graphEditor
   *          the {@link GraphEditor} instance currently in use
   */
  public void setGraphEditor(final GraphEditor graphEditor) {
    this.graphEditor = graphEditor;
  }

  /**
   * Returns the hover effect rectangle.
   *
   * @return the rectangle used to display hover effects over the connection
   */
  public Rectangle getHoverEffect() {
    return hoverEffect;
  }

  private boolean checkEditable() {
    return graphEditor != null && !graphEditor.getProperties().isReadOnly(EditorElement.JOINT);
  }

  /**
   * Adds a mechanism for creating joints by clicking on the connection.
   *
   * @param root
   *          the root JavaFX node of the connection skin
   */
  public void addJointCreationHandler(final Group root) {

    root.getChildren().add(hoverEffect);

    root.setOnMouseEntered(event -> updateHoverEffectPosition(event, root));
    root.setOnMouseMoved(event -> updateHoverEffectPosition(event, root));
    root.setOnMouseExited(event -> hoverEffect.setVisible(false));

    root.setOnMouseDragged(event -> {

      if (!checkEditable() || !event.getButton().equals(MouseButton.PRIMARY) || temporarySelectedJointSkin == null) {
        return;
      }

      temporarySelectedJointSkin.getRoot().fireEvent(event);
      if (!event.isConsumed()) {
        event.consume();
      }
    });

    // This handler creates 2 temporary joints which can be dragged around.
    root.setOnMousePressed(event -> {

      final double sceneX = event.getSceneX();
      final double sceneY = event.getSceneY();

      final Point2D offset = offsetCalculator.getOffset(sceneX, sceneY);

      if (!checkEditable() || !event.getButton().equals(MouseButton.PRIMARY) || offset == null) {
        return;
      }

      final SkinManager skinManager = (SkinManager) graphEditor.getSkinLookup();
      List<GJointSkin> jointSkins = connectionSkin.getJoints().stream().map(joint -> skinManager.lookupJoint(joint))
          .collect(Collectors.toList());
      oldJointPositions = GeometryUtils.getJointPositions(jointSkins);

      int index = offsetCalculator.getJointSkinIndex(jointSkins);
      if (index < 0) {
        index = getNewJointLocation(event, root); // this miscalculate for Bezier connections
      }
      if ( connectionSkin.getGeometry() == Geometry.BEZIER ) {
        index = -1;  // the joints on a bezier curve are somewhat close to working, but need more attention.
      }
      if (index > -1) {

        calculateJointPosition(event, root, index);
        final Point2D clickPositionInParent = root.localToParent(event.getX(), event.getY());
        System.out.println("Joint X: " + newJointX + " Y: " + newJointY + " click position: " + clickPositionInParent);

        final int oldJointCount = jointSkins.size();

        addTemporaryJoints(index, newJointX, newJointY);

        if (index == oldJointCount) {
          temporarySelectedJointSkin = jointSkins.get(index);
        } else {
          temporarySelectedJointSkin = jointSkins.get(index + 1);
        }

        temporarySelectedJointSkin.getRoot().fireEvent(event);
        if (graphEditor != null) {
          graphEditor.getSelectionManager().select(temporarySelectedJointSkin);
        }
      }
      printJoints("OnMousePressed");
      event.consume();
    });

    // This handler updates the model with the new joints *only* if the connection shape has actually changed.
    root.setOnMouseReleased(event -> {

      if (!checkEditable() || !event.getButton().equals(MouseButton.PRIMARY) || temporarySelectedJointSkin == null) {
        return;
      }

      final List<Point2D> newJointPositions = getNewJointPositions();
      printJoints("OnMouseReleased");
      // It is important to remove the temporary joints even if we add new joints, otherwise we mess up the
      // undo/redo stack.
      temporarySelectedJointSkin.getRoot().fireEvent(event);
      if (!event.isConsumed()) {
        event.consume();
      }
      removeTemporaryJoints();

      if (checkForNetChange(oldJointPositions, newJointPositions)) {
        JointCommands.setNewJoints(graphEditor, newJointPositions, connectionSkin);
      }
      event.consume();
    });
  }

  public void printJoints(String title) {
    System.out.println(title);
    // Assuming 'connectionSkin' is already initialized
    final SkinManager skinManager = (SkinManager) graphEditor.getSkinLookup();
    List<GJointSkin> jointSkinsCopy = connectionSkin.getJoints().stream().map(joint -> skinManager.lookupJoint(joint))
        .collect(Collectors.toList());

    // Print each element in the copied list
    for (GJointSkin jointSkin : jointSkinsCopy) {
      System.out.println("Joint Skin: " + jointSkin);
    }

  }

  /**
   * Updates the position of the joint creator effect based on the cursor position.
   *
   * @param event
   *          the mouse event containing information about the cursor position
   * @param root
   *          the root node of the connection
   */
  private void updateHoverEffectPosition(final MouseEvent event, final Group root) {

    final double sceneX = event.getSceneX();
    final double sceneY = event.getSceneY();

    final Point2D offset = offsetCalculator.getOffset(sceneX, sceneY);

    // Do not show the joint-creator effect if the cursor is on/near a detour (too messy).
    if (offset == null) {
      hoverEffect.setVisible(false);
      return;
    } else {
      hoverEffect.setVisible(true);
    }

    final Point2D sceneCoordinatesOfParent = root.getParent().localToScene(0, 0);

    final double scaleFactor = root.getLocalToSceneTransform().getMxx();

    final double x = (sceneX - sceneCoordinatesOfParent.getX() + offset.getX()) / scaleFactor;
    final double y = (sceneY - sceneCoordinatesOfParent.getY() + offset.getY()) / scaleFactor;

    hoverEffect.setX(GeometryUtils.moveOnPixel(x - HOVER_EFFECT_SIZE / 2));
    hoverEffect.setY(GeometryUtils.moveOnPixel(y - HOVER_EFFECT_SIZE / 2));
  }

  /**
   * Gets the location of the new joint based on the cursor position.
   *
   * @param event
   *          the mouse event object containing cursor information
   * @param root
   *          the root node of the connection skin
   * @return the index in the connection's joint list of where the new joint would go
   */
  private int getNewJointLocation(final MouseEvent event, final Group root) {

    final int index = offsetCalculator.getNearestSegment(event.getSceneX(), event.getSceneY());

    // final SkinLookup skinLookup = graphEditor.getSkinLookup();
    if (index == -1 || connectionSkin.getJoints().isEmpty()) {
      return -1;
    }

    // calculateJointPosition(event, root, index);

    return index;
  }

  /**
   * This will populate the values or newJointX and newJointY with the suggested values for a new joint.
   * 
   * @param event
   *          the mouse event object containing cursor information
   * @param root
   *          the root node of the connection skin
   * @param index
   *          the index of the PathElement in the path. Technically, the index of the joint skin in the jointskin array
   */
  protected void calculateJointPosition(final MouseEvent event, final Group root, final int index) {
    final Point2D clickPositionInParent = root.localToParent(event.getX(), event.getY());
    if (connectionSkin.getGeometry() == GConnectionSkin.Geometry.RECTANGULAR) {
      final double adjacentJointX;
      final double adjacentJointY;
      final SkinManager skinManager = (SkinManager) graphEditor.getSkinLookup();
      List<GJointSkin> jointSkins = connectionSkin.getJoints().stream().map(joint -> skinManager.lookupJoint(joint))
          .collect(Collectors.toList());

      if (index < jointSkins.size()) {
        adjacentJointX = jointSkins.get(index).getX();
        adjacentJointY = jointSkins.get(index).getY();
      } else {
        adjacentJointX = jointSkins.get(index - 1).getX();
        adjacentJointY = jointSkins.get(index - 1).getY();
      }

      final SkinLookup skinLookup = graphEditor.getSkinLookup();
      if (RectangularConnections.isSegmentHorizontal(connectionSkin, skinLookup, index)) {
        newJointX = GeometryUtils.moveOnPixel(clickPositionInParent.getX());
        newJointY = GeometryUtils.moveOnPixel(adjacentJointY);
      } else {
        newJointX = GeometryUtils.moveOnPixel(adjacentJointX);
        newJointY = GeometryUtils.moveOnPixel(clickPositionInParent.getY());
      }
    } else { // bezier curves create joint at the mouse click position...
      newJointX = GeometryUtils.moveOnPixel(clickPositionInParent.getX());
      newJointY = GeometryUtils.moveOnPixel(clickPositionInParent.getY());
    }
  }

  /**
   * Adds two new joints on top of each other to this connection.
   *
   * <p>
   * <b> Note: </b><br>
   * We add the joints directly to the model and not via EMF commands. This is because we don't want the "add joints"
   * action to be added to the undo/redo stack *unless* the net result of the gesture is to create a new connection
   * shape.
   * </p>
   *
   * @param index
   *          the index in the connection's joint list where the new joints are to be added
   * @param x
   *          the x position for the new joints
   * @param y
   *          the y position for the new joints
   */
  private void addTemporaryJoints(final int index, final double x, final double y) {
    // some of these methods should be delegated to the skin...
    if (connectionSkin.getGeometry() == GConnectionSkin.Geometry.RECTANGULAR) {

      final GJoint firstNewJoint = graphEditor.getModel().getGraphFactory().create(GJoint.class);
      final GJoint secondNewJoint = graphEditor.getModel().getGraphFactory().create(GJoint.class);

      final SkinManager skinManager = (SkinManager) graphEditor.getSkinLookup();
      GJointSkin firstNewJointSkin = skinManager.lookupOrCreateJoint(firstNewJoint);
      firstNewJointSkin.setX(x);
      firstNewJointSkin.setY(y);
      firstNewJoint.setTemporary(true);

      GJointSkin secondNewJointSkin = skinManager.lookupOrCreateJoint(secondNewJoint);
      secondNewJointSkin.setX(x);
      secondNewJointSkin.setY(y);
      secondNewJoint.setTemporary(true);

      // temporaryJoints = new ArrayList<>();

      connectionSkin.getJoints().add(firstNewJoint);
      connectionSkin.getJoints().add(secondNewJoint);

    } else {
      // create only one joint
      final GJoint firstNewJoint = graphEditor.getModel().getGraphFactory().create(GJoint.class);
      firstNewJoint.setTemporary(true);

      final SkinManager skinManager = (SkinManager) graphEditor.getSkinLookup();
      GJointSkin firstNewJointSkin = skinManager.lookupOrCreateJoint(firstNewJoint);
      firstNewJointSkin.setX(x);
      firstNewJointSkin.setY(y);

      // temporaryJoints = new ArrayList<>();

      connectionSkin.getJoints().add(firstNewJoint);

    }

    // skinManager.updateJoints(connectionSkin);
    graphEditor.reload();
  }

  /**
   * Removes the temporary joints that were created by the mouse-pressed gesture.
   */
  private void removeTemporaryJoints() {
    // Collect all temporary joints into a new list
    List<GJoint> temporaryJoints = connectionSkin.getJoints().stream().filter(GJoint::isTemporary)
        .collect(Collectors.toList());

    // Remove those joints from the connection skin
    connectionSkin.getJoints().removeAll(temporaryJoints);

    // Refresh the UI
    graphEditor.reload();
  }

  /**
   * Gets the new joint positions of the temporary joints.
   *
   * @return the list of positions of the temporary joints
   */
  private List<Point2D> getNewJointPositions() {
    final SkinManager skinManager = (SkinManager) graphEditor.getSkinLookup();
    List<GJointSkin> jointSkins = connectionSkin.getJoints().stream().map(joint -> skinManager.lookupJoint(joint))
        .collect(Collectors.toList());
    final List<Point2D> allJointPositions = GeometryUtils.getJointPositions(jointSkins);

    final BitSet jointsToCleanUp = JointCleaner.findJointsToCleanUp(allJointPositions);
    final List<Point2D> newJointPositions = new ArrayList<>();

    for (int i = 0; i < allJointPositions.size(); i++) {
      if (!jointsToCleanUp.get(i)) {
        newJointPositions.add(allJointPositions.get(i));
      }
    }

    return newJointPositions;
  }

  /**
   * Checks if the old and new positions lead to different connection shapes.
   *
   * <p>
   * This check is important because we do not want to add an 'identity' operation to the undo/redo stack.
   * </p>
   *
   * @param oldPositions
   *          the list of old joint positions
   * @param newPositions
   *          the list of new joint positions
   * @return {@code true} if the new joint positions lead to a different connection shape
   */
  private boolean checkForNetChange(final List<Point2D> oldPositions, final List<Point2D> newPositions) {
    return !(oldPositions.containsAll(newPositions) && newPositions.containsAll(oldPositions));
  }
}
