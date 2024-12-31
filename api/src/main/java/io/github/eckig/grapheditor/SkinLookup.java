/*
 * Copyright (C) 2024 ErgoTech Systems, Inc.
 */
package io.github.eckig.grapheditor;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GNode;

/**
 * Provides lookup methods to connect each model instance to its skin instance.
 */
public interface SkinLookup {

    /**
     * Gets the skin for the given node.
     *
     * @param node a {@link GNode} instance
     *
     * @return the associated {@link GNodeSkin} instance
     */
    GNodeSkin lookupNode(final GNode node);

    /**
     * Gets the skin for the given connector.
     *
     * @param connector a {@link GConnectorPort} instance
     *
     * @return the associated {@link GConnectorSkin} instance
     */
    GConnectorSkin lookupConnector(final GConnectorPort connector);

    /**
     * Gets the skin for the given connection.
     *
     * @param connection a {@link GConnection} instance
     *
     * @return the associated {@link GConnectionSkin} instance
     */
    GConnectionSkin lookupConnection(final GConnection connection);

    /**
     * Gets the skin for the given joint.
     *
     * @param joint a {@link GJoint} instance
     *
     * @return the associated {@link GJointSkin} instance
     */
    GJointSkin lookupJoint(final GJoint joint);

    /**
     * Gets the tail skin for the given connector.
     *
     * @param connector a {@link GConnectorPort} instance
     *
     * @return the associated {@link GTailSkin} instance
     */
    GTailSkin lookupTail(final GConnectorPort connector);
}
