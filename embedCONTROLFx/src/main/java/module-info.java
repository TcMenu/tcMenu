module com.thecoderscorner.tcmenu.exampleui {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;

    requires java.logging;
    requires com.fazecast.jSerialComm;

    requires com.thecoderscorner.tcmenu.javaapi;
    requires java.prefs;
    requires java.desktop;

    exports com.thecoderscorner.menu.controller;
}