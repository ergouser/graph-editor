/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.command.Command;
import com.ergotech.grapheditor.model.command.CommandStack;

import io.github.eckig.grapheditor.utils.DraggableBox;
import io.github.eckig.grapheditor.utils.ResizableBox;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;

/**
 * The skin class for a {@link GNode}. Responsible for visualizing nodes in the graph editor.
 *
 * <p>
 * A custom node skin must extend this class. It <b>must</b> also provide a constructor taking exactly one {@link GNode}
 * parameter.
 * </p>
 *
 * <p>
 * The node skin is responsible for adding its connectors to the scene graph and laying them out.
 * </p>
 *
 * <p>
 * The root JavaFX node of this skin is a {@link ResizableBox}.
 * </p>
 */
public abstract class GNodeSkin extends GSkin<GNode> {

    protected final DraggableBox root;

    protected final DoubleProperty x = new SimpleDoubleProperty(this, "x", 0);
    protected final DoubleProperty y = new SimpleDoubleProperty(this, "y", 0);
    protected final DoubleProperty width = new SimpleDoubleProperty(this, "width", 151);
    protected final DoubleProperty height = new SimpleDoubleProperty(this, "height", 101);

    /**
     * Creates a new {@link GNodeSkin}.
     */
    public GNodeSkin() {
        this(null);
    }

    /**
     * Creates a new {@link GNodeSkin}.
     *
     * @param node the {@link GNode} represented by the skin
     */
    public GNodeSkin(final GNode node) {
        super(node);
        root = createContainer();
    }

    /**
     * Gets the root JavaFX node of the skin.
     *
     * @return a {@link ResizableBox} containing the skin's root JavaFX node
     */
    @Override
    public DraggableBox getRoot()
    {
        return root;
    }

    /**
     * Initializes the node skin.
     *
     * <p>
     * The skin's layout values, e.g. its x and y position, are loaded from the {@link GNode} at this point.
     * </p>
     */
    public void initialize() {

        getRoot().setLayoutX(getX());
        getRoot().setLayoutY(getY());

        getRoot().resize(getWidth(), getHeight());
    }

    /**
     * Sets the node's connector skins.
     *
     * <p>
     * This will be called as the node is created, or if a connector is added or removed. The connector skin's regions
     * should be added to the scene graph.
     * </p>
     *
     * @param connectorSkins a list of {@link GConnectorSkin} objects for each of the node's connectors
     */
    public abstract void setConnectorSkins(List<GConnectorSkin> connectorSkins);

    /**
     * Lays out the node's connectors.
     */
    public abstract void layoutConnectors();

    /**
     * Gets the position of the <b>center</b> of a connector relative to the node region.
     *
     * <p>
     * This will be the point where a connection will attach to.
     * </p>
     *
     * @param connectorSkin a {@link GConnectorSkin} instance
     *
     * @return the x and y coordinates of the connector
     */
    public abstract Point2D getConnectorPosition(GConnectorSkin connectorSkin);

    /**
     * Creates and returns the {@link DraggableBox} that serves as the root for
     * this node skin.<br>
     * By default a {@link ResizableBox} will be created and return as most
     * nodes will be both draggable and resizable.
     *
     * @return {@link DraggableBox}
     */
    protected DraggableBox createContainer()
    {
        return new ResizableBox(EditorElement.NODE)
        {

            @Override
            protected void layoutChildren()
            {
                super.layoutChildren();
                layoutConnectors();
            }

            @Override
            public void positionMoved()
            {
                super.positionMoved();
                GNodeSkin.this.impl_positionMoved();
            }
            
            @Override
            public void executeCommand(Command command) {
              CommandStack commandStack = CommandStack.getCommandStack(GNodeSkin.this.getGraphEditor().getModel());
              try {
                commandStack.execute(command);
              } catch (Exception e) {
                throw new UndeclaredThrowableException(e);
              }
            }
        };
    }
    
    /**
     * Gets the x-coordinate of the joint.
     *
     * @return the x-coordinate as a DoubleProperty.
     */
    public DoubleProperty xProperty() {
        return x;
    }

    /**
     * Returns the x-coordinate of the joint.
     *
     * @return the x-coordinate of the joint.
     */
    public double getX() {
        return x.get();
    }

    /**
     * Sets the x-coordinate of the joint.
     *
     * @param value the new value of the x-coordinate.
     */
    public void setX(double value) {
        x.set(value);
    }

    /**
     * Gets the y-coordinate of the joint.
     *
     * @return the y-coordinate as a DoubleProperty.
     */
    public DoubleProperty yProperty() {
        return y;
    }

    /**
     * Returns the y-coordinate of the joint.
     *
     * @return the y-coordinate of the joint.
     */
    public double getY() {
        return y.get();
    }

    /**
     * Sets the y-coordinate of the joint.
     *
     * @param value the new value of the y-coordinate.
     */
    public void setY(double value) {
        y.set(value);
    }
    
    /**
     * Returns the {@code DoubleProperty} representing the width.
     * This property can be used to observe changes to the width or bind it to another property.
     *
     * @return the {@code DoubleProperty} for the width.
     */
    public DoubleProperty widthProperty() {
        return width;
    }

    /**
     * Gets the current value of the width.
     *
     * @return the current width value.
     */
    public double getWidth() {
        return width.get();
    }

    /**
     * Sets the value of the width.
     *
     * @param value the new width value.
     */
    public void setWidth(double value) {
        width.set(value);
    }

    /**
     * Returns the {@code DoubleProperty} representing the height.
     * This property can be used to observe changes to the height or bind it to another property.
     *
     * @return the {@code DoubleProperty} for the height.
     */
    public DoubleProperty heightProperty() {
        return height;
    }

    /**
     * Gets the current value of the height.
     *
     * @return the current height value.
     */
    public double getHeight() {
        return height.get();
    }

    /**
     * Sets the value of the height.
     *
     * @param value the new height value.
     */
    public void setHeight(double value) {
        height.set(value);
    }

}
