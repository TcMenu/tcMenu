module com.thecoderscorner.tcmenu.menuEditorUI {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;

    requires slf4j.api;

    requires com.fazecast.jSerialComm;
    requires com.thecoderscorner.tcmenu.javaapi;
    requires java.prefs;
    requires java.desktop;
    requires gson;
    requires org.apache.commons.lang3;
    requires java.sql;

    // allow javafx components to see the editor UI packages that contain controllers etc.
    exports com.thecoderscorner.menu.editorui;
    exports com.thecoderscorner.menu.editorui.controller;
    exports com.thecoderscorner.menu.editorui.generator.ui;
}