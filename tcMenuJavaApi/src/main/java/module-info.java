module com.thecoderscorner.tcmenu.javaapi {
    requires com.google.gson;
    requires java.xml;
    exports com.thecoderscorner.menu.domain.state;
    exports com.thecoderscorner.menu.domain.util;
    exports com.thecoderscorner.menu.domain;
    exports com.thecoderscorner.menu.remote;
    exports com.thecoderscorner.menu.remote.encryption;
    exports com.thecoderscorner.menu.remote.states;
    exports com.thecoderscorner.menu.remote.commands;
    exports com.thecoderscorner.menu.remote.protocol;
    exports com.thecoderscorner.menu.remote.socket;
    exports com.thecoderscorner.menu.persist;
    exports com.thecoderscorner.menu.auth;
    exports com.thecoderscorner.menu.mgr;
    exports com.thecoderscorner.menu.remote.mgrclient;

    opens com.thecoderscorner.menu.domain to com.google.gson;
    opens com.thecoderscorner.menu.persist to com.google.gson;
}