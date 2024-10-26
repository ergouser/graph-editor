package com.ergotech.grapheditor.model;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class GConnectionBeanInfo extends SimpleBeanInfo {

    private final static Class<GConnection> beanClass = GConnection.class;

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor id = new PropertyDescriptor("id", beanClass, "getId", "setId");
            PropertyDescriptor type = new PropertyDescriptor("type", beanClass, "getType", "setType");
            PropertyDescriptor source = new PropertyDescriptor("source", beanClass, "getSource", "setSource");
            PropertyDescriptor target = new PropertyDescriptor("target", beanClass, "getTarget", "setTarget");
            PropertyDescriptor bidirectional = new PropertyDescriptor(
                    "bidirectional", beanClass, "isBidirectional", "setBidirectional");
            PropertyDescriptor joints = new PropertyDescriptor("joints", beanClass, "getJoints", null);

            return new PropertyDescriptor[] {
                id, type, source, target, bidirectional, joints
            };
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
