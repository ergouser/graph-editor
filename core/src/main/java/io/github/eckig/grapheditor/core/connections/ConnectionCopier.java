package io.github.eckig.grapheditor.core.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GNode;

import io.github.eckig.grapheditor.core.utils.BeanUtils;

/**
 * Helper methods to copy {@link GConnection connections}
 */
public final class ConnectionCopier {

  /**
   * Static class.
   */
  private ConnectionCopier() {
  }

  /**
   * Copies connection information from one set of nodes to another.
   *
   * <p>
   * Connections between nodes in the <b>keys</b> of the input map are copied (including their joint information) and
   * the new connections are set inside the corresponding nodes in the <b>values</b> of the input map.
   * </p>
   *
   * <p>
   * The new connection information is set <em>directly</em>. EMF commands are not used
   * </p>
   *
   * @param copies
   *          a map that links source nodes to their copies in its key-value pairs
   * @return the list of created connections
   */
  @SuppressWarnings("unchecked")
  public static List<GConnection> copyConnections(final Map<GNode, GNode> copies) {
    final Map<GConnection, GConnection> copiedConnections = new HashMap<>();

    for (final GNode node : copies.keySet()) {
        final GNode copy = copies.get(node);

        Iterator<? extends GConnectorPort> originalIterator = node.getConnectorPorts().iterator();
        Iterator<? extends GConnectorPort> copyIterator = copy.getConnectorPorts().iterator();

        while (originalIterator.hasNext() && copyIterator.hasNext()) {
            final GConnectorPort connector = originalIterator.next();
            final GConnectorPort copiedConnector = copyIterator.next();

            // Clear connections of the copied connector
            copiedConnector.getConnections().clear();

            for (final GConnection connection : connector.getConnections()) {
                final GNode opposingNode = getOpposingNode(connector, connection);
                final boolean opposingNodePresent = copies.containsKey(opposingNode);

                if (opposingNodePresent) {
                    final GConnection copiedConnection;
                    if (!copiedConnections.containsKey(connection)) {
                        copiedConnection = BeanUtils.copyBean(connection);
                        copiedConnections.put(connection, copiedConnection);
                    } else {
                        copiedConnection = copiedConnections.get(connection);
                    }

                    // Update the source or target of the copied connection
                    if (connection.getSourcePort().equals(connector)) {
                        copiedConnection.setSourcePort(copiedConnector);
                    } else {
                        copiedConnection.setTargetPort(copiedConnector);
                    }

                    // Add the copied connection to the copied connector
                    ((Collection<GConnection>) copiedConnector.getConnections()).add(copiedConnection);
                }
            }
        }
    }

    return new ArrayList<>(copiedConnections.values());
}

  /**
   * Gets the node on the other side of the connection to the given connector.
   *
   * @param connector
   *          a {@link GConnectorPort} instance
   * @param connection
   *          a {@link GConnection} attached to this connector
   * @return the {@link GNode} on the other side of the connection, or {@code null} if none exists
   */
  private static GNode getOpposingNode(final GConnectorPort connector, final GConnection connection) {
    GConnectorPort opposingConnector;
    if (connection.getSourcePort().equals(connector)) {
      opposingConnector = connection.getTargetPort();
    } else {
      opposingConnector = connection.getSourcePort();
    }

    if (opposingConnector != null && opposingConnector.getParent() != null) {
      return opposingConnector.getParent();
    } else {
      return null;
    }
  }
}
