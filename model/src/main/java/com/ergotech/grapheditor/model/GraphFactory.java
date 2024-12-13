package com.ergotech.grapheditor.model;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory class for creating instances of graph components.
 * Supports creating objects either through a predefined map or by dynamically loading
 * classes that implement the desired interface.
 * Usage examples:
 * <pre>{@code
 * GraphFactory factory = new GraphFactory(factoryMap);
 * 
 * // Dynamically create an instance using the default system class loader
 * GNode dynamicNode = factory.create("com.ergotech.servers.BroadcastServer", GNode.class);
 * 
 * ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
 * GraphFactory factory = new GraphFactory(factoryMap);
 * }</pre>
 * </p>
 * 
 * <pre>{@code
 * // Dynamically create an instance using a custom class loader
 * GNode dynamicNode = factory.create("com.ergotech.servers.BroadcastServer", GNode.class, customClassLoader);
 * }</pre>
 * </p>
 * 
 * <p>
 * Example usage from the Map:
 * <pre>{@code
 * Map<Class<?>, Supplier<?>> factoryMap = new HashMap<>();
 * factoryMap.put(GNode.class, GNodeImpl::new);
 * factoryMap.put(GConnection.class, GConnectionImpl::new);
 * 
 * GraphFactory factory = new GraphFactory(factoryMap);
 * GNode node = factory.create(GNode.class);
 * }</pre>
 * </p>
  * 
 */
public class GraphFactory {

    private final Map<Class<?>, Supplier<?>> factoryMap;

    /**
     * Constructs a new {@code GraphFactory} with the specified factory map.
     *
     * @param factoryMap a map where the keys are classes representing graph components,
     *                   and the values are suppliers for creating instances of those classes
     * @throws IllegalArgumentException if the provided map is null
     */
    public GraphFactory(Map<Class<?>, Supplier<?>> factoryMap) {
        if (factoryMap == null) {
            throw new IllegalArgumentException("Factory map cannot be null");
        }
        this.factoryMap = factoryMap;
    }

    /**
     * Creates a new instance of the specified class.
     *
     * @param <T>   the type of the graph component to create
     * @param clazz the class representing the type of graph component to create
     * @return a new instance of the specified class
     * @throws IllegalArgumentException if no supplier is registered for the specified class
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clazz) {
        Supplier<?> supplier = factoryMap.get(clazz);
        if (supplier != null) {
            return (T) supplier.get();
        }
        throw new IllegalArgumentException("No factory registered for class: " + clazz.getName());
    }

    /**
     * Dynamically creates a new instance of the specified class name, ensuring that
     * it implements the given interface.
     *
     * @param <T>           the type of the graph component to create
     * @param className     the fully qualified name of the class to instantiate
     * @param interfaceClass the interface the class must implement
     * @return a new instance of the specified class
     * @throws IllegalArgumentException if the class cannot be found, instantiated, or does not implement the interface
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(String className, Class<T> interfaceClass) {
        return create(className, interfaceClass, ClassLoader.getSystemClassLoader());
    }

    /**
     * Dynamically creates a new instance of the specified class name using the given class loader,
     * ensuring that it implements the given interface.
     *
     * @param <T>           the type of the graph component to create
     * @param className     the fully qualified name of the class to instantiate
     * @param interfaceClass the interface the class must implement
     * @param classLoader    the class loader to use for loading the class
     * @return a new instance of the specified class
     * @throws IllegalArgumentException if the class cannot be found, instantiated, or does not implement the interface
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(String className, Class<T> interfaceClass, ClassLoader classLoader) {
        try {
            // Load the class dynamically using the specified class loader
            Class<?> clazz = Class.forName(className, true, classLoader);

            // Check if it implements the required interface
            if (!interfaceClass.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(
                        "Class " + className + " does not implement " + interfaceClass.getName());
            }

            // Create a new instance
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + className, e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to instantiate class: " + className, e);
        }
    }
}
