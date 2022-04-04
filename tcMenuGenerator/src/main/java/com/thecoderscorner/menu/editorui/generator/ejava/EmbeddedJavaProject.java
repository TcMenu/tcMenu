package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeGeneratorCapable;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.XMLDOMHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.persist.XMLDOMHelper.writeXml;
import static java.lang.System.Logger.Level.*;

public class EmbeddedJavaProject {
    private final static String VERSION_PROPERTIES_CONTENTS = """
            # During builds, the parameters below are populated with the version information by maven.
            build.version=${project.version}
            build.groupId=${project.groupId}
            build.artifactId=${project.artifactId}
            build.timestamp=${timestamp}
                        
            # server name properties
            server.name=%SERVERNAME%
            server.uuid=%SERVERUUID%
                        
            # we create an executor that you can also use, depending on load you can adjust the number of threads
            threading.pool.size=4
                        
            ## here we put the location of the data files needed by the application
            file.menu.storage=%DATADIR%
            file.auth.storage=%DATADIR%/auth.properties
            
            # the default font size to be used when no other provided
            default.font.size = 16

            # you can add any other properties needed by your application here.
            """;

    private final Pattern dependencyMatcher = Pattern.compile("mvn:(.*)/(.*)@(.*)");
    private final Path mainResources;
    private final Path mainJava;
    private final Path testJava;
    private final Path src;
    private final Path root;
    private final Path data;
    private final CodeGeneratorOptions codeOptions;
    private final ConfigurationStorage configStorage;
    private final BiConsumer<System.Logger.Level, String> uiLogger;

    public EmbeddedJavaProject(Path directory, CodeGeneratorOptions options, ConfigurationStorage storage,
                               BiConsumer<System.Logger.Level, String> uiLogger) {
        this.uiLogger = uiLogger;
        configStorage = storage;
        root = directory;
        codeOptions = options;
        src = directory.resolve("src");
        mainJava = src.resolve("main").resolve("java");
        mainResources = src.resolve("main").resolve("resources");
        testJava = src.resolve("test").resolve("java");
        data = root.resolve("data");
    }

    public void setupProjectIfNeeded() throws IOException {
        uiLogger.accept(INFO, "Checking the the core directories and files exist, create when needed");
        Files.createDirectories(mainJava);
        Files.createDirectories(mainResources);
        Files.createDirectories(testJava);
        Files.createDirectories(data);

        Path versionPropertiesPath = getMainResources().resolve("application.properties");
        if(!Files.exists(versionPropertiesPath)) Files.writeString(versionPropertiesPath,
                VERSION_PROPERTIES_CONTENTS.replaceAll("%SERVERNAME%", codeOptions.getApplicationName())
                        .replaceAll("%SERVERUUID%", codeOptions.getApplicationUUID().toString())
                        .replaceAll("%DATADIR%", data.toString()));

        Path readmeFile = getProjectRoot().resolve("README.md");
        var readmeTemplate = new String(Objects.requireNonNull(getClass().getResourceAsStream("/packaged-plugins/packaged-readme.md")).readAllBytes());
        readmeTemplate = readmeTemplate.replaceAll("%APPNAME%", codeOptions.getApplicationName())
                .replaceAll("%APPCLASSNAME%", getAppClassName(""));
        if(!Files.exists(readmeFile)) Files.writeString(readmeFile, readmeTemplate);

        Path pomXml = root.resolve("pom.xml");
        if(!Files.exists(root.resolve("build.gradle")) && !Files.exists(pomXml)) {
            uiLogger.accept(INFO, "No build file, creating a pom.xml for maven builds, you can also use gradle");
            var pom = new String(Objects.requireNonNull(getClass().getResourceAsStream("/packaged-plugins/packaged-mvn-pom.xml")).readAllBytes());
            pom = pom.replaceAll("%APP_NAME%", getAppClassName(""));
            pom = pom.replaceAll("%APP_DESCRIPTION%", "An application built with TcMenu Designer");
            pom = pom.replaceAll("%PACKAGE_NAME%", codeOptions.getPackageNamespace());
            pom = pom.replaceAll("%TCMENU_VERSION%", configStorage.getVersion());

            Files.writeString(pomXml, pom);
        }
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

    public String getAppClassName(String postfix) {
        var className = codeOptions.getApplicationName();
        Collection<String> parts = Arrays.asList(className.split("[\\s]+"));
        return parts.stream().map(StringHelper::capitaliseFirst).collect(Collectors.joining()) + postfix;
    }

    public String getMenuPackage() {
        return codeOptions.getPackageNamespace() + ".tcmenu";
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

    public void addDependencyToPomIfNeeded(String dependency) {
        var pom = root.resolve("pom.xml");
        if(!Files.exists(pom)) {
            uiLogger.accept(WARNING, "Not using maven: You need to manually add dependency - " + dependency);
            return;
        }
        try {
            var matcher = dependencyMatcher.matcher(dependency);
            if(!matcher.matches()) {
                uiLogger.accept(WARNING, "Not a maven dependency " + dependency);
            }
            var org = matcher.group(1);
            var artifact = matcher.group(2);
            var version = matcher.group(3);

            var doc = XMLDOMHelper.loadDocumentFromPath(pom);
            var allDeps = XMLDOMHelper.transformElements(
                    doc.getDocumentElement(), "dependencies", "dependency", (ele) ->
                        new MavenDependency(
                                XMLDOMHelper.textOfElementByName(ele, "groupId"),
                                XMLDOMHelper.textOfElementByName(ele, "artifactId")
                        )
            );
            if(allDeps.contains(new MavenDependency(org, artifact))) {
                uiLogger.accept(INFO, "POM already contains dependency for " + org + "/" + artifact);
            } else {
                var deps = XMLDOMHelper.elementWithName(doc.getDocumentElement(), "dependencies");
                if(deps == null) throw new IllegalStateException("No dependencies section in maven pom");
                var dep = doc.createElement("dependency");
                var orgEle = doc.createElement("groupId");
                orgEle.setTextContent(org);
                var artifactEle = doc.createElement("artifactId");
                artifactEle.setTextContent(artifact);
                var verEle = doc.createElement("version");
                verEle.setTextContent(version);
                dep.appendChild(orgEle);
                dep.appendChild(artifactEle);
                dep.appendChild(verEle);
                deps.appendChild(dep);

                try (FileOutputStream output = new FileOutputStream(pom.toFile())) {
                    writeXml(doc, output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            uiLogger.accept(ERROR, "Did not manage to modify project model" + e.getMessage());
        }
    }

    private record MavenDependency(String org, String artifact) {}
}
