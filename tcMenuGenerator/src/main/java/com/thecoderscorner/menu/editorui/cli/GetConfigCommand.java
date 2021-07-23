package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "get-config")
public class GetConfigCommand implements Callable<Integer> {
   public enum ConfigParameter {
       EXTRA_PLUGIN_PATHS,
       RECURSIVE_NAMING,
       SAVE_TO_SRC,
       ARDUINO_DIR,
       LIBS_DIR,
       PLUGIN_VERSIONS
   }

    @Option(names = {"-p", "--param"}, required = true, description = "One of EXTRA_PLUGIN_PATHS, RECURSIVE_NAMING, SAVE_TO_SRC, ARDUINO_DIR, LIBS_DIR, PLUGIN_VERSIONS")
    ConfigParameter param;

    @Override
    public Integer call() throws Exception {
        var storage = new PrefsConfigurationStorage();
        switch (param) {
            case EXTRA_PLUGIN_PATHS -> System.out.println("Paths: " + storage.getAdditionalPluginPaths());
            case ARDUINO_DIR -> System.out.println("Arduino directory: " + storage.getArduinoOverrideDirectory().orElse("Not Set"));
            case LIBS_DIR -> System.out.println("Arduino libraries directory: " + storage.getArduinoLibrariesOverrideDirectory().orElse("Not Set"));
            case RECURSIVE_NAMING -> System.out.println("Recursive naming by default: " + storage.isDefaultRecursiveNamingOn());
            case SAVE_TO_SRC -> System.out.println("Save to src folder by default: " + storage.isDefaultSaveToSrcOn());
            case PLUGIN_VERSIONS -> printAllPluginVersions(storage);
        }
        return 0;
    }

    static void printAllPluginVersions(ConfigurationStorage storage) {
        var loader = new DefaultXmlPluginLoader(new PluginEmbeddedPlatformsImpl(), storage, true);
        loader.loadPlugins();
        if(loader.getLoadErrors().isEmpty()) {
            loader.getLoadedPlugins().forEach(pl ->
                    System.out.format("Plugin %s - Version %s by %s\n", pl.getModuleName(), pl.getVersion(), pl.getVendor()));
        }
        else {
            System.out.println("Errors occurred while loading plugins");
            System.out.println(String.join(CoreCodeGenerator.LINE_BREAK, loader.getLoadErrors()));
            System.out.println();
        }
    }
}
