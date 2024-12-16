package io.github.eckig.grapheditor.demo.customskins;


import com.ergotech.grapheditor.model.GConnector;
import com.ergotech.grapheditor.model.GConnector.Direction;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.GraphFactory;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.command.CompoundCommand;
import com.ergotech.grapheditor.model.command.RemoveCommand;

import io.github.eckig.grapheditor.Commands;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.connectors.DefaultConnectorTypes;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import io.github.eckig.grapheditor.core.view.GraphEditorContainer;
import io.github.eckig.grapheditor.demo.selections.SelectionCopier;
import javafx.geometry.Side;

/**
 * Responsible for default-skin specific logic in the graph editor demo.
 */
public class DefaultSkinController implements SkinController {

    protected static final int NODE_INITIAL_X = 19;
    protected static final int NODE_INITIAL_Y = 19;

    protected final GraphEditor graphEditor;
    protected final GraphEditorContainer graphEditorContainer;

    private static final int MAX_CONNECTOR_COUNT = 5;

    /**
     * Creates a new {@link DefaultSkinController} instance.
     *
     * @param graphEditor the graph editor on display in this demo
     * @param graphEditorContainer the graph editor container on display in this demo
     */
    public DefaultSkinController(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {

        this.graphEditor = graphEditor;
        this.graphEditorContainer = graphEditorContainer;
    }

    @Override
    public void activate()
    {
        graphEditorContainer.getMinimap().setConnectionFilter(c -> true);
    }

    @Override
    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.getContentX() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.getContentY() / currentZoomFactor;
        final GraphFactory factory = graphEditor.getModel().getGraphFactory();

        final GNode node = factory.create(GNode.class);
        //node.setY(NODE_INITIAL_Y + windowYOffset);

        final GConnector rightOutput = factory.create(GConnector.class);

        node.getConnectors().add(rightOutput);

        final GConnector leftInput = factory.create(GConnector.class);

        node.getConnectors().add(leftInput);

        //node.setX(NODE_INITIAL_X + windowXOffset);

        rightOutput.setDirection(Direction.OUTPUT);
        leftInput.setDirection(Direction.INPUT);

        Commands.addNode(graphEditor.getModel(), node);
    }

    /**
     * Adds a connector of the given type to all nodes that are currently selected.
     *
     * @param position the position of the new connector
     * @param input {@code true} for input, {@code false} for output
     */
    @Override
    public void addConnector(final Side position, final boolean input) {

      //final String type = getType(position, input);

      final GModel model = graphEditor.getModel();
      final GraphFactory factory = model.getGraphFactory();
      final SkinLookup skinLookup = graphEditor.getSkinLookup();
      final CompoundCommand command = new CompoundCommand();

      for (final GNode node : model.getNodes()) {

        if (skinLookup.lookupNode(node).isSelected()) {
          if (countConnectors(node, position) < MAX_CONNECTOR_COUNT) {

            final GConnector connector = factory.create(GConnector.class);
            if ( input ) {
              connector.setDirection(Direction.INPUT);
            } else {
              connector.setDirection(Direction.OUTPUT);
            }
            //  need the skin to set the side...
            GConnectorSkin connectorSkin = ((SkinManager)skinLookup).lookupOrCreateConnector(connector);
            connectorSkin.setSide(position);
            
            command.append(RemoveCommand.create(model, owner -> model.getNodes(), node));
            //command.append(AddCommand.create(editingDomain, node, connectors, connector));
          }
        }
      }

      if (command.canExecute()) {
        CommandStack.getCommandStack(model).execute(command);
      }
    }

    @Override
    public void clearConnectors() {
        Commands.clearConnectors(graphEditor.getModel(), graphEditor.getSelectionManager().getSelectedNodes());
    }

    @Override
    public void handlePaste(final SelectionCopier selectionCopier) {
    	selectionCopier.paste(null);
    }

    @Override
    public void handleSelectAll() {
    	graphEditor.getSelectionManager().selectAll();
    }

    /**
     * Counts the number of connectors the given node currently has of the given type.
     *
     * @param node a {@link GNode} instance
     * @param side the {@link Side} the connector is on
     * @return the number of connectors this node has on the given side
     */
    private int countConnectors(final GNode node, final Side side) {

        int count = 0;

        final SkinManager skinLookup = (SkinManager)graphEditor.getSkinLookup();
        for (final GConnector connector : node.getConnectors()) {
          GConnectorSkin connectorSkin = skinLookup.lookupOrCreateConnector(connector);
            if (side.equals(connectorSkin.getSide())) {
                count++;
            }
        }

        return count;
    }

    /**
     * Gets the connector type string corresponding to the given position and input values.
     *
     * @param position a {@link Side} value
     * @param input {@code true} for input, {@code false} for output
     * @return the connector type corresponding to these values
     */
    private String getType(final Side position, final boolean input)
    {
        switch (position)
        {
            case TOP:
                if (input)
                {
                    return DefaultConnectorTypes.TOP_INPUT;
                }
                return DefaultConnectorTypes.TOP_OUTPUT;
            case RIGHT:
                if (input)
                {
                    return DefaultConnectorTypes.RIGHT_INPUT;
                }
                return DefaultConnectorTypes.RIGHT_OUTPUT;
            case BOTTOM:
                if (input)
                {
                    return DefaultConnectorTypes.BOTTOM_INPUT;
                }
                return DefaultConnectorTypes.BOTTOM_OUTPUT;
            case LEFT:
                if (input)
                {
                    return DefaultConnectorTypes.LEFT_INPUT;
                }
                return DefaultConnectorTypes.LEFT_OUTPUT;
        }
        return null;
    }
}
