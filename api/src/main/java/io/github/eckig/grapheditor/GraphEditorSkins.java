/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.eckig.grapheditor;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GNode;

import javafx.util.Callback;

/**
 * Provides functionality for customizing the display of the graph elements.
 * @author eckig
 */
public interface GraphEditorSkins {

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
  public <T, R> void setSkinFactory(Class<T> componentType, Class<R> skinType, Callback<T, R> factory);
  /**
   * Sets the custom node skin factory.
   *
   * @param nodeSkinFactory factory for creating the skins
   */
  default public void setNodeSkinFactory(final Callback<GNode, GNodeSkin> nodeSkinFactory) {
    setSkinFactory(GNode.class, GNodeSkin.class, nodeSkinFactory);
  }


  /**
   * Sets the custom connector skin factory.
   *
   * @param connectorSkinFactory factory for creating the skins
   */
  default public void setConnectorSkinFactory(final Callback<GConnectorPort, GConnectorSkin> pConnectorSkinFactory) {
    setSkinFactory(GConnectorPort.class, GConnectorSkin.class, pConnectorSkinFactory);
  }

  /**
   * Sets the custom connection skin factory.
   *
   * @param connectionSkinFactory factory for creating the skins
   */
  default public void setConnectionSkinFactory(final Callback<GConnection, GConnectionSkin> pConnectionSkinFactory) {
    setSkinFactory(GConnection.class, GConnectionSkin.class, pConnectionSkinFactory);
  }

  /**
   * Sets the custom joint skin factory.
   *
   * @param jointSkinFactory factory for creating the skins
   */
  default public void setJointSkinFactory(final Callback<GJoint, GJointSkin> pJointSkinFactory) {
    setSkinFactory(GJoint.class, GJointSkin.class, pJointSkinFactory);
  }

  /**
   * Sets the custom tail skin factory.
   *
   * @param tailSkinFactory factory for creating the skins
   */
  default public void setTailSkinFactory(final Callback<GConnectorPort, GTailSkin> pTailSkinFactory) {
    setSkinFactory(GConnectorPort.class, GTailSkin.class, pTailSkinFactory);
  }

}
