/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults.connection;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnection;

import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.connections.RectangularConnections;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;

/**
 * The default connection skin.
 *
 * <p>
 * Extension of {@link SimpleConnectionSkin} that provides a mechanism for creating and removing joints.
 * </p>
 */
public class BezierConnectionSkin extends SimpleConnectionSkin {

    private static final Logger LOGGER = LoggerFactory.getLogger(BezierConnectionSkin.class);

    private final JointCreator jointCreator;
    private final JointCleaner jointCleaner;
    private final JointAlignmentManager jointAlignmentManager;
    private final CursorOffsetCalculator cursorOffsetCalculator;

    private static final String BEZIER_STYLE_CLASS = "bezier-connection";

    private static final String BEZIER_STYLE_CLASS_BACKGROUND = "bezier-connection-background";


    /**
     * Creates a new default connection skin instance.
     *
     * @param connection the {@link GConnection} the skin is being created for
     */
    public BezierConnectionSkin(final GConnection connection) {

        super(connection);

        performChecks();

        cursorOffsetCalculator = new CursorOffsetCalculator(connection, path, backgroundPath, connectionSegments);
        jointCreator = new JointCreator(this, cursorOffsetCalculator);
        jointCleaner = new JointCleaner(this);
        jointAlignmentManager = new JointAlignmentManager(connection);

        jointCreator.addJointCreationHandler(root);
        backgroundPath.getStyleClass().clear();
        path.getStyleClass().clear();
        backgroundPath.getStyleClass().setAll(BEZIER_STYLE_CLASS_BACKGROUND);
        path.getStyleClass().setAll(BEZIER_STYLE_CLASS);
   }

    @Override
    public void setGraphEditor(final GraphEditor graphEditor) {

        super.setGraphEditor(graphEditor);

        jointCreator.setGraphEditor(graphEditor);
        jointCleaner.setGraphEditor(graphEditor);
        jointAlignmentManager.setSkinLookup(graphEditor.getSkinLookup());
    }

    @Override
    public void setJointSkins(final List<GJointSkin> jointSkins) {

        super.setJointSkins(jointSkins);

        jointCleaner.addCleaningHandlers(jointSkins);
        jointAlignmentManager.addAlignmentHandlers(jointSkins);
    }

    /**
     * Checks that the connection has the correct values to be displayed using this skin.
     */
    protected void performChecks() {
      final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();

      if ( skinLookup != null ) {
        if (!RectangularConnections.checkJointCount(this, skinLookup)) {
          LOGGER.error("Joint count not compatible with source and target connector types.");
        }
      }
    }

    /**
     * Draws all segments of the connection using Bezier curves.
     *
     * @param points
     *          all points that the connection should pass through (both connector and joint positions)
     * @param intersections
     *          all intersection-points of this connection with other connections
     */
    protected void drawAllSegments(final Point2D[] points, final double[][] intersections) {
        final double startX = points[0].getX();
        final double startY = points[0].getY();

        // Starting point
        final MoveTo moveTo = new MoveTo(GeometryUtils.moveOffPixel(startX), GeometryUtils.moveOffPixel(startY));

        connectionSegments.clear();
        path.getElements().clear();
        path.getElements().add(moveTo);

        // Loop through points and create cubic Bezier curves
        for (int i = 0; i < points.length - 1; i++) {
            final Point2D start = points[i];
            final Point2D end = points[i + 1];

            // Control points for the Bezier curve
            final double controlX1 = (start.getX() + end.getX()) / 2;
            final double controlY1 = start.getY();
            final double controlX2 = (start.getX() + end.getX()) / 2;
            final double controlY2 = end.getY();

            final CubicCurveTo cubicCurveTo = new CubicCurveTo(
                    GeometryUtils.moveOffPixel(controlX1), GeometryUtils.moveOffPixel(controlY1),
                    GeometryUtils.moveOffPixel(controlX2), GeometryUtils.moveOffPixel(controlY2),
                    GeometryUtils.moveOffPixel(end.getX()), GeometryUtils.moveOffPixel(end.getY())
            );

            // Add the curve to the path
            path.getElements().add(cubicCurveTo);
        }

        // Copy elements to background path
        backgroundPath.getElements().clear();
        backgroundPath.getElements().addAll(path.getElements());
    }

    @Override
    protected void selectionChanged(boolean isSelected) {
      super.selectionChanged(isSelected);
      if (isSelected) {
        path.getStyleClass().remove(BEZIER_STYLE_CLASS);
        path.getStyleClass().add("bezier-connection-selected");
      } else {
        path.getStyleClass().remove("bezier-connection-selected");
        path.getStyleClass().add(BEZIER_STYLE_CLASS);
      }
    }

    @Override
    public String toString() {
      return "BezierConnectionSkin [isSelected()=" + isSelected() + ", getItem()=" + getItem() + "]";
    }
    
}
