package com.ergotech.grapheditor.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SelectableType implements Selectable {
  
  protected final StringProperty type = new SimpleStringProperty(this, "type");
  
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


}
