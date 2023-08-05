/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.embedcontrol.core.service.CoreControlAppConfig;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.util.SimpleHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.System.Logger.Level.WARNING;

/**
 * The application configuration for the JavaFX version of the application
 */
@Configuration
@Import(CoreControlAppConfig.class)
public class MenuEditorConfig {
    @Bean
    JdbcTcMenuConfigurationStore configurationStorage(JdbcTemplate jdbcTemplate) {
        return new JdbcTcMenuConfigurationStore(jdbcTemplate);
    }

    @Bean
    LibraryVersionDetector libraryVersionDetector(JdbcTcMenuConfigurationStore config) {
        var httpClient = new SimpleHttpClient();

        var urlBase = "https://www.thecoderscorner.com";
        if(System.getProperty("localTccService") != null) {
            urlBase = System.getProperty("localTccService");
            System.getLogger("Main").log(WARNING, "Overriding the TCC service to " + urlBase);
        }

        return new OnlineLibraryVersionDetector(urlBase, httpClient, config);
    }

    @Bean
    ArduinoLibraryInstaller installer(LibraryVersionDetector libraryVersionDetector,
                                      ConfigurationStorage configurationStorage,
                                      DefaultXmlPluginLoader pluginLoader) {
        return new ArduinoLibraryInstaller(libraryVersionDetector, pluginLoader, configurationStorage);
    }

    @Bean
    DefaultXmlPluginLoader pluginLoader(Path tcMenuHome,
                                        ConfigurationStorage configurationStorage,
                                        EmbeddedPlatforms platforms) throws IOException {
        return new DefaultXmlPluginLoader(tcMenuHome, platforms, configurationStorage, true);
    }

    @Bean
    EmbeddedPlatforms embeddedPlatforms() {
         return new PluginEmbeddedPlatformsImpl();
    }

    @Bean
    FileBasedProjectPersistor projectPersistor(EmbeddedPlatforms platforms) {
        return new FileBasedProjectPersistor(platforms);
    }

    @Bean
    CurrentEditorProject editorProject(FileBasedProjectPersistor projectPersistor,
                                       CurrentProjectEditorUI editorUI,
                                       ConfigurationStorage configStore) {
        return new CurrentEditorProject(editorUI, projectPersistor, configStore);
    }

    @Bean
    CurrentProjectEditorUI editorUI(DefaultXmlPluginLoader loader, EmbeddedPlatforms platforms,
                                    ArduinoLibraryInstaller installer, ConfigurationStorage storage,
                                    LibraryVersionDetector versionDetector, CodeGeneratorSupplier codeGeneratorSupplier,
                                    Path tcMenuHome) {
        return new CurrentProjectEditorUIImpl(loader, platforms, installer, storage, versionDetector, codeGeneratorSupplier, tcMenuHome.toString());
    }

    @Bean
    CodeGeneratorSupplier codeGeneratorSupplier(ArduinoLibraryInstaller installer, ConfigurationStorage storage) {
        return new CodeGeneratorSupplier(storage, installer);
    }
}
