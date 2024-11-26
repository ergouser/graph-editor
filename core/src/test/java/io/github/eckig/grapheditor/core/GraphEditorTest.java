/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.Selectable;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.impl.GConnectionImpl;
import com.ergotech.grapheditor.model.impl.GConnectorImpl;
import com.ergotech.grapheditor.model.impl.GJointImpl;
import com.ergotech.grapheditor.model.impl.GNodeImpl;

import io.github.eckig.grapheditor.Commands;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.data.DummyDataFactory;
import io.github.eckig.grapheditor.core.skins.defaults.utils.ConnectionCommands;
import io.github.eckig.grapheditor.core.utils.FXTestUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 * This test treats the graph editor as a single unit.
 *
 * <p>
 * A fully-functional {@link DefaultGraphEditor} instance is created and populated with a dummy {@link GModel}. We make
 * some changes to the model and check that the graph editor is reinitialized correctly.
 * </p>
 */
public class GraphEditorTest {

  private GraphEditor graphEditor;

  private GModel model;

  private SkinLookup skinLookup;

  private CommandStack commandStack;

  @Before
  public void setUp() throws InterruptedException {
    final CountDownLatch waitInit = new CountDownLatch(1);
    try {
      Platform.startup(waitInit::countDown);
    } catch (Exception e) {
      waitInit.countDown();
    }

    graphEditor = new DefaultGraphEditor();
    model = DummyDataFactory.createModel();
    skinLookup = graphEditor.getSkinLookup();

    graphEditor.setModel(model);

    commandStack = CommandStack.getCommandStack(model);

    waitInit.await();

    reloadEditor();

    graphEditor.getView().autosize();
    graphEditor.getView().layout();
  }

  private void reloadEditor() throws InterruptedException {
    final CountDownLatch wait = new CountDownLatch(1);
    Platform.runLater(() -> {
      graphEditor.reload();
      wait.countDown();
    });
    wait.await();
  }

  @Test
  public void checkInitializedCorrectly() {

    assertNotNull("Command stack should exist.", commandStack);
    assertNotNull("Skin lookup should exist.", skinLookup);
  }

  @Test
  public void undoRedoNode() throws InterruptedException {

    final GNodeImpl node = addNodeToModel();
    reloadEditor();

    assertNotNull("Node skin instance should exist.", skinLookup.lookupNode(node));
    assertTrue("Undo should be possible.", commandStack.canUndo());

    CommandStack.getCommandStack(model).undo();
    reloadEditor();

    assertFalse("Node should have been removed.", model.getNodes().contains(node));
    assertNull("Node skin instance should no longer exist.", skinLookup.lookupNode(node));
    assertTrue("Redo should be possible.", commandStack.canRedo());

    CommandStack.getCommandStack(model).redo();
    reloadEditor();

    assertTrue("Node should be back in again.", model.getNodes().contains(node));
    assertNotNull("Node skin instance should exist again.", skinLookup.lookupNode(node));
  }

  @Test
  public void undoRedoConnection() throws InterruptedException {

    Commands.clear(model);

    final GNodeImpl firstNode = addNodeToModel();
    final GNodeImpl secondNode = addNodeToModel();

    final GConnectorImpl firstNodeOutput = firstNode.getConnectors().get(1);
    final GConnectorImpl secondNodeInput = secondNode.getConnectors().get(0);

    final List<GJointImpl> joints = new ArrayList<>();
    joints.add(new GJointImpl());
    joints.add(new GJointImpl());

    ConnectionCommands.addConnection(model, firstNodeOutput, secondNodeInput, null, joints, null);
    reloadEditor();

    assertFalse("A connection should be present.", model.getConnections().isEmpty());

    final GConnectionImpl connection = model.getConnections().get(0);

    assertNotNull("Connection skin instance should exist.", skinLookup.lookupConnection(connection));
    assertTrue("Undo should be possible.", commandStack.canUndo());

    CommandStack.getCommandStack(model).undo();
    reloadEditor();

    assertFalse("Connection should have been removed.", model.getConnections().contains(connection));
    assertNull("Connection skin instance should no longer exist.", skinLookup.lookupConnection(connection));
    assertTrue("Redo should be possible.", commandStack.canRedo());

    CommandStack.getCommandStack(model).redo();
    reloadEditor();

    assertTrue("Connection should be back in again.", model.getConnections().contains(connection));
    assertNotNull("Connection skin instance should exist again.", skinLookup.lookupConnection(connection));
  }

  @Test
  public void selectAllAndDelete() {

    graphEditor.getSelectionManager().selectAll();
    final List<Selectable> selection = new ArrayList<>(graphEditor.getSelectionManager().getSelectedItems());
    graphEditor.delete(selection);

    assertTrue("All nodes should have gone.", model.getNodes().isEmpty());
    assertTrue("All connections should have gone.", model.getConnections().isEmpty());
  }

  @Test
  public void moveJointAndUpdateLayout() {

    ObservableList<GConnectionImpl> connections = model.getConnections();
    //    for (GConnection connection : connections) {
    //      System.out.println("Connection: " + connection + " Skin " + skinLookup.lookupConnection(connection));
    //      for (GJoint joint : connection.getJoints()) {
    //        System.out.println("Joint: " + joint + " Skin " + skinLookup.lookupJoint(joint));
    //
    //      }
    //    }
    final GJointImpl firstJoint = connections.get(0).getJoints().get(0);
    final GJointImpl secondJoint = connections.get(0).getJoints().get(1);

    final double secondJointInitialX = skinLookup.lookupJoint(secondJoint).getRoot().getLayoutX();

    FXTestUtils.dragBy(skinLookup.lookupJoint(firstJoint).getRoot(), 17, 0);

    // This will call layoutChildren method of view and trigger connection redraw.
    graphEditor.getView().layout();

    final double secondJointFinalX = skinLookup.lookupJoint(secondJoint).getRoot().getLayoutX();

    assertTrue("Second joint should have moved right by 17 pixels.", secondJointFinalX == secondJointInitialX + 17);
  }

  /**
   * Adds a node to the model that has an input and output connector.
   *
   * @return the newly-added node
   */
  private GNodeImpl addNodeToModel() {
    final GNodeImpl node = DummyDataFactory.createNode();
    Commands.addNode(model, node);
    return node;
  }
}
