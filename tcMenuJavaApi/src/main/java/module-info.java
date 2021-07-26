module com.thecoderscorner.tcmenu.javaapi {
    requires com.google.gson;
    exports com.thecoderscorner.menu.domain.state;
    exports com.thecoderscorner.menu.domain.util;
    exports com.thecoderscorner.menu.domain;
    exports com.thecoderscorner.menu.remote;
    exports com.thecoderscorner.menu.remote.states;
    exports com.thecoderscorner.menu.remote.commands;
    exports com.thecoderscorner.menu.remote.protocol;
    exports com.thecoderscorner.menu.remote.socket;
    exports com.thecoderscorner.menu.persist;

    opens com.thecoderscorner.menu.domain to com.google.gson;
    opens com.thecoderscorner.menu.persist to com.google.gson;
}