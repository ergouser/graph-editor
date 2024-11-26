/**
 * Module info for Model
 */
module com.ergotech.grapheditor.model {
  requires javafx.base;
  requires javafx.controls;
  requires java.desktop;

  exports com.ergotech.grapheditor.model;
  exports com.ergotech.grapheditor.model.impl;
  exports com.ergotech.grapheditor.model.command;
}