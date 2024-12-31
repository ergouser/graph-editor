/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.demo.customskins.tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.GraphFactory;
import com.ergotech.grapheditor.model.command.AddCommand;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.command.CompoundCommand;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import io.github.eckig.grapheditor.demo.utils.AwesomeIcon;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Node skin for a 'tree-like' graph.
 */
public class TreeNodeSkin extends GNodeSkin {

  private static final String STYLE_CLASS_BORDER = "tree-node-border"; //$NON-NLS-1$
  private static final String STYLE_CLASS_BACKGROUND = "tree-node-background"; //$NON-NLS-1$
  private static final String STYLE_CLASS_SELECTION_HALO = "tree-node-selection-halo"; //$NON-NLS-1$
  private static final String STYLE_CLASS_BUTTON = "tree-node-button"; //$NON-NLS-1$

  private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected"); //$NON-NLS-1$

  private static final double HALO_OFFSET = 5;
  private static final double HALO_CORNER_SIZE = 10;

  private static final double MIN_WIDTH = 81;
  private static final double MIN_HEIGHT = 61;

  // Child nodes will be added this far below their parent.
  private static final double CHILD_Y_OFFSET = 80;

  private static final double VIEW_PADDING = 15;

  private final Rectangle selectionHalo = new Rectangle();
  private final Button addChildButton = new Button();

  private GConnectorSkin inputConnectorSkin;
  private GConnectorSkin outputConnectorSkin;

  // Border and background are separated into 2 rectangles so they can have different effects applied to them.
  private final Rectangle border = new Rectangle();
  private final Rectangle background = new Rectangle();

  /**
   * Creates a new {@link TreeNodeSkin} instance.
   *
   * @param node the {link GNode} this skin is representing
   */
  public TreeNodeSkin(final GNode node) {

    super(node);

    background.widthProperty().bind(border.widthProperty().subtract(border.strokeWidthProperty().multiply(2)));
    background.heightProperty().bind(border.heightProperty().subtract(border.strokeWidthProperty().multiply(2)));

    border.widthProperty().bind(getRoot().widthProperty());
    border.heightProperty().bind(getRoot().heightProperty());

    border.getStyleClass().setAll(STYLE_CLASS_BORDER);
    background.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);

    getRoot().getChildren().addAll(border, background);
    getRoot().setMinSize(MIN_WIDTH, MIN_HEIGHT);

    addSelectionHalo();
    addButton();

