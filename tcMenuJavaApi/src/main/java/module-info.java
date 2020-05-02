module com.thecoderscorner.tcmenu.javaapi {
    exports com.thecoderscorner.menu.domain.state;
    exports com.thecoderscorner.menu.domain.util;
    exports com.thecoderscorner.menu.domain;
    exports com.thecoderscorner.menu.remote;
    exports com.thecoderscorner.menu.remote.states;
    exports com.thecoderscorner.menu.remote.commands;
    exports com.thecoderscorner.menu.remote.protocol;
    exports com.thecoderscorner.menu.remote.rs232;
    exports com.thecoderscorner.menu.remote.socket;

    requires com.fazecast.jSerialComm;

    opens com.thecoderscorner.menu.domain to gson;
}