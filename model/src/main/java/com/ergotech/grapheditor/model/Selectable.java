package com.ergotech.grapheditor.model;

/** This interface is the superinterface for all nodes, connections, joints, etc. that can be selected on the screen. */
public interface Selectable {

  /**
   * Gets the unique identifier of the connection.
   *
   * @return the id of the connection as a String.
   */
  public void setId(String value);

  /**
   * Returns the value of the 'Id' attribute.
   *
   * @return the id of the connection.
   */
  public String getId();

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
