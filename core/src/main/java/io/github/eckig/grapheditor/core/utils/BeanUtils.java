package io.github.eckig.grapheditor.core.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Utility methods for copying JavaBean properties between objects.
 */
public class BeanUtils {

  /**
   * Creates a new instance of the same class as the original and copies all
   * readable/writable bean properties to it.
   *
   * @param <T>      the bean type
   * @param original the source bean
   * @return a new instance with all bean properties copied, or {@code null} on error
   */
  public static <T> T copyBean(T original) {
    return copyBean(original, false);
  }
  /**
   * Creates a new instance of the same class as the original and copies all
   * readable/writable bean properties to it.
   *
   * @param <T>      the bean type
   * @param original the source bean
   * @return a new instance with all bean properties copied, or {@code null} on error
   */
  public static <T> T copyBean(T original, boolean copyHidden) {
    try {
      @SuppressWarnings("unchecked")
      T newInstance = (T) original.getClass().getDeclaredConstructor().newInstance();
      copyBeanProperties(original, newInstance, copyHidden);
      return newInstance;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Copies all readable/writable bean properties from the original onto an
   * existing target instance. The target may be a different subclass than the
   * original as long as it shares the same bean properties; only properties
   * that exist on <em>both</em> objects (by matching the original's BeanInfo)
   * and that are not hidden will be copied.
   *
   * <p>
   * This is intended for cases where the target instance must be created
   * through a specific factory or builder (e.g. skin objects that require
   * graph-editor initialization) and cannot simply be instantiated via
   * a no-arg constructor.
   * </p>
   *
   * @param <T>      the bean type
   * @param original the source bean to copy properties from
   * @param target   the pre-created target bean to copy properties into
   * @return the target instance with properties copied, or {@code null} on error
   */
  public static <T> T copyBean(T original, T target) {
    return copyBean(original, target, false);
  }
  /**
   * Copies all readable/writable bean properties from the original onto an
   * existing target instance. The target may be a different subclass than the
   * original as long as it shares the same bean properties; only properties
   * that exist on <em>both</em> objects (by matching the original's BeanInfo)
   * and that are not hidden will be copied.
   *
   * <p>
   * This is intended for cases where the target instance must be created
   * through a specific factory or builder (e.g. skin objects that require
   * graph-editor initialization) and cannot simply be instantiated via
   * a no-arg constructor.
   * </p>
   *
   * @param <T>      the bean type
   * @param original the source bean to copy properties from
   * @param target   the pre-created target bean to copy properties into
   * @param copyHidden if true hidden properties will be copied
   * @return the target instance with properties copied, or {@code null} on error
   */
  public static <T> T copyBean(T original, T target, boolean copyHidden) {
    try {
      copyBeanProperties(original, target, copyHidden);
      return target;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Copies all readable/writable, non-hidden bean properties from source to target.
   *
   * @param source the source bean
   * @param target the target bean
   * @throws Exception if introspection or property access fails
   */
  private static void copyBeanProperties(Object source, Object target, boolean copyHidden) throws Exception {
    BeanInfo beanInfo = findBeanInfo(source.getClass());
    PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

    for (PropertyDescriptor pd : pds) {
      // Skip hidden properties — these are internal wiring (Item, etc.)
      // that should not be copied between instances
      if (!copyHidden && pd.isHidden()) {
        continue;
      }

      Method getter = pd.getReadMethod();
      Method setter = pd.getWriteMethod();

      if (getter != null && setter != null) {
        Object value = getter.invoke(source);
        setter.invoke(target, value);
      }
    }
  }
  
  /** Find the beaninfo using the classloader of the bean
   * @param beanClass
   * @return
   * @throws IntrospectionException 
   */
  private static BeanInfo findBeanInfo(Class<?> beanClass) throws Exception {
    BeanInfo beanInfo = null;
    try {
      // this is not quite correct, there is a rule about searching for
      // beaninfos in places other than the current directory, etc. etc.
      // but we'll just assume that there in the current directory
      // see Introspector.getBeanInfoSearchPath() if you want to do it right.
      final String beanInfoClassName;
      beanInfoClassName = beanClass.getName() + "BeanInfo";
      Class<?> beanInfoClass = beanClass.getClassLoader().loadClass(beanInfoClassName);
      //System.out.println ("findBeanInfo " + beanInfoClass.getClassLoader() + " " + beanClass.getClassLoader());
      beanInfo = (BeanInfo) beanInfoClass.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException noBeanInfo) {    
      beanInfo = Introspector.getBeanInfo(beanClass);
    }
    return beanInfo;
  }


}