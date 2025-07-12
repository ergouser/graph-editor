/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults.connection;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.command.AddCommand;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.command.CompoundCommand;
import com.ergotech.grapheditor.model.command.RemoveCommand;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import javafx.geometry.Point2D;

/**
 * A set of helper methods to add and remove joints from the default connection skin using EMF commands.
 */
public class JointCommands {

  /**
   * Static class.
   */
  private JointCommands() {
  }

  /**
   * Removes any existing joints from the connection and creates a new set of joints at the given positions.
   *
   * <p>
   * This is executed as a single compound command and is therefore a single element in the undo-redo stack.
   * </p>
   *
   * @param positions
   *          a list of {@link Point2D} instances specifying the x and y positions of the new joints
   * @param connection
   *          the connection in which the joints will be set
   */
  public static void setNewJoints(final GraphEditor graphEditor, final List<Point2D> positions, final GConnectionSkin connectionSkin) {
    final GModel model = graphEditor.getModel();
    // Create a compound command to group all operations
    final CompoundCommand command = new CompoundCommand();

    // Remove existing joints
//    List<GJointSkin> existingJointSkins = new ArrayList<>(connectionSkin.getJointSkins());
//    for (GJointSkin jointSkin : existingJointSkins) {
//      command.append(RemoveCommand.create(connectionSkin, owner -> ((GConnectionSkin) owner).getJointSkins(), jointSkin));
//    }

    final SkinManager skinManager = (SkinManager)graphEditor.getSkinLookup();
    // Create and add new joints
    for (final Point2D position : positions) {
      final GJoint newJoint = model.getGraphFactory().create(GJoint.class);
      final GJointSkin newJointSkin = skinManager.lookupOrCreateJoint(newJoint);
      newJointSkin.setX(position.getX());
      newJointSkin.setY(position.getY());

      command.append(AddCommand.create(connectionSkin, owner -> ((GConnectionSkin) owner).getJoints(), newJoint));
    }

    // Execute the command and add it to the command stack
    if (command.canExecute()) {
      try {
        CommandStack.getCommandStack(model).execute(command);
      } catch (Exception e) {
        throw new UndeclaredThrowableException(e);
      }
    }
  }

  /**
   * Removes joints from a connection.
   *
   * <p>
   * This method adds the remove operations to the given compound command and does not execute it.
   * </p>
   *
   * @param command
   *          a {@link CompoundCommand} to which the remove commands will be added
   * @param indices
   *          the indices within the connection's list of joints specifying the joints to be removed
   * @param connection
   *          the connection whose joints are to be removed
   */
  public static void removeJoints(final CompoundCommand command, final BitSet indices, final GConnectionSkin connectionSkin) {
    for (int i = 0; i < connectionSkin.getJoints().size(); i++) {
      if (indices.get(i)) {
        final GJoint joint = connectionSkin.getJoints().get(i);
        command.append(RemoveCommand.create(connectionSkin, owner -> ((GConnectionSkin) owner).getJoints(), joint));
      }
    }
    // not executed, presumably after the return...
  }
}
