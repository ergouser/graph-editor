package com.ergotech.grapheditor.model;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class GNodeBeanInfo extends SimpleBeanInfo {

    private final static Class<GNode> beanClass = GNode.class;

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor x = new PropertyDescriptor("x", beanClass, "getX", "setX");
            PropertyDescriptor y = new PropertyDescriptor("y", beanClass, "getY", "setY");
            PropertyDescriptor width = new PropertyDescriptor("width", beanClass, "getWidth", "setWidth");
            PropertyDescriptor height = new PropertyDescriptor("height", beanClass, "getHeight", "setHeight");
            PropertyDescriptor type = new PropertyDescriptor("type", beanClass, "getType", "setType");
            PropertyDescriptor connectors = new PropertyDescriptor("connectors", beanClass, "getConnectors", null);

            return new PropertyDescriptor[] { x, y, width, height, type, connectors };
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
