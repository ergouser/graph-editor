package com.ergotech.grapheditor.model.impl;

import com.ergotech.grapheditor.model.Selectable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SelectableType implements Selectable {
  
  
  private final StringProperty id = new SimpleStringProperty(this, "id");
 
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
