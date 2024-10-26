module io.github.eckig.grapheditor.demo
{
  requires transitive javafx.controls;
  requires transitive javafx.base;
  requires transitive javafx.fxml;
    requires transitive com.ergotech.grapheditor.model;
    requires transitive io.github.eckig.grapheditor.api;
    requires transitive io.github.eckig.grapheditor.core;
    requires org.slf4j;
    requires java.desktop;

    exports io.github.eckig.grapheditor.demo;
    exports io.github.eckig.grapheditor.demo.customskins.titled;
    exports io.github.eckig.grapheditor.demo.selections;
    exports io.github.eckig.grapheditor.demo.utils;
    exports io.github.eckig.grapheditor.demo.customskins.tree;
    
    // Open the package for reflection by javafx.fxml
    opens io.github.eckig.grapheditor.demo to javafx.fxml;

 }
