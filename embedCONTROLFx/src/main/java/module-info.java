module com.thecoderscorner.tcmenu.embedcontrolfx {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;

    requires java.logging;
    requires com.fazecast.jSerialComm;

    requires com.thecoderscorner.tcmenu.javaapi;
    requires java.prefs;
    requires java.desktop;
    requires org.jfxtras.styles.jmetro;
    requires com.thecoderscorner.embedcontrol.core;

    exports com.thecoderscorner.embedcontrol.jfx;
    exports com.thecoderscorner.embedcontrol.jfx.dialog;
    exports com.thecoderscorner.embedcontrol.jfx.panel;
}