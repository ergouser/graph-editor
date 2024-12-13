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

}
