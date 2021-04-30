package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.validation.StringPropertyValidationRules.VAR_PATTERN;
import static picocli.CommandLine.*;

@Command(name = "create-project")
public class CreateProjectCommand implements Callable<Integer> {

    private static final String DEFAULT_ARDUINO_SKETCH_STRING = "// default CPP main file for sketch" + LINE_BREAK +
            "#include <PlatformDetermination.h>" + LINE_BREAK + LINE_BREAK +
            "#include <TaskManagerIO.h>" + LINE_BREAK + LINE_BREAK +
            "void setup() {" + LINE_BREAK +
            "}" + LINE_BREAK + LINE_BREAK +
            "void loop() {" + LINE_BREAK +
            "    taskManager.runLoop();" + LINE_BREAK +
            "}" + LINE_BREAK;

    private static final String DEFAULT_MBED_SKETCH_STRING = "// default CPP main file for sketch" + LINE_BREAK +
            "#include <PlatformDetermination.h>" + LINE_BREAK + LINE_BREAK +
            "volatile bool appRunning = true;" + LINE_BREAK + LINE_BREAK +
            "void setup() {" + LINE_BREAK +
            "}" + LINE_BREAK + LINE_BREAK +
            "int main() {" + LINE_BREAK +
            "    setup();" + LINE_BREAK +
            "    while(appRunning) {" + LINE_BREAK +
            "        taskManager.runLoop();" + LINE_BREAK +
            "    }" + LINE_BREAK +
            "}" + LINE_BREAK;

    public enum SupportedPlatform { ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32, MBED_RTOS }

    @Option(names = {"-d", "--directory"}, description = "optional directory name (defaults to current)")
    private File projectLocation;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(names = {"-p", "--platform"}, description = "one of ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32, MBED_RTOS", required = true)
    private SupportedPlatform platform = SupportedPlatform.ARDUINO_AVR;

    @Option(names = {"-m", "--cpp"}, description = "use a cpp file for main")
    private boolean cppMain;

    @Parameters(paramLabel = "project name")
    private String[] newProject;

    @Option(names = {"-v", "--verbose"}, description = "verbose logging")
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        if(projectLocation == null) projectLocation = new File(System.getProperty("user.dir"));

        if(!projectLocation.exists()) {
            System.out.println("directory to create does not exist: " + projectLocation);
            return -1;
        }

        if(newProject == null || newProject.length != 1 || !VAR_PATTERN.matcher(newProject[0]).matches()) {
            System.out.println("Please enter a valid new project name without spaces");
            return -1;
        }

        try {
            createNewProject(Paths.get(projectLocation.toString()), newProject[0], cppMain, platform, System.out::println);
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
     * @param suppPlat the platform as a SupportedPlatform.
     * @throws IOException when the directory and files are not properly created
     */
    private static void createNewProject(Path location, String newProject, boolean cppMain,
                                         SupportedPlatform suppPlat, Consumer<String> logger) throws IOException {
        var platforms = new PluginEmbeddedPlatformsImpl();
        var configurationStorage = new PrefsConfigurationStorage();
        var platform = platforms.getEmbeddedPlatformFromId(suppPlat.toString());

        logger.accept(String.format("Creating directory %s in %s", newProject, location));
        var dir = Paths.get(location.toString(), newProject);
        Files.createDirectory(dir);

        var cppExt = platforms.isMbed(platform) || cppMain;
        var sketch = dir.resolve(newProject + (cppExt ? ".cpp" : ".ino"));
        logger.accept(String.format("Creating main project code file: %s", sketch));
        Files.writeString(sketch, platforms.isMbed(platform) ? DEFAULT_MBED_SKETCH_STRING : DEFAULT_ARDUINO_SKETCH_STRING);

        var projectEmf = dir.resolve(newProject + ".emf");
        logger.accept(String.format("Creating basic EMF file: %s\n", sketch));

        var recursiveNaming = configurationStorage.isDefaultRecursiveNamingOn();
        var saveToSrc = configurationStorage.isDefaultSaveToSrcOn();

        var persistor = new FileBasedProjectPersistor();
        var  tree = new MenuTree();
        persistor.save(projectEmf.toString(), tree, new CodeGeneratorOptions(platform.getBoardId(), null, null, null, null,
                List.of(), UUID.randomUUID(), newProject, recursiveNaming, saveToSrc, cppMain));
        logger.accept("Project created!");
    }
}
