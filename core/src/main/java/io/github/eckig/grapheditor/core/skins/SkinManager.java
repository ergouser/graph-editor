package io.github.eckig.grapheditor.core.skins;

import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GNode;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GSkin;
import io.github.eckig.grapheditor.GraphEditorSkins;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.view.ConnectionLayouter;


/**
 * Graph Editor Skin Manager
 *
 * @since 09.02.2016
 */
public interface SkinManager extends SkinLookup, GraphEditorSkins
{

  /**
   * @param pConnectionLayouter
   *            {@link ConnectionLayouter}
   * @since 16.01.2019
   */
  void setConnectionLayouter(final ConnectionLayouter pConnectionLayouter);

  /**
   * remove all cached skins and clear the graph editor view
   */
  void clear();

  /**
   * Removes the given {@link GNode} skin from the view
   *
   * @param pNodeToRemove
   *            node to remove
   * @since 10.02.2016
   */
  void removeNode(final GNode pNodeToRemove);

  /**
   * Removes the given {@link GNode} skin from the view if showing
   * Removes the node from the Skin map
   * Disposes the Skin.
   *
   * @param pNodeToRemove
   *            node to remove
   */
 void disposeNode(GNode pNodeToRemove);
 
 /**
  * Connectors are part of the node so remove does not remove 
  * them from the node.  
  * This does remove the transient tailskins
  *
  * @param pConnectorToRemove
  *            connector to remove
  * @since 10.02.2016
  */
 void removeConnector(final GConnectorPort pConnectorToRemove);

 /**
  * Removes the given {@link GConnectorPort} skin from the Skin map
  * Disposes the Skin.
  *
  * @param pConnectorToRemove
  *            connector to remove
  * @since 10.02.2016
  */
 void disposeConnector(final GConnectorPort pConnectorToRemove);

  /**
   * Removes the given {@link GConnection} skin from the view
   *
   * @param pConnectionToRemove
   *            connection to remove
   * @since 10.02.2016
   */
  void removeConnection(final GConnection pConnectionToRemove);

  /**
   * Removes the given {@link GConnection} skin from the view
   * Removes the connection from the Skin map
   * Disposes the Skin.
   *
   * @param pConnectionToRemove
   *            connection to remove
   */
  void disposeConnection(final GConnection pConnectionToRemove);

  /**
   * Removes the given {@link GJoint} skin from the view
   *
   * @param pJointToRemove
   *            joint to remove
   * @since 10.02.2016
   */
  void removeJoint(final GJoint pJointToRemove);
  /**
   * Calls {@link GNodeSkin#setConnectorSkins(List)} to update a nodes list of
   * connectors.
   *
   * @param pNode
   *            node to update
   * @since 10.02.2016
   */
  void updateConnectors(final GNode pNode);

  //    /**
  //     * Calls {@link GConnectionSkin#setJointSkins(List)} to update a connections
  //     * list of joints.
  //     *
  //     * @param pConnection
  //     *            connection to update
  //     * @since 17.02.2016
  //     */
  //    void updateJoints(final GConnection pConnection);

  /**
   * Creates (if not yet existing) and returns the skin for the given item
   *
   * @param pNode
   * @return skin
   * @since 21.01.2019
   */
  default GNodeSkin lookupOrCreateNode(final GNode pNode) {
    return lookupOrCreateSkin(pNode.getClass(), GNodeSkin.class); 
  }

  /**
   * Creates (if not yet existing) and returns the skin for the given item
   *
   * @param pConnector
   * @return skin
   * @since 21.01.2019
   */
  default GConnectorSkin lookupOrCreateConnector(final GConnectorPort pConnector)  {
    return lookupOrCreateSkin(pConnector.getClass(), GConnectorSkin.class); 
  }


  /**
   * Creates (if not yet existing) and returns the skin for the given item
   *
   * @param pConnection
   * @return skin
   * @since 21.01.2019
   */
  default GConnectionSkin lookupOrCreateConnection(final GConnection pConnection)  {
    return lookupOrCreateSkin(pConnection.getClass(), GConnectionSkin.class); 
  }


  /**
   * Creates (if not yet existing) and returns the skin for the given item
   *
   * @param pJoint
   * @return skin
   * @since 21.01.2019
   */
  default GJointSkin lookupOrCreateJoint(final GJoint pJoint)  {
    return lookupOrCreateSkin(pJoint.getClass(), GJointSkin.class); 
  }


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
  public <T, R extends GSkin<?>> R lookupOrCreateSkin(T component, Class<R> skinType);

}
