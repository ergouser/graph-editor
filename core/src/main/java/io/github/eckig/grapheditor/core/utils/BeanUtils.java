package io.github.eckig.grapheditor.core.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class BeanUtils {

    public static <T> T copyBean(T original) {
        try {
            // Get the class of the original object
            Class<?> beanClass = original.getClass();

            // Get BeanInfo for the class
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);

            // Create a new instance of the class
            @SuppressWarnings("unchecked")
            T newInstance = (T) beanClass.getDeclaredConstructor().newInstance();

            // Get all the property descriptors for the class
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                Method getter = pd.getReadMethod();
                Method setter = pd.getWriteMethod();

                // Only copy properties that have both getter and setter
                if (getter != null && setter != null) {
                    Object value = getter.invoke(original);  // Get value from the original bean
                    setter.invoke(newInstance, value);       // Set value to the new bean
                }
            }

            return newInstance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
