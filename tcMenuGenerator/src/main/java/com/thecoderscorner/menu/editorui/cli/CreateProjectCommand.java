package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.NoLocaleEnabledLocalHandler;
import com.thecoderscorner.menu.persist.SafeBundleLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.editorui.generator.validation.StringPropertyValidationRules.VAR_PATTERN;
import static com.thecoderscorner.menu.editorui.project.CurrentEditorProject.MENU_PROJECT_LANG_FILENAME;
import static picocli.CommandLine.*;

@Command(name = "create-project")
public class CreateProjectCommand implements Callable<Integer> {
    @Option(names = {"-d", "--directory"}, description = "optional directory name (defaults to current)")
    private File projectLocation;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(names = {"-p", "--platform"}, description = "A platformID returned by the list-platforms command", required = true)
    private String platform = "ARDUINO_AVR";

    @Option(names = {"-m", "--cpp"}, description = "use a cpp file for main")
    private boolean cppMain;

    @Parameters(paramLabel = "project name")
    private String[] newProject;

    @Option(names = {"-v", "--verbose"}, description = "verbose logging")
    private boolean verbose;

    @Option(names = {"-n", "namespace"}, description = "the java package name or C++ namespace")
    private String namespace;

    @Option(names = {"-s", "--src"}, description = "Override save to src default")
    private boolean[] saveToSrcArr;

    @Option(names = {"-e", "--sized-rom"}, description = "Override sized ROM option")
    private boolean[] useSizedRomArr;

    @Option(names = {"-r", "--recursive"}, description = "Override recursive naming default")
    private boolean[] recursiveNamingArr;

    @Option(names = {"-i", "--i18n"}, description = "enable i18n support for the following locales, comma separated")
    private String i18nLocales;

    @Override
    public Integer call() throws Exception {
        var platforms = new PluginEmbeddedPlatformsImpl();
        var prefsStore = new PrefsConfigurationStorage();
        DefaultXmlPluginLoader loader = new DefaultXmlPluginLoader(platforms, prefsStore, true);
        loader.loadPlugins();
        var versionDetector = MenuEditorApp.createLibraryVersionDetector();
        platforms.setInstallerConfiguration(new ArduinoLibraryInstaller(versionDetector, loader, prefsStore), prefsStore);

        if(projectLocation == null) projectLocation = new File(System.getProperty("user.dir"));

        if(!projectLocation.exists()) {
            System.out.println("directory to create does not exist: " + projectLocation);
            return -1;
        }

        if(newProject == null  || newProject.length != 1 || !VAR_PATTERN.matcher(newProject[0]).matches()) {
            System.out.println("Please enter a valid new project name without spaces");
            return -1;
        }

        try {
            var embeddedPlatform = platforms.getEmbeddedPlatformFromId(platform);
            createNewProject(Paths.get(projectLocation.toString()), newProject[0], cppMain, embeddedPlatform, platforms, System.out::println, namespace);

            if(!StringHelper.isStringEmptyOrNull(i18nLocales)) {
                var localeList = Arrays.stream(i18nLocales.split("\\s*,\\s*")).map(Locale::of).toList();
                enableI18nSupport(projectLocation.toPath().resolve(newProject[0]), localeList, System.out::println);
            }
            return 0;
        }
        catch(Exception ex) {
            if(verbose)
                ex.printStackTrace();
            else
                System.out.format("Error while creating project %s, %s", ex.getClass().getSimpleName(), ex.getMessage());
            return -1;
        }
    }

    /**
     * Used by both the CLI and the UI to create a new project, prepare the default main and project file with the
     * right settings.
     * @param location location for creation
     * @param newProject new project name (will create dir)
     * @param cppMain if a c++ main file is to be used
     * @param platform the platform as a SupportedPlatform.
     * @throws IOException when the directory and files are not properly created
     */
    public void createNewProject(Path location, String newProject, boolean cppMain, EmbeddedPlatform platform,
                                 EmbeddedPlatforms platforms, Consumer<String> logger, String packageOrNamespace) throws IOException {
        var configurationStorage = new PrefsConfigurationStorage();
        logger.accept(String.format("Creating directory %s in %s", newProject, location));
        var dir = Paths.get(location.toString(), newProject);
        Files.createDirectory(dir);

        var recursiveNaming = recursiveNamingArr != null && recursiveNamingArr.length > 0 ? recursiveNamingArr[0] : configurationStorage.isDefaultRecursiveNamingOn();
        var saveToSrc = saveToSrcArr != null && saveToSrcArr.length > 0 ? saveToSrcArr[0] : configurationStorage.isDefaultSaveToSrcOn();
        var sizedRom = useSizedRomArr != null && useSizedRomArr.length > 0 ? useSizedRomArr[0] : configurationStorage.isDefaultSizedEEPROMStorage();

        if(saveToSrc) {
            Files.createDirectory(dir.resolve("src"));
        }

        if(platforms.isJava(platform) && StringHelper.isStringEmptyOrNull(packageOrNamespace)) {
            throw new IllegalArgumentException("For Java platforms you must provide a package name");
        }

        var persistor = new FileBasedProjectPersistor();
        var  tree = new MenuTree();
        var generator = new CodeGeneratorOptionsBuilder()
                .withPlatform(platform.getBoardId())
                .withAppName(newProject).withNewId(UUID.randomUUID())
                .withCppMain(cppMain).withSaveToSrc(saveToSrc).withRecursiveNaming(recursiveNaming)
                .withUseSizedEEPROMStorage(sizedRom)
                .withPackageNamespace(packageOrNamespace)
                .codeOptions();

        var projectEmf = dir.resolve(newProject + ".emf");
        persistor.save(projectEmf.toString(), "Project description", tree, generator, new NoLocaleEnabledLocalHandler());

        if(platforms.isJava(platform)) {
            logger.accept("Java project, all files will be created on first generate");
        } else{
            var codeGen = platforms.getCodeGeneratorFor(platform, generator);
            BiConsumer<System.Logger.Level, String> l = (level, s) -> logger.accept(level + ": " + s);
            codeGen.setLoggerFunction(l);
            codeGen.getSketchFileAdjuster().createFileIfNeeded(l, dir, generator);
        }

        logger.accept("Project created!");
    }

    public void enableI18nSupport(Path location, List<Locale> locales, Consumer<String> logger) throws IOException {
        var i18n = location.resolve("i18n");
        Files.createDirectory(i18n);
        SafeBundleLoader safeBundleLoader = new SafeBundleLoader(i18n, MENU_PROJECT_LANG_FILENAME);
        safeBundleLoader.saveChangesKeepingFormatting(MenuEditorApp.EMPTY_LOCALE, Map.of());
        logger.accept("Created directory " + i18n + " and saved default file");
        for(var locale : locales) {
            safeBundleLoader.saveChangesKeepingFormatting(locale, Map.of());
            logger.accept("Created locale file " + locale);
        }
    }
}
