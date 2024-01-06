package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.embedcontrol.core.service.TcMenuFormPersistence;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.core.*;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.END_OF_METHODS_TEXT;
import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.*;
import static com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition.ConnectionType;
import static com.thecoderscorner.menu.editorui.project.CurrentEditorProject.MENU_PROJECT_LANG_FILENAME;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class EmbeddedJavaGenerator implements CodeGenerator {
    public static final Pattern MODULE_MATCHER = Pattern.compile("mod:([^:]*):(.+)");
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final ConfigurationStorage configStorage;
    private final EmbeddedPlatform platform;
    private BiConsumer<System.Logger.Level, String> loggerDelegate;
    private VariableNameGenerator varGenerator;
    private final JavaCodeGeneratorCapableWrapper wrapper = new JavaCodeGeneratorCapableWrapper();
    private CodeConversionContext context;
    private EmbeddedJavaPluginCreator pluginCreator;
    private List<CodePluginItem> allPlugins;
    private LocaleMappingHandler handler;

    public EmbeddedJavaGenerator(ConfigurationStorage storage, EmbeddedPlatform platform) {
        this.configStorage = storage;
        this.platform = platform;
    }

    @Override
    public boolean startConversion(Path directory, List<CodePluginItem> plugins, MenuTree menuTree,
                                   List<String> previousPluginFiles, CodeGeneratorOptions options,
                                   LocaleMappingHandler handler, List<TcMenuFormPersistence> formPersistence) {
        try {
            logLine(INFO,"Starting conversion, Embedded Java to directory " + directory);
            this.allPlugins = plugins;
            this.handler = handler;
            varGenerator = new VariableNameGenerator(menuTree, options.isNamingRecursive(), Set.of());
            var rootMenuName = getFirstMenuItemVariableName(menuTree, options.isNamingRecursive());
            context = new CodeConversionContext(platform, rootMenuName, options, options.getLastProperties());
            pluginCreator = new EmbeddedJavaPluginCreator(context);

            EmbeddedJavaProject javaProject = new EmbeddedJavaProject(directory, options, configStorage, handler, this::logLine);
            logLine(INFO, "Determining that required project files are in place");
            javaProject.setupProjectIfNeeded();

            logLine(INFO, "The package for conversion is " + options.getPackageNamespace());

            logLine(INFO, "Generating the menu definition class");
            generateMenuDefinitionsClass(menuTree, options, javaProject);

            logLine(INFO, "Generating the application level class");
            generateMenuApplicationClass(javaProject, options);

            logLine(INFO, "Generating the menu controller class");
            generateMenuControllerClass(javaProject, menuTree);

            logLine(INFO, "Generating the menu application context class");
            generateMenuAppContext(javaProject, javaProject.getAppClassName(""));

            if(options.isModularApp()) {
                createJavaModuleFile(options, javaProject);
            }

            var fileProcessor = new PluginRequiredFileProcessor(context, this::logLine);
            fileProcessor.dealWithRequiredPlugins(plugins, makePluginPath(javaProject), javaProject.getProjectRoot(),
                    options.getSaveLocation(), previousPluginFiles);

            if(handler.isLocalSupportEnabled()) {
                copyLocaleDataIntoProject(javaProject);
            }

            logLine(INFO, "Checking if all dependencies are in the maven POM");
            allPlugins.stream().flatMap(p -> p.getIncludeFiles().stream())
                    .filter(inc -> inc.getApplicability().isApplicable(context.getProperties()))
                    .filter(inc -> inc.getHeaderType() == GLOBAL)
                    .forEach(inc -> javaProject.addDependencyToPomIfNeeded(inc.getHeaderName()));
            logLine(INFO, "Completed code generation for java project");
        }
        catch (Exception ex) {
            loggerDelegate.accept(ERROR, "Failed to generate code for java project" + ex.getMessage());
            logger.log(ERROR, "Exception during java project conversion", ex);
        }
        return false;
    }

    private void copyLocaleDataIntoProject(EmbeddedJavaProject javaProject) throws IOException {
        loggerDelegate.accept(INFO, "Copying resource bundles to data/i18n directory");
        Files.copy(javaProject.getProjectRoot().resolve("i18n"), javaProject.getProjectRoot().resolve("data"),
                StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    }

    private void createJavaModuleFile(CodeGeneratorOptions options, EmbeddedJavaProject javaProject) throws IOException {
        logLine(INFO, "Generating the Java module-info file");
        var patcher = new ModuleFilePatcher(javaProject.getMainJava());
        patcher.addOpens(options.getPackageNamespace() + ".tcmenu");
        patcher.addExports(options.getPackageNamespace() + ".tcmenu.plugins");
        patcher.addRequires("java.logging");
        patcher.addRequires("java.prefs");
        patcher.addRequires("java.desktop");
        patcher.addRequires("com.google.gson");
        patcher.addRequires("com.fazecast.jSerialComm");
        patcher.addRequires("com.thecoderscorner.tcmenu.javaapi");
        patcher.addRequires("com.thecoderscorner.embedcontrol.core");

        allPlugins.stream().flatMap(p -> p.getIncludeFiles().stream())
                .filter(inc -> inc.getApplicability().isApplicable(context.getProperties()))
                .filter(inc -> inc.getHeaderType() == GLOBAL)
                .forEach(inc -> {
                    var matcher = MODULE_MATCHER.matcher(inc.getHeaderName());
                    if(matcher.matches() && inc.getHeaderType() == GLOBAL) {
                        logLine(INFO, "Module dependency from plugin " + inc.getHeaderName());
                        switch(matcher.group(1)) {
                            case "opens" -> patcher.addOpens(matcher.group(2));
                            case "exports" -> patcher.addExports(matcher.group(2));
                            default -> patcher.addRequires(matcher.group(2));
                        }
                    }
                });
        if(patcher.startConversion(options, javaProject)) {
            logLine(INFO, "Module file was updated");
        } else {
            logLine(INFO, "No changes needed in module file");
        }
    }

    private Path makePluginPath(EmbeddedJavaProject javaProject) throws IOException {
        var pluginStr = javaProject.getMenuPackage() + ".plugins";
        var pluginPath = javaProject.getMainJava();
        for(var part : pluginStr.split("\\.")) {
            pluginPath = pluginPath.resolve(part);
        }
        if(!Files.exists(pluginPath)) {
            Files.createDirectories(pluginPath);
        }
        return pluginPath;
    }

    private String getFirstMenuItemVariableName(MenuTree tree, boolean recursive) {
        var rootList = tree.getMenuItems(MenuTree.ROOT);
        if(rootList.isEmpty()) return "";
        return varGenerator.makeNameToVar(rootList.get(0));
    }

    private void generateMenuAppContext(EmbeddedJavaProject javaProject, String clazzBaseName) throws IOException {
        logLine(INFO, "Creating or updating the application context");
        var builder = javaProject.classBuilderFullName("MenuConfig").extendsClass("BaseMenuConfig")
                .addPackageImport("com.thecoderscorner.menu.auth.*")
                .addPackageImport("com.thecoderscorner.menu.mgr.MenuManagerServer")
                .addPackageImport("com.thecoderscorner.menu.persist.*")
                .addPackageImport("com.thecoderscorner.embedcontrol.core.util.*")
                .addPackageImport("com.thecoderscorner.embedcontrol.core.service.*")
                .addPackageImport("com.thecoderscorner.embedcontrol.customization.*")
                .addPackageImport("com.thecoderscorner.embedcontrol.jfx.controlmgr.*")
                .addPackageImport("com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter")
                .addPackageImport("java.time.Clock")
                .addPackageImport("java.util.*")
                .addPackageImport("java.util.concurrent.*")
                .addPackageImport("java.nio.file.Path")
                .setStatementBeforeClass("""
                        /**
                         * This class creates an application context out of all these components, and you can request any components that are
                         * put into the context using getBean(ClassName.class). See the base class BaseMenuConfig for more details. Generally
                         * don't change the constructor, as it is rebuild each time around. Prefer putting your own code in appCustomConfiguration
                         */
                        """)
                .addStatement(new GeneratedJavaMethod(CONSTRUCTOR_REPLACE)
                        .withStatement("// Do not change this constructor, it is replaced with each build, put your objects in appCustomConfiguration")
                        .withStatement("super(null, null);")
                        .withStatement("Clock clock = asBean(Clock.systemUTC());")
                        .withStatement("var executorService = asBean(Executors.newScheduledThreadPool(propAsIntWithDefault(\"threading.pool.size\", 4)));")
                        .withStatement("var menuDef = asBean(new " + clazzBaseName + "Menu());")
                        .withStatement("asBean(new PropertiesMenuStateSerialiser(menuDef.getMenuTree(), Path.of(resolvedProperties.getProperty(\"file.menu.storage\")).resolve(\"menuStorage.properties\")));")
                        .withStatement("scanForComponents();"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "JfxNavigationHeader", "navMgr").withTcComponent()
                        .withParameter("ScheduledExecutorService executorService")
                        .withParameter("GlobalSettings settings")
                        .withStatement("return new JfxNavigationHeader(executorService, settings);"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "ConfigurableProtocolConverter", "protocol").withTcComponent()
                        .withStatement("return new ConfigurableProtocolConverter(true);"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuManagerServer", "menuManagerServer").withTcComponent()
                        .withParameter("Clock clock")
                        .withParameter(clazzBaseName + "Menu menuDef")
                        .withParameter("ScheduledExecutorService executorService")
                        .withParameter("MenuAuthenticator authenticator")
                        .withStatement("return new MenuManagerServer(executorService, menuDef.getMenuTree(), mandatoryStringProp(\"server.name\"), UUID.fromString(mandatoryStringProp(\"server.uuid\")), authenticator, clock);"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, clazzBaseName + "Controller", "menuController").withTcComponent()
                        .withStatement("return new EmbeddedJavaDemoController(")
                        .withStatement("        getBean(EmbeddedJavaDemoMenu.class),")
                        .withStatement("        getBean(JfxNavigationManager.class),")
                        .withStatement("        getBean(ScheduledExecutorService.class),")
                        .withStatement("        getBean(GlobalSettings.class),")
                        .withStatement("        getBean(MenuItemStore.class)")
                        .withStatement(");"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuItemStore", "itemStore").withTcComponent()
                        .withParameter("GlobalSettings settings")
                        .withParameter(clazzBaseName + "Menu menuDef")
                        .withStatement("return new MenuItemStore(settings, menuDef.getMenuTree(), \"\", 7, 2, settings.isDefaultRecursiveRendering());"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "GlobalSettings", "globalSettings").withTcComponent()
                        .withStatement("var settings = new GlobalSettings(new ApplicationThemeManager());")
                        .withStatement("// load or adjust the settings as needed here. You could use the JDBC components with SQLite to load and store")
                        .withStatement("// these values just like embed control does. See TcPreferencesPersistence and TccDatabaseUtilities.")
                        .withStatement("settings.setDefaultFontSize(16);")
                        .withStatement("settings.setDefaultRecursiveRendering(false);")
                        .withStatement("return settings;"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuAppVersion", "versionInfo").withTcComponent()
                        .withStatement("var version = mandatoryStringProp(\"build.version\");")
                        .withStatement("var timestamp = mandatoryStringProp(\"build.timestamp\");")
                        .withStatement("var groupId = mandatoryStringProp(\"build.groupId\");")
                        .withStatement("var artifact = mandatoryStringProp(\"build.artifactId\");")
                        .withStatement("return new MenuAppVersion(new VersionInfo(version), timestamp, groupId, artifact);"));


        if(handler.isLocalSupportEnabled()) {
            builder.addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "LocaleMappingHandler", "localeHandler")
                    .withTcComponent()
                    .withStatement("return new PropertiesLocaleEnabledHandler(new SafeBundleLoader(\"./data/i18n/" + MENU_PROJECT_LANG_FILENAME+ "\"));"));
        } else {
            builder.addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "LocaleMappingHandler", "localeHandler")
                    .withTcComponent().withStatement("return LocaleMappingHandler.NOOP_IMPLEMENTATION;"));
        }


        for(var cap : javaProject.getAllCodeGeneratorCapables()) {
            wrapper.addToContext(cap, builder);
        }

        pluginCreator.mapImports(allPlugins.stream().flatMap(p -> p.getIncludeFiles().stream()).distinct().toList(), builder);
        pluginCreator.mapContext(allPlugins.stream().flatMap(p -> p.getVariables().stream()).distinct().toList(), builder);

        builder.addStatement(END_OF_METHODS_TEXT);

        builder.persistClassByPatching();
    }

    private void generateMenuApplicationClass(EmbeddedJavaProject javaProject, CodeGeneratorOptions options) throws IOException {
        logLine(INFO, "Building the menu application class based on the plugins and options");
        var builder = javaProject.classBuilder("App")
                .setStatementBeforeClass("""
                        /**
                         * This class is the application class and should not be edited, it will be recreated on each code generation
                         */
                         """)
                .addPackageImport("com.thecoderscorner.menu.mgr.*")
                .addPackageImport("com.thecoderscorner.menu.persist.MenuStateSerialiser")
                .addStatement(new GeneratedJavaField("MenuManagerServer", "manager"))
                .addStatement(new GeneratedJavaField("MenuConfig", "context"));
        for(var cap : javaProject.getAllCodeGeneratorCapables()) {
            wrapper.addAppFields(cap, builder);
        }
        pluginCreator.mapVariables(allPlugins.stream().flatMap(p -> p.getVariables().stream()).distinct().toList(), builder);
        pluginCreator.mapImports(allPlugins.stream().flatMap(p -> p.getIncludeFiles().stream()).distinct().toList(), builder);

        var constructor = new GeneratedJavaMethod(CONSTRUCTOR_REPLACE)
                .withStatement("context = new MenuConfig();")
                .withStatement("manager = context.getBean(MenuManagerServer.class);");
        pluginCreator.mapConstructorStatements(allPlugins.stream().flatMap(p -> p.getVariables().stream()).toList(), constructor);

        builder.blankLine().addStatement(constructor);
        var startMethod = new GeneratedJavaMethod(METHOD_REPLACE, "void", "start")
                .withStatement("var serializer = context.getBean(MenuStateSerialiser.class);")
                .withStatement("serializer.loadMenuStatesAndApply();")
                .withStatement("Runtime.getRuntime().addShutdownHook(new Thread(serializer::saveMenuStates));")
                .withStatement("manager.addMenuManagerListener(context.getBean(" + javaProject.getAppClassName("Controller") + ".class));");
        boolean hasMenuInMenuDefinitions = !options.getMenuInMenuCollection().getAllDefinitions().isEmpty();
        if(hasMenuInMenuDefinitions) {
            startMethod.withStatement("buildMenuInMenuComponents();");
            builder.addPackageImport("com.thecoderscorner.menu.remote.*")
                    .addPackageImport("com.thecoderscorner.menu.remote.socket.*")
                    .addPackageImport("java.util.concurrent.*")
                    .addPackageImport("java.time.*");
            if(options.getMenuInMenuCollection().getAllDefinitions().stream().anyMatch(def -> def.getConnectionType() == ConnectionType.SERIAL)) {
                builder.addPackageImport("com.thecoderscorner.embedcontrol.core.rs232");
            }
        }
        pluginCreator.mapMethodCalls(allPlugins.stream().flatMap(p -> p.getFunctions().stream()).distinct().toList(), startMethod, List.of());

        builder.addStatement(startMethod);
        builder.addStatement(new GeneratedJavaMethod(METHOD_REPLACE, "static void", "main").withParameter("String[] args")
                        .withStatement("new " + javaProject.getAppClassName("App") + "().start();"));

        for(var cap : javaProject.getAllCodeGeneratorCapables()) {
            wrapper.addAppMethods(cap, builder);
        }

        if(hasMenuInMenuDefinitions) {
            var method = new GeneratedJavaMethod(METHOD_REPLACE, "void", "buildMenuInMenuComponents")
                    .withStatement("MenuManagerServer menuManager = context.getBean(MenuManagerServer.class);")
                    .withStatement("MenuCommandProtocol protocol = context.getBean(MenuCommandProtocol.class);")
                    .withStatement("ScheduledExecutorService executor = context.getBean(ScheduledExecutorService.class);")
                    .withStatement("LocalIdentifier localId = new LocalIdentifier(menuManager.getServerUuid(), menuManager.getServerName());");
            for(var definition : options.getMenuInMenuCollection().getAllDefinitions().stream()
                    .sorted(Comparator.comparingInt(MenuInMenuDefinition::getIdOffset)).toList()) {
                var variableName = "remMenu"  + StringHelper.capitaliseWords(definition.getVariableName()).replaceAll("\\s", "");
                switch (definition.getConnectionType()) {
                    case SOCKET -> method.withStatement(String.format("var %sConnector = new SocketBasedConnector(localId, executor, Clock.systemUTC(), protocol, \"%s\", %d, ConnectMode.FULLY_AUTHENTICATED);",
                            variableName, definition.getPortOrIpAddress(), definition.getPortOrBaud()));
                    case SERIAL -> method.withStatement(String.format("var %sConnector = new Rs232RemoteConnector(localId, %s, %d, protocol, executor, Clock.systemUTC(), ConnectMode.FULLY_AUTHENTICATED);",
                            variableName, definition.getPortOrIpAddress(), definition.getPortOrBaud()));
                }
                method.withStatement(String.format("var %s = new MenuInMenu(%s, menuManager, menuManager.getManagedMenu().getMenuById(%d).orElseThrow(), MenuInMenu.ReplicationMode.%s, %d, %d);",
                        variableName, variableName + "Connector",  definition.getSubMenuId(),
                        definition.getReplicationMode(), definition.getIdOffset(), definition.getMaximumRange()
                ));
                method.withStatement(variableName + ".start();");
            }
            builder.addStatement(method);
        }

        builder.persistClass();
    }

    private void generateMenuControllerClass(EmbeddedJavaProject project, MenuTree tree) throws IOException {
        var builder = project.classBuilder("Controller")
                .supportsInterface("MenuManagerListener")
                .addPackageImport("com.thecoderscorner.menu.mgr.*")
                .addPackageImport("com.thecoderscorner.menu.domain.*")
                .addPackageImport("com.thecoderscorner.menu.domain.state.*")
                .addStatement(new GeneratedJavaField(project.getAppClassName("Menu"), " menuDef", true, true))
                .blankLine()
                .addStatement(new GeneratedJavaMethod(CONSTRUCTOR_IF_MISSING)
                        .withParameter(project.getAppClassName("Menu") + " menuDef")
                        .withStatement("this.menuDef = menuDef;"));

        tree.getAllMenuItems().stream()
                .filter(item -> !StringHelper.isStringEmptyOrNull(item.getFunctionName()))
                .sorted(Comparator.comparingInt(MenuItem::getId))
                .forEach(item -> {
                    String methodName = item.getFunctionName().replace('@', '_');
                    var javaMethod = new GeneratedJavaMethod(METHOD_IF_MISSING, "void", methodName)
                            .withParameter("Object sender").withParameter(item.getClass().getSimpleName() +  " item")
                            .withStatement("// TODO - implement your menu behaviour here for " + item.getName());
                    if(item instanceof RuntimeListMenuItem) {
                        javaMethod.withAnnotation("MenuCallback(id=" + item.getId() + ", listResult=true)");
                        javaMethod.withParameter("ListResponse selInfo");

                    } else {
                        javaMethod.withAnnotation("MenuCallback(id=" + item.getId() + ")");
                    }
                    builder.addStatement(javaMethod);
                });

        builder.blankLine()
               .addStatement(END_OF_METHODS_TEXT)
               .blankLine();

        builder.addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "menuItemHasChanged")
                        .withParameter("Object sender").withParameter("MenuItem item")
                        .withStatement("// Called every time any menu item changes"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "managerWillStart")
                        .withAnnotation("Override")
                        .withStatement("// This is called just before the menu manager starts up, you can initialise your system here."))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "managerWillStop")
                        .withAnnotation("Override")
                        .withStatement("// This is called just before the menu manager stops, you can do any shutdown tasks here."))
                .persistClassByPatching();
    }

    private void generateMenuDefinitionsClass(MenuTree menuTree, CodeGeneratorOptions options, EmbeddedJavaProject javaProject) throws IOException {
        logLine(INFO, "Building the menu definitions class");
        var copyTextGenerator = new FileBasedProjectPersistor(new PluginEmbeddedPlatformsImpl());
        var menusAsText = variableAsJavaString(copyTextGenerator.itemsToCopyText(MenuTree.ROOT, menuTree));
        var builder = javaProject.classBuilder("Menu")
                .addPackageImport("com.thecoderscorner.menu.domain.*")
                .addPackageImport("com.thecoderscorner.menu.domain.state.*")
                .addPackageImport("com.thecoderscorner.menu.persist.JsonMenuItemSerializer")
                .addStatement(menusAsText)
                .addStatement(new GeneratedJavaField("MenuTree", "menuTree", true, true))
                .addStatement(new GeneratedJavaField("JsonMenuItemSerializer", "jsonSerializer", true, true))
                .blankLine()
                .addStatement(new GeneratedJavaMethod(CONSTRUCTOR_REPLACE)
                        .withStatement("jsonSerializer = new JsonMenuItemSerializer();")
                        .withStatement("menuTree = jsonSerializer.newMenuTreeWithItems(APP_MENU_ITEMS);")
                        .withStatement("menuTree.initialiseStateForEachItem();"))
                .addStatement(new GeneratedJavaMethod(METHOD_REPLACE, "MenuTree", "getMenuTree")
                        .withStatement("return menuTree;"))
                .addStatement(new GeneratedJavaMethod(METHOD_REPLACE, "JsonMenuItemSerializer", "getJsonSerializer")
                        .withStatement("return jsonSerializer;"))
                .addStatement("// Accessors for each menu item now follow").blankLine();
        for(var item : menuTree.getAllMenuItems().stream().sorted(Comparator.comparingInt(MenuItem::getId)).toList()) {
            if(item == MenuTree.ROOT) continue;
            String clzName = item.getClass().getSimpleName();
            String nameAsVariable = varGenerator.makeNameToVar(item);
            builder.addStatement(new GeneratedJavaMethod(METHOD_REPLACE, clzName, "get" + nameAsVariable)
                    .withStatement(String.format("return (%s) menuTree.getMenuById(%d).orElseThrow();", clzName, item.getId())));
        }
        builder.persistClass();
    }

    private void logLine(System.Logger.Level lvl, String s) {
        if(loggerDelegate != null) loggerDelegate.accept(lvl, s);
        logger.log(lvl, "JavaGen - " + s);
    }

    private String variableAsJavaString(String itemsToCopyText) {
        var sb = new StringBuilder(4096);
        sb.append("private final static String APP_MENU_ITEMS = \"\"\"").append(LINE_BREAK);
        sb.append(itemsToCopyText).append("\"\"\"");
        sb.append(';');
        return sb.toString();
    }

    @Override
    public void setLoggerFunction(BiConsumer<System.Logger.Level, String> logLine) {
        loggerDelegate = logLine;
    }

    @Override
    public SketchFileAdjuster getSketchFileAdjuster() {
        return new SketchFileAdjuster() {

            @Override
            public void makeAdjustments(BiConsumer<System.Logger.Level, String> logger, Path dir, String inoFile, String projectName, Collection<CallbackRequirement> callbacks, MenuTree tree) throws IOException {}

            @Override
            public Path createFileIfNeeded(BiConsumer<System.Logger.Level, String> logger, Path path, CodeGeneratorOptions projectOptions) throws IOException {
                throw new IOException("No sketch adjuster on Java");
            }
        };
    }
}
