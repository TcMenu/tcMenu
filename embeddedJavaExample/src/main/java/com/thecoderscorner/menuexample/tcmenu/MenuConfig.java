package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.menu.auth.*;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.persist.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.*;
import java.nio.file.Path;
import com.thecoderscorner.menuexample.tcmenu.plugins.*;
import javafx.application.Application;
import com.thecoderscorner.menu.remote.protocol.*;
import com.thecoderscorner.menu.remote.mgrclient.*;
import java.time.*;

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
        return new PropertiesMenuStateSerialiser(menuDef.getMenuTree(), Path.of(filePath));
    }

    @Bean
    public EmbeddedJavaDemoMenu menuDef() {
        return new EmbeddedJavaDemoMenu();
    }

    @Bean
    public EmbeddedJavaDemoController menuController(EmbeddedJavaDemoMenu menuDef) {
        return new EmbeddedJavaDemoController(menuDef);
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
    public TagValMenuCommandProtocol tagVal() {
        return new TagValMenuCommandProtocol();
    }

    @Bean
    public SocketServerConnectionManager socketClient(TagValMenuCommandProtocol protocol, ScheduledExecutorService executor, Clock clock) {
        return new SocketServerConnectionManager(protocol, executor, 3333, clock);
    }

    // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
}
