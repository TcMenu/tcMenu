module com.thecoderscorner.tcmenu.tcMenuExampleUI {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;

    requires java.logging;
    requires com.fazecast.jSerialComm;

    requires com.thecoderscorner.tcmenu.javaapi;

    exports com.thecoderscorner.menu.examples.simpleui;
}