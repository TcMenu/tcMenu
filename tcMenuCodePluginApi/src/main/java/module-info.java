module com.thecoderscorner.tcmenu.CodePluginApi {
    requires com.thecoderscorner.tcmenu.javaapi;
    requires javafx.base;

    exports com.thecoderscorner.menu.pluginapi;
    exports com.thecoderscorner.menu.pluginapi.model;
    exports com.thecoderscorner.menu.pluginapi.model.parameter;
}