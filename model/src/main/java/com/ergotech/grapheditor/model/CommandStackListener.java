package com.ergotech.grapheditor.model;

import java.util.EventObject;

public interface CommandStackListener {
  
  /**
   * Called when the command stack state has changed.
   * @param event the event.
   */
  void commandStackChanged(EventObject event);
}

