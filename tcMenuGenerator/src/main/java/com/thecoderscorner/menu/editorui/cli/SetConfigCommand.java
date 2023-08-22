package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.MenuEditorConfig;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "set-config")
public class SetConfigCommand implements Callable<Integer> {

    @Option(names = {"-p", "--param"}, required = true, description = "the parameter to set")
    private GetConfigCommand.ConfigParameter param;

    @Option(names = {"-v", "--value"}, required = true, description = "the new value for the parameter")
    private String value;

    @Override
    public Integer call() throws Exception {
        MenuEditorApp.configureBundle(Locale.getDefault());

        var appContext = new AnnotationConfigApplicationContext(MenuEditorConfig.class);
        var storage = appContext.getBean(ConfigurationStorage.class);

        switch (param) {
            case EXTRA_PLUGIN_PATHS -> {
                storage.setAdditionalPluginPaths(Arrays.asList(value.split("\\s[,;]\\s")));
                System.out.println("Paths list changed to: " + storage.getAdditionalPluginPaths());
            }
            case ARDUINO_DIR -> {
                if (!Files.exists(Path.of(value))) {
                    System.out.println(value + " does not exist, exiting");
                    return -1;
                }
                storage.setArduinoOverrideDirectory(value);
                System.out.println("Arduino directory changed to: " + storage.getArduinoOverrideDirectory());
            }
            case LIBS_DIR -> {
                if (!Files.exists(Path.of(value))) {
                    System.out.println(value + " does not exist, exiting");
                    return -1;
                }
                storage.setArduinoLibrariesOverrideDirectory(value);
                System.out.println("Arduino libraries directory changed to: " + storage.getArduinoLibrariesOverrideDirectory());
            }
            case RECURSIVE_NAMING -> {
                storage.setDefaultRecursiveNamingOn(Boolean.parseBoolean(value));
                System.out.println("Recursive naming by default changed to: " + storage.isDefaultRecursiveNamingOn());
            }
            case SAVE_TO_SRC -> {
                storage.setDefaultSaveToSrcOn(Boolean.parseBoolean(value));
                System.out.println("Save to src folder by default changed to: " + storage.isDefaultSaveToSrcOn());
            }
            case PLUGIN_VERSIONS -> System.out.println("Set config cannot yet update plugins to specific versions, please use the UI");
        }
        return 0;
    }
}
