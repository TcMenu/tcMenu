module com.thecoderscorner.embedcontrol.core {
    requires java.logging;
    requires com.google.gson;

    requires com.thecoderscorner.tcmenu.javaapi;
    requires java.prefs;
    requires java.desktop;

    exports com.thecoderscorner.embedcontrol.core.controlmgr;
    exports com.thecoderscorner.embedcontrol.core.serial;
    exports com.thecoderscorner.embedcontrol.core.service;
    exports com.thecoderscorner.embedcontrol.core.util;
    exports com.thecoderscorner.embedcontrol.core.creators;
    exports com.thecoderscorner.embedcontrol.core.controlmgr.color;
}
