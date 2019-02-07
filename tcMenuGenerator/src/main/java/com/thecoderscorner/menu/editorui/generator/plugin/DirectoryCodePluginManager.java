package com.thecoderscorner.menu.editorui.generator.plugin;

import com.google.gson.Gson;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.System.Logger.Level;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class loads the code generator plugins into memory and stores the
 */
public class DirectoryCodePluginManager implements CodePluginManager {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private volatile Map<String, Image> imagesLoaded = new HashMap<>();
    private volatile List<CodePluginConfig> configurationsLoaded = new ArrayList<>();

    @Override
    public void loadPlugins(String sourceDir) throws Exception {

        Files.list(Paths.get(sourceDir)).filter(p->p.toString().endsWith(".jar")).forEach(p->{
            try(ZipInputStream zipFile = new ZipInputStream(new FileInputStream(p.toFile()))) {
                ZipEntry entry;
                while((entry = zipFile.getNextEntry()) != null) {
                    if(entry.getName().equals("tcmenu/tcmenu-plugin.json")) {
                        logger.log(Level.INFO, "Found configuration file " + entry.getName());
                        CodePluginConfig config = parsePluginConfig(zipFile);
                        configurationsLoaded.add(config);
                    }
                    else if(entry.getName().matches("tcmenu/.*\\.(png|jpg)")) {
                        logger.log(Level.INFO, "Found image file " + entry.getName());
                        Image img = new Image(zipFile);
                        logger.log(Level.INFO, "Image loaded, Width: {0}, height: {1}", img.getWidth(), img.getHeight());
                        imagesLoaded.put(entry.getName(), img);
                    }
                }
            }
            catch(Exception e) {
                logger.log(Level.ERROR, "Unable to load JAR file", e);
            }
        });

        loadModules(sourceDir, configurationsLoaded.stream()
                .map(CodePluginConfig::getModuleName)
                .collect(Collectors.toList()));
    }

    @Override
    public List<CodePluginConfig> getLoadedPlugins() {
        return Collections.unmodifiableList(configurationsLoaded);
    }

    @Override
    public Optional<Image> getImageForName(String imageName) {
        return Optional.ofNullable(imagesLoaded.get(imageName));
    }

    @Override
    public List<CodePluginItem> getPluginsThatMatch(EmbeddedPlatform platform, SubSystem subSystem) {
        return configurationsLoaded.stream()
                .flatMap(module -> module.getPlugins().stream())
                .filter(item -> item.getApplicability().contains(platform) && item.getSubsystem() == subSystem)
                .collect(Collectors.toList());
    }

    /**
     * Load all the modules that were configured into the main class loader, we don't want any additional class
     * loaders, we just want to load the classes in.
     * @param sourceDir the directory where the modules exist
     * @param moduleNames the names of all the modules to load
     */
    private void loadModules(String sourceDir, Collection<String> moduleNames) {
        ModuleFinder finder = ModuleFinder.of(Paths.get(sourceDir));
        ClassLoader scl = ClassLoader.getSystemClassLoader();
        ModuleLayer parent = ModuleLayer.boot();
        Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), moduleNames);
        ModuleLayer.defineModules(cf, List.of(), (m)->scl);
    }

    /**
     * Create a plugin config object from JSON format.
     * @param inputStream the stream containing the JSON file.
     * @return the config object
     */
    private CodePluginConfig parsePluginConfig(InputStream inputStream) {
        Gson gson = new Gson();
        CodePluginConfig config = gson.fromJson(new InputStreamReader(inputStream), CodePluginConfig.class);
        logger.log(Level.INFO, "Loaded configuration " + config);
        return config;
    }

}
