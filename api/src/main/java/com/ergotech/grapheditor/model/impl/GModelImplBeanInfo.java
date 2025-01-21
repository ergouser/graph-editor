package com.ergotech.grapheditor.model.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class GModelImplBeanInfo extends SimpleBeanInfo {

    private final static Class<GModelImpl> beanClass = GModelImpl.class;

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor nodes = new PropertyDescriptor("nodes", beanClass, "getNodes", null);
            PropertyDescriptor connections = new PropertyDescriptor("connections", beanClass, "getConnections", null);

            return new PropertyDescriptor[] { nodes, connections };
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
