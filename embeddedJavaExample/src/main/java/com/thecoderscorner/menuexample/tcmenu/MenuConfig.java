package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.MenuAppVersion;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.auth.PropertiesAuthenticator;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.persist.MenuStateSerialiser;
import com.thecoderscorner.menu.persist.PropertiesMenuStateSerialiser;
import com.thecoderscorner.menu.persist.VersionInfo;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.mgrclient.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.nio.file.Path;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executors;
import com.thecoderscorner.menuexample.tcmenu.plugins.*;
import javafx.application.Application;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Spring creates an application context out of all these components, you can wire together your own objects in either
 * this same file, or you can import another file. See the spring configuration for more details. You're safe to edit
 * this file as the designer only appends new entries
 */
@Configuration
@PropertySource("classpath:application.properties")
public class MenuConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public MenuStateSerialiser menuStateSerialiser(EmbeddedJavaDemoMenu menuDef, @Value("${file.menu.storage}") String filePath) {
        return new PropertiesMenuStateSerialiser(menuDef.getMenuTree(), Path.of(filePath).resolve("menuStorage.properties"));
    }

    @Bean
    public ScreenLayoutPersistence menuLayoutPersistence(
            EmbeddedJavaDemoMenu menuDef,
            GlobalSettings settings,
            MenuManagerServer manager,
            @Value("${file.menu.storage}") String filePath,
            @Value("${default.font.size}") int fontSize) {
        var layout = new ScreenLayoutPersistence(menuDef.getMenuTree(), settings, manager.getServerUuid(), Path.of(filePath), fontSize);
        layout.loadApplicationData();
        return layout;
    }

    @Bean
    public JfxNavigationHeader navigationManager(ScreenLayoutPersistence layoutPersistence) {
        return new JfxNavigationHeader(layoutPersistence);
    }

    @Bean
    public EmbeddedJavaDemoMenu menuDef() {
        return new EmbeddedJavaDemoMenu();
    }

    @Bean
    public EmbeddedJavaDemoController menuController(EmbeddedJavaDemoMenu menuDef, JfxNavigationManager navigationMgr, ScheduledExecutorService executor, GlobalSettings settings, ScreenLayoutPersistence layoutPersistence) {
        return new EmbeddedJavaDemoController(menuDef, navigationMgr, executor, settings, layoutPersistence);
    }

    @Bean
    public ScheduledExecutorService executor(@Value("${threading.pool.size}") int poolSize) {
        return Executors.newScheduledThreadPool(poolSize);
    }

    @Bean
    public MenuManagerServer menuManagerServer(ScheduledExecutorService executor, EmbeddedJavaDemoMenu menuDef, @Value("${server.name}") String serverName, @Value("${server.uuid}") String serverUUID, MenuAuthenticator authenticator, Clock clock) {
        return new MenuManagerServer(executor, menuDef.getMenuTree(), serverName, UUID.fromString(serverUUID), authenticator, clock);
    }

    @Bean
    public MenuAuthenticator menuAuthenticator(@Value("${file.auth.storage}") String propsPath) {
        return new PropertiesAuthenticator(propsPath);
    }

    @Bean
    public SocketServerConnectionManager socketClient(MenuCommandProtocol protocol, ScheduledExecutorService executor, Clock clock) {
        return new SocketServerConnectionManager(protocol, executor, 3333, clock);
    }

    @Bean
    public MenuAppVersion versionInfo(@Value("${build.version}") String version, @Value("${build.timestamp}") String timestamp,
                                      @Value("${build.groupId}") String groupId, @Value("${build.artifactId}") String artifact) {
        return new MenuAppVersion(new VersionInfo(version), timestamp, groupId, artifact);
    }

    @Bean
    public GlobalSettings globalSettings() {
        var settings = new GlobalSettings(EmbeddedJavaDemoMenu.class);
        settings.load();
        return settings;
    }

    @Bean
    public ConfigurableProtocolConverter tagVal() {
        return new ConfigurableProtocolConverter(true);
    }

    @Bean
    public TcJettyWebServer webServer(ConfigurableProtocolConverter protocol, Clock clock) {
        return new TcJettyWebServer(protocol, clock, "./data/www", 8080, false);
    }

    // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
}
