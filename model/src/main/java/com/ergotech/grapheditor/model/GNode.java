package com.ergotech.grapheditor.model;

import java.util.List;

public interface GNode extends Selectable {
 
  // Connectors
  public List<GConnector> getConnectors();

  // Methods to add and remove connectors
  public void addConnector(GConnector connector);

  public void removeConnector(GConnector connector);

}
