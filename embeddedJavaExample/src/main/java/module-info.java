module com.thecoderscorner.embedcontrol.localjavaexample {
    requires java.logging;
    requires java.prefs;
    requires java.desktop;
    requires spring.beans;
    requires com.google.gson;
    requires com.fazecast.jSerialComm;
    requires com.thecoderscorner.tcmenu.javaapi;
    requires com.thecoderscorner.embedcontrol.core;
    requires spring.core;
    requires spring.context;
    opens com.thecoderscorner.menuexample.tcmenu;
    exports com.thecoderscorner.menuexample.tcmenu;
    exports com.thecoderscorner.menuexample.tcmenu.plugins;

    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
}
