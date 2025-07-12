/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.model;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;
import com.ergotech.grapheditor.model.GJoint;
import com.ergotech.grapheditor.model.GModel;
import com.ergotech.grapheditor.model.GNode;
import com.ergotech.grapheditor.model.Selectable;
import com.ergotech.grapheditor.model.command.Command;
import com.ergotech.grapheditor.model.command.CommandStack;
import com.ergotech.grapheditor.model.command.CommandStackListener;
import com.ergotech.grapheditor.model.command.CompoundCommand;
import com.ergotech.grapheditor.model.command.RemoveCommand;

import io.github.eckig.grapheditor.Commands;
import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.ModelEditingManager;
import io.github.eckig.grapheditor.utils.RemoveContext;

/**
 * Default {@link ModelEditingManager} ementation
 */
public class DefaultModelEditingManager implements ModelEditingManager {

  protected GModel model;

  private BiFunction<RemoveContext, GConnection, Command> mOnConnectionRemoved;

  private BiFunction<RemoveContext, GNode, Command> mOnNodeRemoved;
  
  /**
   * Creates a new model editing manager. Only one instance should exist per {@link DefaultGraphEditor} instance.
   *
   * @param pCommandStackListener
   *          the {@link CommandStackListener} that listens for changes in the model
   */
  public DefaultModelEditingManager() {
  }

  @Override
  public void initialize(final GModel pModel) {
    // Only initialize the editing domain if the model object has actually changed.
    //    if (!pModel.equals(model)) {
    //      initializeEditingDomain(model, pModel);
    //    }
    model = pModel;
  }

  @Override
  public void setOnConnectionRemoved(final BiFunction<RemoveContext, GConnection, Command> pOnConnectionRemoved) {
    mOnConnectionRemoved = pOnConnectionRemoved;
  }

  @Override
  public void setOnNodeRemoved(final BiFunction<RemoveContext, GNode, Command> pOnNodeRemoved) {
    mOnNodeRemoved = pOnNodeRemoved;
  }

  @Override
  public void updateLayoutValues(final SkinLookup skinLookup) {
    final CompoundCommand command = new CompoundCommand();

    CommandStack.getCommandStack(model).suspendStackChangeNotifications();
    try {
      Commands.updateLayoutValues(command, model, skinLookup);

      if (command.canExecute()) {
        try {
          CommandStack.getCommandStack(model).execute(command);
        } catch ( Exception e ) {
          // keep the default behavior of updateLayoutValues, where there is no error handling, but permit it 
          throw new UndeclaredThrowableException(e);
        }
      }
    } finally {
      CommandStack.getCommandStack(model).resumeStackChangeNotifications();
    }
  }

  @Override
  public void remove(final Collection<Selectable> pToRemove, final SkinLookup skinLookup ) {
    if (pToRemove == null || pToRemove.isEmpty()) {
      return;
    }

    final CompoundCommand command = new CompoundCommand();
    final RemoveContext editContext = new RemoveContext();
    
    // remove all the joints that are referenced in pToRemove
    // that are also part of a Connection that is about to be deleted.
    Collection<Selectable> filteredToRemove = pToRemove.stream()
        .filter(sel -> {
            if (!(sel instanceof GJoint)) return true;

            GJoint joint = (GJoint) sel;

            return pToRemove.stream()
                .filter(c -> c instanceof GConnection)
                .map(c -> skinLookup.lookupConnection((GConnection) c))
                .filter(Objects::nonNull)
                .noneMatch(connectionSkin -> connectionSkin.getJoints().contains(joint));
        })
        .collect(Collectors.toList());

    final List<Selectable> delete = new ArrayList<>(filteredToRemove.size());

    // pre-fill the RemoveContext with all elements to be removed:
    for (final Selectable obj : filteredToRemove) {
      if (obj instanceof GNode n && editContext.canRemove(obj)) {
        delete.add(obj);
        for (final GConnectorPort connector : n.getConnectorPorts()) {
          for (final GConnection connection : connector.getConnections()) {
            if (connection != null && editContext.canRemove(connection)) {
              delete.add(connection);
            }
          }
        }
      } else if (obj instanceof GConnection connection && editContext.canRemove(obj)) {
        delete.add(obj);
        GConnectionSkin connectionSkin = skinLookup.lookupConnection(connection);
        delete.addAll(connectionSkin.getJoints());
      } else if (obj instanceof GJoint && editContext.canRemove(obj)) {
        delete.add(obj);
      }
    }

    // delete the elements and call business logic add-ins:
    for (final Selectable obj : delete) {
      if (obj instanceof GNode) {
        command.append(createNodeRemoveCommand((GNode) obj));
        // command.append(RemoveCommand.create(model, owner -> model.getNodes(), (GNode)obj));

        final Command onRemoved = mOnNodeRemoved == null ? null : mOnNodeRemoved.apply(editContext, (GNode) obj);
        if (onRemoved != null) {
          command.append(onRemoved);
        }
      } else if (obj instanceof GConnection) {
        remove(editContext, command, (GConnection) obj);
      } else if (obj instanceof GJoint joint) {
        // find the matching connection...
        GConnectionSkin matchingSkin = model.getNodes().stream()
            .flatMap(node -> node.getConnectorPorts().stream())
            .flatMap(port -> port.getConnections().stream())
            .map(connection -> skinLookup.lookupConnection(connection))
            .filter(Objects::nonNull)
            .filter(connectionSkin -> connectionSkin.getJoints().contains(joint))
            .findFirst()
            .orElse(null);
        command.append(RemoveCommand.create(matchingSkin, owner -> matchingSkin.getJoints(), joint));
      }
    }
    

    if (!command.isEmpty() && command.canExecute()) {
      try {
        CommandStack.getCommandStack(model).execute(command);
      } catch ( Exception e ) {
        // keep the default behavior of remove, where there is no error handling, but permit it 
        throw new UndeclaredThrowableException(e);
      }
    }
  }

