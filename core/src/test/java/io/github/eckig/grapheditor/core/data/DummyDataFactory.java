/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.command.AddCommand;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.command.CompoundCommand;

import javafx.geometry.Point2D;

public class DummyDataFactory {

  private static final String TEST_FILE = "test.graph";
  private static final String INPUT_TYPE = "left-input";
  private static final String OUTPUT_TYPE = "right-output";

  private static GJoint createJoint(double x, double y, GConnection connection) {
    GJoint joint = new GJoint();
    joint.setX(x);
    joint.setY(y);
    joint.setConnection(connection);
    return joint;
  }

  private static void printModel(GModel model) {
    System.out.println("GModel:");
    for (GNode node : model.getNodes()) {
      System.out.println("  GNode:");
      System.out.println("    x: " + node.getX());
      System.out.println("    y: " + node.getY());
      System.out.println("    width: " + node.getWidth());
      System.out.println("    height: " + node.getHeight());
      System.out.println("    connectors:");
      for (GConnectorPort connector : node.getConnectorPorts()) {
        System.out.println("      GConnector:");
        System.out.println("        type: " + connector.getType());
        System.out.println("        connections:");
        for (GConnection connection : connector.getConnections()) {
          System.out.println("          GConnection id: " + connection);
        }
      }
    }
    System.out.println("Connections:");
    for (GConnection connection : model.getConnections()) {
      System.out.println("  GConnection:");
      System.out.println("    source: " + connection.getSourcePort());
      System.out.println("    target: " + connection.getTargetPort());
      System.out.println("    joints:");
      for (GJoint joint : connection.getJoints()) {
        System.out.println("      GJoint:");
        System.out.println("        x: " + joint.getX());
        System.out.println("        y: " + joint.getY());
      }
    }
  }

  private static GConnectorPort createConnector(String type, GNode node, CompoundCommand compoundCommand) {
    GConnectorPort connector = new GConnectorPort();
    connector.setType(type);
    connector.setParent(node);

    // Add the connector to the node's connectors using a command
    compoundCommand.append(AddCommand.create(node, owner -> ((GNode) owner).getConnectorPorts(), connector));

    return connector;
  }


