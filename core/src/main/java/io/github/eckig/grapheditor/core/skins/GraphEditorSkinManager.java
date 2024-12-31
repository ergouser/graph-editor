package io.github.eckig.grapheditor.core.skins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GNode;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GSkin;
import io.github.eckig.grapheditor.GTailSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.VirtualSkin;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultJointSkin;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultNodeSkin;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultTailSkin;
import io.github.eckig.grapheditor.core.skins.defaults.connection.BezierConnectionSkin;
import io.github.eckig.grapheditor.core.view.ConnectionLayouter;
import io.github.eckig.grapheditor.core.view.GraphEditorView;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.util.Callback;

/**
 * Default {@link SkinManager} ementation
 *
 * @since 09.02.2016
 */
public class GraphEditorSkinManager implements SkinManager {

  private final GraphEditor mGraphEditor;

  private final GraphEditorView mView;

  /** The map of class factories.*/
  private final Map<SkinFactoryKey, Callback<?, ?>> factoryMap = new HashMap<>();

  private final ObservableMap<GNode, GNodeSkin> mNodeSkins = FXCollections.observableHashMap();

  private final ObservableMap<GConnection, GConnectionSkin> mConnectionSkins = FXCollections.observableHashMap();

  // private final Map<GNode, GNodeSkin> mNodeSkins = new HashMap<>();
  private final Map<GConnectorPort, GConnectorSkin> mConnectorSkins = new HashMap<>();

  // private final Map<GConnection, GConnectionSkin> mConnectionSkins = new HashMap<>();
  private final Map<GJoint, GJointSkin> mJointSkins = new HashMap<>();

  private final Map<GConnectorPort, GTailSkin> mTailSkins = new HashMap<>();

  private ConnectionLayouter mConnectionLayouter;

  private final Consumer<GSkin<?>> mOnPositionMoved = this::positionMoved;

  /**
   * A composite key used to uniquely identify a skin factory based on both the component type
   * and the skin type. This is necessary when multiple factories are registered for the same
   * component type but producing different skin types.
   */
  protected static class SkinFactoryKey {
    private final Class<?> componentType;
    private final Class<?> skinType;

    /**
     * Constructs a new {@code SkinFactoryKey} with the specified component type and skin type.
     *
     * @param componentType the class of the graph component
     * @param skinType      the class of the skin associated with the graph component
     * @throws IllegalArgumentException if {@code componentType} or {@code skinType} is {@code null}
     */
    public SkinFactoryKey(Class<?> componentType, Class<?> skinType) {
      if (componentType == null || skinType == null) {
        throw new IllegalArgumentException("Component type and skin type cannot be null.");
      }
      this.componentType = componentType;
      this.skinType = skinType;
    }

    /**
     * Returns the component type of this key.
     *
     * @return the component type
     */
    public Class<?> getComponentType() {
      return componentType;
    }