  /**
   * return a RemoveCommand - the default
   */
  protected Command createNodeRemoveCommand(GNode node) {
    return RemoveCommand.create(model, owner -> model.getNodes(), node);
  }
  
  private void remove(final RemoveContext pRemoveContext, final CompoundCommand pCommand, final GConnection pToDelete) {
    final GConnectorPort source = pToDelete.getSourcePort();
    final GConnectorPort target = pToDelete.getTargetPort();

    if ( source != null && target != null ) { // not a GConnection dragged on the screen - 
      // Remove the connection from the model's connections list
      //pCommand.append(RemoveCommand.create(model, owner -> model.getConnections(), pToDelete));

      // Remove the connection from the source connector's connections list
      pCommand.append(RemoveCommand.create(source, owner -> ((GConnectorPort) owner).getConnections(), pToDelete));

      // Remove the connection from the target connector's connections list
      //pCommand.append(RemoveCommand.create(target, owner -> ((GConnectorPort) owner).getConnections(), pToDelete));

      final Command onRemoved = mOnConnectionRemoved == null ? null
          : mOnConnectionRemoved.apply(pRemoveContext, pToDelete);
      if (onRemoved != null) {
        pCommand.append(onRemoved);
      }
    }
  }
  
  /**
   * Initializes the editing domain and resource for the new model.
   *
   * <p>
   * If a resource and/or editing domain are already associated to this model, these will be used. Otherwise they will
   * be created.
   * </p>
   * @throws NoSuchMethodException this method is not emented
   */
  private void initializeEditingDomain(final GModel oldModel, final GModel newModel) throws NoSuchMethodException {
    throw new NoSuchMethodException("initializeEditingDomain not implemented");
    // First remove the listener from the old model, if it exists.
    //    if (oldModel != null) {
    //      final EditingDomain oldDomain = AdapterFactoryEditingDomain.getEditingDomainFor(oldModel);
    //      if (oldDomain != null) {
    //        oldDomain.getCommandStack().removeCommandStackListener(commandStackListener);
    //      }
    //    }
    //
    //    if (newModel.eResource() == null) {
    //      final XMIResourceFactory resourceFactory = new XMIResourceFactory();
    //      final Resource resource = resourceFactory.createResource(DEFAULT_URI);
    //      resource.getContents().add(newModel);
    //    }
    //
    //    editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(newModel);
    //
    //    if (editingDomain == null) {
    //      final Registry registry = ComposedAdapterFactory.Descriptor.Registry.INSTANCE;
    //      final AdapterFactory adapterFactory = new ComposedAdapterFactory(registry);
    //
    //      editingDomain = new AdapterFactoryEditingDomain(adapterFactory, new BasicCommandStack());
    //      editingDomain.getResourceSet().getResources().add(newModel.eResource());
    //    }

  }

 }
