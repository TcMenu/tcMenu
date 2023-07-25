module com.thecoderscorner.tcmenu.embedcontrolfx {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires java.logging;
    requires com.fazecast.jSerialComm;
    requires com.google.gson;
    requires java.prefs;
    requires java.desktop;
    requires com.thecoderscorner.embedcontrol.core;
    requires com.thecoderscorner.tcmenu.javaapi;

    requires spring.context;
    requires spring.beans;

    opens com.thecoderscorner.embedcontrol.jfxapp;

    exports com.thecoderscorner.embedcontrol.jfxapp.dialog;
    exports com.thecoderscorner.embedcontrol.jfxapp.panel;
    exports com.thecoderscorner.embedcontrol.jfxapp;
}