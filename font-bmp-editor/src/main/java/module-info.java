module com.thecoderscorner.bmped {
    requires java.prefs;
    requires java.desktop;
    requires java.sql;
    requires java.logging;

    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.swing;
    requires java.net.http;

    opens com.thecoderscorner.bmped.controller;
    opens com.thecoderscorner.bmped;
    opens com.thecoderscorner.bmped.util;
    opens com.thecoderscorner.bmped.gfxui;
    opens com.thecoderscorner.bmped.gfxui.font;
    opens com.thecoderscorner.bmped.gfxui.imgedit;
    opens com.thecoderscorner.bmped.gfxui.pixmgr;

    requires com.google.gson;
    requires com.thecoderscorner.tcmenu.javaapi;
    requires com.thecoderscorner.embedcontrol.core;
}