module com.thecoderscorner.menuexample.embeddedjavademo {
    requires java.logging;
    requires java.prefs;
    requires java.desktop;
    requires com.google.gson;
    requires com.fazecast.jSerialComm;
    requires com.thecoderscorner.tcmenu.javaapi;
    requires com.thecoderscorner.embedcontrol.core;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    exports com.thecoderscorner.menuexample.tcmenu.plugins;
    opens com.thecoderscorner.menuexample.tcmenu;

    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires jetty.websocket.api;
    requires org.eclipse.jetty.websocket.javax.server;
}
