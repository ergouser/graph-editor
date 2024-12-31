package com.ergotech.grapheditor.model;

import java.util.Collection;

public interface GNode extends Selectable {
 
  // Connectors
  public Collection<? extends GConnectorPort> getConnectorPorts();

  // Methods to add and remove connectors
  public void addConnectorPort(GConnectorPort connectorPort);

  public void removeConnectorPort(GConnectorPort connectorPort);

}
