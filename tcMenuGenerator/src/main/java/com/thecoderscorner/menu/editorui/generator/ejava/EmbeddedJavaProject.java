package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeGeneratorCapable;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class EmbeddedJavaProject {
    private final Path mainResources;
    private final Path mainJava;
    private final Path testJava;
    private final Path src;
    private final Path root;
    private final Path data;
    private final Path controllerPath;
    private final CodeGeneratorOptions codeOptions;
    private final ConfigurationStorage configStorage;
    private final LocaleMappingHandler handler;
    private final BiConsumer<System.Logger.Level, String> uiLogger;

    public EmbeddedJavaProject(Path directory, CodeGeneratorOptions options, ConfigurationStorage storage,
                               LocaleMappingHandler handler, BiConsumer<System.Logger.Level, String> uiLogger) {
        this.handler = handler;
        this.uiLogger = uiLogger;
        configStorage = storage;
        root = directory;
        codeOptions = options;
        src = directory.resolve("src");
        mainJava = src.resolve("main").resolve("java");
        mainResources = src.resolve("main").resolve("resources");
        testJava = src.resolve("test").resolve("java");
        data = root.resolve("data");
        var cp = findController();
        if(!Files.exists(mainJava) || !Files.exists(mainResources) || !Files.exists(root.resolve("pom.xml")) || cp.isEmpty()) {
            throw new IllegalArgumentException(root + " does not contain a workable java project. Run the code generator first.");
        }

        // get the actual directory and not the java file
        controllerPath = cp.get().getParent();
    }

    public Path getProjectRoot() {
        return root;
    }

    public Path getMainResources() {
        return mainResources;
    }

    public Path getMainJava() {
        return mainJava;
    }

    public Path getTestJava() {
        return testJava;
    }

    public Path getSrc() {
        return src;
    }

    public String findClassImplementingInPackage(String implementing) throws IOException {
        var myLocation = mainJava;
        for (var dirPart : getMenuPackage().split("\\.")) {
            myLocation = myLocation.resolve(dirPart);
        }

        var match = Files.walk(myLocation, FileVisitOption.FOLLOW_LINKS).filter(f -> f.toString().endsWith(".java"))
                .filter(f-> fileContains(f, implementing)).findFirst();
        return match.map(f -> f.getFileName().toString().replace(".java", "")).orElseThrow();
    }

    private boolean fileContains(Path f, String match) {
        try {
            return Files.readString(f).contains(match);
        } catch (Exception ex) {
            return false;
        }
    }

    public String getAppClassName(String postfix) {
        var className = ensureLocalized(codeOptions.getApplicationName());
        Collection<String> parts = Arrays.asList(className.split("[\\s]+"));
        var varName = parts.stream().map(StringHelper::capitaliseFirst).collect(Collectors.joining()) + postfix;
        varName = varName.replaceAll("[\\W%]+", "");
        return varName;
    }

    private String ensureLocalized(String applicationName) {
        if(applicationName.startsWith("%") && !applicationName.startsWith("%%")) {
            applicationName = handler.getLocalSpecificEntry(applicationName);
            if(applicationName == null) {
                throw new IllegalArgumentException("The application name is an undefined locale reference: " + applicationName);
            }
        }
        return applicationName;
    }

    public String getMenuPackage() {
        return codeOptions.getPackageNamespace();
    }

    public JavaClassBuilder classBuilder(String postfix) {
        return new JavaClassBuilder(this, getMenuPackage(), getAppClassName(postfix), uiLogger);
    }

    public JavaClassBuilder classBuilderFullName(String name) {
        return new JavaClassBuilder(this, getMenuPackage(), name, uiLogger);
    }

    public List<CodeGeneratorCapable> getAllCodeGeneratorCapables() {
        var capables = new ArrayList<CodeGeneratorCapable>();
        capables.add(codeOptions.getEepromDefinition());
        capables.add(codeOptions.getAuthenticatorDefinition());
        // TODO, do we need to support io expanders?
        return capables;
    }

    public Optional<Path> findController() {
        try(var fileStream = Files.walk(mainJava, 10, FileVisitOption.FOLLOW_LINKS)){
             return fileStream.filter(path -> path.toString().endsWith(".java") && containsController(path))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private boolean containsController(Path path) {
        try {
            var data = Files.readString(path);
            return data.contains("implements MenuManagerListener") &&
                    data.contains("Auto generated menu callbacks end here. Please do not remove this line or change code after it.");
        } catch(Exception e) {
            return false;
        }
    }

    public Path getActualPackageDir() {
        return controllerPath;
    }
}
