module io.github.eckig.grapheditor.api
{
    requires transitive javafx.controls;
    requires transitive com.ergotech.grapheditor.model;
    requires org.slf4j;

    exports io.github.eckig.grapheditor;
    exports io.github.eckig.grapheditor.utils;
    exports io.github.eckig.grapheditor.window;
}