    /**
     * Returns the skin type of this key.
     *
     * @return the skin type
     */
    public Class<?> getSkinType() {
      return skinType;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof SkinFactoryKey)) return false;
      SkinFactoryKey other = (SkinFactoryKey) obj;
      return componentType.equals(other.componentType) && skinType.equals(other.skinType);
    }

    @Override
    public int hashCode() {
      return 31 * componentType.hashCode() + skinType.hashCode();
    }
  }

  /**
   * Creates a new skin manager instance. Only one instance should exist per {@link DefaultGraphEditor} instance.
   *
   * @param pGraphEditor
   *          {@link GraphEditor}
   * @param pView
   *          {@link GraphEditorView}
   */
  public GraphEditorSkinManager(final GraphEditor pGraphEditor, final GraphEditorView pView) {
    mView = pView;
    mGraphEditor = pGraphEditor;

    // Register default factories
    setSkinFactory(GConnectorPort.class, GConnectorSkin.class, connector -> new DefaultConnectorSkin(connector));
    //setSkinFactory(GConnection.class, GConnectionSkin.class, connection -> new DefaultConnectionSkin(connection));
    setSkinFactory(GConnection.class, GConnectionSkin.class, connection -> new BezierConnectionSkin(connection));
    setSkinFactory(GJoint.class, GJointSkin.class, joint -> new DefaultJointSkin(joint));
    setSkinFactory(GNode.class, GNodeSkin.class, node -> new DefaultNodeSkin(node));
    setSkinFactory(GConnectorPort.class, GTailSkin.class, connector -> new DefaultTailSkin(connector));

    // Add listener to mNodeSkins
    mNodeSkins.addListener((MapChangeListener<GNode, GNodeSkin>) change -> {
      if (change.wasAdded() || change.wasRemoved()) {
        updateConnectors(change.getKey()); // Call updateConnectors with the affected node
      }
    });

    //    // Add listener to mConnectionSkins
    //    mConnectionSkins.addListener((MapChangeListener<GConnection, GConnectionSkin>) change -> {
    //      if (change.wasAdded() || change.wasRemoved()) {
    //        updateJoints(change.getKey()); // Call updateJoints with the affected connection
    //      }
    //    });
    //    
    //    // Add listener to mJointSkins
    //    mConnectionSkins.addListener((MapChangeListener<GConnection, GConnectionSkin>) change -> {
    //      if (change.wasAdded() || change.wasRemoved()) {
    //        updateJoints(change.getKey()); // Call updateJoints with the affected connection
    //      }
    //    });
  }

  @Override
  public void setConnectionLayouter(final ConnectionLayouter pConnectionLayouter) {
    mConnectionLayouter = pConnectionLayouter;
  }

  /**
   * Registers a factory for creating skins for a specific type of graph component and skin type.
   * <p>
   * This method associates a specific {@link Callback} with a combination of a component type and
   * a skin type. The callback will be used to create instances of the corresponding skin whenever
   * a skin is requested for that component type and skin type, following the type hierarchy.
   * </p>
   *
   * <p>
   * If a factory is already registered for the specified component type and skin type, it will be
   * replaced with the new factory.
   * </p>
   *
   * <p><b>Example:</b></p>
   * <pre>{@code
   * GraphEditorSkinManager skinManager = new GraphEditorSkinManager();
   *
   * // Register a factory for GNode and GNodeSkin
   * skinManager.setSkinFactory(GNode.class, GNodeSkin.class, node -> new CustomNodeSkin(node));
   *
   * // Register a factory for GConnector and GConnectorSkin
   * skinManager.setSkinFactory(GConnector.class, GConnectorSkin.class, connector -> new CustomConnectorSkin(connector));
   * }</pre>
   *
   * @param <T>           the type of the graph component
   * @param <R>           the type of the skin created for the graph component
   * @param componentType the class of the graph component
   * @param skinType      the class of the skin
   * @param factory       a {@link Callback} to create skins for the specified component and skin types
   * @throws IllegalArgumentException if any of the parameters are {@code null}
   */
  public <T, R> void setSkinFactory(Class<T> componentType, Class<R> skinType, Callback<T, R> factory) {
    if (componentType == null || skinType == null || factory == null) {
      throw new IllegalArgumentException("Component type, skin type, and factory cannot be null.");
    }
    factoryMap.put(new SkinFactoryKey(componentType, skinType), factory);
  }

  /**
   * Removes the factory for the specified component and skin types, unless the factory is required
   * by the create methods (i.e., the default factories registered in the constructor).
   *
   * @param <T>           the type of the graph component
   * @param <R>           the type of the skin
   * @param componentType the class of the graph component
   * @param skinType      the class of the skin
   * @return the removed factory, or {@code null} if no factory was registered for the specified types
   * @throws IllegalArgumentException if the factory for the specified types is required and cannot be removed
   */
  public <T, R> Callback<?, ?> removeSkinFactory(Class<T> componentType, Class<R> skinType) {
    // List of required factories that cannot be removed
    if (isRequiredFactory(componentType, skinType)) {
      throw new IllegalArgumentException("Cannot remove the required factory for component type: "
          + componentType.getName() + " and skin type: " + skinType.getName());
    }

    // Remove and return the factory if it exists
    return factoryMap.remove(new SkinFactoryKey(componentType, skinType));
  }

  // Helper method to determine if a factory is required
  private boolean isRequiredFactory(Class<?> componentType, Class<?> skinType) {
    // Define the required component and skin type pairs
    return (componentType == GConnectorPort.class && (skinType == GConnectorSkin.class || skinType == GTailSkin.class))
        || (componentType == GConnection.class && skinType == GConnectionSkin.class)
        || (componentType == GJoint.class && skinType == GJointSkin.class)
        || (componentType == GNode.class && skinType == GNodeSkin.class);
  }

  /**
   * Retrieves the factory for creating skins of the specified type.
   * <p>
   * This method checks for a factory in the following order:
   * <ol>
   *     <li>First, it searches for a factory registered for the exact class type or one of its superclasses,
   *     starting with the most specific class (i.e., the class provided as the argument).</li>
   *     <li>If no factory is found in the class hierarchy, it checks the interfaces implemented by the class,
   *     looking for a factory registered for the interface itself.</li>
   * </ol>
   * If no factory is found in the class hierarchy or implemented interfaces, this method returns {@code null}.
   * </p>
   *
   * <p><b>Example 1: Using an Exact Type</b></p>
   * <pre>{@code
   * skinManager.setSkinFactory(GConnector.class, connector -> new DefaultConnectorSkin(connector));
   *
   * // Retrieve the factory for GConnector
   * Callback<GConnector, GConnectorSkin> factory = skinManager.getSkinFactory(GConnector.class);
   * GConnectorSkin skin = factory.call(new GConnector());
   * }</pre>
   *
   * <p><b>Example 2: Using a Subclass</b></p>
   * <pre>{@code
   * skinManager.setSkinFactory(GConnector.class, connector -> new DefaultConnectorSkin(connector));
   *
   * // ConnectorA implements GConnector
   * Callback<ConnectorA, GConnectorSkin> subclassFactory = skinManager.getSkinFactory(ConnectorA.class);
   * GConnectorSkin skin = subclassFactory.call(new ConnectorA());
   * }</pre>
   *
   * <p><b>Example 3: Fallback to an Interface</b></p>
   * <pre>{@code
   * skinManager.setSkinFactory(GConnector.class, connector -> new DefaultConnectorSkin(connector));
   *
   * // ConnectorB extends ConnectorA implements GConnector
   * Callback<ConnectorB, GConnectorSkin> fallbackFactory = skinManager.getSkinFactory(ConnectorB.class);
   * GConnectorSkin skin = fallbackFactory.call(new ConnectorB());
   * }</pre>
   *
   * @param <T> the type of the graph component
   * @param <R> the type of the skin for the graph component
   * @param componentType the class of the graph component 
   * @param skinType      the class of the skin
   * @return the factory for the specified type, or {@code null} if no factory is found
   */
  @SuppressWarnings("unchecked")
  public <T, R> Callback<T, R> getSkinFactory(Class<?> componentType, Class<R> skinType) {
    Class<?> currentType = componentType;

    // Traverse the class hierarchy
    while (currentType != null) {
      SkinFactoryKey key = new SkinFactoryKey(currentType, skinType);
      Callback<?, ?> factory = factoryMap.get(key);
      if (factory != null) {
        return (Callback<T, R>) factory;
      }
      currentType = currentType.getSuperclass(); // Move up the hierarchy
    }

    // No match found in the class hierarchy, check all interfaces in the hierarchy
    currentType = componentType;
    while (currentType != null) {
      for (Class<?> iface : currentType.getInterfaces()) {
        SkinFactoryKey key = new SkinFactoryKey(iface, skinType);
        Callback<?, ?> factory = factoryMap.get(key);
        if (factory != null) {
          return (Callback<T, R>) factory;
        }
      }
      currentType = currentType.getSuperclass(); // Move up the hierarchy
    }

    // No factory found
    return null;
  }

  @Override
  public void setNodeSkinFactory(final Callback<GNode, GNodeSkin> pSkinFactory) {
    setSkinFactory(GNode.class, GNodeSkin.class, pSkinFactory);
  }

  @Override
  public void setConnectorSkinFactory(final Callback<GConnectorPort, GConnectorSkin> pConnectorSkinFactory) {
    setSkinFactory(GConnectorPort.class, GConnectorSkin.class, pConnectorSkinFactory);
  }

  @Override
  public void setConnectionSkinFactory(final Callback<GConnection, GConnectionSkin> pConnectionSkinFactory) {
    setSkinFactory(GConnection.class, GConnectionSkin.class, pConnectionSkinFactory);
  }

  @Override
  public void setJointSkinFactory(final Callback<GJoint, GJointSkin> pJointSkinFactory) {
    setSkinFactory(GJoint.class, GJointSkin.class, pJointSkinFactory);
  }

  @Override
  public void setTailSkinFactory(final Callback<GConnectorPort, GTailSkin> pTailSkinFactory) {
    setSkinFactory(GConnectorPort.class, GTailSkin.class, pTailSkinFactory);
  }

  @Override
  public void clear() {
    if (!mNodeSkins.isEmpty()) {
      final GNode[] nodes = mNodeSkins.keySet().toArray(new GNode[0]);
      for (final GNode n : nodes) {
        removeNode(n);
      }
    }

    if (!mConnectorSkins.isEmpty()) {
      final GConnectorPort[] connectors = mConnectorSkins.keySet().toArray(new GConnectorPort[0]);
      for (final GConnectorPort c : connectors) {
        removeConnector(c);
      }
    }

    if (!mConnectionSkins.isEmpty()) {
      final GConnection[] connections = mConnectionSkins.keySet().toArray(new GConnection[0]);
      for (final GConnection c : connections) {
        removeConnection(c);
      }
    }

    if (!mJointSkins.isEmpty()) {
      final GJoint[] joints = mJointSkins.keySet().toArray(new GJoint[0]);
      for (final GJoint c : joints) {
        removeJoint(c);
      }
    }

    if (!mTailSkins.isEmpty()) {
      final GTailSkin[] tails = mTailSkins.values().toArray(new GTailSkin[0]);
      for (final GTailSkin tail : tails) {
        mView.remove(tail);
        tail.dispose();
      }
    }

    // remove any remainders that might have been left over:
    mView.clear();
  }

  @Override
  public void removeNode(final GNode pNodeToRemove) {
      if (pNodeToRemove != null) {
          final GNodeSkin removedSkin = mNodeSkins.remove(pNodeToRemove);
          if (removedSkin != null) {
              mView.remove(removedSkin);
              removedSkin.dispose();
          }

          // Iterate over the connector ports directly
          for (GConnectorPort connectorPort : pNodeToRemove.getConnectorPorts()) {
              removeConnector(connectorPort);
          }
      }
  }

  @Override
  public void removeConnector(final GConnectorPort pConnectorToRemove) {
    if (pConnectorToRemove != null) {
      final GConnectorSkin removedSkin = mConnectorSkins.remove(pConnectorToRemove);
      if (removedSkin != null) {
        removedSkin.dispose();
      }
      final GTailSkin removedTailSkin = mTailSkins.remove(pConnectorToRemove);
      if (removedTailSkin != null) {
        removedTailSkin.dispose();
      }
    }
  }

  @Override
  public void removeConnection(final GConnection pConnectionToRemove) {
    if (pConnectionToRemove != null) {
      final GConnectionSkin removedSkin = mConnectionSkins.remove(pConnectionToRemove);
      if (removedSkin != null) {
        mView.remove(removedSkin);
        removedSkin.dispose();
      }
    }
  }

  @Override
  public void removeJoint(final GJoint pJointToRemove) {
    if (pJointToRemove != null) {
      final GJointSkin removedSkin = mJointSkins.remove(pJointToRemove);
      if (removedSkin != null) {
        mView.remove(removedSkin);
        removedSkin.dispose();
      }
    }
  }

  @Override
  public void updateConnectors(final GNode pNode) {
    final GNodeSkin nodeSkin = mNodeSkins.get(pNode);
    if (nodeSkin != null) {
      final List<GConnectorSkin> nodeConnectorSkins = pNode.getConnectorPorts().stream().map(this::lookupOrCreateConnector)
          .collect(Collectors.toList());
      nodeSkin.setConnectorSkins(nodeConnectorSkins);
    }
  }