    background.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::filterMouseDragged);
  }

  @Override
  public void setConnectorSkins(final List<GConnectorSkin> connectorSkins) {

    removeConnectors();

    if (connectorSkins == null || connectorSkins.isEmpty() || connectorSkins.size() > 2) {
      return;
    }

    for (final GConnectorSkin skin : connectorSkins) {
      if (TreeSkinConstants.TREE_OUTPUT_CONNECTOR.equals(skin.getType())) {
        outputConnectorSkin = skin;
        getRoot().getChildren().add(skin.getRoot());
      } else if (TreeSkinConstants.TREE_INPUT_CONNECTOR.equals(skin.getType())) {
        inputConnectorSkin = skin;
        getRoot().getChildren().add(skin.getRoot());
      }
    }
  }

  @Override
  public void layoutConnectors() {
    layoutTopAndBottomConnectors();
    layoutSelectionHalo();
  }

  @Override
  public Point2D getConnectorPosition(final GConnectorSkin connectorSkin) {

    final Node connectorRoot = connectorSkin.getRoot();

    final double x = connectorRoot.getLayoutX() + connectorSkin.getWidth() / 2;
    final double y = connectorRoot.getLayoutY() + connectorSkin.getHeight() / 2;

    return new Point2D(x, y);
  }

  /**
   * Lays out the connectors. Inputs on top, outputs on the bottom.
   */
  private void layoutTopAndBottomConnectors() {

    if (inputConnectorSkin != null) {

      final double inputX = (getRoot().getWidth() - inputConnectorSkin.getWidth()) / 2;
      final double inputY = -inputConnectorSkin.getHeight() / 2;

      inputConnectorSkin.getRoot().setLayoutX(inputX);
      inputConnectorSkin.getRoot().setLayoutY(inputY);
    }

    if (outputConnectorSkin != null) {

      final double outputX = (getRoot().getWidth() - outputConnectorSkin.getWidth()) / 2;
      final double outputY = getRoot().getHeight() - outputConnectorSkin.getHeight() / 2;

      outputConnectorSkin.getRoot().setLayoutX(outputX);
      outputConnectorSkin.getRoot().setLayoutY(outputY);
    }
  }

  /**
   * Adds the selection halo and initializes some of its values.
   */
  private void addSelectionHalo() {

    getRoot().getChildren().add(selectionHalo);

    selectionHalo.setManaged(false);
    selectionHalo.setMouseTransparent(false);
    selectionHalo.setVisible(false);

    selectionHalo.setLayoutX(-HALO_OFFSET);
    selectionHalo.setLayoutY(-HALO_OFFSET);

    selectionHalo.getStyleClass().add(STYLE_CLASS_SELECTION_HALO);
  }

  /**
   * Lays out the selection halo based on the current width and height of the node skin region.
   */
  private void layoutSelectionHalo() {

    if (selectionHalo.isVisible()) {

      selectionHalo.setWidth(border.getWidth() + 2 * HALO_OFFSET);
      selectionHalo.setHeight(border.getHeight() + 2 * HALO_OFFSET);

      final double cornerLength = 2 * HALO_CORNER_SIZE;
      final double xGap = border.getWidth() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;
      final double yGap = border.getHeight() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;

      selectionHalo.setStrokeDashOffset(HALO_CORNER_SIZE);
      selectionHalo.getStrokeDashArray().setAll(cornerLength, yGap, cornerLength, xGap);
    }
  }

  @Override
  protected void selectionChanged(boolean isSelected) {
    if (isSelected) {
      background.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
      selectionHalo.setVisible(true);
      layoutSelectionHalo();
      getRoot().toFront();
    } else {
      background.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
      selectionHalo.setVisible(false);
    }
  }

  /**
   * Removes any input and output connectors from the list of children, if they exist.
   */
  private void removeConnectors() {

    if (inputConnectorSkin != null) {
      getRoot().getChildren().remove(inputConnectorSkin.getRoot());
    }

    if (outputConnectorSkin != null) {
      getRoot().getChildren().remove(outputConnectorSkin.getRoot());
    }
  }

  /**
   * Adds a button to the node skin that will add a child node when pressed.
   */
  private void addButton() {

    StackPane.setAlignment(addChildButton, Pos.BOTTOM_RIGHT);

    addChildButton.getStyleClass().setAll(STYLE_CLASS_BUTTON);
    addChildButton.setCursor(Cursor.DEFAULT);
    addChildButton.setPickOnBounds(false);

    addChildButton.setGraphic(AwesomeIcon.PLUS.node());
    addChildButton.setOnAction(event -> addChildNode());

    getRoot().getChildren().add(addChildButton);
  }

  /**
   * Adds a child node with one input and one output connector, placed directly underneath its parent.
   */
  private void addChildNode() {

    final GraphFactory factory = graphEditor.getModel().getGraphFactory();
    final SkinManager skinManager = (SkinManager)graphEditor.getSkinLookup();

    final GNode childNode = factory.create(GNode.class);
    final GNodeSkin childNodeSkin = skinManager.lookupOrCreateNode(childNode);
    childNodeSkin.setType(TreeSkinConstants.TREE_NODE);
    childNodeSkin.setX(getX() + (getWidth() - childNodeSkin.getWidth()) / 2);
    childNodeSkin.setY(getY() + getHeight() + CHILD_Y_OFFSET);

    final GModel model = getGraphEditor().getModel();
    final double maxAllowedY = getGraphEditor().getView().getHeight() - VIEW_PADDING;

    if (childNodeSkin.getY() + childNodeSkin.getHeight() > maxAllowedY) {
      childNodeSkin.setY(maxAllowedY - childNodeSkin.getHeight());
    }

    final GConnectorPort input = factory.create(GConnectorPort.class);
    final GConnectorPort output = factory.create(GConnectorPort.class);
    final GConnectorSkin inputConnectorSkin = skinManager.lookupOrCreateConnector(input);
    final GConnectorSkin outputConnectorSkin = skinManager.lookupOrCreateConnector(output);

    inputConnectorSkin.setType(TreeSkinConstants.TREE_INPUT_CONNECTOR);
    outputConnectorSkin.setType(TreeSkinConstants.TREE_OUTPUT_CONNECTOR);

    childNode.addConnectorPort(input);
    childNode.addConnectorPort(output);

    // This allows multiple connections to be created from the output.
    outputConnectorSkin.setConnectionDetachedOnDrag(false);

    final GConnectorPort parentOutput = findOutput();
    final GConnection connection = factory.create(GConnection.class);
    final GConnectionSkin connectionSkin = skinManager.lookupOrCreateConnection(connection);

    connectionSkin.setType(TreeSkinConstants.TREE_CONNECTION);
    connection.setSource(parentOutput);
    connection.setTarget(input);

    input.addConnection(connection);

    // Set the rest of the values via EMF commands because they touch the currently-edited model.
    final CompoundCommand command = new CompoundCommand();

    //command.append(AddCommand.create(editingDomain, model, NODES, childNode));
    command.append(AddCommand.create(model, owner -> model.getNodes(), childNode));
    //command.append(AddCommand.create(editingDomain, model, CONNECTIONS, connection));
    command.append(AddCommand.create(model, owner -> model.getConnections(), connection));
    //command.append(AddCommand.create(editingDomain, parentOutput, CONNECTOR_CONNECTIONS, connection));  ??????
    command.append(AddCommand.create(parentOutput, owner -> parentOutput.getConnections(), connection));

    if (command.canExecute()) {
      CommandStack.getCommandStack(model).execute(command);
    }
  }

  /**
   * Finds the output connector of this skin's node.
   *
   * <p>
   * Assumes the node has 1 or 2 connectors, and if there are 2 connectors the second is the output. Bit dodgy but
   * only used in the demo.
   * </p>
   */
  private GConnectorPort findOutput() {
      Collection<? extends GConnectorPort> connectorPorts = getItem().getConnectorPorts(); // assumes a collection with a rational iteration order

      if (connectorPorts.size() == 1) {
          return connectorPorts.iterator().next(); // Return the first and only connector
      } else if (connectorPorts.size() == 2) {
          Iterator<? extends GConnectorPort> iterator = connectorPorts.iterator();
          iterator.next(); // Skip the first connector
          return iterator.next(); // Return the second connector
      } else {
          return null;
      }
  }

  /**
   * Stops the node being dragged if it isn't selected.
   *
   * @param event a mouse-dragged event on the node
   */
  private void filterMouseDragged(final MouseEvent event) {
    if (event.isPrimaryButtonDown() && !isSelected()) {
      event.consume();
    }
  }
}
