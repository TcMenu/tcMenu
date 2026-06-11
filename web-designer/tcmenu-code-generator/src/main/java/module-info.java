module com.thecoderscorner.tcmenu.generator {
    requires java.prefs;
    requires java.desktop;
    requires java.sql;
    requires java.logging;

    requires java.net.http;

    requires com.thecoderscorner.tcmenu.javaapi;
    requires com.google.gson;
    requires info.picocli;
    requires spring.context;
    requires static lombok;
    requires org.slf4j;
    requires spring.core;
    requires spring.beans;

    exports com.thecoderscorner.menu.editorui;
    exports com.thecoderscorner.menu.editorui.storage;
    exports com.thecoderscorner.menu.editorui.project;
    exports com.thecoderscorner.menu.editorui.util;
    exports com.thecoderscorner.menu.editorui.generator;
    exports com.thecoderscorner.menu.editorui.generator.core;
    exports com.thecoderscorner.menu.editorui.generator.plugin;
    exports com.thecoderscorner.menu.editorui.generator.validation;
    exports com.thecoderscorner.menu.editorui.generator.applicability;
    exports com.thecoderscorner.menu.editorui.generator.util;
    exports com.thecoderscorner.menu.editorui.generator.arduino;
    exports com.thecoderscorner.menu.editorui.generator.parameters;
    exports com.thecoderscorner.menu.editorui.generator.parameters.eeprom;
    exports com.thecoderscorner.menu.editorui.generator.parameters.auth;

    opens com.thecoderscorner.menu.editorui.generator.parameters to com.google.gson;
    opens com.thecoderscorner.menu.editorui.generator.parameters.eeprom to com.google.gson;
    opens com.thecoderscorner.menu.editorui.generator.parameters.auth to com.google.gson;
    opens com.thecoderscorner.menu.editorui.generator to com.google.gson;
    opens com.thecoderscorner.menu.editorui.generator.core to com.google.gson;
    opens com.thecoderscorner.menu.editorui.project to com.google.gson;
    opens com.thecoderscorner.menu.editorui.storage;
    opens com.thecoderscorner.menu.editorui.generator.arduino;
    opens com.thecoderscorner.menu.editorui.generator.mbed;
    opens com.thecoderscorner.menu.editorui.generator.plugin;
    exports com.thecoderscorner.menu.editorui.generator.logger;
    opens com.thecoderscorner.menu.editorui.generator.logger to com.google.gson;
    exports com.thecoderscorner.menu.editorui.config;
    opens com.thecoderscorner.menu.editorui.config;
}