/*
 * Copyright (C) 2005 - 2015 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import java.util.function.Consumer;

import com.ergotech.grapheditor.model.Selectable;

import io.github.eckig.grapheditor.utils.DraggableBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 *  * Abstract base class for all graphical "skin" components in the graph editor.
 *  All skins inherit from. Contains logic common to all skins.
 *
 * This class provides a consistent mechanism for managing CSS style classes for visual elements
 * such as connections and nodes. Subclasses must specify the JavaFX {@link Node} that should receive
 * the style classes by implementing {@link #getStylableNode()}.
 * </p>
 * 
 * <p>
 * Clients can modify the style of a skin by manipulating the {@link #getStyleClass()} list,
 * similar to how JavaFX's own {@link Node#getStyleClass()} works. Any changes to this observable list
 * are automatically forwarded to the target visual node.
 * </p>
 * @param <T> the model type associated with this skin (e.g., {@code GNode}, {@code GConnection})
 */
public abstract class GSkin<T extends Selectable> {

  /**
   * An observable list of CSS style class names applied to this skin.
   * <p>
   * Modifying this list will automatically apply or remove the style classes
   * from the underlying visual {@link Node} returned by {@link #getStylableNode()}.
   * </p>
   */
  private final ObservableList<String> styleClass = FXCollections.observableArrayList();

  protected final StringProperty type = new SimpleStringProperty(this, "type");

  private final BooleanProperty selectedProperty = new BooleanPropertyBase(false) {

    @Override
    protected void invalidated() {
      selectionChanged(get());
    }

    @Override
    public Object getBean() {
      return GSkin.this;
    }

    @Override
    public String getName() {
      return "selected"; //$NON-NLS-1$
    }

  };

  protected GraphEditor graphEditor;

  protected T item;

  private Consumer<GSkin<?>> onPositionMoved;

  /**
   * Constructs a new {@code GSkin} instance and wires the {@code styleClass} list
   * to automatically forward its changes to the {@link #getStylableNode()}.
   */
  protected GSkin() {
    styleClass.addListener((ListChangeListener<String>) change -> {
      Node node = getStylableNode();
      while (change.next()) {
        if (change.wasAdded()) {
          node.getStyleClass().addAll(change.getAddedSubList());
        }
        if (change.wasRemoved()) {
          node.getStyleClass().removeAll(change.getRemoved());
        }
      }
    });
  }

  /**
   * Constructor
   *
   * @param pItem
   *          item represented by this skin
   */
  protected GSkin(T pItem) {
    this();
    this.item = pItem;
  }

  /**
   * Initializes the node skin. The default does nothing.
   *
   */
  public void initialize() {

  }

  /**
   * Sets the graph editor instance that this skin is a part of.
   *
   * @param pGraphEditor
   *          a {@link GraphEditor} instance
   */
  public void setGraphEditor(final GraphEditor pGraphEditor) {
    this.graphEditor = pGraphEditor;
    selectedProperty.addListener((obs, wasSelected, isSelected) -> {
      selectionChanged(isSelected);
    });
    updateSelection();
  }

  /**
   * Gets the graph editor instance that this skin is a part of.
   *
   * <p>
   * This is provided for advanced skin customization purposes only. Use at your own risk.
   * </p>
   *
   * @return the {@link GraphEditor} instance that this skin is a part of
   */
  protected GraphEditor getGraphEditor() {
    return graphEditor;
  }

  /**
   * Returns the observable list of style classes associated with this skin.
   * <p>
   * Add or remove entries to change the CSS classes applied to the visual node.
   * </p>
   *
   * @return an observable list of style class names.
   */
  public ObservableList<String> getStyleClass() {
    return styleClass;
  }

  /**
   * Returns the underlying JavaFX {@link Node} that visualizes this skin and should
   * receive the style classes.
   * <p>
   * Subclasses must override this method to return the visual component that the
   * style classes will be applied to.
   * </p>
   *
   * @return the stylable JavaFX node for this skin.
   */
  protected Node getStylableNode() {
    return getRoot();  // probably wrong, but not likely to be null
  }

  /**
   * Gets whether the skin is selected or not.
   *
   * @return {@code true} if the skin is selected, {@code false} if not
   */
  public boolean isSelected() {
    return selectedProperty.get();
  }

  /**
   * Sets whether the skin is selected or not.
   * <p>
   * <b>Should not</b> be called directly, the selection state is managed by the selection manager of the graph editor!
   * </p>
   *
   * @param isSelected
   *          {@code true} if the skin is selected, {@code false} if not
   */
  protected void setSelected(final boolean isSelected) {
    selectedProperty.set(isSelected);
  }

  /**
   * Updates whether this skin is in a selected state or not.
   * <p>
   * This method will be automatically called by the SelectionTracker when needed.
   * </p>
   */
  public void updateSelection() {
    setSelected(graphEditor != null && graphEditor.getSelectionManager().isSelected(item));
  }

  /**
   * The property that determines whether the skin is selected or not.
   *
   * @return a {@link BooleanProperty} containing {@code true} if the skin is selected, {@code false} if not
   */
  public ReadOnlyBooleanProperty selectedProperty() {
    return selectedProperty;
  }

  /**
   * Is called whenever the selection state has changed.
   *
   * @param isSelected
   *          {@code true} if the skin is selected, {@code false} if not
   */
  protected abstract void selectionChanged(final boolean isSelected);

  /**
   * Called after the skin is removed. Can be overridden for cleanup.
   */
  public void dispose() {
    final Node root = getRoot();
    if (root instanceof DraggableBox db) {
      db.dispose();
    }
    onPositionMoved = null;
    graphEditor = null;
  }

  /**
   * Gets the root JavaFX node of the skin.
   *
   * @return a the skin's root JavaFX {@link Node}
   */
  public abstract Node getRoot();

  /**
   * @return item represented by this skin
   */
  public T getItem() {
    return item;
  }

  /**
   * Sets the item represented by this skin.
   *
   * @param item
   *          the item to set
   */
  public void setItem(T item) {
    this.item = item;
  }

  /**
   * <p>
   * INTERNAL API
   * </p>
   *
   * @param pOnPositionMoved
   *          internal update hook to be informed when the position has been changed
   */
  public final void impl_setOnPositionMoved(final Consumer<GSkin<?>> pOnPositionMoved) {
    onPositionMoved = pOnPositionMoved;
  }

  /**
   * <p>
   * INTERNAL API
   * </p>
   * will be called when the position of this skin has been moved
   *
   * @since 16.01.2019
   */
  public final void impl_positionMoved() {
    final Consumer<GSkin<?>> inform = onPositionMoved;
    if (inform != null) {
      inform.accept(this);
    }
  }

  /**
   * Gets the type of the connection.
   *
   * @return the type of the connection as a StringProperty.
   */
  public StringProperty typeProperty() {
    return type;
  }

  /**
   * Returns the value of the 'Type' attribute.
   *
   * @return the type of the connection.
   */
  public String getType() {
    return type.get();
  }

  /**
   * Sets the value of the 'Type' attribute.
   *
   * @param value the new value of the type.
   */
  public void setType(String value) {
    type.set(value);
  }

}
