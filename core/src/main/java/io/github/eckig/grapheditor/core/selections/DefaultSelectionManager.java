/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.selections;

import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.Selectable;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GSkin;
import io.github.eckig.grapheditor.SelectionManager;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.view.GraphEditorView;
import javafx.collections.ObservableSet;


/**
 * Manages all graph editor logic relating to selections of one or more nodes
 * and/or joints.
 *
 * <p>
 * Delegates certain jobs to the following classes.
 *
 * <ol>
 * <li>SelectionCreator - creates selections of objects via clicking or dragging
 * <li>SelectionDragManager - ensures selected objects move together when one is
 * dragged
 * <li>SelectionTracker - keeps track of the current selection
 * </ol>
 *
 * </p>
 */
public class DefaultSelectionManager implements SelectionManager
{

  private final SelectionCreator selectionCreator;
  private final SelectionTracker selectionTracker;
  private final SkinLookup skinLookup;

  private GModel model;

  /**
   * Creates a new default selection manager. Only one instance should exist
   * per {@link DefaultGraphEditor} instance.
   *
   * @param skinLookup
   *            the {@link SkinLookup} instance in use
   * @param view
   *            the {@link GraphEditorView} instance in use
   */
  public DefaultSelectionManager(final SkinLookup skinLookup, final GraphEditorView view)
  {
    final SelectionDragManager selectionDragManager = new SelectionDragManager(skinLookup, view, this);
    selectionCreator = new SelectionCreator(skinLookup, view, this, selectionDragManager);
    selectionTracker = new SelectionTracker(skinLookup);
    this.skinLookup = skinLookup;
  }

  /**
   * Initializes the selection manager for the given model.
   *
   * @param model
   *            the {@link GModel} currently being edited
   */
  public void initialize(final GModel model)
  {
    this.model = model;

    selectionCreator.initialize(model);
    selectionTracker.initialize();
  }

  public void addNode(final GNode node)
  {
    selectionCreator.addNode(node);
  }

  public void removeNode(final GNode node)
  {
    selectionCreator.removeNode(node);
  }

  public void addConnector(final GConnectorPort connector)
  {
    selectionCreator.addConnector(connector);
  }

  public void removeConnector(final GConnectorPort connector)
  {
    selectionCreator.removeConnector(connector);
  }

  public void addConnection(final GConnection connection)
  {
    selectionCreator.addConnection(connection);
  }

  public void removeConnection(final GConnection connection)
  {
    selectionCreator.removeConnection(connection);
  }

  public void addJoint(final GJoint joint)
  {
    selectionCreator.addJoint(joint);
  }

  public void removeJoint(final GJoint joint)
  {
    selectionCreator.removeJoint(joint);
  }

  @Override
  public ObservableSet<GSkin<?>> getSelectedItems()
  {
    return selectionTracker.getSelectedItems();
  }

  @Override
  public void select(final GSkin<?> skin)
  {
    getSelectedItems().add(skin);
  }

  @Override
  public void clearSelection(final GSkin<?> skin)
  {
    getSelectedItems().remove(skin);
  }

  @Override
  public boolean isSelected(final GSkin<?> skin)
  {
    return getSelectedItems().contains(skin);
  }

  @Override
  public boolean isSelected(final Selectable node)
  {
    return getSelectedItems().stream()
        .anyMatch(skin -> skin.getItem() == node);  }

  @Override
  public List<GSkin<?>> getSelectedNodes()
  {
    return selectionTracker.getSelectedNodes();
  }

  @Override
  public List<GSkin<?>> getSelectedConnections()
  {
    return selectionTracker.getSelectedConnections();
  }

  @Override
  public List<GSkin<?>> getSelectedJoints()
  {
    return selectionTracker.getSelectedJoints();
  }

  @Override
  public void clearSelection()
  {
    if (!getSelectedItems().isEmpty())
    {
      // copy to prevent ConcurrentModificationException
      // (removal triggers update notification which in turn could modify the selection)
      final GSkin<?>[] selectedItems = getSelectedItems().toArray(new GSkin<?>[0]);
      for (final GSkin<?> remove : selectedItems)
      {
        getSelectedItems().remove(remove);
      }
    }
  }

  @Override
  public void selectAll()
  {
    if (model != null)
    {
      for (final GNode node : model.getNodes())
      {
        GNodeSkin nodeskin = skinLookup.lookupNode(node);
        if (nodeskin != null)
        {
          getSelectedItems().add(nodeskin);
        }
      }

      for (final GConnection connection : model.getConnections())
      {
        GConnectionSkin connectionSkin = skinLookup.lookupConnection(connection);
        if (connectionSkin != null)
        {
          getSelectedItems().add(connectionSkin);

          for (final GJoint joint : connectionSkin.getJoints())
          {
            GJointSkin jointSkin = skinLookup.lookupJoint(joint);
            if (jointSkin != null)
            {
              getSelectedItems().add(jointSkin);
            }
          }
        }
      }
    }
  }}
