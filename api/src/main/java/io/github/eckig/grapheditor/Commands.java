/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnector;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.command.AddCommand;
import com.ergotech.grapheditor.model.command.Command;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.command.CompoundCommand;
import com.ergotech.grapheditor.model.command.RemoveCommand;
import com.ergotech.grapheditor.model.command.SetPropertyCommand;

import javafx.geometry.Point2D;
import javafx.scene.layout.Region;

/**
 * Provides utility methods for editing a {@link GModel} 
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>GModel model = new GModel();
 * GNode node = new GNode();
 *
 * node.setX(100);
 * node.setY(50);
 * node.setWidth(150);
 * node.setHeight(200);
 *
 * Commands.addNode(model, node);
 * Commands.undo(model);
 * Commands.redo(model);</code>
 * </pre>
 */
public class Commands {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);

  /**
   * Static class, not to be instantiated.
   */
  private Commands() {
  }

  /**
   * Adds a node to the model.
   *
   * <p>
   * The node's x, y, width, and height values should be set before calling this method.
   * </p>
   *
   * @param model
   *          the {@link GModel} to which the node should be added
   * @param node
   *          the {@link GNode} to add to the model
   */
  public static void addNode(final GModel model, final GNode node) {

    final Command command = AddCommand.create(model, owner -> model.getNodes(), node);

    if (command.canExecute()) {
      CommandStack.getCommandStack(model).execute(command);
    }

  }

  /**
   * Removes a node from the model.
   *
   * <p>
   * Also removes any connections that were attached to the node.
   * </p>
   *
   * @param model
   *          the {@link GModel} from which the node should be removed
   * @param node
   *          the {@link GNode} to remove from the model
   */
  public static void removeNode(final GModel model, final GNode node) {

      final CompoundCommand command = new CompoundCommand();
      command.append(RemoveCommand.create(model, owner -> model.getNodes(), node));

      final List<GConnection> connectionsToDelete = new ArrayList<>();

      for (final GConnector connector : node.getConnectors()) {
        for (final GConnection connection : connector.getConnections()) {
          if (connection != null && !connectionsToDelete.contains(connection)) {
            connectionsToDelete.add(connection);
          }
        }
      }

      for (final GConnection connection : connectionsToDelete) {
        command.append(RemoveCommand.create(model, owner -> model.getConnections(), connection));

        final GConnector source = connection.getSource();
        final GConnector target = connection.getTarget();

        if (!node.equals(source.getParent())) {
          // need to include what the connection is being removed from
          command.append(RemoveCommand.create(source, owner -> ((GConnector) owner).getConnections(), connection));
        }

        if (!node.equals(target.getParent())) {
          command.append(RemoveCommand.create(target, owner -> ((GConnector) owner).getConnections(), connection));
        }
      }

      if (command.canExecute()) {
        CommandStack.getCommandStack(model).execute(command);
      }
  }

  /**
   * Clears everything in the given model.
   *
   * @param model
   *          the {@link GModel} to be cleared
   */
  public static void clear(final GModel model) {

    final CompoundCommand command = new CompoundCommand();

    List<GConnection> existingConnections = new ArrayList<>(model.getConnections());
    for (final GConnection connection : existingConnections) {
      command.append(RemoveCommand.create(model, owner -> model.getConnections(), connection));
    }
    List<GNode> existingNodes = new ArrayList<>(model.getNodes());
    for (final GNode node : existingNodes) {
      command.append(RemoveCommand.create(model, owner -> model.getNodes(), node));
    }
    if (command.canExecute()) {
      CommandStack.getCommandStack(model).execute(command);
    }

  }

  /**
   * Removes all connectors from the given nodes, and all connections attached to them.
   *
   * @param model
   *          the {@link GModel} being edited
   * @param nodes
   *          a list of {@link GNode} instances whose connectors should be removed
   */
  public static void clearConnectors(final GModel model, final List<GNode> nodes) {

      final CompoundCommand command = new CompoundCommand();

      final Set<GConnection> connectionsToRemove = new HashSet<>();
      final Set<GConnector> connectorsToRemove = new HashSet<>();

      List<GNode> existingNodes = new ArrayList<>(nodes);
      for (final GNode node : existingNodes) {
        List<GConnector> existingConnectors = new ArrayList<>(node.getConnectors());
        for (GConnector connector : existingConnectors) {
          command.append(RemoveCommand.create(node, owner -> ((GNode) owner).getConnectors(), connector));
        }

        connectorsToRemove.addAll(node.getConnectors());

        for (final GConnector connector : node.getConnectors()) {
          connectionsToRemove.addAll(connector.getConnections());
        }
      }

      for (final GConnection connection : connectionsToRemove) {
        final GConnector source = connection.getSource();
        final GConnector target = connection.getTarget();

        if (!connectorsToRemove.contains(source)) {
          command.append(RemoveCommand.create(source, owner -> ((GConnector) owner).getConnections(), connection));
        }

        if (!connectorsToRemove.contains(target)) {
          command.append(RemoveCommand.create(target, owner -> ((GConnector) owner).getConnections(), connection));
        }
        command.append(RemoveCommand.create(model, owner -> model.getConnections(), connection));
      }

      if (command.canExecute()) {
        CommandStack.getCommandStack(model).execute(command);
      
    }
  }

  /**
   * Updates the model's layout values to match those in the skin instances.
   *
   * <p>
   * This method adds set operations to the given compound command but does <b>not</b> execute it.
   * </p>
   *
   * @param command
   *          a {@link CompoundCommand} to which the set commands will be added
   * @param model
   *          the {@link GModel} whose layout values should be updated
   * @param skinLookup
   *          the {@link SkinLookup} in use for this graph editor instance
   */
  public static void updateLayoutValues(final CompoundCommand command, final GModel model,
      final SkinLookup skinLookup) {
      for (final GNode node : model.getNodes()) {
        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        if (nodeSkin != null && checkNodeChanged(node, nodeSkin)) {
          final Region nodeRegion = nodeSkin.getRoot();
          command.append(SetPropertyCommand.create(nodeSkin.xProperty(), nodeRegion.getLayoutX()));
          command.append(SetPropertyCommand.create(nodeSkin.yProperty(), nodeRegion.getLayoutY()));
          command.append(SetPropertyCommand.create(nodeSkin.widthProperty(), nodeRegion.getWidth()));
          command.append(SetPropertyCommand.create(nodeSkin.heightProperty(), nodeRegion.getHeight()));
        }
      }

      for (final GConnection connection : model.getConnections()) {
        updateConnector(connection.getSource(), command, skinLookup);
        updateConnector(connection.getTarget(), command, skinLookup);

        for (final GJoint joint : connection.getJoints()) {
          final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
          if (jointSkin != null && checkJointChanged(joint, jointSkin)) {
            final Region jointRegion = jointSkin.getRoot();
            final double x = jointRegion.getLayoutX() + jointSkin.getWidth() / 2;
            final double y = jointRegion.getLayoutY() + jointSkin.getHeight() / 2;

            command.append(SetPropertyCommand.create(jointSkin.xProperty(), x));
            command.append(SetPropertyCommand.create(jointSkin.yProperty(), y));
          }
        }
      }
    }

  private static void updateConnector(final GConnector connector, final CompoundCommand command,
      final SkinLookup skinLookup) {
    final GNode node = connector.getParent();
    final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
    final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
    if (nodeSkin != null && connectorSkin != null) {
      final Point2D connectorPosition = nodeSkin.getConnectorPosition(connectorSkin);
      if (checkConnectorChanged(connectorSkin, connectorPosition)) {
        command.append(SetPropertyCommand.create(connectorSkin.xProperty(), connectorPosition.getX()));
        command.append(SetPropertyCommand.create(connectorSkin.yProperty(), connectorPosition.getY()));
      }
    }
  }

  /**
   * Checks if a connector's JavaFX region has different layout values than those currently stored in the model.
   *
   * @param connector
   *          the model instance for the connector
   *
   * @return {@code true} if any layout value has changed, {@code false if not}
   */
  private static boolean checkConnectorChanged(final GConnectorSkin connectorSkin, final Point2D connectorPosition) {
    if (connectorPosition.getX() != connectorSkin.getX()) {
      return true;
    } else if (connectorPosition.getY() != connectorSkin.getY()) {
      return true;
    }
    return false;
  }

  /**
   * Checks if a node's JavaFX region has different layout values than those currently stored in the model.
   *
   * @param node
   *          the model instance for the node
   *
   * @return {@code true} if any layout value has changed, {@code false if not}
   */
  private static boolean checkNodeChanged(final GNode node, final GNodeSkin nodeSkin) {
    final Region nodeRegion = nodeSkin.getRoot();

    if (nodeRegion.getLayoutX() != nodeSkin.getX()) {
      return true;
    } else if (nodeRegion.getLayoutY() != nodeSkin.getY()) {
      return true;
    } else if (nodeRegion.getWidth() != nodeSkin.getWidth()) {
      return true;
    } else if (nodeRegion.getHeight() != nodeSkin.getHeight()) {
      return true;
    }
    return false;
  }

  /**
   * Checks if a joint's JavaFX region has different layout values than those currently stored in the model.
   *
   * @param joint
   *          the model instance for the joint
   *
   * @return {@code true} if any layout value has changed, {@code false if not}
   */
  private static boolean checkJointChanged(final GJoint joint, final GJointSkin jointSkin) {
    final Region jointRegion = jointSkin.getRoot();

    final double jointRegionX = jointRegion.getLayoutX() + jointSkin.getWidth() / 2;
    final double jointRegionY = jointRegion.getLayoutY() + jointSkin.getHeight() / 2;

    if (jointRegionX != jointSkin.getX()) {
      return true;
    } else if (jointRegionY != jointSkin.getY()) {
      return true;
    }
    return false;
  }

  /**
   * Attempts to undo the given model to its previous state.
   *
   * @param model
   *          the {@link GModel} to undo
   */
  public static void undo(final GModel model) {

    if (CommandStack.getCommandStack(model).canUndo()) {
      CommandStack.getCommandStack(model).undo();
    }
  }

  /**
   * Attempts to redo the given model to its next state.
   *
   * @param model
   *          the {@link GModel} to redo
   */
  public static void redo(final GModel model) {

    if ( CommandStack.getCommandStack(model).canRedo()) {
      CommandStack.getCommandStack(model).redo();
    }
  }

}
