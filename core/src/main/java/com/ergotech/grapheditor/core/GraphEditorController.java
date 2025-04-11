package com.ergotech.grapheditor.core;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.command.CompoundCommand;
import com.ergotech.grapheditor.model.impl.GConnectionImpl;
import com.ergotech.grapheditor.model.impl.GNodeImpl;

import io.github.eckig.grapheditor.Commands;
import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GConnectorValidator;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SelectionManager;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.ModelEditingManager;
import io.github.eckig.grapheditor.core.connections.ConnectionEventManager;
import io.github.eckig.grapheditor.core.connections.ConnectorDragManager;
import io.github.eckig.grapheditor.core.model.DefaultModelEditingManager;
import io.github.eckig.grapheditor.core.model.ModelLayoutUpdater;
import io.github.eckig.grapheditor.core.model.ModelSanityChecker;
import io.github.eckig.grapheditor.core.selections.DefaultSelectionManager;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import io.github.eckig.grapheditor.core.view.ConnectionLayouter;
import io.github.eckig.grapheditor.core.view.GraphEditorView;
import io.github.eckig.grapheditor.core.view.impl.DefaultConnectionLayouter;
import io.github.eckig.grapheditor.utils.GraphEditorProperties;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * The central controller class for the default graph editor implementation.
 *
 * <p>
 * Responsible for using the {@link SkinManager} to create all skin instances for the current {@link GModel}, and adding
 * them to the {@link GraphEditorView view}.
 * </p>
 *
 * <p>
 * Also responsible for creating all secondary managers like the {@link ConnectorDragManager} and reinitializing them
 * when the model changes.
 * </p>
 *
 * <p>
 * The process of synchronizing is rather complicated in case more than one model is part of the resource set:
 * <ol>
 * <li>register listener on every model in the resource set (with an {@link EContentAdapter}</li>
 * <li>receive notifications</li>
 * <li>put notification into queue</li>
 * <li>{@link #process() process queue} on every reload and/or command stack change</li>
 * </ol>
 * This procedure (processing a chunk of notifications on command stack change or {@link GraphEditor#reload()} is a very
 * safe way to determine a valid package of changes.
 * </p>
 *
 * <p>
 * This implementation is thread safe: It is able to process notifications in parallel and processes them in chunks on
 * the FX Thread.
 * </p>
 */
public class GraphEditorController<E extends GraphEditor> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GraphEditorController.class);

  private final ModelEditingManager mModelEditingManager = new DefaultModelEditingManager();

  private final ModelLayoutUpdater mModelLayoutUpdater;

  private final ConnectionLayouter mConnectionLayouter;

  private final ConnectorDragManager mConnectorDragManager;

  private final DefaultSelectionManager mSelectionManager;

  private final SkinManager mSkinManager;

  private final E mEditor;

  private final ChangeListener<GModel> mModelChangeListener = (w, o, n) -> modelChanged(o, n);

  private final ListChangeListener<GNode> nodesChangeListener = change -> {
    while (change.next()) {
      if (change.wasAdded()) {
        for (GNode node : change.getAddedSubList()) {
          addNode(node);
        }
      }
      if (change.wasRemoved()) {
        for (GNode node : change.getRemoved()) {
          removeNode(node);
        }
      }
    }
  };

  private final ListChangeListener<GConnection> connectionsChangeListener = change -> {
    while (change.next()) {
      if (change.wasAdded()) {
        for (GConnection connection : change.getAddedSubList()) {
          addConnection(connection);
        }
      }
      if (change.wasRemoved()) {
        for (GConnection connection : change.getRemoved()) {
          removeConnection(connection);
        }
      }
    }
  };

  public GraphEditorController(final E pEditor, final SkinManager pSkinManager, final GraphEditorView pView,
      final ConnectionEventManager pConnectionEventManager, final GraphEditorProperties pProperties) {

    mEditor = Objects.requireNonNull(pEditor, "GraphEditor instance may not be null!");
    mConnectionLayouter = new DefaultConnectionLayouter(pSkinManager);

    mSkinManager = Objects.requireNonNull(pSkinManager, "SkinManager may not be null!");

    mModelLayoutUpdater = new ModelLayoutUpdater(pSkinManager, mModelEditingManager, pProperties);
    mConnectorDragManager = new ConnectorDragManager(pSkinManager, pConnectionEventManager, pView);
    mSelectionManager = new DefaultSelectionManager(pSkinManager, pView);

    //itDefaultListeners();

    pEditor.modelProperty().addListener(mModelChangeListener);
    if ( pEditor.getModel() != null ) {
      modelChanged(null, pEditor.getModel());
    }
  }

//  private void initDefaultListeners() {
  // this is a duplicate of code on ModelChanged.
//    GModel model = mEditor.modelProperty().get();
//    addModelListeners(model);
//
//    for (GNode node : model.getNodes()) {
//      addNode(node);
//    }
//    for (GConnection connection : model.getConnections()) {
//      addConnection(connection);
//    }
//  }

  private void addModelListeners(GModel model) {
    // Set up listeners on the model's nodes list
    ((ObservableList<? extends GNode>) model.getNodes()).addListener(nodesChangeListener);
    // Set up listeners on the model's connections list
    //((ObservableList<? extends GConnection>)model.getConnections()).addListener(connectionsChangeListener);
  }

  private void removeModelListeners(GModel model) {
    // Remove listeners on the model's nodes list
    ((ObservableList<? extends GNode>) model.getNodes()).removeListener(nodesChangeListener);

    // Remove listeners on the model's connections list
    //((ObservableList<? extends GConnection>) model.getConnections()).removeListener(connectionsChangeListener);

  }

  private void addConnection(GConnection connection) {
    addConnectionListeners(connection);
    mSkinManager.lookupOrCreateConnection(connection);
    mSelectionManager.addConnection(connection);
    mConnectionLayouter.draw();

  }  

  private void addConnectionListeners(GConnection connection) {
    // Create listeners
    ChangeListener<GConnectorPort> sourceListener = (observable, oldValue, newValue) -> updateConnection(connection);
    ChangeListener<GConnectorPort> targetListener = (observable, oldValue, newValue) -> updateConnection(connection);
    ChangeListener<String> typeListener = (observable, oldValue, newValue) -> updateConnection(connection);
    ChangeListener<Boolean> bidirectionalListener = (observable, oldValue, newValue) -> updateConnection(connection);

    ListChangeListener<GJoint> jointsListener = change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          for (GJoint joint : change.getAddedSubList()) {
            addJoint(joint, connection);
          }
        }
        if (change.wasRemoved()) {
          for (GJoint joint : change.getRemoved()) {
            removeJoint(joint);
          }
        }
      }
    };

    // Use the GConnection's method to add listeners
    ((GConnectionImpl)connection).addListeners(sourceListener, targetListener, typeListener, bidirectionalListener, jointsListener);

    GConnectionSkin connectionSkin = mSkinManager.lookupConnection(connection);
    // Process existing joints
    for (GJointSkin jointSkin : connectionSkin.getJointSkins()) {
      addJoint(jointSkin.getItem(),connection);
    }
  }

  private void addNode(GNode node) {
    addNodeListeners(node);
    mSkinManager.lookupOrCreateNode(node);
    mModelLayoutUpdater.addNode(node);
    mSelectionManager.addNode(node);
    markConnectorsDirty(node); // Ensure connectors are marked dirty when a node is added
   // probably should be something done with ConnectorPorts here...
  } 
  private void addNodeListeners(GNode node) {
    // Create listeners
    ChangeListener<Number> xListener = (observable, oldValue, newValue) -> nodePositionChanged(node);
    ChangeListener<Number> yListener = (observable, oldValue, newValue) -> nodePositionChanged(node);
    ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> nodeSizeChanged(node);
    ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> nodeSizeChanged(node);
    ChangeListener<String> typeListener = (observable, oldValue, newValue) -> {
      removeNode(node);
      addNode(node);
    };
    ListChangeListener<GConnectorPort> connectorsListener = change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          for (GConnectorPort connector : change.getAddedSubList()) {
            addConnector(connector);
          }
        }
        if (change.wasRemoved()) {
          for (GConnectorPort connector : change.getRemoved()) {
            removeConnector(connector);
          }
        }
      }
      markConnectorsDirty(node);
    };

    // Use the GNode's method to add listeners
    ((GNodeImpl)node).addListeners(typeListener, connectorsListener);

    // Process existing connectors
    for (GConnectorPort connector : node.getConnectorPorts()) {
      addConnector(connector);
    }
  }

  private void markConnectorsDirty(GNode node) {
    Platform.runLater(() -> {
      mSkinManager.updateConnectors(node);
    });
  }

  /** This action is not undoable and so will also invalidate the
   * undo stack.
   * 
   * @param pOldModel
   * @param pNewModel
   */
   private void modelChanged(final GModel pOldModel, final GModel pNewModel) {
    if (pOldModel != null) {
      // Remove listeners from the old model
      removeModelListeners(pOldModel);

      // Remove all nodes
      for (GNode node : new ArrayList<>(pOldModel.getNodes())) {
        removeNode(node);
      }

      //      // Remove all connections
      //      for (GConnection connection : new ArrayList<>(pOldModel.getConnections())) {
      //        removeConnection(connection);
      //      }

      // Clear any remaining skins
      mSkinManager.clear();
    }

    if (pNewModel != null) {
      // Validate the new model
      ModelSanityChecker.validate(pNewModel,mSkinManager);

      // Initialize the model editing manager
      mModelEditingManager.initialize(pNewModel);

      // Set up listeners on the new model
      addModelListeners(pNewModel);

      // Add existing nodes
      for (GNode node : pNewModel.getNodes()) {
        addNode(node);
      }

      //      // Add existing connections
      //      for (GConnection connection : pNewModel.getConnections()) {
      //        addConnection(connection);
      //      }

      // Initialize managers with the new model
      mSelectionManager.initialize(pNewModel);
      mConnectionLayouter.initialize(pNewModel);
      mConnectorDragManager.initialize(pNewModel);

      // Wait until the view's scene is available, then update layout values
      executeOnceWhenPropertyIsNonNull(mEditor.getView().sceneProperty(),
          scene -> Platform.runLater(() -> updateLayoutValues(pNewModel)));
    }
  }

  private void updateLayoutValues(final GModel pModel) {
    // because we defer execution with Platform.runLater()
    // we have to check if the given model is still valid:
    if (mEditor.getModel() != pModel) {
      return;
    }

    // When the model is loaded from the database and painted to the UI sometimes
    // the rendering process calculates different sizes than the ones stored in the model
    // which triggers a change..
    // for this case we wait until the rendering is done and update the layout values by hand
    final CompoundCommand cmd = new CompoundCommand();
    Commands.updateLayoutValues(cmd, pModel, getSkinLookup());
    if (!cmd.getCommandList().isEmpty() && cmd.canExecute()) {
      cmd.execute();
    }
  }

  private void updateConnection(GConnection connection) {
    Platform.runLater(() -> {
      mConnectionLayouter.draw();
    });
  }

  private void addJoint(GJoint joint,GConnection connection) {
    //joint.setConnection(connection);
    addJointListeners(joint);
    mSkinManager.lookupOrCreateJoint(joint);
    mSelectionManager.addJoint(joint);
    jointPositionChanged(joint);

  }
  private void addJointListeners(GJoint joint) {
    // Create listeners
    ChangeListener<Number> xListener = (observable, oldValue, newValue) -> jointPositionChanged(joint);
    ChangeListener<Number> yListener = (observable, oldValue, newValue) -> jointPositionChanged(joint);

    // Use GJoint's method to add listeners
    //joint.addListeners(xListener, yListener);
  }

  private void removeJoint(GJoint joint) {
    //joint.setConnection(null);
    mSelectionManager.removeJoint(joint);
    mSkinManager.removeJoint(joint);
    //joint.removeListeners();
  }

  private void addConnector(GConnectorPort connector) {
    GConnectorSkin connectorSkin = mSkinManager.lookupOrCreateConnector(connector);
    mConnectorDragManager.addConnector(connectorSkin);
    mSelectionManager.addConnector(connector);
  }

  private void removeConnector(GConnectorPort connector) {
    mSelectionManager.removeConnector(connector);
    mConnectorDragManager.removeConnector(connector);
    mSkinManager.removeConnector(connector);
    // nothing to remove, not listeners added.  connector.removeListeners();
  }

  private void removeConnection(GConnection connection) {
    GConnectionSkin connectionSkin = mSkinManager.lookupConnection(connection);
    mSelectionManager.removeConnection(connection);
    mSkinManager.removeConnection(connection);
    for (GJointSkin jointSkin : connectionSkin.getJointSkins()) {
      removeJoint(jointSkin.getItem());
    }
    ((GConnectionImpl)connection).removeListeners();
  }

  private void removeNode(GNode node) {
    mSelectionManager.removeNode(node);
    mSkinManager.removeNode(node);
    ((GNodeImpl)node).removeListeners();
    for (GConnectorPort connector : node.getConnectorPorts()) {
      removeConnector(connector);
    }
  }

  private void nodePositionChanged(GNode node) {
    Platform.runLater(() -> {
      GNodeSkin skin = mSkinManager.lookupNode(node);
      if (skin != null) {
        skin.getRoot().relocate(skin.getX(), skin.getY());
      }
    });
  }

  private void nodeSizeChanged(GNode node) {
    Platform.runLater(() -> {
      GNodeSkin skin = mSkinManager.lookupNode(node);
      if (skin != null) {
        skin.getRoot().resize(skin.getWidth(), skin.getHeight());
      }
    });
  }

  private void jointPositionChanged(GJoint joint) {
    Platform.runLater(() -> {
      GJointSkin skin = mSkinManager.lookupJoint(joint);
      if (skin != null) {
        skin.initialize();
      }
    });
  }

  public final ConnectionLayouter getConnectionLayouter() {
    return mConnectionLayouter;
  }

  public final E getEditor() {
    return mEditor;
  }

  public final SkinLookup getSkinLookup() {
    return mSkinManager;
  }

  public final SelectionManager getSelectionManager() {
    return mSelectionManager;
  }

  public final void setConnectorValidator(final GConnectorValidator validator) {
    mConnectorDragManager.setValidator(validator);
  }

  public final ModelEditingManager getModelEditingManager() {
    return mModelEditingManager;
  }

  private static <T> void executeOnceWhenPropertyIsNonNull(final ObservableValue<T> pProperty,
      final Consumer<T> pConsumer) {
    if (pProperty == null) {
      return;
    }

    final T value = pProperty.getValue();
    if (value != null) {
      pConsumer.accept(value);
    } else {
      final InvalidationListener listener = new InvalidationListener() {
        @Override
        public void invalidated(final Observable observable) {
          final T newValue = pProperty.getValue();
          if (newValue != null) {
            pProperty.removeListener(this);
            pConsumer.accept(newValue);
          }
        }
      };
      pProperty.addListener(new WeakInvalidationListener(listener));
    }
  }
}
