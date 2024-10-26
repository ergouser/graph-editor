package com.ergotech.grapheditor.model;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class GConnectorBeanInfo extends SimpleBeanInfo {

    private final static Class<GConnector> beanClass = GConnector.class;

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor id = new PropertyDescriptor("id", beanClass, "getId", "setId");
            PropertyDescriptor type = new PropertyDescriptor("type", beanClass, "getType", "setType");
            PropertyDescriptor parent = new PropertyDescriptor("parent", beanClass, "getParent", "setParent");
            PropertyDescriptor connections = new PropertyDescriptor("connections", beanClass, "getConnections", null);
            PropertyDescriptor x = new PropertyDescriptor("x", beanClass, "getX", "setX");
            PropertyDescriptor y = new PropertyDescriptor("y", beanClass, "getY", "setY");
            PropertyDescriptor connectionDetachedOnDrag = new PropertyDescriptor(
                    "connectionDetachedOnDrag", beanClass, "isConnectionDetachedOnDrag", "setConnectionDetachedOnDrag");

            return new PropertyDescriptor[] {
                id, type, parent, connections, x, y, connectionDetachedOnDrag
            };
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
