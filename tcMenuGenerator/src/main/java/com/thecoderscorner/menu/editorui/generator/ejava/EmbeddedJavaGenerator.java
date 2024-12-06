package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.SketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
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
import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.END_OF_METHODS_TEXT;
import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.*;
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
                                   LocaleMappingHandler handler) {
        try {
            logLine(INFO,"Starting conversion, Embedded Java to directory " + directory);
            this.allPlugins = plugins;
            this.handler = handler;
            varGenerator = new VariableNameGenerator(menuTree, options.isNamingRecursive(), Set.of());
            var rootMenuName = getFirstMenuItemVariableName(menuTree, options.isNamingRecursive());
            context = new CodeConversionContext(platform, rootMenuName, options, options.getLastProperties());
            pluginCreator = new EmbeddedJavaPluginCreator(context);

            logLine(INFO, "Loading the java project from " + directory);
            EmbeddedJavaProject javaProject = new EmbeddedJavaProject(directory, options, configStorage, handler, this::logLine);
            logLine(INFO,"Java project looks to be in place, dir is " + javaProject.getActualPackageDir().relativize(javaProject.getMainJava()));

            logLine(INFO, "Generating the menu definition class");
            generateMenuDefinitionsClass(menuTree, options, javaProject);

            logLine(INFO, "Generating the menu controller class");
            generateMenuControllerClass(javaProject, menuTree);

            if(handler.isLocalSupportEnabled()) {
                copyLocaleDataIntoProject(javaProject);
            }
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

    private String getFirstMenuItemVariableName(MenuTree tree, boolean recursive) {
        var rootList = tree.getMenuItems(MenuTree.ROOT);
        if(rootList.isEmpty()) return "";
        return varGenerator.makeNameToVar(rootList.get(0));
    }


    private void generateMenuInMenu(JavaClassBuilder classBuilder, CodeGeneratorOptions options) throws IOException {
        boolean hasMenuInMenuDefinitions = !options.getMenuInMenuCollection().getAllDefinitions().isEmpty();
        if(hasMenuInMenuDefinitions) {
            var method = new GeneratedJavaMethod(METHOD_REPLACE, "void", "buildMenuInMenuComponents")
                    .withParameter("BaseMenuConfig context")
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
            classBuilder.addPackageImport("com.thecoderscorner.menu.mgr.*")
                    .addPackageImport("com.thecoderscorner.menu.remote.*")
                    .addPackageImport("java.util.concurrent.ScheduledExecutorService");
            classBuilder.addStatement(method);
        } else {
            classBuilder.addStatement(new GeneratedJavaMethod(METHOD_REPLACE, "void", "configureMenuInMenuComponents")
                    .withParameter("BaseMenuConfig config"));
        }
    }

    private void generateMenuControllerClass(EmbeddedJavaProject project, MenuTree tree) throws IOException {
        var menuClass = project.findClassImplementingInPackage("implements TcApiDefinitions");
        var builder = project.classBuilderFullName(project.findClassImplementingInPackage("implements MenuManagerListener"))
                .supportsInterface("MenuManagerListener")
                .addPackageImport("com.thecoderscorner.menu.mgr.*")
                .addPackageImport("com.thecoderscorner.menu.domain.*")
                .addPackageImport("com.thecoderscorner.menu.domain.state.*")
                .addStatement(new GeneratedJavaField(menuClass, " menuDef", true, true))
                .blankLine()
                .addStatement(new GeneratedJavaMethod(CONSTRUCTOR_IF_MISSING)
                        .withParameter(menuClass + " menuDef")
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
        var builder = javaProject.classBuilderFullName(javaProject.findClassImplementingInPackage("implements TcApiDefinitions"))
                .supportsInterface("TcApiDefinitions")
                .addPackageImport("com.thecoderscorner.menu.domain.*")
                .addPackageImport("com.thecoderscorner.menu.domain.state.*")
                .addPackageImport("com.thecoderscorner.menu.persist.JsonMenuItemSerializer")
                .addPackageImport("com.thecoderscorner.embedcontrol.core.util.BaseMenuConfig")
                .addPackageImport("com.thecoderscorner.embedcontrol.core.util.TcApiDefinitions;")
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
        builder.blankLine();
        generateMenuInMenu(builder, options);
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
