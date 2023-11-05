package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.ProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.MenuEditorConfig;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
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

import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.persistProject;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.ALL_TO_CURRENT;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.ALL_TO_SRC;
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

    void projectTestAccess(Path location, String projectName, EmbeddedPlatform platform, boolean saveToSrc, boolean recursiveNaming, boolean cppMain, String locales) {
        this.platform = platform.getBoardId();
        this.cppMain = cppMain;
        this.saveToSrcArr = new boolean[] { saveToSrc};
        this.recursiveNamingArr = new boolean[] { recursiveNaming };
        this.useSizedRomArr = new boolean[] { true };
        this.i18nLocales = locales;
        this.newProject = new String[] { projectName};
        this.projectLocation = location.toFile();
    }

    @Override
    public Integer call() throws Exception {
        MenuEditorApp.configureBundle(Locale.getDefault());

        var appContext = new MenuEditorConfig();
        DefaultXmlPluginLoader loader = appContext.getPluginLoader();
        loader.loadPlugins();
        var platforms = appContext.getPlatforms();

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
            createNewProject(Paths.get(projectLocation.toString()), newProject[0], cppMain, embeddedPlatform,
                    System.out::println, namespace, appContext.getCodeGeneratorSupplier(),
                    appContext.getProjectPersistor());

            if(!StringHelper.isStringEmptyOrNull(i18nLocales)) {
                var localeList = Arrays.stream(i18nLocales.split("\\s*,\\s*")).map(Locale::of).toList();
                enableI18nSupport(projectLocation.toPath().resolve(newProject[0]), localeList, System.out::println, Optional.empty());
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
                                 Consumer<String> logger, String packageOrNamespace,
                                 CodeGeneratorSupplier codeGeneratorSupplier,
                                 ProjectPersistor projectPersistor) throws IOException {
        var configurationStorage = new PrefsConfigurationStorage();
        logger.accept(String.format("Creating directory %s in %s", newProject, location));
        var dir = Paths.get(location.toString(), newProject);
        Files.createDirectories(dir);

        var recursiveNaming = recursiveNamingArr != null && recursiveNamingArr.length > 0 ? recursiveNamingArr[0] : configurationStorage.isDefaultRecursiveNamingOn();
        var saveToSrc = saveToSrcArr != null && saveToSrcArr.length > 0 ? saveToSrcArr[0] : configurationStorage.isDefaultSaveToSrcOn();
        var sizedRom = useSizedRomArr != null && useSizedRomArr.length > 0 ? useSizedRomArr[0] : configurationStorage.isDefaultSizedEEPROMStorage();

        if(saveToSrc) {
            Files.createDirectory(dir.resolve("src"));
        }

        boolean isJava = PluginEmbeddedPlatformsImpl.javaPlatforms.contains(platform);
        if(isJava && StringHelper.isStringEmptyOrNull(packageOrNamespace)) {
            throw new IllegalArgumentException("For Java platforms you must provide a package name");
        }

        var  tree = new MenuTree();
        var generator = new CodeGeneratorOptionsBuilder()
                .withPlatform(platform)
                .withAppName(newProject).withNewId(UUID.randomUUID())
                .withCppMain(cppMain)
                .withSaveLocation(saveToSrc ? ALL_TO_SRC : ALL_TO_CURRENT)
                .withRecursiveNaming(recursiveNaming)
                .withUseSizedEEPROMStorage(sizedRom)
                .withPackageNamespace(packageOrNamespace)
                .codeOptions();

        var projectEmf = dir.resolve(newProject + ".emf");
        projectPersistor.save(projectEmf.toString(), "Project description", tree, generator, LocaleMappingHandler.NOOP_IMPLEMENTATION);

        if(isJava) {
            logger.accept("Java project, all files will be created on first generate");
        } else{
            var codeGen = codeGeneratorSupplier.getCodeGeneratorFor(platform, generator);
            BiConsumer<System.Logger.Level, String> l = (level, s) -> logger.accept(level + ": " + s);
            codeGen.setLoggerFunction(l);
            codeGen.getSketchFileAdjuster().createFileIfNeeded(l, dir, generator);
        }

        logger.accept("Project created!");
    }

    public static void enableI18nSupport(Path location, List<Locale> locales, Consumer<String> logger, Optional<Path> maybeEmf) throws IOException {
        // if we are creating i18n support, IE we must create the directory, and then escape any existing %'s in text
        boolean creating = false;

        var i18n = location.resolve("i18n");
        if(!Files.exists(i18n)) {
            Files.createDirectory(i18n);
            logger.accept("Created directory " + i18n);
            creating = true;
        } else {
            logger.accept("Directory already exists " + i18n);
        }
        SafeBundleLoader safeBundleLoader = new SafeBundleLoader(i18n, MENU_PROJECT_LANG_FILENAME);
        safeBundleLoader.saveChangesKeepingFormatting(MenuEditorApp.EMPTY_LOCALE, Map.of());
        logger.accept("Saved default locale file");
        for(var locale : locales) {
            safeBundleLoader.saveChangesKeepingFormatting(locale, Map.of());
            logger.accept("Created locale file " + locale);
        }

        if(creating && maybeEmf.isPresent()) {
            logger.accept("Trying to escape any names, units or enum entries " + maybeEmf);

            var prj = CodeGeneratorCommand.projectFileOrNull(maybeEmf.get().toFile(), new PrefsConfigurationStorage());
            var menuTree = prj.getMenuTree();
            var needsSave = false;

            // Now go through all items one at a time and escape any known %'s that are likely to cause the
            // code generator to fail because. This will deal with strings in enum items, names, and units.
            for(var item : menuTree.getAllMenuItems()) {
                var builder = MenuItemHelper.builderWithExisting(item);
                boolean changed = false;
                if(item instanceof AnalogMenuItem an && an.getUnitName() != null && an.getUnitName().startsWith("%")) {
                    builder = new AnalogMenuItemBuilder().withExisting(an).withUnit("\\" + an.getUnitName());
                    changed = true;
                } else if(item instanceof EnumMenuItem en && en.getEnumEntries().stream().anyMatch(es -> es.startsWith("%"))) {
                    builder = new EnumMenuItemBuilder().withExisting(en)
                            .withEnumList(en.getEnumEntries().stream().map(e -> e.startsWith("%") ? "\\" + e : e).toList());
                    changed = true;
                } else if(item instanceof RuntimeListMenuItem rtl) {
                    List<String> stringList = MenuItemHelper.getValueFor(rtl, menuTree, List.of());
                    if(stringList.stream().anyMatch(s -> s.startsWith("%"))) {
                        MenuItemHelper.setMenuState(rtl, stringList.stream().map(s -> s.startsWith("%") ? "\\" + s : s).toList(), menuTree);
                        needsSave = true;
                    }
                }

                if(item.getName() != null && item.getName().startsWith("%")) {
                    builder.withName("\\" + item.getName());
                    changed = true;
                }

                if(changed) {
                    var par = menuTree.findParent(item);
                    menuTree.addOrUpdateItem(par.getId(), builder.menuItem());
                    needsSave = true;
                }
            }

            if(needsSave) {
                logger.accept("Saving changes to emf and taking backup " + maybeEmf.get());
                persistProject(prj.getMenuTree(), prj.getOptions());
            }
        }
    }
}
