package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.CoreControlAppConfig;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Import(CoreControlAppConfig.class)
public class EmbedControlAppConfig {
    @Bean
    public RemoteUiEmbedControlContext controlContext(ScheduledExecutorService executor, JsonMenuItemSerializer serializer,
                                                      PlatformSerialFactory serialFactory, AppDataStore dataStore,
                                                      GlobalSettings settings) {
        return new RemoteUiEmbedControlContext(executor, serializer, serialFactory, dataStore, settings);
    }
}
