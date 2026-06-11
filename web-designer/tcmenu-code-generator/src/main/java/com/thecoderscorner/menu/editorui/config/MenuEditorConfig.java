/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.config;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.SimpleHttpClient;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The application configuration for the JavaFX version of the application
 */
@Slf4j
@Configuration
public class MenuEditorConfig {

    @Bean
    public SimpleHttpClient simpleHttpClient() {
        return new SimpleHttpClient();
    }

    @Bean
    public EmbeddedPlatforms platforms() {
        return new PluginEmbeddedPlatformsImpl();
    }

    @Bean
    public CodeGeneratorSupplier codeGeneratorSupplier(ConfigurationStorage configStore) {
        return new CodeGeneratorSupplier(configStore);
    }

    @Bean
    public ConfigurationStorage getConfigStore() {
        return new PrefsConfigurationStorage();
    }

    @Bean
    public ScheduledExecutorService executorService() {
        return Executors.newScheduledThreadPool(4);
    }

    @Bean
    public FileBasedProjectPersistor getPersistor(EmbeddedPlatforms platforms) {
        return new FileBasedProjectPersistor(platforms);
    }

    @Bean
    public JsonMenuItemSerializer getSerializer() {
        return new JsonMenuItemSerializer();
    }

    @Bean()
    @Scope("prototype")
    public CurrentEditorProject newProject(FileBasedProjectPersistor persistor,
                                           ConfigurationStorage configStore) {
        return new CurrentEditorProject(persistor, configStore);
    }

    @Bean
    public CodeGeneratorSupplier getCodeGeneratorSupplier(ConfigurationStorage configurationStorage) {
        return new CodeGeneratorSupplier(configurationStorage);
    }

    @Bean
    public DefaultXmlPluginLoader pluginLoader(EmbeddedPlatforms platforms, ConfigurationStorage configurationStorage,
                                               @Value("${core.plugin.dir}") String pathOnHost) {
        var plugins = new DefaultXmlPluginLoader(platforms, configurationStorage, pathOnHost);
        plugins.loadPlugins();
        log.info("Loaded plugins: {}", String.join(", ", plugins.getLoadedTopLevelPluginNames()));
        return plugins;
    }
}
