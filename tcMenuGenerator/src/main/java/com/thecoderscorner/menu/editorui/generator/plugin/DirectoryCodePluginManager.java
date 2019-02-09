package com.thecoderscorner.menu.editorui.generator.plugin;

import com.google.gson.Gson;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
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

import static java.lang.System.Logger.Level.ERROR;

/**
 * This class loads the code generator plugins into memory and stores the
 */
public class DirectoryCodePluginManager implements CodePluginManager {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private Map<String, Image> imagesLoaded = new HashMap<>();
    private List<CodePluginConfig> configurationsLoaded = new ArrayList<>();
    private Map<CodePluginItem, CodePluginConfig> itemToConfig = new HashMap<>();
    private ModuleLayer layer;

    @Override
    public synchronized void loadPlugins(String sourceDir) throws Exception {
        itemToConfig.clear();
        configurationsLoaded.clear();
        imagesLoaded.clear();

        Files.list(Paths.get(sourceDir)).filter(p->p.toString().endsWith(".jar")).forEach(p->{
            try(ZipInputStream zipFile = new ZipInputStream(new FileInputStream(p.toFile()))) {
                ZipEntry entry;
                while((entry = zipFile.getNextEntry()) != null) {
                    if(entry.getName().equals("META-INF/tcmenu/tcmenu-plugin.json")) {
                        logger.log(Level.INFO, "Found configuration file " + entry.getName());
                        CodePluginConfig config = parsePluginConfig(zipFile);
                        configurationsLoaded.add(config);
                        config.getPlugins().forEach(item -> itemToConfig.put(item, config));
                    }
                    else if(entry.getName().matches("META-INF/tcmenu/.*\\.(png|jpg)")) {
                        logger.log(Level.INFO, "Found image file " + entry.getName());
                        Image img = new Image(zipFile);
                        logger.log(Level.INFO, "Image loaded, Width: {0}, height: {1}", img.getWidth(), img.getHeight());
                        imagesLoaded.put(entry.getName().substring(16), img);
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
    public synchronized List<CodePluginConfig> getLoadedPlugins() {
        return Collections.unmodifiableList(configurationsLoaded);
    }

    @Override
    public synchronized Optional<Image> getImageForName(String imageName) {
        return Optional.ofNullable(imagesLoaded.get(imageName));
    }

    @Override
    public synchronized List<CodePluginItem> getPluginsThatMatch(EmbeddedPlatform platform, SubSystem subSystem) {
        return configurationsLoaded.stream()
                .flatMap(module -> module.getPlugins().stream())
                .filter(item -> item.getApplicability().contains(platform) && item.getSubsystem() == subSystem)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CodePluginConfig> getPluginConfigForItem(CodePluginItem item) {
        return Optional.ofNullable(itemToConfig.get(item));
    }

    @SuppressWarnings("unchecked")
    public synchronized Optional<EmbeddedCodeCreator> makeCreator(CodePluginItem item) {
        try {
            Class<EmbeddedCodeCreator> clazz = (Class<EmbeddedCodeCreator>)
                    layer.findLoader(itemToConfig.get(item).getModuleName()).loadClass(item.getCodeCreatorClass());

            return Optional.of(clazz.getConstructor().newInstance());
        } catch (Exception e) {
            System.getLogger("CodePlugin").log(ERROR, "Plugin Class did not load " + item.getDescription(), e);
            return Optional.empty();
        }
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
        layer = parent.defineModulesWithOneLoader(cf, scl);
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
