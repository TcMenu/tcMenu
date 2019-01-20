module com.thecoderscorner.tcmenu.menuEditorUI {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;

    requires slf4j.api;

    requires com.fazecast.jSerialComm;
    requires com.thecoderscorner.tcmenu.javaapi;

    exports com.thecoderscorner.menu.examples.simpleui;
}