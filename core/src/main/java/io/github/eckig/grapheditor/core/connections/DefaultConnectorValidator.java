/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.connections;

import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GConnectorPort.Direction;

import io.github.eckig.grapheditor.GConnectorValidator;

/**
 * Default validation rules that determine which connectors can be connected to each other.
 */
public class DefaultConnectorValidator implements GConnectorValidator {

    @Override
    public boolean prevalidate(final GConnectorPort source, final GConnectorPort target) {

        if (source == null || target == null) {
            return false;
        } else if (source.equals(target)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(final GConnectorPort source, final GConnectorPort target) {

        if (source.getDirection() == null || target.getDirection() == null) {
            return false;
        } else if (!source.getConnections().isEmpty() || !target.getConnections().isEmpty()) {
            return false;
        } else if (source.getParent().equals(target.getParent())) {
            return false;
        }

        final boolean sourceIsInput = source.getDirection() == Direction.INPUT;
        final boolean targetIsInput = target.getDirection() == Direction.INPUT;

        return sourceIsInput != targetIsInput;
    }

    @Override
    public String createConnectionType(final GConnectorPort source, final GConnectorPort target) {
        return null;
    }

    @Override
    public String createJointType(final GConnectorPort source, final GConnectorPort target) {
        return null;
    }
}
