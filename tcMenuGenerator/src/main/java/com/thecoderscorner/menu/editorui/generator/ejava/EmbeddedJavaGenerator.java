package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class EmbeddedJavaGenerator implements CodeGenerator {
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final ConfigurationStorage configStorage;
    private BiConsumer<System.Logger.Level, String> loggerDelegate;
    private VariableNameGenerator varGenerator;

    public EmbeddedJavaGenerator(ConfigurationStorage storage) {
        this.configStorage = storage;
    }

    @Override
    public boolean startConversion(Path directory, List<CodePluginItem> plugins, MenuTree menuTree,
                                   List<String> previousPluginFiles, CodeGeneratorOptions options) {
        try {
            logLine(INFO,"Starting conversion, Embedded Java to directory " + directory);
            varGenerator = new VariableNameGenerator(menuTree, options.isNamingRecursive(), Set.of());
            EmbeddedJavaProject javaProject = new EmbeddedJavaProject(directory, options, configStorage, this::logLine);
            logLine(INFO, "Determining that required project files are in place");
            javaProject.setupProjectIfNeeded();

            logLine(INFO, "The package for conversion is " + options.getPackageNamespace());

            logLine(INFO, "Generating the menu definition class");
            generateMenuDefinitionsClass(menuTree, options, javaProject);

            logLine(INFO, "Generating the application level class");
            generateMenuApplicationClass(javaProject);

            logLine(INFO, "Generating the menu controller class");
            generateMenuControllerClass(javaProject, menuTree);

            logLine(INFO, "Generating the menu application context class");
            generateMenuAppContext(javaProject, javaProject.getAppClassName(""));
        }
        catch (Exception ex) {
            loggerDelegate.accept(ERROR, "Failed to generate code for java project" + ex.getMessage());
            logger.log(ERROR, "Exception during java project conversion", ex);
        }
        return false;
    }

    private void generateMenuAppContext(EmbeddedJavaProject javaProject, String clazzBaseName) throws IOException {
        logLine(INFO, "Creating or updating the spring application context");
        javaProject.classBuilderFullName("MenuConfig")
                .addPackageImport("com.thecoderscorner.menu.auth.*")
                .addPackageImport("com.thecoderscorner.menu.mgr.MenuManagerServer")
                .addPackageImport("com.thecoderscorner.menu.persist.*")
                .addPackageImport("org.springframework.beans.factory.annotation.Value")
                .addPackageImport("org.springframework.context.annotation.*")
                .addPackageImport("java.time.Clock")
                .addPackageImport("java.util.UUID")
                .addPackageImport("java.util.concurrent.*")
                .setStatementBeforeClass("""
                        /**
                         * Spring creates an application context out of all these components, you can wire together your own objects in either
                         * this same file, or you can import another file. See the spring configuration for more details. You're safe to edit
                         * this file as the designer only appends new entries
                         */
                        @Configuration
                        @PropertySource("classpath:application.properties")
                        """)
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "Clock", "clock")
                        .withStatement("return new PropertiesMenuStateSerialiser(menuDef.getMenuTree(), Path.of(filePath));").withAnnotation("Bean"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuStateSerialiser", "menuStateSerialiser")
                        .withParameter(clazzBaseName + "Menu menuDef").withParameter("@Value(\"${file.menu.storage}\") String filePath")
                        .withStatement("return Clock.systemUTC();").withAnnotation("Bean"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, clazzBaseName + "Menu", "menuDef")
                        .withStatement("return new " + clazzBaseName + "Menu();").withAnnotation("Bean"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, clazzBaseName + "Controller", "menuController")
                        .withStatement("return new " + clazzBaseName + "Controller(menuDef);").withAnnotation("Bean")
                        .withParameter(clazzBaseName + "Menu menuDef"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuAuthenticator", "menuAuthenticator")
                        .withStatement("return new PreDefinedAuthenticator(true);").withAnnotation("Bean"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "ScheduledExecutorService", "executor")
                        .withParameter("@Value(\"${threading.pool.size}\") int poolSize").withAnnotation("Bean")
                        .withStatement("return Executors.newScheduledThreadPool(poolSize);"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuManagerServer", "menuManagerServer")
                        .withAnnotation("Bean").withParameter("ScheduledExecutorService executor")
                        .withParameter(clazzBaseName + "Menu menuDef").withParameter("@Value(\"${server.name}\") String serverName")
                        .withParameter("@Value(\"${server.uuid}\") String serverUUID").withParameter("MenuAuthenticator authenticator").withParameter("Clock clock")
                        .withStatement("return new MenuManagerServer(executor, menuDef.getMenuTree(), serverName, UUID.fromString(serverUUID), authenticator, clock);"))
                .persistClassByPatching();
    }

    private void generateMenuApplicationClass(EmbeddedJavaProject javaProject) throws IOException {
        logLine(INFO, "Building the menu application class based on the plugins and options");
        javaProject.classBuilder("App")
                .setStatementBeforeClass("""
                        /**
                         * This class is the application class and should not be edited, it will be recreated on each code generation
                         */
                         """)
                .addPackageImport("com.thecoderscorner.menu.mgr.*")
                .addPackageImport("org.springframework.context.ApplicationContext")
                .addPackageImport("org.springframework.context.annotation.AnnotationConfigApplicationContext")
                .addStatement(new GeneratedJavaField("MenuManagerServer", "manager"))
                .addStatement(new GeneratedJavaField("ApplicationContext", "context"))
                .blankLine()
                .addStatement(new GeneratedJavaMethod(CONSTRUCTOR_REPLACE)
                        .withStatement("context = new AnnotationConfigApplicationContext(MenuConfig.class);")
                        .withStatement("manager = context.getBean(MenuManagerServer.class);"))
                .addStatement(new GeneratedJavaMethod(METHOD_REPLACE, "void", "start")
                        .withStatement("manager.addMenuManagerListener(context.getBean(Testsupp123Controller.class));")
                        .withStatement("manager.start();"))
                .addStatement(new GeneratedJavaMethod(METHOD_REPLACE, "static void", "main").withParameter("String[] args")
                        .withStatement("new Testsupp123App().start();"))
                .persistClass();
    }

    private void generateMenuControllerClass(EmbeddedJavaProject project, MenuTree tree) throws IOException {
        var builder = project.classBuilder("Controller")
                .supportsInterface("MenuManagerListener")
                .addPackageImport("com.thecoderscorner.menu.mgr.*")
                .addPackageImport("com.thecoderscorner.menu.domain.*")
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
                    builder.addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", methodName)
                            .withParameter(item.getClass().getSimpleName() +  " item").withParameter("boolean remoteAction")
                            .withStatement("// TODO - implement your menu behaviour here for " + item.getName())
                            .withAnnotation("MenuCallback(id=" + item.getId() + ")"));
                });

        builder.addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "menuItemHasChanged")
                        .withParameter("MenuItem item").withParameter("boolean remoteAction")
                        .withStatement("// Called every time any menu item changes"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "managerWillStart")
                        .withAnnotation("Override")
                        .withStatement("// This is called just before the menu manager starts up, you can initialise your system here."))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "managerWillStop")
                        .withAnnotation("Override")
                        .withStatement("// This is called just before the menu manager stops, you can do any shutdown tasks here."))
                .persistClass();
    }

    private void generateMenuDefinitionsClass(MenuTree menuTree, CodeGeneratorOptions options, EmbeddedJavaProject javaProject) throws IOException {
        logLine(INFO, "Building the menu definitions class");
        var copyTextGenerator = new FileBasedProjectPersistor();
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
                        .withStatement("        jsonSerializer = new JsonMenuItemSerializer();")
                        .withStatement("        menuTree = jsonSerializer.newMenuTreeWithItems(APP_MENU_ITEMS);"))
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
}
