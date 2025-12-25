package io.github.eckig.grapheditor.core.selections;

import java.util.HashSet;
import java.util.List;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GNode;

import io.github.eckig.grapheditor.GSkin;
import io.github.eckig.grapheditor.SkinLookup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 * Provides observable lists of selected nodes and joints for convenience.
 */
public class SelectionTracker
{

    private final ObservableSet<GSkin<?>> selectedElements = FXCollections.observableSet(new HashSet<>());
    private final SkinLookup skinLookup;

    /**
     * Creates a new {@link SelectionTracker} instance.
     *
     * @param skinLookup
     *         the {@link SkinLookup}
     */
    public SelectionTracker(final SkinLookup skinLookup)
    {
        this.skinLookup = skinLookup;
        selectedElements.addListener(this::selectedElementsChanged);
    }

    private void selectedElementsChanged(final SetChangeListener.Change<? extends GSkin<?>> change)
    {
        if (change.wasRemoved())
        {
            update(change.getElementRemoved());
        }
        if (change.wasAdded())
        {
            update(change.getElementAdded());
        }
    }

    private void update(final GSkin<?> skin)
    {
//        GSkin<?> skin = null;
//        if (obj instanceof GNode n)
//        {
//            skin = skinLookup.lookupNode(n);
//        }
//        else if (obj instanceof GJoint j)
//        {
//            skin = skinLookup.lookupJoint(j);
//        }
//        else if (obj instanceof GConnection c)
//        {
//            skin = skinLookup.lookupConnection(c);
//        }
//        else if (obj instanceof GConnectorPort c)
//        {
//            skin = skinLookup.lookupConnector(c);
//        }

        if (skin != null)
        {
            skin.updateSelection();
        }
    }

    /**
     * Initializes the selection tracker for the given model.
     */
    public void initialize()
    {
        selectedElements.clear();
    }

    /**
     * @return the list of currently selected nodes
     */
    public List<GSkin<?>> getSelectedNodes()
    {
        return selectedElements.stream().filter(e -> e.getItem() instanceof GNode).toList();
    }

    /**
     * @return the list of currently selected connections
     */
    public List<GSkin<?>> getSelectedConnections()
    {
        return selectedElements.stream().filter(e -> e.getItem() instanceof GConnection).toList();
    }

    /**
     * @return the list of currently selected joints
     */
    public List<GSkin<?>> getSelectedJoints()
    {
        return selectedElements.stream().filter(e -> e.getItem() instanceof GJoint).toList();
    }

    public ObservableSet<GSkin<?>> getSelectedItems()
    {
        return selectedElements;
    }
}
