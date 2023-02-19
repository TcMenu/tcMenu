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

    requires com.thecoderscorner.tcmenu.javaapi;
    requires com.google.gson;
    requires info.picocli;
    requires org.jfxtras.styles.jmetro;

    exports com.thecoderscorner.menu.editorui;
    exports com.thecoderscorner.menu.editorui.controller;
    exports com.thecoderscorner.menu.editorui.storage;
    exports com.thecoderscorner.menu.editorui.project;
    exports com.thecoderscorner.menu.editorui.util;
    exports com.thecoderscorner.menu.editorui.uimodel;
    exports com.thecoderscorner.menu.editorui.generator;
    exports com.thecoderscorner.menu.editorui.generator.ui;
    exports com.thecoderscorner.menu.editorui.generator.font;
    exports com.thecoderscorner.menu.editorui.generator.core;
    exports com.thecoderscorner.menu.editorui.generator.plugin;
    exports com.thecoderscorner.menu.editorui.generator.validation;
    exports com.thecoderscorner.menu.editorui.generator.applicability;
    exports com.thecoderscorner.menu.editorui.generator.util;
    exports com.thecoderscorner.menu.editorui.generator.arduino;
    exports com.thecoderscorner.menu.editorui.generator.parameters;
    exports com.thecoderscorner.menu.editorui.generator.parameters.eeprom;
    exports com.thecoderscorner.menu.editorui.generator.parameters.auth;

    opens i18n to com.thecoderscorner.tcmenu.javaapi;
    opens com.thecoderscorner.menu.editorui.generator.parameters to com.google.gson;
    opens com.thecoderscorner.menu.editorui.generator.parameters.eeprom to com.google.gson;
    opens com.thecoderscorner.menu.editorui.generator.parameters.auth to com.google.gson;
    opens com.thecoderscorner.menu.editorui.generator to com.google.gson;
    opens com.thecoderscorner.menu.editorui.generator.core to com.google.gson;
    opens com.thecoderscorner.menu.editorui.controller to com.google.gson;
    opens com.thecoderscorner.menu.editorui.cli to info.picocli;
    opens com.thecoderscorner.menu.editorui.project to com.google.gson;
    opens com.thecoderscorner.menu.editorui.storage to com.google.gson;
}