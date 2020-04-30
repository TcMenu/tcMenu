module com.thecoderscorner.tcmenu.menuEditorUI {
    requires java.prefs;
    requires java.desktop;
    requires java.sql;
    requires java.logging;

    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires java.net.http;

    requires com.fazecast.jSerialComm;
    requires com.thecoderscorner.tcmenu.javaapi;
    requires gson;

    // allow javafx components to see the editor UI packages that contain controllers etc.
    exports com.thecoderscorner.menu.editorui;
    exports com.thecoderscorner.menu.editorui.controller;
    exports com.thecoderscorner.menu.editorui.generator;
    exports com.thecoderscorner.menu.editorui.generator.ui;
    exports com.thecoderscorner.menu.editorui.util;
    exports com.thecoderscorner.menu.editorui.project;
    exports com.thecoderscorner.menu.editorui.generator.core;

    opens com.thecoderscorner.menu.editorui.project to gson;
    opens com.thecoderscorner.menu.editorui.generator to gson;
    opens com.thecoderscorner.menu.editorui.generator.core to gson;
}