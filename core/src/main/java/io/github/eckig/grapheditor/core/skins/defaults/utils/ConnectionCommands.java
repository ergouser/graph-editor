/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.command.AddCommand;
import com.ergotech.grapheditor.model.command.Command;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.command.CompoundCommand;
import com.ergotech.grapheditor.model.command.RemoveCommand;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.core.connections.ConnectionEventManager;
import io.github.eckig.grapheditor.core.skins.SkinManager;

/**
 * Provides utility methods for adding and removing connections via EMF commands.
 */
public class ConnectionCommands {

  /**
   * Static class, not to be instantiated.
   */
  private ConnectionCommands() {
  }

  /**
   * Adds a connection to the model.
   *
   * @param model
   *          the {@link GModel} to which the connection should be added
   * @param source
   *          the source {@link GConnectorPort} of the new connection
   * @param target
   *          the target {@link GConnectorPort} of the new connection
   * @param type
   *          the type attribute for the new connection
   * @param joints
   *          the list of {@link GJoint} instances to be added inside the new connection
   */
  public static void addConnection(final GModel model, final GConnectorPort source, final GConnectorPort target,
      final String type, final List<GJoint> joints, final ConnectionEventManager connectionEventManager, final SkinManager skinManager) {

    final CompoundCommand command = new CompoundCommand();

    // prepare new connection:
    final GConnection connection =model.getGraphFactory().create(GConnection.class);
    connection.setSource(source);
    connection.setTarget(target);
    List<GJointSkin> jointSkins = joints.stream()
        .map(joint -> skinManager.lookupJoint(joint))
        .collect(Collectors.toList());
    GConnectionSkin gConnectionSkin = skinManager.lookupOrCreateConnection(connection);
    gConnectionSkin.getJointSkins().addAll(jointSkins);

    // attributes that involve other members of the model, are modified through commands:
    //command.append(AddCommand.create(editingDomain, model, GraphPackage.Literals.GMODEL__CONNECTIONS, connection));
    //command.append(AddCommand.create(editingDomain, source, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, connection));
    //command.append(AddCommand.create(editingDomain, target, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, connection));
    command.append(AddCommand.create(model, owner -> model.getConnections(), connection));
    command.append(AddCommand.create(source, owner -> ((GConnectorPort) owner).getConnections(), connection));
    command.append(AddCommand.create(target, owner -> ((GConnectorPort) owner).getConnections(), connection));

    final Command onCreate;
    if (connectionEventManager != null
        && (onCreate = connectionEventManager.notifyConnectionAdded(connection)) != null) {
      command.append(onCreate);
    }

    if (command.canExecute()) {
      CommandStack.getCommandStack(model).execute(command);
    }
  }

  /**
   * Removes a connection from the model.
   *
   * @param model
   *          the {@link GModel} from which the connection should be removed
   * @param connection
   *          the {@link GConnection} to be removed
   * @param connectionEventManager
   */
  public static void removeConnection(final GModel model, final GConnection connection,
      ConnectionEventManager connectionEventManager) {

    final CompoundCommand command = new CompoundCommand();

    final GConnectorPort source = connection.getSource();
    final GConnectorPort target = connection.getTarget();

    //      command.append(RemoveCommand.create(editingDomain, model, GraphPackage.Literals.GMODEL__CONNECTIONS, connection));
    //      command.append(
    //          RemoveCommand.create(editingDomain, source, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, connection));
    //      command.append(
    //          RemoveCommand.create(editingDomain, target, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, connection));
    command.append(RemoveCommand.create(model, owner -> model.getConnections(), connection));
    command.append(RemoveCommand.create(source, owner -> ((GConnectorPort) owner).getConnections(), connection));
    command.append(RemoveCommand.create(target, owner -> ((GConnectorPort) owner).getConnections(), connection));



    final Command onRemove;
    if (connectionEventManager != null
        && (onRemove = connectionEventManager.notifyConnectionRemoved(connection)) != null) {
      command.append(onRemove);
    }

    if (command.canExecute()) {
      CommandStack.getCommandStack(model).execute(command);
    }

  }
}
