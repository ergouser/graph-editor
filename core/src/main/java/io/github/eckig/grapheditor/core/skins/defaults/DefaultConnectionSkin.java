/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnection;

import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.connections.RectangularConnections;
import io.github.eckig.grapheditor.core.skins.defaults.connection.CursorOffsetCalculator;
import io.github.eckig.grapheditor.core.skins.defaults.connection.JointAlignmentManager;
import io.github.eckig.grapheditor.core.skins.defaults.connection.JointCleaner;
import io.github.eckig.grapheditor.core.skins.defaults.connection.JointCreator;
import io.github.eckig.grapheditor.core.skins.defaults.connection.SimpleConnectionSkin;

/**
 * The default connection skin.
 *
 * <p>
 * Extension of {@link SimpleConnectionSkin} that provides a mechanism for creating and removing joints.
 * </p>
 */
public class DefaultConnectionSkin extends SimpleConnectionSkin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionSkin.class);

    private final JointCreator jointCreator;
    private final JointCleaner jointCleaner;
    private final JointAlignmentManager jointAlignmentManager;
    private final CursorOffsetCalculator cursorOffsetCalculator;

    /**
     * Creates a new default connection skin instance.
     *
     * @param connection the {@link GConnection} the skin is being created for
     */
    public DefaultConnectionSkin(final GConnection connection) {

        super(connection);

        performChecks();

        cursorOffsetCalculator = new CursorOffsetCalculator(connection, path, backgroundPath, connectionSegments);
        jointCreator = new JointCreator(this, cursorOffsetCalculator);
        jointCleaner = new JointCleaner(this);
        jointAlignmentManager = new JointAlignmentManager(connection);

        jointCreator.addJointCreationHandler(root);
    }

    @Override
    public void setGraphEditor(final GraphEditor graphEditor) {

        super.setGraphEditor(graphEditor);

        jointCreator.setGraphEditor(graphEditor);
        jointCleaner.setGraphEditor(graphEditor);
        jointAlignmentManager.setSkinLookup(graphEditor.getSkinLookup());
    }

    /**
     * Checks that the connection has the correct values to be displayed using this skin.
     */
    private void performChecks() {
      final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();

      if ( skinLookup != null ) {
        if (!RectangularConnections.checkJointCount(this, skinLookup)) {
          LOGGER.error("Joint count not compatible with source and target connector types.");
        }
      }
    }

    @Override
    public String toString() {
      return "DefaultConnectionSkin [isSelected()=" + isSelected() + ", getItem()=" + getItem() + "]";
    }
    
}
