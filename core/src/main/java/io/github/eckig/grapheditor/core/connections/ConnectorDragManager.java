/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.connections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;

import io.github.eckig.grapheditor.EditorElement;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GConnectorStyle;
import io.github.eckig.grapheditor.GConnectorValidator;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.VirtualSkin;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import io.github.eckig.grapheditor.core.skins.defaults.utils.ConnectionCommands;
import io.github.eckig.grapheditor.core.utils.EventUtils;
import io.github.eckig.grapheditor.core.view.GraphEditorView;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import io.github.eckig.grapheditor.utils.GraphEditorProperties;
import io.github.eckig.grapheditor.utils.GraphEventManager;
import io.github.eckig.grapheditor.utils.GraphInputGesture;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

/**
 * Responsible for what happens when connectors are dragged in the graph editor.
 *
 * <p>
 * Namely, the creation, removal, and repositioning of connections.
 * </p>
 */
public class ConnectorDragManager {

  protected final TailManager tailManager;

  protected final GraphEditorView view;

  protected final SkinManager skinManager;

  protected final ConnectionEventManager connectionEventManager;

  protected GModel model;

  /**
   * Consume the Event so the parent container (ResizableBox/DraggableBox) does not move on connection detach
   */
  protected final EventHandler<MouseEvent> mousePressedHandler = Event::consume;

  protected final Map<Node, EventHandler<MouseEvent>> mouseEnteredHandlers = new HashMap<>();

  protected final Map<Node, EventHandler<MouseEvent>> mouseReleasedHandlers = new HashMap<>();
  
  protected final Map<Node, EventHandler<MouseEvent>> mouseExitedHandlers = new HashMap<>();

  protected final Map<Node, EventHandler<MouseEvent>> dragDetectedHandlers = new HashMap<>();

  protected final Map<Node, EventHandler<MouseEvent>> mouseDraggedHandlers = new HashMap<>();

  protected final Map<Node, EventHandler<MouseDragEvent>> mouseDragEnteredHandlers = new HashMap<>();

  protected final Map<Node, EventHandler<MouseDragEvent>> mouseDragExitedHandlers = new HashMap<>();

  protected final Map<Node, EventHandler<MouseDragEvent>> mouseDragReleasedHandlers = new HashMap<>();

  protected GConnectorValidator validator = new DefaultConnectorValidator();

  protected GConnectorSkin hoveredConnectorSkin;

  protected GConnectorSkin sourceConnectorSkin;

  protected GConnectorSkin targetConnectorSkin;

  protected GConnectorSkin removalConnectorSkin;

  protected boolean repositionAllowed;

  /**
   * Creates a new {@link ConnectorDragManager}. Only one instance should exist per {@link DefaultGraphEditor} instance.
   *
   * @param skinLookup
   *          the {@link SkinLookup} used to look up connector and tail skins
   * @param connectionEventManager
   *          the {@link ConnectionEventManager} used to notify users of connection events
   * @param view
   *          the {@link GraphEditorView} to which tail skins will be added and removed during drag events
   */
  public ConnectorDragManager(final SkinManager skinManager, final ConnectionEventManager connectionEventManager,
      final GraphEditorView view) {
    this.view = view;
    this.skinManager = skinManager;
    this.connectionEventManager = connectionEventManager;
    this.tailManager = new TailManager(skinManager, view);
  }

  /**
   * Initializes the drag manager for the given model.
   *
   * @param model
   *          the {@link GModel} currently being edited
   */
  public void initialize(final GModel model) {
    this.model = model;
    clearTrackingParameters();
    setHandlers();
  }

  /**
   * Sets the validator that determines what connections can be created.
   *
   * @param validator
   *          a {@link GConnectorValidator} ementation, or null to use the default
   */
  public void setValidator(final GConnectorValidator validator) {
    this.validator = Objects.requireNonNullElseGet(validator, DefaultConnectorValidator::new);
  }

  /**
   * Clears all parameters that track things like what connector is currently hovered over, and so on.
   */
  protected void clearTrackingParameters() {
    tailManager.cleanUp();
    hoveredConnectorSkin = null;
    removalConnectorSkin = null;
    repositionAllowed = true;
  }

  public void addConnector(final GConnectorSkin connectorSkin) {
    addMouseHandlers(connectorSkin);
  }

