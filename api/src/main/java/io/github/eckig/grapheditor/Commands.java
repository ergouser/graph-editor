/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.command.AddCommand;
import com.ergotech.grapheditor.model.command.Command;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.command.CompoundCommand;
import com.ergotech.grapheditor.model.command.RemoveCommand;
import com.ergotech.grapheditor.model.command.SetCommand;
import com.ergotech.grapheditor.model.command.SetPropertyCommand;

import io.github.eckig.grapheditor.utils.DraggableBox;
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

    executeCommand(model, command);

  }

  /** Execute the provided command through the command Stack .*/
  public static void executeCommand(final GModel model, final Command command) {
    if (command.canExecute()) {
      try {
        CommandStack.getCommandStack(model).execute(command);
      } catch ( Exception e ) {
        // keep the default behavior of addNode, where there is no error handling, but permit it 
        throw new UndeclaredThrowableException(e);
      }
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
  public static  void removeNode(final GModel model, final GNode node ) {
    removeNode(model, node, null);
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
   * @param baseCommand
   *          a command provided as part of a larger removal process (eg clearing the model)
   *          the command will not be executed by this method if passed in
   */
  public static  void removeNode(final GModel model, final GNode node, final CompoundCommand baseCommand) {

    final CompoundCommand command;
    if ( baseCommand != null ) {
      command = baseCommand;
    } else {
      command = new CompoundCommand();
    }

    final List<GConnection> connectionsToDelete = new ArrayList<>();

    // Iterate through the connector ports and remove all the connections
    for (final GConnectorPort connector : node.getConnectorPorts()) {
      // Create an undoable command for the connectioin removal
      for (final GConnection connection : connector.getConnections()) {
        if (connection != null && !connectionsToDelete.contains(connection)) {
          command.append(RemoveCommand.create(connector, owner -> ((GConnectorPort) owner).getConnections(), connection));
          connectionsToDelete.add(connection);
        }
      }
    }
    // remove the node...
    command.append(RemoveCommand.create(model, owner -> model.getNodes(), node));

    // execute the command if it can be executed and it isn't part of a larger removal
    if (baseCommand == null && command.canExecute()) {
      try {
        CommandStack.getCommandStack(model).execute(command);
      } catch ( Exception e ) {
        // keep the default behavior of removeNode, where there is no error handling, but permit it 
        throw new UndeclaredThrowableException(e);
      }
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

    List<GNode> existingNodes = new ArrayList<>(model.getNodes());
    for (final GNode node : existingNodes) {
      removeNode(model, node, command);
    }
    if (command.canExecute()) {
      try {
        CommandStack.getCommandStack(model).execute(command);
      } catch ( Exception e ) {
        // keep the default behavior of clear, where there is no error handling, but permit it 
        throw new UndeclaredThrowableException(e);
      }
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
    //final Set<GConnectorPort connectorsToRemove = new HashSet<>();

    List<GNode> existingNodes = new ArrayList<>(model.getNodes());
    for (final GNode node : existingNodes) {
      List<GConnectorPort> existingConnectors = new ArrayList<>(node.getConnectorPorts());
      for (GConnectorPort connector : existingConnectors) {
        for (final GConnection connection : connector.getConnections()) {
          if (connection != null && !connectionsToRemove.contains(connection)) {
            command.append(RemoveCommand.create(connection, owner -> ((GConnectorPort) owner).getConnections(), connection));
            connectionsToRemove.add(connection);
          }
        }
        command.append(RemoveCommand.create(node, owner -> ((GNode) owner).getConnectorPorts(), connector));
      }
    }
    if (command.canExecute()) {
      try {
        CommandStack.getCommandStack(model).execute(command);
      } catch ( Exception e ) {
        // keep the default behavior of clearConnectors, where there is no error handling, but permit it 
        throw new UndeclaredThrowableException(e);
      }
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
    @SuppressWarnings("unchecked")
    List<GNode> existingNodes = new ArrayList<>(model.getNodes());
    for (final GNode node : existingNodes) {
      final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
      if (nodeSkin != null && checkNodeChanged(node, nodeSkin)) {
        final DraggableBox nodeRegion = nodeSkin.getRoot();

        if (nodeSkin.xProperty().get() != nodeRegion.getLayoutX()) {
          //command.append(SetPropertyCommand.create(nodeSkin.xProperty(), nodeRegion.getLayoutX()));
          command.append(SetCommand.create (nodeSkin.xProperty().get(),nodeRegion::setLayoutX,nodeRegion.getLayoutX()));
        }

        if (nodeSkin.yProperty().get() != nodeRegion.getLayoutY()) {
          //command.append(SetPropertyCommand.create(nodeSkin.yProperty(), nodeRegion.getLayoutY()));
          command.append(SetCommand.create (nodeSkin.yProperty().get(),nodeRegion::setLayoutY,nodeRegion.getLayoutY()));
        }

        if (nodeSkin.widthProperty().get() != nodeRegion.getWidth()) {
          //command.append(SetPropertyCommand.create(nodeSkin.widthProperty(), nodeRegion.getWidth()));
          command.append(SetCommand.create (nodeSkin.widthProperty().get(),nodeRegion::setBoxWidth,nodeRegion.getWidth()));
        }

        if (nodeSkin.heightProperty().get() != nodeRegion.getHeight()) {
          //command.append(SetPropertyCommand.create(nodeSkin.heightProperty(), nodeRegion.getHeight()));
          command.append(SetCommand.create (nodeSkin.heightProperty().get(),nodeRegion::setBoxHeight,nodeRegion.getHeight()));
        }
      }
      for (GConnectorPort connector : node.getConnectorPorts()) {
        updateConnector(connector, command, skinLookup);  // update the position of all the connectors first, and only once.
      }
      for (GConnectorPort connector : node.getConnectorPorts()) {
        for (final GConnection connection : connector.getConnections()) {
          //          updateConnector(connection.getSource(), command, skinLookup);
          //          updateConnector(connection.getTarget(), command, skinLookup);
          final GConnectionSkin connectionSkin = skinLookup.lookupConnection(connection);
          if ( connectionSkin != null ) {

            List<GJointSkin> jointSkins = connectionSkin.getJoints().stream()
                .map(joint -> skinLookup.lookupJoint(joint))
                .collect(Collectors.toList());
            for (final GJointSkin jointSkin : jointSkins) {
              if (jointSkin != null && checkJointChanged(jointSkin)) {
                final Region jointRegion = jointSkin.getRoot();
                final double x = jointRegion.getLayoutX() + jointSkin.getWidth() / 2;
                final double y = jointRegion.getLayoutY() + jointSkin.getHeight() / 2;

                if (jointSkin.xProperty().get() != x) {
                  command.append(SetPropertyCommand.create(jointSkin.xProperty(), x));
                }

                if (jointSkin.yProperty().get() != y) {
                  command.append(SetPropertyCommand.create(jointSkin.yProperty(), y));
                }
              }
            }
          }
        }
      }
    }

  }

  /**
   * Updates the connector's position values to match the corresponding node skin layout.
   *
   * <p>
   * This method checks if the connector's position values differ from the calculated values
   * in the associated node skin. If so, it appends set commands to the given compound command
   * to update the position.
   * </p>
   *
   * @param connector
   *          the {@link GConnectorPort} whose position values need to be updated
   * @param command
   *          a {@link CompoundCommand} to which the set commands will be added
   * @param skinLookup
   *          the {@link SkinLookup} in use for this graph editor instance
   */
  private static void updateConnector(final GConnectorPort connector, final CompoundCommand command,
      final SkinLookup skinLookup) {
    final GNode node = connector.getParent();
    final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
    final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
    if (nodeSkin != null && connectorSkin != null) {
      final Point2D connectorPosition = nodeSkin.getConnectorPosition(connectorSkin);
      if (checkConnectorChanged(connectorSkin, connectorPosition)) {
        if (connectorSkin.xProperty().get() != connectorPosition.getX()) {
          command.append(SetPropertyCommand.create(connectorSkin.xProperty(), connectorPosition.getX()));
        }

        if (connectorSkin.yProperty().get() != connectorPosition.getY()) {
          command.append(SetPropertyCommand.create(connectorSkin.yProperty(), connectorPosition.getY()));
        }
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
   * @param jointSkin
   *          the instance of the jointSkin
   *
   * @return {@code true} if any layout value has changed, {@code false if not}
   */
  private static boolean checkJointChanged(final GJointSkin jointSkin) {
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
      try {
        CommandStack.getCommandStack(model).undo();
      } catch ( Exception e ) {
        // keep the default behavior of undo, where there is no error handling, but permit it 
        throw new UndeclaredThrowableException(e);
      }
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
      try {
        CommandStack.getCommandStack(model).redo();
      } catch ( Exception e ) {
        // keep the default behavior of redo, where there is no error handling, but permit it 
        throw new UndeclaredThrowableException(e);
      }
    }
  }

}
