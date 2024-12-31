/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.demo.customskins.tree;

import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GConnectorPort.Direction;

import io.github.eckig.grapheditor.GConnectorValidator;

/**
 * Validation rules for how connectors can be connected for the 'tree-like' graph.
 */
public class TreeConnectorValidator implements GConnectorValidator {

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

        if (source.getParent().equals(target.getParent())) {
            return false;
        } else if (source.getDirection().equals(target.getDirection())) {
            return false;
        } else if (source.getDirection() == Direction.OUTPUT
                && !source.getConnections().isEmpty()) {
            return false;
        } else if (target.getDirection() == Direction.INPUT
                && !target.getConnections().isEmpty()) {
            return false;
        }

        return true;
    }

//    @Override
//    public boolean validate(final GConnector source, final GConnector target) {
//
//        if (source.getType() == null || target.getType() == null) {
//            return false;
//        } else if (source.getParent().equals(target.getParent())) {
//            return false;
//        } else if (source.getType().equals(target.getType())) {
//            return false;
//        } else if (source.getType().equals(TreeSkinConstants.TREE_INPUT_CONNECTOR)
//                && !source.getConnections().isEmpty()) {
//            return false;
//        } else if (target.getType().equals(TreeSkinConstants.TREE_INPUT_CONNECTOR)
//                && !target.getConnections().isEmpty()) {
//            return false;
//        }
//
//        return true;
//    }

    @Override
    public String createConnectionType(final GConnectorPort source, final GConnectorPort target) {
        return TreeSkinConstants.TREE_CONNECTION;
    }

    @Override
    public String createJointType(final GConnectorPort source, final GConnectorPort target) {
        return null;
    }
}
