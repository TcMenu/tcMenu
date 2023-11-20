package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.embedcontrol.core.service.TcMenuFormPersistence;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.project.MenuTreeWithCodeOptions;
import com.thecoderscorner.menu.editorui.project.ProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.MenuEditorConfig;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.BackupManager;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import com.thecoderscorner.menu.persist.SafeBundleLoader;
import com.thecoderscorner.menu.persist.VersionInfo;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.project.CurrentEditorProject.MENU_PROJECT_LANG_FILENAME;
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
            MenuEditorApp.configureBundle(Locale.getDefault());
            var appContext = new MenuEditorConfig();
            ConfigurationStorage configStore = appContext.getConfigStore();
            var project = projectFileOrNull(projectFile, configStore);

            System.out.format("Starting code generator for %s\n", project.getOptions().getApplicationName());

            configStore.setLastRunVersion(new VersionInfo(configStore.getVersion()));

            DefaultXmlPluginLoader loader = appContext.getPluginLoader();
            loader.loadPlugins();
            var embeddedPlatform = project.getOptions().getEmbeddedPlatform();
            var generatorSupplier = appContext.getCodeGeneratorSupplier();
            var codeGen = generatorSupplier.getCodeGeneratorFor(embeddedPlatform, project.getOptions());

            List<CodePluginItem> allPlugins = loader.getLoadedPlugins().stream()
                    .flatMap(pluginLib -> pluginLib.getPlugins().stream())
                    .collect(Collectors.toList());

            List<CodePluginItem> plugins = new ArrayList<>();

            Map<String, CreatorProperty> propMap = project.getOptions().getLastProperties().stream()
                    .collect(Collectors.toMap(CreatorProperty::getName, v -> v));

            var displayPlugin = getPluginOrDefault(allPlugins, project.getOptions().getLastDisplayUuid(), DEFAULT_DISPLAY_PLUGIN, propMap);
            plugins.add(displayPlugin);
            plugins.add(getPluginOrDefault(allPlugins, project.getOptions().getLastInputUuid(), DEFAULT_INPUT_PLUGIN, propMap));

            for(var plugin : project.getOptions().getLastRemoteCapabilitiesUuids()) {
                plugins.add(getPluginOrDefault(allPlugins, plugin, DEFAULT_REMOTE_PLUGIN, propMap));
            }

            if (displayPlugin.isThemeNeeded() && project.getOptions().getLastThemeUuid() != null) {
                plugins.add(getPluginOrDefault(allPlugins, project.getOptions().getLastThemeUuid(), DEFAULT_THEME_PLUGIN, propMap));
            }

            var location = Paths.get(loadedProjectFile.getParent());
            codeGen.setLoggerFunction((level, s) -> {
                if(verbose) System.out.format("Gen: %s: %s\n", level, s);
            });
            var enabledFormObjects = project.getOptions().getListOfEmbeddedForms().stream()
                    .map(form -> getFirstByNameAndUuid(project, form)).toList();

            codeGen.startConversion(location, plugins, project.getMenuTree(), Collections.emptyList(), project.getOptions(),
                    getLocaleHandler(location), enabledFormObjects);
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

    private TcMenuFormPersistence getFirstByNameAndUuid(MenuTreeWithCodeOptions project, String formName) {
        var uuid = project.getOptions().getApplicationUUID().toString();
        try {
            return MenuEditorApp.getContext().getAppContext().getEcDataStore().getUtilities()
                    .queryRecords(TcMenuFormPersistence.class, "FORM_UUID=? and FORM_NAME=?", uuid, formName)
                    .stream().findFirst().orElseThrow();
        } catch (DataException e) {
            throw new RuntimeException(e);
        }
    }


    private CodePluginItem getPluginOrDefault(List<CodePluginItem> plugins, String lastPlugin, String defaultPlugin, Map<String, CreatorProperty> propertiesMap) {

        var selected = plugins.stream().filter(pl -> pl.getId().equals(lastPlugin)).findFirst();
        CodePluginItem toReturn;
        if(selected.isPresent()) {
             toReturn = selected.get();
        }
        else {
            var def = plugins.stream().filter(pl -> pl.getId().equals(defaultPlugin)).findFirst();
            if(def.isEmpty()) throw new IllegalStateException("Plugin load failure");
            toReturn = def.get();
        }

        // replace all properties in plugin with latest values
        for(var prop : toReturn.getProperties()) {
            if(propertiesMap.containsKey(prop.getName())) {
                prop.setLatestValue(propertiesMap.get(prop.getName()).getLatestValue());
            }
        }
        return toReturn;
    }

    public static MenuTreeWithCodeOptions projectFileOrNull(File projectFile, ConfigurationStorage config) throws IOException {
        projectFile = locateProjectFile(projectFile, false);

        loadedProjectFile = projectFile;

        // just in case we make a backup.
        var backup = new BackupManager(config);
        backup.backupFile(projectFile.toPath().getParent(), projectFile.toPath());

        if(persistor == null) persistor = new FileBasedProjectPersistor(new PluginEmbeddedPlatformsImpl());

        var loadedProject = persistor.open(projectFile.getAbsolutePath());
        projectDescription = loadedProject.getDescription();
        return loadedProject;
    }

    public static LocaleMappingHandler getLocaleHandler(Path basePath) {
        Path i18nDir = basePath.resolve(CurrentEditorProject.TCMENU_I18N_SRC_DIR);
        if(Files.exists(i18nDir)) {
            var rootProperties = i18nDir.resolve(MENU_PROJECT_LANG_FILENAME + ".properties");
            if(Files.exists(rootProperties))
                return new PropertiesLocaleEnabledHandler(new SafeBundleLoader(i18nDir, MENU_PROJECT_LANG_FILENAME));
        }

        return LocaleMappingHandler.NOOP_IMPLEMENTATION;
    }

    public static File locateProjectFile(File projectFile, boolean createIfNeeded) throws IOException {
        if(projectFile == null) {
            var path = Paths.get(System.getProperty("user.dir"));
            var maybeEmfPath = Files.find(path, 1, (filePath, attrs) -> filePath.toString().endsWith(".emf")).findFirst();
            if(maybeEmfPath.isEmpty()) {
                if(createIfNeeded) {
                    try(var emptyEmfFile = CodeGeneratorCommand.class.getResourceAsStream("/packaged-plugins/empty-project.emf")) {
                        var data = emptyEmfFile.readAllBytes();
                        Files.write(path.resolve(path.getFileName() + ".emf"), data);
                    }
                    catch(Exception ex) {
                        throw new IOException("Could not create empty EMF file", ex);
                    }
                } else {
                    throw new IOException("Could not find an emf file in directory (create option was false)" + path);
                }

            }

            projectFile = new File(maybeEmfPath.get().toString());
        }

        if(!projectFile.exists()) {
            throw new IOException("Project file does not exist " + projectFile);
        }

        return projectFile;
    }

    public static void persistProject(MenuTree tree, CodeGeneratorOptions opts) throws IOException {
        if(persistor != null && loadedProjectFile != null) {
            BackupManager backupManager = new BackupManager(new PrefsConfigurationStorage());
            backupManager.backupFile(loadedProjectFile.toPath().getParent(), loadedProjectFile.toPath());
            persistor.save(loadedProjectFile.toString(), projectDescription, tree, opts, LocaleMappingHandler.NOOP_IMPLEMENTATION);
        }
    }

}
