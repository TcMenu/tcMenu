package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.persist.VersionInfo;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.project.MenuTreeWithCodeOptions;
import com.thecoderscorner.menu.editorui.project.ProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static picocli.CommandLine.Command;

@Command(name="generate")
public class CodeGeneratorCommand implements Callable<Integer> {
    private static final String DEFAULT_INPUT_PLUGIN = "89cd7f70-0457-4884-97c2-0db904ccb0ba";
    private static final String DEFAULT_DISPLAY_PLUGIN = "cdd0be35-f6ff-40ae-86fc-f9d04a6e8679";
    private static final String DEFAULT_REMOTE_PLUGIN = "2c101fec-1f7d-4ff3-8d2b-992ad41e7fcb";
    private static final String DEFAULT_THEME_PLUGIN = "b186c809-d9ef-4ca8-9d4b-e4780a041ccc";

    private static ProjectPersistor persistor = null;
    private static File loadedProjectFile = null;
    private static String projectDescription = "";

    @CommandLine.Option(names = {"-f", "--emf-file"}, description = "emf file name")
    private File projectFile;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "verbose logging")
    private boolean verbose;

    @Override
    public Integer call() {
        try {
            var project = projectFileOrNull(projectFile);

            System.out.format("Starting code generator for %s\n", project.getOptions().getApplicationName());

            var prefsStore = new PrefsConfigurationStorage();
            MenuEditorApp.createOrUpdateDirectoriesAsNeeded(prefsStore);
            prefsStore.setLastRunVersion(new VersionInfo(prefsStore.getVersion()));

            var platforms = new PluginEmbeddedPlatformsImpl();
            DefaultXmlPluginLoader loader = new DefaultXmlPluginLoader(platforms, prefsStore, true);
            loader.loadPlugins();
            platforms.setInstallerConfiguration(new ArduinoLibraryInstaller(new OfflineDetector(), loader, prefsStore), prefsStore);
            var embeddedPlatform = platforms.getEmbeddedPlatformFromId(project.getOptions().getEmbeddedPlatform());
            var codeGen = platforms.getCodeGeneratorFor(embeddedPlatform, project.getOptions());

            System.out.println("Preparing to execute generator");

            List<CodePluginItem> allPlugins = loader.getLoadedPlugins().stream()
                    .flatMap(pluginLib -> pluginLib.getPlugins().stream())
                    .collect(Collectors.toList());

            List<CodePluginItem> plugins = new ArrayList<>();

            plugins.add(getPluginOrDefault(allPlugins, project.getOptions().getLastInputUuid(), DEFAULT_INPUT_PLUGIN));
            plugins.add(getPluginOrDefault(allPlugins, project.getOptions().getLastDisplayUuid(), DEFAULT_DISPLAY_PLUGIN));
            for(var plugin : project.getOptions().getLastRemoteCapabilitiesUuids()) {
                plugins.add(getPluginOrDefault(allPlugins, plugin, DEFAULT_REMOTE_PLUGIN));
            }
            if (project.getOptions().getLastThemeUuid() != null) {
                plugins.add(getPluginOrDefault(allPlugins, project.getOptions().getLastThemeUuid(), DEFAULT_THEME_PLUGIN));
            }

            System.out.format("Executing code generator");

            var location = Paths.get(loadedProjectFile.getParent());
            codeGen.setLoggerFunction((level, s) -> {
                if(verbose) System.out.format("Gen: %s: %s\n", level, s);
            });
            codeGen.startConversion(location, plugins, project.getMenuTree(), Collections.emptyList(), project.getOptions());
            return 0;
        }
        catch (Exception ex) {
            System.out.println("Error during code generation " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            if(verbose) {
                ex.printStackTrace();
            }
            return -1;
        }
    }

    private CodePluginItem getPluginOrDefault(List<CodePluginItem> plugins, String lastPlugin, String defaultPlugin) {

        var selected = plugins.stream().filter(pl -> pl.getId().equals(lastPlugin)).findFirst();
        if(selected.isPresent()) {
            return selected.get();
        }
        else {
            var def = plugins.stream().filter(pl -> pl.getId().equals(defaultPlugin)).findFirst();
            if(def.isEmpty()) throw new IllegalStateException("Plugin load failure");
            return def.get();
        }
    }

    public static MenuTreeWithCodeOptions projectFileOrNull(File projectFile) throws IOException {
        projectFile = locateProjectFile(projectFile);

        loadedProjectFile = projectFile;

        // just in case we make a backup.
        Files.copy(Paths.get(projectFile.toString()), Paths.get(projectFile + ".last"), StandardCopyOption.REPLACE_EXISTING);

        if(persistor == null) persistor = new FileBasedProjectPersistor();

        var loadedProject = persistor.open(projectFile.getAbsolutePath());
        projectDescription = loadedProject.getDescription();
        return loadedProject;
    }

    public static File locateProjectFile(File projectFile) throws IOException {
        if(projectFile == null) {
            var path = Paths.get(System.getProperty("user.dir"));
            var maybeEmfPath = Files.find(path, 1, (filePath, attrs) -> filePath.toString().endsWith(".emf")).findFirst();
            if(maybeEmfPath.isEmpty()) throw new IOException("Could not find an emf file in directory " + path);
            projectFile = new File(maybeEmfPath.get().toString());
        }

        if(!projectFile.exists()) throw new IOException("Project file does not exist " + projectFile);
        return projectFile;
    }

    public static void persistProject(MenuTree tree, CodeGeneratorOptions opts) throws IOException {
        if(persistor != null && loadedProjectFile != null) {
            persistor.save(loadedProjectFile.toString(), projectDescription, tree, opts);
        }
    }

    /**
     * An unimplemented version of the library upgrade system, as we would never upgrade a plugin from this command
     * line facility, that would be done through the designer, or manually.
     */
    private static class OfflineDetector implements LibraryVersionDetector {
        @Override
        public void changeReleaseType(OnlineLibraryVersionDetector.ReleaseType releaseType) { }

        @Override
        public OnlineLibraryVersionDetector.ReleaseType getReleaseType() {
            return OnlineLibraryVersionDetector.ReleaseType.STABLE;
        }

        @Override
        public Map<String, VersionInfo> acquireVersions() {
            return Map.of();
        }

        @Override
        public boolean availableVersionsAreValid(boolean refresh) {
            return true;
        }
    }
}
