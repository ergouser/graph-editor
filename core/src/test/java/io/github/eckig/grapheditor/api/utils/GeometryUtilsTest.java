/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.api.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.impl.GModelImpl;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GConnectorStyle;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GTailSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultNodeSkin;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;

public class GeometryUtilsTest {

    private static final double NODE_X = 55;
    private static final double NODE_Y = 32;
    private static final double CONNECTOR_CENTER_X = 13;
    private static final double CONNECTOR_CENTER_Y = 29;
    private static final double CONNECTOR_WIDTH = 18;
    private static final double CONNECTOR_HEIGHT = 12;

    private final GModel model = new GModelImpl();
    private final GNode node = model.getGraphFactory().create(GNode.class);
    private final GConnectorPort connector = model.getGraphFactory().create(GConnectorPort.class);

    private SkinLookup skinLookup;

    @Before
    public void setUp() {

      skinLookup = new MockSkinLoop();
      GNodeSkin skin = new DefaultNodeSkin(node);

      final GNodeSkin nodeSkin = ((SkinManager)skinLookup).lookupOrCreateNode(node);
        // Assign some arbitrary position to the node.
      nodeSkin.setX(NODE_X);
      nodeSkin.setY(NODE_Y);

        model.getNodes().add(node);
        node.getConnectorPorts().add(connector);

    }

    @Test
    public void testGetConnectorPosition() {
        // Should return the absolute position of the center of the connector.
        final Point2D target = new Point2D(NODE_X + CONNECTOR_CENTER_X, NODE_Y + CONNECTOR_CENTER_Y);
        assertEquals(GeometryUtils.getConnectorPosition(connector, skinLookup), target);
    }

    private static class MockSkinLoop implements SkinLookup
    {

        @Override
        public GNodeSkin lookupNode(GNode pNode)
        {
            return new GNodeSkin(pNode)
            {

                {
                    getRoot().relocate(NODE_X, NODE_Y);
                }

                @Override
                protected void selectionChanged(boolean pIsSelected)
                {
                    //  Auto-generated method stub

                }

                @Override
                public void setConnectorSkins(List<GConnectorSkin> pConnectorSkins)
                {
                    //  Auto-generated method stub

                }

                @Override
                public void layoutConnectors()
                {
                    //  Auto-generated method stub

                }

                @Override
                public Point2D getConnectorPosition(GConnectorSkin pConnectorSkin)
                {
                    return new Point2D(CONNECTOR_CENTER_X, CONNECTOR_CENTER_Y);
                }
            };
        }

        @Override
        public GConnectorSkin lookupConnector(GConnectorPort pConnector)
        {
            return new GConnectorSkin(pConnector)
            {

                @Override
                protected void selectionChanged(boolean pIsSelected)
                {
                    //  Auto-generated method stub

                }

                @Override
                public Node getRoot()
                {
                    return null;
                }

                @Override
                public double getWidth()
                {
                    return CONNECTOR_WIDTH;
                }

                @Override
                public double getHeight()
                {
                    return CONNECTOR_HEIGHT;
                }

                @Override
                public void applyStyle(GConnectorStyle pStyle)
                {
                    //  Auto-generated method stub
                }
            };
        }

        @Override
        public GConnectionSkin lookupConnection(GConnection pConnection)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public GJointSkin lookupJoint(GJoint pJoint)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public GTailSkin lookupTail(GConnectorPort pConnector)
        {
            throw new UnsupportedOperationException();
        }

    }
}
