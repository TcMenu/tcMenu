package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.BaseMenuConfig;
import com.thecoderscorner.embedcontrol.core.util.MenuAppVersion;
import com.thecoderscorner.embedcontrol.core.util.TcComponent;
import com.thecoderscorner.embedcontrol.customization.ApplicationThemeManager;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.auth.PropertiesAuthenticator;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesMenuStateSerialiser;
import com.thecoderscorner.menu.persist.VersionInfo;
import com.thecoderscorner.menu.remote.mgrclient.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menuexample.tcmenu.plugins.TcJettyWebServer;

import java.nio.file.Path;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This class creates an application context out of all these components, and you can request any components that are
 * put into the context using getBean(ClassName.class).
 */
public class MenuConfig extends BaseMenuConfig {
    public MenuConfig(String env) {
        super(null, env);
        var executorService = asBean(Executors.newScheduledThreadPool(propAsIntWithDefault("threading.pool.size", 4)));
        var menuDef = asBean(new EmbeddedJavaDemoMenu());

        asBean(new PropertiesMenuStateSerialiser(menuDef.getMenuTree(), Path.of(resolvedProperties.getProperty("file.menu.storage")).resolve("menuStorage.properties")));

        var settings = asBean(new GlobalSettings(new ApplicationThemeManager()));
        // load or adjust the settings as needed here. You could use the JDBC components with SQLite to load and store
        // these values just like embed control does. See TcPreferencesPersistence and TccDatabaseUtilities.
        settings.setDefaultFontSize(14);
        settings.setDefaultRecursiveRendering(false);

        asBean(new JfxNavigationHeader(executorService, settings));

        asBean(new MenuItemStore(settings, menuDef.getMenuTree(), "", 7, 2, settings.isDefaultRecursiveRendering()));

        var protocol =  asBean(new ConfigurableProtocolConverter(true));

        Clock clock = asBean(Clock.systemUTC());
        asBean(new TcJettyWebServer(protocol, clock, "./data/www", 8080, false));

        var authenticator = asBean(new PropertiesAuthenticator(mandatoryStringProp("file.auth.storage")));

        asBean(new MenuManagerServer(executorService, menuDef.getMenuTree(), mandatoryStringProp("server.name"), UUID.fromString(mandatoryStringProp("server.uuid")), authenticator, clock));

        asBean(new SocketServerConnectionManager(protocol, executorService, 3333, clock));

        asBean(createVersionInfo());

        scanForComponents();
    }

    @TcComponent
    LocaleMappingHandler localeHandler() {
        return LocaleMappingHandler.NOOP_IMPLEMENTATION;
    }

    void appCustomConfiguration() {
        asBean(new EmbeddedJavaDemoController(
                getBean(EmbeddedJavaDemoMenu.class),
                getBean(JfxNavigationManager.class),
                getBean(ScheduledExecutorService.class),
                getBean(GlobalSettings.class),
                getBean(MenuItemStore.class)
        ));

        // you can put your configurations here, example:
        // var myComponent = asBean(new MyOwnComponent("hello");
        // to later access it simple call getBean(MyOwnComponent.class) on the context.
    }

    public MenuAppVersion createVersionInfo() {
        return new MenuAppVersion(new VersionInfo(mandatoryStringProp("build.version")),
                mandatoryStringProp("build.timestamp"), mandatoryStringProp("build.groupId"),
                mandatoryStringProp("build.artifactId"));
    }

    // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
}
