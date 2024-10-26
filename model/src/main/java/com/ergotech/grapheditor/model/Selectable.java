package com.ergotech.grapheditor.model;

/** This interface is the superinterface for all nodes, connections, joints, etc. that can be selected on the screen. */
public interface Selectable {

  /**
   * Returns the value of the 'Type' attribute.
   *
   * @return the type of the connection.
   */
  public String getType();
  
  /**
   * Sets the value of the 'Type' attribute.
   *
   * @param value the new value of the type.
   */
  public void setType(String value);

}
