package com.ergotech.grapheditor.model.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class GJointBeanInfo extends SimpleBeanInfo {

    private final static Class<GJointImpl> beanClass = GJointImpl.class;

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor x = new PropertyDescriptor("x", beanClass, "getX", "setX");
            PropertyDescriptor y = new PropertyDescriptor("y", beanClass, "getY", "setY");
            PropertyDescriptor connection = new PropertyDescriptor("connection", beanClass, "getConnection", "setConnection");

            return new PropertyDescriptor[] { x, y, connection };
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
