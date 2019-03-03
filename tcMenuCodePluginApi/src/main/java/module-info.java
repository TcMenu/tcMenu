module com.thecoderscorner.tcmenu.plugin.api {
    requires com.thecoderscorner.tcmenu.javaapi;
    requires javafx.base;

    exports com.thecoderscorner.menu.pluginapi;
    exports com.thecoderscorner.menu.pluginapi.validation;
    exports com.thecoderscorner.menu.pluginapi.model;
    exports com.thecoderscorner.menu.pluginapi.model.parameter;
}