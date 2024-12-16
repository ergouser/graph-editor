/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.connections;

import com.ergotech.grapheditor.model.GConnector;
import com.ergotech.grapheditor.model.GConnector.Direction;

import io.github.eckig.grapheditor.GConnectorValidator;

/**
 * Default validation rules that determine which connectors can be connected to each other.
 */
public class DefaultConnectorValidator implements GConnectorValidator {

    @Override
    public boolean prevalidate(final GConnector source, final GConnector target) {

        if (source == null || target == null) {
            return false;
        } else if (source.equals(target)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(final GConnector source, final GConnector target) {

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
    public String createConnectionType(final GConnector source, final GConnector target) {
        return null;
    }

    @Override
    public String createJointType(final GConnector source, final GConnector target) {
        return null;
    }
}
