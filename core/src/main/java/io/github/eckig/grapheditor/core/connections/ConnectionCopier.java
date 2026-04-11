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

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import io.github.eckig.grapheditor.core.utils.BeanUtils;

/**
 * Helper methods to copy {@link GConnection connections} and their associated skins.
 *
 * <p>
 * Since graphical attributes (joints, positions, etc.) are managed by skin objects
 * rather than the model, copying a connection also requires copying its skin. The
 * {@link SkinManager} is used to create properly-initialized skins for the copies,
 * and {@link BeanUtils#copyBean(Object, Object)} transfers the visual properties
 * from the original skin onto the new one.
 * </p>
 */
public final class ConnectionCopier {

    private ConnectionCopier() {
    }

    /**
     * Copies connections between copied nodes, including their skins.
     *
     * <p>
     * Only connections where <em>both</em> the source and target nodes are present
     * in the {@code copies} map will be copied. For each copied connection, a new
     * connection skin is created via the {@link SkinManager} and populated with
     * properties from the original skin.
     * </p>
     *
     * @param copies      a map linking original nodes to their copies
     * @param skinManager the {@link SkinManager} for creating and looking up skins
     * @return the list of newly created connections
     */
    @SuppressWarnings("unchecked")
    public static List<GConnection> copyConnections(final Map<GNode, GNode> copies, final SkinManager skinManager) {
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
                            copiedConnection = BeanUtils.copyBean(connection, true);
                            copiedConnections.put(connection, copiedConnection);

                            // Update the source or target of the copied connection
                            if (connection.getSourcePort().equals(connector)) {
                                copiedConnection.setSourcePort(copiedConnector);
                            } else {
                                copiedConnection.setTargetPort(copiedConnector);
                            }
                            // Update the source or target of the copied connection
                            if (copies.get(connection.getSourcePort().getParent()) instanceof GConnectorPort sourcePort) {
                              copiedConnection.setSourcePort(sourcePort);
                            }
 
                           // Create a properly-initialized connection skin via the
                            // SkinManager, then copy visual properties from the original
                            copyConnectionSkin(connection, copiedConnection, skinManager);
                        } else {
                            copiedConnection = copiedConnections.get(connection);
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
     * Creates a new connection skin for the copied connection and copies the
     * visual properties from the original connection's skin.
     *
     * <p>
     * The skin is created through {@link SkinManager#lookupOrCreateConnection}
     * so it is properly initialized (graph editor set, added to the view, etc.).
     * Then {@link BeanUtils#copyBean(Object, Object)} copies the non-hidden bean
     * properties (joint positions, visual attributes) from the original skin.
     * </p>
     *
     * @param originalConnection the original connection
     * @param copiedConnection   the newly copied connection
     * @param skinManager        the skin manager
     */
    private static void copyConnectionSkin(final GConnection originalConnection,
                                           final GConnection copiedConnection,
                                           final SkinManager skinManager) {
        final GConnectionSkin originalSkin = skinManager.lookupConnection(originalConnection);
        if (originalSkin != null) {
            // This creates and initializes the skin through the factory
            final GConnectionSkin copiedSkin = skinManager.lookupOrCreateConnection(copiedConnection);
            // Copy visual properties from the original onto the factory-created skin
            BeanUtils.copyBean(originalSkin, copiedSkin);
        }
    }

    /**
     * Gets the node on the other side of the connection to the given connector.
     *
     * @param connector  a {@link GConnectorPort} instance
     * @param connection a {@link GConnection} attached to this connector
     * @return the {@link GNode} on the other side, or {@code null}
     */
    private static GNode getOpposingNode(final GConnectorPort connector, final GConnection connection) {
      GConnectorPort opposingConnector;
      if ( connection.getSourcePort() != null && connection.getTargetPort() != null ) {
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
      return null;
    }
}