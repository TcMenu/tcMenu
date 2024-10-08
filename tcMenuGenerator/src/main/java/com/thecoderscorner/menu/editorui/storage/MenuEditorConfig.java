/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.embedcontrol.core.service.CoreControlAppConfig;
import com.thecoderscorner.menu.editorui.embed.RemoteUiEmbedControlContext;
import com.thecoderscorner.menu.editorui.generator.AppVersionDetector;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.GitHubAppVersionChecker;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.project.TccProjectWatcherImpl;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.util.SimpleHttpClient;

/**
 * The application configuration for the JavaFX version of the application
 */
public class MenuEditorConfig extends CoreControlAppConfig {
    private final JdbcTcMenuConfigurationStore configStore;
    private final GitHubAppVersionChecker libraryVersionDetector;
    private final ArduinoLibraryInstaller installer;
    private final DefaultXmlPluginLoader pluginLoader;
    private final CodeGeneratorSupplier codeGenSupplier;
    private final EmbeddedPlatforms platforms;
    private final RemoteUiEmbedControlContext remoteContext;

    public MenuEditorConfig() throws Exception {
        super();
        configStore = new JdbcTcMenuConfigurationStore(databaseUtils, getThemeManager());
        remoteContext = new RemoteUiEmbedControlContext(executor, serializer, serialFactory, ecDataStore, globalSettings);

        var httpClient = new SimpleHttpClient();
        libraryVersionDetector = new GitHubAppVersionChecker(GitHubAppVersionChecker.GITHUB_VERSIONING_URL, httpClient);
        platforms = new PluginEmbeddedPlatformsImpl();
        pluginLoader = new DefaultXmlPluginLoader(platforms, configStore, true);
        installer = new ArduinoLibraryInstaller(libraryVersionDetector, pluginLoader, configStore);
        codeGenSupplier = new CodeGeneratorSupplier(configStore, installer);
    }
    public JdbcTcMenuConfigurationStore getConfigStore() {
        return configStore;
    }

    public AppVersionDetector getLibraryVersionDetector() {
        return libraryVersionDetector;
    }

    public RemoteUiEmbedControlContext getRemoteContext() {
        return remoteContext;
    }

    public ArduinoLibraryInstaller getInstaller() {
        return installer;
    }

    public DefaultXmlPluginLoader getPluginLoader() {
        return pluginLoader;
    }

    public FileBasedProjectPersistor createProjectPersistor() {
        return new FileBasedProjectPersistor(platforms);
    }

    public CurrentEditorProject newProject() {
        var editorUI = new CurrentProjectEditorUIImpl(pluginLoader, platforms, installer, configStore, libraryVersionDetector,
                codeGenSupplier, databaseUtils, globalSettings, tcMenuHome.toString());
        FileBasedProjectPersistor persistor = new FileBasedProjectPersistor(platforms);
        return new CurrentEditorProject(editorUI, persistor, configStore, executor, new TccProjectWatcherImpl());
    }

    public CodeGeneratorSupplier getCodeGeneratorSupplier() {
        return codeGenSupplier;
    }

    public EmbeddedPlatforms getPlatforms() {
        return platforms;
    }
}