  public void removeConnector(final GConnectorPort pConnectorToRemove) {
    final GConnectorSkin connectorSkin = skinManager.lookupConnector(pConnectorToRemove);
    if (connectorSkin != null) {
      final Node root = connectorSkin.getRoot();
      if (root != null) {
        removeSingleEventHandler(root, mouseEnteredHandlers, MouseEvent.MOUSE_ENTERED);
        removeSingleEventHandler(root, mouseReleasedHandlers, MouseEvent.MOUSE_RELEASED);
        removeSingleEventHandler(root, mouseExitedHandlers, MouseEvent.MOUSE_RELEASED);
        removeSingleEventHandler(root, dragDetectedHandlers, MouseEvent.DRAG_DETECTED);
        removeSingleEventHandler(root, mouseDraggedHandlers, MouseEvent.MOUSE_DRAGGED);
        removeSingleEventHandler(root, mouseDragEnteredHandlers, MouseDragEvent.MOUSE_DRAG_ENTERED);
        removeSingleEventHandler(root, mouseDragExitedHandlers, MouseDragEvent.MOUSE_DRAG_EXITED);
        removeSingleEventHandler(root, mouseDragReleasedHandlers, MouseDragEvent.MOUSE_DRAG_RELEASED);

        removeGeneralEventHandlers(root);
      }
    }

    // the connector's tail we are dragging around has been removed..
    if (sourceConnectorSkin == pConnectorToRemove || targetConnectorSkin == pConnectorToRemove
        || removalConnectorSkin == pConnectorToRemove) {
      clearTrackingParameters();
    }
  }

  protected void removeGeneralEventHandlers(final Node node) {
    node.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
    //node.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
  }

  private static <T extends Event> void removeSingleEventHandler(final Node node,
      final Map<Node, EventHandler<T>> eventHandlerMap, final EventType<T> eventType) {
    final EventHandler<T> handler = eventHandlerMap.remove(node);
    if (handler != null) {
      node.removeEventHandler(eventType, handler);
    }
  }

  /**
   * Sets all mouse and mouse-drag handlers for all connectors in the current model.
   */
  protected void setHandlers() {
    // here we assume that all event handler maps are of exactly the same size
    for (final Node node : mouseEnteredHandlers.keySet()) {
      removeGeneralEventHandlers(node);
    }

    EventUtils.removeEventHandlers(mouseEnteredHandlers, MouseEvent.MOUSE_ENTERED);
    EventUtils.removeEventHandlers(mouseReleasedHandlers, MouseEvent.MOUSE_RELEASED);
    EventUtils.removeEventHandlers(dragDetectedHandlers, MouseEvent.DRAG_DETECTED);
    EventUtils.removeEventHandlers(mouseDraggedHandlers, MouseEvent.MOUSE_DRAGGED);
    EventUtils.removeEventHandlers(mouseDragEnteredHandlers, MouseDragEvent.MOUSE_DRAG_ENTERED);
    EventUtils.removeEventHandlers(mouseDragExitedHandlers, MouseDragEvent.MOUSE_DRAG_EXITED);
    EventUtils.removeEventHandlers(mouseDragReleasedHandlers, MouseDragEvent.MOUSE_DRAG_RELEASED);

    for (final GNode node : model.getNodes()) {
      for (final GConnectorPort connector : node.getConnectorPorts()) {
        addMouseHandlers(skinManager.lookupOrCreateConnector(connector));
      }
    }
  }