//  @Override
//  public void updateJoints(final GConnection pConnection) {
//    final GConnectionSkin connectionSkin = lookupConnection(pConnection);
//    if (connectionSkin != null) {
//      final List<GJointSkin> connectionJointSkins = connectionSkin.getJointSkins().stream().map(this::lookupOrCreateJoint)
//          .collect(Collectors.toList());
//      connectionSkin.setJointSkins(connectionJointSkins);
//    }
//  }
//
  /**
   * Looks up or creates a skin for the given component based on its type and the skin type.
   * <p>
   * The appropriate map is selected based on the {@code skinType}, and the skin is either
   * retrieved from the map or created using the associated factory.
   * </p>
   *
   * @param <T>       the type of the component (e.g., GNode, GConnector)
   * @param <R>       the type of the skin (e.g., GNodeSkin, GConnectorSkin)
   * @param component the component instance for which a skin should be created
   * @param skinType  the class of the skin
   * @return the existing or newly created skin for the component
   */
  public <T, R> R lookupOrCreateSkin(T component, Class<R> skinType) {
      // Determine the appropriate map based on the skin type
      Map<T, R> skinsMap = getSkinsMapForType(skinType);

      // Use computeIfAbsent to either retrieve an existing skin or create a new one
      return skinsMap.computeIfAbsent(component, key -> {
          // Look up the factory using the component's runtime class and the skin type
          Callback<T, R> factory = getSkinFactory(component.getClass(), skinType);
          if (factory == null) {
              throw new IllegalStateException(
                  "No factory registered for component type: " + component.getClass().getName()
                  + " and skin type: " + skinType.getName()
              );
          }
          // Create a new skin using the factory
          return factory.call(key);
      });
  }

  /**
   * Returns the appropriate map for the given skin type.
   *
   * @param <T>      the type of the component
   * @param <R>      the type of the skin
   * @param skinType the class of the skin
   * @return the map corresponding to the given skin type
   * @throws IllegalArgumentException if no map is associated with the given skin type
   */
  @SuppressWarnings("unchecked")
  private <T, R> Map<T, R> getSkinsMapForType(Class<R> skinType) {
      if (skinType == GNodeSkin.class) {
          return (Map<T, R>) mNodeSkins;
      } else if (skinType == GConnectionSkin.class) {
          return (Map<T, R>) mConnectionSkins;
      } else if (skinType == GConnectorSkin.class) {
          return (Map<T, R>) mConnectorSkins;
      } else if (skinType == GJointSkin.class) {
          return (Map<T, R>) mJointSkins;
      } else if (skinType == GTailSkin.class) {
          return (Map<T, R>) mTailSkins;
      } else {
          throw new IllegalArgumentException("Unsupported skin type: " + skinType.getName());
      }
  }

  @Override
  public GNodeSkin lookupOrCreateNode(final GNode pNode) {
    return mNodeSkins.computeIfAbsent(pNode, this::createNodeSkin);
  }

  @Override
  public GConnectorSkin lookupOrCreateConnector(final GConnectorPort pConnector) {
    return mConnectorSkins.computeIfAbsent(pConnector, this::createConnectorSkin);
  }

  @Override
  public GConnectionSkin lookupOrCreateConnection(final GConnection pConnection) {
    return mConnectionSkins.computeIfAbsent(pConnection, this::createConnectionSkin);
  }

  @Override
  public GJointSkin lookupOrCreateJoint(final GJoint pJoint) {
    return mJointSkins.computeIfAbsent(pJoint, this::createJointSkin);
  }

  @Override
  public GNodeSkin lookupNode(final GNode pNode) {
    return mNodeSkins.get(pNode);
  }

  @Override
  public GConnectorSkin lookupConnector(final GConnectorPort pConnector) {
    return mConnectorSkins.get(pConnector);
  }

  @Override
  public GConnectionSkin lookupConnection(final GConnection pConnection) {
    return mConnectionSkins.get(pConnection);
  }

  @Override
  public GJointSkin lookupJoint(final GJoint pJoint) {
    return mJointSkins.get(pJoint);
  }

  @Override
  public GTailSkin lookupTail(final GConnectorPort pConnector) {
    // GTailSkin is always/only created on demand
    return mTailSkins.computeIfAbsent(pConnector, this::createTailSkin);
  }

  /**
   * Creates a new {@link GConnectorSkin} for the given {@link GConnectorPort} using the registered factory.
   * <p>
   * This method retrieves the appropriate factory based on the component type and skin type,
   * and uses it to create a new skin instance. If no custom factory is registered, the default
   * factory will be used.
   * </p>
   *
   * @param pConnector the {@link GConnectorPort} for which to create a skin
   * @return a new instance of {@link GConnectorSkin}
   */
  private GConnectorSkin createConnectorSkin(final GConnectorPort pConnector) {
    Callback<GConnectorPort, GConnectorSkin> factory = getSkinFactory(pConnector.getClass(), GConnectorSkin.class);
    GConnectorSkin skin = factory.call(pConnector);

    skin.setGraphEditor(mGraphEditor);
    return skin;
  }

  /**
   * Creates a new {@link GTailSkin} for the given {@link GConnectorPort} using the registered factory.
   * <p>
   * This method retrieves the appropriate factory based on the component type and skin type,
   * and uses it to create a new skin instance. If no custom factory is registered, the default
   * factory will be used.
   * </p>
   *
   * @param pConnector the {@link GConnectorPort} for which to create a skin
   * @return a new instance of {@link GTailSkin}
   */
  private GTailSkin createTailSkin(final GConnectorPort pConnector) {
    Callback<GConnectorPort, GTailSkin> factory = getSkinFactory(pConnector.getClass(), GTailSkin.class);
    GTailSkin skin = factory.call(pConnector);

    skin.setGraphEditor(mGraphEditor);
    return skin;
  }

  /**
   * Creates a new {@link GConnectionSkin} for the given {@link GConnection} using the registered factory.
   * <p>
   * This method retrieves the appropriate factory based on the component type and skin type,
   * and uses it to create a new skin instance. If no custom factory is registered, the default
   * factory will be used.
   * </p>
   *
   * @param pConnection the {@link GConnection} for which to create a skin
   * @return a new instance of {@link GConnectionSkin}
   */
  private GConnectionSkin createConnectionSkin(final GConnection pConnection) {
    Callback<GConnection, GConnectionSkin> factory = getSkinFactory(pConnection.getClass(), GConnectionSkin.class);
    GConnectionSkin skin = factory.call(pConnection);

    skin.setGraphEditor(mGraphEditor);
    if (!(skin instanceof VirtualSkin)) {
      mView.add(skin);
    }
    return skin;
  }

  /**
   * Creates a new {@link GJointSkin} for the given {@link GJoint} using the registered factory.
   * <p>
   * This method retrieves the appropriate factory based on the component type and skin type,
   * and uses it to create a new skin instance. If no custom factory is registered, the default
   * factory will be used.
   * </p>
   *
   * @param pJoint the {@link GJoint} for which to create a skin
   * @return a new instance of {@link GJointSkin}
   */
  private GJointSkin createJointSkin(final GJoint pJoint) {
    Callback<GJoint, GJointSkin> factory = getSkinFactory(pJoint.getClass(), GJointSkin.class);
    GJointSkin skin = factory.call(pJoint);

    skin.setGraphEditor(mGraphEditor);
    skin.getRoot().setEditorProperties(mGraphEditor.getProperties());
    skin.impl_setOnPositionMoved(mOnPositionMoved);
    skin.initialize();
    if (!(skin instanceof VirtualSkin)) {
      mView.add(skin);
    }
    return skin;
  }

  /**
   * Creates a new {@link GNodeSkin} for the given {@link GNode} using the registered factory.
   * <p>
   * This method retrieves the appropriate factory based on the component type and skin type,
   * and uses it to create a new skin instance. If no custom factory is registered, the default
   * factory will be used.
   * </p>
   *
   * @param pNode the {@link GNode} for which to create a skin
   * @return a new instance of {@link GNodeSkin}
   */
  private GNodeSkin createNodeSkin(final GNode pNode) {
    Callback<GNode, GNodeSkin> factory = getSkinFactory(pNode.getClass(), GNodeSkin.class);
    GNodeSkin skin = factory.call(pNode);

    skin.setGraphEditor(mGraphEditor);
    skin.getRoot().setEditorProperties(mGraphEditor.getProperties());
    skin.impl_setOnPositionMoved(mOnPositionMoved);
    skin.initialize();
    if (!(skin instanceof VirtualSkin)) {
      mView.add(skin);
    }
    return skin;
  }

  private void positionMoved(final GSkin<?> pMovedSkin) {
    final ConnectionLayouter layouter = mConnectionLayouter;
    if (layouter != null && (pMovedSkin instanceof GNodeSkin || pMovedSkin instanceof GJointSkin)) {
      layouter.draw();
    }
  }
}
