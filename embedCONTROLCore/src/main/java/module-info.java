module com.thecoderscorner.embedcontrol.core {
    requires java.logging;
    requires com.google.gson;
    requires com.fazecast.jSerialComm;
    requires com.thecoderscorner.tcmenu.javaapi;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires java.prefs;
    requires java.desktop;
    requires javafx.fxml;

    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.thecoderscorner.embedcontrol.core.service;

    exports com.thecoderscorner.embedcontrol.core.controlmgr;
    exports com.thecoderscorner.embedcontrol.core.serial;
    exports com.thecoderscorner.embedcontrol.core.service;
    exports com.thecoderscorner.embedcontrol.core.util;
    exports com.thecoderscorner.embedcontrol.core.creators;
    exports com.thecoderscorner.embedcontrol.core.rs232;
    exports com.thecoderscorner.embedcontrol.core.controlmgr.color;
    exports com.thecoderscorner.embedcontrol.jfx.controlmgr;
    exports com.thecoderscorner.embedcontrol.customization;
    exports com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;
    exports com.thecoderscorner.embedcontrol.customization.formbuilder;
    exports com.thecoderscorner.embedcontrol.customization.customdraw;
}
