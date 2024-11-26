package io.github.eckig.grapheditor.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ergotech.grapheditor.model.impl.GConnectionImpl;
import com.ergotech.grapheditor.model.impl.GConnectorImpl;
import com.ergotech.grapheditor.model.impl.GNodeImpl;

import io.github.eckig.grapheditor.core.connections.ConnectionCopier;

public class GModelUtilsTest {

    @Test
    public void copyConnections() {

        final List<GNodeImpl> nodes = createNodes();
        final List<GNodeImpl> copies = createNodes();

        connect(nodes.get(0).getConnectors().get(1), nodes.get(1).getConnectors().get(0));

        final Map<GNodeImpl, GNodeImpl> map = new HashMap<>();

        for (int i = 0; i < 3; i++) {
            map.put(nodes.get(i), copies.get(i));
        }

        final List<GConnectionImpl> connections = ConnectionCopier.copyConnections(map);

        assertTrue(connections.size() == 1);
        assertTrue(copies.get(0).getConnectors().get(1).getConnections().size() == 1);

        final GConnectionImpl newConnection = copies.get(0).getConnectors().get(1).getConnections().get(0);

        assertTrue(copies.get(1).getConnectors().get(0).getConnections().size() == 1);
        assertEquals(copies.get(1).getConnectors().get(0).getConnections().get(0), newConnection);

        assertEquals(copies.get(0).getConnectors().get(1), newConnection.getSource());
        assertEquals(copies.get(1).getConnectors().get(0), newConnection.getTarget());

        // Check no other connections have appeared.
        assertTrue(copies.get(0).getConnectors().get(0).getConnections().isEmpty());
        assertTrue(copies.get(1).getConnectors().get(1).getConnections().isEmpty());
        assertTrue(copies.get(2).getConnectors().get(0).getConnections().isEmpty());
        assertTrue(copies.get(2).getConnectors().get(1).getConnections().isEmpty());
    }

    private static List<GNodeImpl> createNodes() {

        final List<GNodeImpl> nodes = new ArrayList<>();

        final GNodeImpl firstNode = createNode();
        final GNodeImpl secondNode = createNode();
        final GNodeImpl thirdNode = createNode();

        nodes.add(firstNode);
        nodes.add(secondNode);
        nodes.add(thirdNode);

        return nodes;
    }

    private static final GNodeImpl createNode() {

        final GNodeImpl node = new GNodeImpl();

        final GConnectorImpl firstConnector = new GConnectorImpl();
        final GConnectorImpl secondConnector = new GConnectorImpl();

        node.getConnectors().add(firstConnector);
        node.getConnectors().add(secondConnector);

        return node;
    }

    private static final void connect(final GConnectorImpl source, final GConnectorImpl target) {

        final GConnectionImpl connection = new GConnectionImpl();

        source.getConnections().add(connection);
        target.getConnections().add(connection);

        connection.setSource(source);
        connection.setTarget(target);
    }
}
