module com.thecoderscorner.tcmenu.menuEditorUI {
    requires java.prefs;
    requires java.desktop;
    requires java.sql;
    requires java.logging;

    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;

    requires com.fazecast.jSerialComm;
    requires com.thecoderscorner.tcmenu.javaapi;
    requires gson;

    // allow javafx components to see the editor UI packages that contain controllers etc.
    exports com.thecoderscorner.menu.editorui;
    exports com.thecoderscorner.menu.editorui.controller;
    exports com.thecoderscorner.menu.editorui.generator.ui;
}