  /**
   * Adds mouse handlers to a particular connector.
   *
   * @param connector
   *          the {@link GConnectorPort} to which mouse handlers should be added
   */
  private void addMouseHandlers(final GConnectorSkin connectorSkin) {
    if (connectorSkin != null) {
      final Node root = connectorSkin.getRoot();
      if (root == null || mouseEnteredHandlers.containsKey(root)) {
        return;
      }

      final EventHandler<MouseEvent> newMouseEnteredHandler = event -> handleMouseEntered(event, connectorSkin);
      final EventHandler<MouseEvent> newMouseReleasedHandler = event -> handleMouseReleased(event, connectorSkin);
      final EventHandler<MouseEvent> newMouseExitedHandler = event -> handleMouseExited(event, connectorSkin);

      final EventHandler<MouseEvent> newDragDetectedHandler = event -> handleDragDetected(event, connectorSkin);
      final EventHandler<MouseEvent> newMouseDraggedHandler = event -> handleMouseDragged(event, connectorSkin);
      final EventHandler<MouseDragEvent> newMouseDragEnteredHandler = event -> handleDragEntered(event, connectorSkin);
      final EventHandler<MouseDragEvent> newMouseDragExitedHandler = event -> handleDragExited(event, connectorSkin);
      final EventHandler<MouseDragEvent> newMouseDragReleasedHandler = event -> handleDragReleased(event,
          connectorSkin);

      root.addEventHandler(MouseEvent.MOUSE_ENTERED, newMouseEnteredHandler);
      root.addEventHandler(MouseEvent.MOUSE_EXITED, newMouseExitedHandler);
      root.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
      root.addEventHandler(MouseEvent.MOUSE_RELEASED, newMouseReleasedHandler);

      root.addEventHandler(MouseEvent.DRAG_DETECTED, newDragDetectedHandler);
      root.addEventHandler(MouseEvent.MOUSE_DRAGGED, newMouseDraggedHandler);
      root.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, newMouseDragEnteredHandler);
      root.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, newMouseDragExitedHandler);
      root.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, newMouseDragReleasedHandler);

      mouseEnteredHandlers.put(root, newMouseEnteredHandler);
      mouseReleasedHandlers.put(root, newMouseReleasedHandler);
      mouseExitedHandlers.put(root, newMouseExitedHandler);

      dragDetectedHandlers.put(root, newDragDetectedHandler);
      mouseDraggedHandlers.put(root, newMouseDraggedHandler);
      mouseDragEnteredHandlers.put(root, newMouseDragEnteredHandler);
      mouseDragExitedHandlers.put(root, newMouseDragExitedHandler);
      mouseDragReleasedHandlers.put(root, newMouseDragReleasedHandler);
    }
  }

  /**
   * Handles mouse-entered events on the given connector.
   *
   * @param event
   *          a mouse-entered event
   * @param connector
   *          the {@link GConnectorPort} on which this event occurred
   */
  protected void handleMouseEntered(final MouseEvent event, final GConnectorSkin connectorSkin) {
    hoveredConnectorSkin = connectorSkin;
    connectorSkin.applyStyle(GConnectorStyle.DRAG_OVER_ALLOWED);
    event.consume();
  }

  /**
   * Handles mouse-exited events on the given connector.
   *
   * @param event
   *          a mouse-exited event
   */
  protected void handleMouseExited(final MouseEvent event, final GConnectorSkin connectorSkin) {
    hoveredConnectorSkin = null;
    connectorSkin.applyStyle(GConnectorStyle.DEFAULT);
    event.consume();
  }

  /**
   * Handles mouse-released events on the given connector.
   *
   * @param event
   *          a mouse-released event
   */
  protected void handleMouseReleased(final MouseEvent event, final GConnectorSkin connectorSkin) {
    if (targetConnectorSkin != null && targetConnectorSkin != null) {
      targetConnectorSkin.applyStyle(GConnectorStyle.DEFAULT);
    }

    sourceConnectorSkin = null;
    removalConnectorSkin = null;
    repositionAllowed = true;

    tailManager.cleanUp();
    finishGesture();

    event.consume();
  }

  /**
   * Handles drag-detected events on the given connector.
   *
   * @param pEvent
   *          a drag-detected event
   * @param connectorSkin
   *          the {@link GConnectorSkin} on which this event occurred
   */
  protected void handleDragDetected(final MouseEvent pEvent, final GConnectorSkin connectorSkin) {
    if (pEvent.getButton() != MouseButton.PRIMARY) {
      return;
    }
    ((Node) pEvent.getSource()).startFullDrag();

    final GConnectorPort connector = connectorSkin.getItem();
    if (checkCreatable(connector) && activateGesture(pEvent)) {
      sourceConnectorSkin = skinManager.lookupOrCreateConnector(connector);
      connectorSkin.getRoot().startFullDrag();
      tailManager.cleanUp();
      tailManager.create(connectorSkin, pEvent);
    } else if (checkRemovable(connector) && activateGesture(pEvent)) {
      removalConnectorSkin = skinManager.lookupOrCreateConnector(connector);
      connectorSkin.getRoot().startFullDrag();
    }

    pEvent.consume();
  }

  /**
   * Handles mouse-dragged events on the given connector.
   *
   * @param event
   *          a mouse-dragged event
   * @param connector
   *          the {@link GConnectorPort} on which this event occurred
   */
  protected void handleMouseDragged(final MouseEvent event, final GConnectorSkin connectorSkin) {
    if (repositionAllowed && activateGesture(event)) {
      // Case for when the mouse first exits a connector during a drag gesture.
      if (removalConnectorSkin != null && !removalConnectorSkin.equals(hoveredConnectorSkin)) {
        detachConnection(event, connectorSkin);
      } else {
        tailManager.updatePosition(event);
        //System.out.println ("Drag Event " + event.getX() + " "+ event.getY());
      }
      event.consume();
    }
  }

  /**
   * Handles drag-entered events on the given connector.
   *
   * @param event
   *          a drag-entered event
   * @param connectorSkin
   *          the {@link GConnectorSkin} on which this event occurred
   */
  protected void handleDragEntered(final MouseEvent event, final GConnectorSkin connectorSkin) {
    if (!activateGesture(event)) {
      return;
    }

    final GConnectorPort connector = connectorSkin.getItem();
    if (validator.prevalidate(sourceConnectorSkin.getItem(), connector)) {
      final boolean valid = validator.validate(sourceConnectorSkin.getItem(), connector);
      tailManager.snapPosition(sourceConnectorSkin, connectorSkin, valid);

      repositionAllowed = false;

      if (valid) {
        connectorSkin.applyStyle(GConnectorStyle.DRAG_OVER_ALLOWED);
      } else {
        connectorSkin.applyStyle(GConnectorStyle.DRAG_OVER_FORBIDDEN);
      }
    }

    //Visual feedback: add a highlight style or effect
    //connectorSkin.getRoot().setStyle("-fx-border-color: #33cc33; -fx-border-width: 2px; -fx-border-radius: 4px;");
    event.consume();
  }

  /**
   * Handles drag-exited events on the given connector.
   *
   * @param event
   *          a drag-exited event
   * @param connectorSkin
   *          the {@link GConnectorSkin} on which this event occurred
   */
  protected void handleDragExited(final MouseEvent event, final GConnectorSkin connectorSkin) {
    connectorSkin.applyStyle(GConnectorStyle.DEFAULT);
    repositionAllowed = true;

    tailManager.updatePosition(event);

    // Remove visual highlight
    //connectorSkin.getRoot().setStyle(""); // or set it back to the base style if needed
    event.consume();
  }

  /**
   * Handles drag-released events on the given connector.
   *
   * @param event
   *          a drag-released event
   * @param connectorSkin
   *          the {@link GConnectorSkin} on which this event occurred
   */
  protected void handleDragReleased(final MouseEvent event, final GConnectorSkin connectorSkin) {
    if (event.isConsumed()) {
      return;
    }

    // Consume the event now so it doesn't fire repeatedly after re-initialization.
    event.consume();

    final GConnectorPort connector = connectorSkin.getItem();
    if (validator.prevalidate(sourceConnectorSkin.getItem(), connector) && validator.validate(sourceConnectorSkin.getItem(), connector)) {
      addConnection(sourceConnectorSkin.getItem(), connector);
    }

    connectorSkin.applyStyle(GConnectorStyle.DEFAULT);
    tailManager.cleanUp();
    finishGesture();
  }

  /**
   * Checks if a connection can be created from the given connector.
   *
   * @param connector
   *          a {@link GConnectorPort} instance
   * @return {@code true} if a connection can be created from the given {@link GConnectorPort}, {@code false} if not
   */
  protected boolean checkCreatable(final GConnectorPort connector) {
    final GConnectorSkin gConnectorSkin = skinManager.lookupOrCreateConnector(connector);
    return connector != null && checkEditable()
        && (connector.getConnections().isEmpty() || !gConnectorSkin.isConnectionDetachedOnDrag());
  }

  /**
   * Checks if a connection can be removed from the given connector.
   *
   * @param connector
   *          a {@link GConnectorPort} instance
   * @return {@code true} if a connection can be removed from the given {@link GConnectorPort}, {@code false} if not
   */
  protected boolean checkRemovable(final GConnectorPort connector) {
    final GConnectorSkin gConnectorSkin = skinManager.lookupOrCreateConnector(connector);
    return checkEditable() && !connector.getConnections().isEmpty() && gConnectorSkin.isConnectionDetachedOnDrag();
  }

  private boolean checkEditable() {
    return getEditorProperties() != null && !getEditorProperties().isReadOnly(EditorElement.CONNECTOR);
  }

  private GraphEditorProperties getEditorProperties() {
    return view == null ? null : view.getEditorProperties();
  }

  /**
   * Adds a new connection to the model.
   *
   * <p>
   * This will trigger the model listener and cause everything to be reinitialized.
   * </p>
   *
   * @param source
   *          the source {@link GConnectorPort} for the new connection
   * @param target
   *          the target {@link GConnectorPort} for the new connection
   */
  protected void addConnection(final GConnectorPort source, final GConnectorPort target) {
    final String connectionType = validator.createConnectionType(source, target);
    final String jointType = validator.createJointType(source, target);
    final List<Point2D> jointPositions = skinManager.lookupTail(source).allocateJointPositions();

    final List<GJoint> joints = new ArrayList<>();

    for (final Point2D position : jointPositions) {
      final GJoint joint = model.getGraphFactory().create(GJoint.class);
      final GJointSkin gJointSkin = skinManager.lookupOrCreateJoint(joint);
      gJointSkin.setType(jointType);
      gJointSkin.setX(position.getX());
      gJointSkin.setY(position.getY());

      joints.add(joint);
    }

    ConnectionCommands.addConnection(model, source, target, connectionType, joints, connectionEventManager,skinManager);
  }

  /**
   * Detaches the first connection from the given connector - i.e. removes the connection and replaces it with a tail.
   *
   * @param event
   *          the {@link MouseEvent} that caused the connection to be detached
   * @param connector
   *          the connector that the connection was detached from
   */
  protected void detachConnection(final MouseEvent event, final GConnectorSkin connectorSkin) {
    GConnectorPort connector = connectorSkin.getItem();
    final int connectorCount = getConnectorCount(connector);
    if (connectorSkin != null) {
      connectorSkin.applyStyle(GConnectorStyle.DEFAULT);
    }

    if (connector.getConnections().isEmpty()) {
      return;
    }

    boolean followUpCreated = false;

    final GConnection[] connections = connector.getConnections().toArray(new GConnection[0]);
    for (final GConnection connection : connections) {
      if (skinManager.lookupConnection(connection) instanceof VirtualSkin) {
        // do not touch virtual connections
        continue;
      }
      final GConnectorPort opposingConnector = getOpposingConnector(connection, connector);
      
      final List<Point2D> jointPositions = GeometryUtils.getJointPositions(skinManager.lookupConnection(connection));
      final GConnectorPort newSource;
      if (connector.equals(connection.getSourcePort())) {
        Collections.reverse(jointPositions);
        newSource = connection.getTargetPort();
      } else {
        newSource = connection.getSourcePort();
      }

      ConnectionCommands.removeConnection(model, connection, connectionEventManager);

      final GConnectorSkin updateConnectorSkin;
      if ((updateConnectorSkin = skinManager.lookupConnector(connector)) == null || updateConnectorSkin != connectorSkin
          || connectorCount != getConnectorCount(connector)) {
        // business logic decided to remove this connection or structurally change the parent node
        continue;
      }

      // check if the new source connector allows to create a new connection on the fly:
      if (!followUpCreated && checkCreatable(opposingConnector)) {
        tailManager.updateToNewSource(jointPositions, newSource, event);
        sourceConnectorSkin = skinManager.lookupConnector(opposingConnector);
        targetConnectorSkin = skinManager.lookupConnector(connector);
        followUpCreated = true;
      }
    }

    // no follow up tail created -> clean up
    if (!followUpCreated) {
      clearTrackingParameters();
      sourceConnectorSkin = null;
      targetConnectorSkin = null;
      finishGesture();
    }

    removalConnectorSkin = null;
  }

  private static int getConnectorCount(final GConnectorPort pConnector) {
    return pConnector == null || pConnector.getParent() == null ? 1 : pConnector.getParent().getConnectorPorts().size();
  }

  protected GConnectorPort getOpposingConnector(final GConnection pConnection, final GConnectorPort pConnector) {
    if (!pConnection.getSourcePort().equals(pConnector)) {
      return pConnection.getSourcePort();
    } else {
      return pConnection.getTargetPort();
    }
  }

  protected boolean activateGesture(final Event pEvent) {
    final GraphEventManager eventManager = getEditorProperties();
    if (eventManager != null) {
      eventManager.activateGesture(GraphInputGesture.CONNECT, pEvent, this);
    }
    return true;
  }

  protected void finishGesture() {
    final GraphEventManager eventManager = getEditorProperties();
    if (eventManager != null) {
      eventManager.finishGesture(GraphInputGesture.CONNECT, this);
    }
  }
}