  public static GNode createNode() {

    final GNode node = new GNode();

    final GConnectorPort input = new GConnectorPort();
    input.setType(INPUT_TYPE);

    final GConnectorPort output = new GConnectorPort();
    output.setType(OUTPUT_TYPE);

    node.getConnectorPorts().add(input);
    node.getConnectorPorts().add(output);

    return node;
  }
  /**
   * Creates a new dummy model instance (matching the original test file).
   *
   * @return a new dummy {@link GModel} instance from a test file
   */
  public static GModel createModel() {
    // Create the model
    GModel model = new GModel();

    // Create a command stack to manage undo/redo
    CommandStack commandStack = CommandStack.getCommandStack(model);

    // Create a compound command to group all additions
    CompoundCommand compoundCommand = new CompoundCommand();

    // Create lists to hold nodes and connectors for easy reference
    List<GNode> nodes = new ArrayList<>();
    Map<Integer, List<GConnectorPort>> nodeConnectors = new HashMap<>();

    // Node IDs to keep track (0 to 5)
    for (int nodeId = 0; nodeId <= 5; nodeId++) {
      // Create a GNode
      GNode node = new GNode();

      // Set node properties based on the XML data
      switch (nodeId) {
        case 0:
          node.setX(59.0);
          node.setY(99.0);
          node.setWidth(161.0);
          node.setHeight(121.0);
          break;
        case 1:
          node.setX(89.0);
          node.setY(359.0);
          node.setWidth(301.0);
          node.setHeight(201.0);
          break;
        case 2:
          node.setX(609.0);
          node.setY(59.0);
          node.setWidth(161.0);
          node.setHeight(91.0);
          break;
        case 3:
          node.setX(609.0);
          node.setY(229.0);
          node.setWidth(161.0);
          node.setHeight(171.0);
          break;
        case 4:
          node.setX(609.0);
          node.setY(459.0);
          node.setWidth(161.0);
          node.setHeight(111.0);
          break;
        case 5:
          node.setX(319.0);
          node.setY(79.0);
          node.setWidth(111.0);
          node.setHeight(101.0);
          break;
      }

      // Add the node to the model using an AddCommand
      compoundCommand.append(AddCommand.create(model, owner -> ((GModel) owner).getNodes(), node));
      
      // Create connectors for the node
      List<GConnectorPort> connectors = new ArrayList<>();
      switch (nodeId) {
        case 0:
          // Node 0 connectors
          connectors.add(createConnector(INPUT_TYPE, node, compoundCommand));
          connectors.add(createConnector(OUTPUT_TYPE, node, compoundCommand));
          break;
        case 1:
          // Node 1 connectors
          connectors.add(createConnector(INPUT_TYPE, node, compoundCommand));
          connectors.add(createConnector(OUTPUT_TYPE, node, compoundCommand));
          connectors.add(createConnector(OUTPUT_TYPE, node, compoundCommand));
          break;
        case 2:
          // Node 2 connectors
          connectors.add(createConnector(INPUT_TYPE, node, compoundCommand));
          connectors.add(createConnector(OUTPUT_TYPE, node, compoundCommand));
          break;
        case 3:
          // Node 3 connectors
          connectors.add(createConnector(INPUT_TYPE, node, compoundCommand));
          connectors.add(createConnector(OUTPUT_TYPE, node, compoundCommand));
          break;
        case 4:
          // Node 4 connectors
          connectors.add(createConnector(INPUT_TYPE, node, compoundCommand));
          connectors.add(createConnector(OUTPUT_TYPE, node, compoundCommand));
          break;
        case 5:
          // Node 5 connectors
          connectors.add(createConnector(INPUT_TYPE, node, compoundCommand));
          connectors.add(createConnector(OUTPUT_TYPE, node, compoundCommand));
          break;
      }

      nodeConnectors.put(nodeId, connectors);
    }

    // Now create connections and add them using commands
    // We need to reference the connectors we created above

    // Create connections
    List<GConnection> connections = new ArrayList<>();

    // Connection 0: from node0 connector1 to node1 connector0
    GConnection connection0 = new GConnection();
    GConnectorPort source0 = nodeConnectors.get(0).get(1); // node0's output connector
    GConnectorPort target0 = nodeConnectors.get(1).get(0); // node1's input connector
    connection0.setSourcePort(source0);
    connection0.setTargetPort(target0);

     // Add the connection to the model, source, and target using commands
    compoundCommand.append(AddCommand.create(model, owner -> ((GModel) owner).getConnections(), connection0));
    compoundCommand.append(AddCommand.create(source0, owner -> ((GConnectorPort) owner).getConnections(), connection0));
    compoundCommand.append(AddCommand.create(target0, owner -> ((GConnectorPort) owner).getConnections(), connection0));

    // Add joints to the connection using commands
    List<Point2D> joints0 = Arrays.asList(
        new Point2D(270.0, 160.0),
        new Point2D(270.0, 300.0),
        new Point2D(40.0, 300.0),
        new Point2D(40.0, 460.0)
        );
    for (Point2D point : joints0) {
      GJoint joint = new GJoint();
      joint.setX(point.getX());
      joint.setY(point.getY());
      joint.setConnection(connection0);
      compoundCommand.append(AddCommand.create(connection0, owner -> ((GConnection) owner).getJoints(), joint));
    }

    // Connection 1: from node5 connector1 to node4 connector0
    GConnection connection1 = new GConnection();
    GConnectorPort source1 = nodeConnectors.get(5).get(1); // node5's output connector
    GConnectorPort target1 = nodeConnectors.get(4).get(0); // node4's input connector
    connection1.setSourcePort(source1);
    connection1.setTargetPort(target1);

    // Add the connection and its associations
    compoundCommand.append(AddCommand.create(model, owner -> ((GModel) owner).getConnections(), connection1));
    compoundCommand.append(AddCommand.create(source1, owner -> ((GConnectorPort) owner).getConnections(), connection1));
    compoundCommand.append(AddCommand.create(target1, owner -> ((GConnectorPort) owner).getConnections(), connection1));

    // Add joints to connection1
    List<Point2D> joints1 = Arrays.asList(
        new Point2D(490.0, 130.0),
        new Point2D(490.0, 515.0)
        );
    for (Point2D point : joints1) {
      GJoint joint = new GJoint();
      joint.setX(point.getX());
      joint.setY(point.getY());
      joint.setConnection(connection1);
      compoundCommand.append(AddCommand.create(connection1, owner -> ((GConnection) owner).getJoints(), joint));
    }

    // Connection 2: from node1 connector2 to node2 connector0
    GConnection connection2 = new GConnection();
    GConnectorPort source2 = nodeConnectors.get(1).get(2); // node1's second output connector
    GConnectorPort target2 = nodeConnectors.get(2).get(0); // node2's input connector
    connection2.setSourcePort(source2);
    connection2.setTargetPort(target2);

    // Add the connection and its associations
    compoundCommand.append(AddCommand.create(model, owner -> ((GModel) owner).getConnections(), connection2));
    compoundCommand.append(AddCommand.create(source2, owner -> ((GConnectorPort) owner).getConnections(), connection2));
    compoundCommand.append(AddCommand.create(target2, owner -> ((GConnectorPort) owner).getConnections(), connection2));

    // Add joints to connection2
    List<Point2D> joints2 = Arrays.asList(
        new Point2D(470.0, 494.0),
        new Point2D(470.0, 105.0)
        );
    for (Point2D point : joints2) {
      GJoint joint = new GJoint();
      joint.setX(point.getX());
      joint.setY(point.getY());
      joint.setConnection(connection2);
      compoundCommand.append(AddCommand.create(connection2, owner -> ((GConnection) owner).getJoints(), joint));
    }

    // Connection 3: from node1 connector1 to node3 connector0
    GConnection connection3 = new GConnection();
    GConnectorPort source3 = nodeConnectors.get(1).get(1); // node1's first output connector
    GConnectorPort target3 = nodeConnectors.get(3).get(0); // node3's input connector
    connection3.setSourcePort(source3);
    connection3.setTargetPort(target3);

    // Add the connection and its associations
    compoundCommand.append(AddCommand.create(model, owner -> ((GModel) owner).getConnections(), connection3));
    compoundCommand.append(AddCommand.create(source3, owner -> ((GConnectorPort) owner).getConnections(), connection3));
    compoundCommand.append(AddCommand.create(target3, owner -> ((GConnectorPort) owner).getConnections(), connection3));

    // Add joints to connection3
    List<Point2D> joints3 = Arrays.asList(
        new Point2D(440.0, 427.0),
        new Point2D(440.0, 315.0)
        );
    for (Point2D point : joints3) {
      GJoint joint = new GJoint();
      joint.setX(point.getX());
      joint.setY(point.getY());
      joint.setConnection(connection3);
      compoundCommand.append(AddCommand.create(connection3, owner -> ((GConnection) owner).getJoints(), joint));
    }

    // Execute the compound command to build the model
    if (compoundCommand.canExecute()) {
      commandStack.execute(compoundCommand);
    } else {
      System.err.println("Cannot execute compound command.");
    }

    printModel(model);
    // Return the built model
    return model;
  }



}
