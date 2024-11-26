package com.ergotech.grapheditor.model.impl;

import com.ergotech.grapheditor.model.Selectable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SelectableType implements Selectable {
  
  protected final StringProperty type = new SimpleStringProperty(this, "type");
  
  private final StringProperty id = new SimpleStringProperty(this, "id");
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
  @Override
  public String getType() {
      return type.get();
  }

  /**
   * Sets the value of the 'Type' attribute.
   *
   * @param value the new value of the type.
   */
  @Override
  public void setType(String value) {
      type.set(value);
  }

  /**
   * Returns the value of the 'Id' attribute.
   *
   * @return the id of the joint.
   */
  @Override
  public String getId() {
    return id.get();
  }

  /**
   * Sets the value of the 'Id' attribute.
   *
   * @param value
   *          the new value of the id.
   */
  @Override
  public void setId(String value) {
    id.set(value);
  }


}
