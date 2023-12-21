package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.MenuAppVersion;
import com.thecoderscorner.embedcontrol.customization.ApplicationThemeManager;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.auth.PropertiesAuthenticator;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.persist.MenuStateSerialiser;
import com.thecoderscorner.menu.persist.PropertiesMenuStateSerialiser;
import com.thecoderscorner.menu.persist.VersionInfo;
import com.thecoderscorner.menu.remote.mgrclient.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menuexample.tcmenu.plugins.TcJettyWebServer;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Spring creates an application context out of all these components, you can wire together your own objects in either
 * this same file, or you can import another file. See the spring configuration for more details. You're safe to edit
 * this file as the designer only appends new entries
 */
public class MenuConfig {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final String environment;
    private final Properties resolvedProperties;
    private final ScheduledExecutorService executorService;
    private final EmbeddedJavaDemoMenu menuDef;
    private final PropertiesMenuStateSerialiser menuStateSerialiser;
    private final JfxNavigationHeader navManager;
    private final GlobalSettings settings;
    private final EmbeddedJavaDemoController menuController;
    private final ConfigurableProtocolConverter protocol;
    private final TcJettyWebServer webServer;
    private final MenuManagerServer menuManagerServer;
    private final PropertiesAuthenticator authenticator;
    private final SocketServerConnectionManager socketClient;
    private final MenuAppVersion versionInfo;
    private final MenuItemStore itemStore;


    public MenuConfig(String env) {
        //
        // TcMenu Designer will not touch this file unless you delete it, and then it will recreate it.
        // So you're safe to make changes to it.
        //
        environment = (env != null) ? env : System.getProperty("tc.env", "dev");
        logger.log(System.Logger.Level.INFO, "Starting app in environment " + environment);
        resolvedProperties = resolveProperties(environment);

        executorService = Executors.newScheduledThreadPool(propAsIntWithDefault("threading.pool.size", 4));
        menuDef = new EmbeddedJavaDemoMenu();

        menuStateSerialiser = new PropertiesMenuStateSerialiser(menuDef.getMenuTree(),
                Path.of(resolvedProperties.getProperty("file.menu.storage")).resolve("menuStorage.properties"));

        settings = new GlobalSettings(new ApplicationThemeManager());
        // load or adjust the settings as needed here. You could use the JDBC components with SQLite to load and store
        // these values just like embed control does. See TcPreferencesPersistence and TccDatabaseUtilities.
        settings.setDefaultFontSize(14);
        settings.setDefaultRecursiveRendering(false);

        navManager = new JfxNavigationHeader(executorService, settings);

        itemStore = new MenuItemStore(settings, menuDef.getMenuTree(), "", 7, 2, settings.isDefaultRecursiveRendering());

        menuController = new EmbeddedJavaDemoController(menuDef, navManager, executorService, settings, itemStore);

        protocol =  new ConfigurableProtocolConverter(true);

        Clock clock = Clock.systemUTC();
        webServer = new TcJettyWebServer(protocol, clock, "./data/www", 8080, false);

        authenticator = new PropertiesAuthenticator(resolvedProperties.getProperty("file.auth.storage"));

        menuManagerServer = new MenuManagerServer(executorService, menuDef.getMenuTree(), resolvedProperties.getProperty("server.name"), UUID.fromString(resolvedProperties.getProperty("server.uuid")), authenticator, clock);

        socketClient = new SocketServerConnectionManager(protocol, executorService, 3333, clock);

        versionInfo = createVersionInfo();
    }

    public MenuAppVersion createVersionInfo() {
        return new MenuAppVersion(new VersionInfo(mandatoryStringProp("build.version")),
                mandatoryStringProp("build.timestamp"), mandatoryStringProp("build.groupId"),
                mandatoryStringProp("build.artifactId"));
    }

    private Properties resolveProperties(String environment) {
        Properties p = new Properties();
        try(var envProps = getClass().getResourceAsStream("/application_" + environment + ".properties");
            var globalProps = getClass().getResourceAsStream("/application.properties")) {
            if(globalProps != null) {
                logger.log(System.Logger.Level.INFO, "Reading global properties from " + globalProps);
                p.load(globalProps);
            }

            if(envProps != null) {
                logger.log(System.Logger.Level.INFO, "Reading env properties from " + envProps);
                p.load(envProps);
            }
            logger.log(System.Logger.Level.INFO, "App Properties read finished");
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed to read app property files", ex);
        }
        return p;
    }

    public MenuManagerServer getMenuManagerServer() {
        return menuManagerServer;
    }

    public TcJettyWebServer getWebServer() {
        return webServer;
    }

    public MenuStateSerialiser getMenuStateSerialiser() {
        return menuStateSerialiser;
    }

    public EmbeddedJavaDemoController getMenuController() {
        return menuController;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return executorService;
    }

    public MenuAppVersion getMenuAppVersion() {
        return versionInfo;
    }

    public MenuAuthenticator getMenuAuthenticator() {
        return authenticator;
    }

    public JfxNavigationHeader getNavigationHeader() {
        return navManager;
    }

    public MenuItemStore getItemStore() {
        return itemStore;
    }


    // to refactor out


    public String getEnvironment() {
        return environment;
    }

    public Properties getResolvedProperties() {
        return resolvedProperties;
    }

    int propAsIntWithDefault(String propName, int def) {
        if(resolvedProperties.containsKey(propName)) {
            return Integer.parseInt(resolvedProperties.getProperty(propName));
        }
        return def;
    }

    String mandatoryStringProp(String propName) {
        if(!resolvedProperties.containsKey(propName)) {
            throw new IllegalArgumentException("Missing property in configuration " + propName);
        }
        return resolvedProperties.getProperty(propName);
    }

    // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
}
