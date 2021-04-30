package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.controller.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.generator.core.NameAndKey;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;

@Command(name="generate")
public class CodeGeneratorCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-f", "--emf-file"}, description = "emf file name")
    private File projectFile;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "verbose logging")
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        if(projectFile == null || !projectFile.exists()) {
            System.out.println("Chosen emf file does not exist");
            return -1;
        }

        System.out.format("Starting code generator for %s", projectFile.toString());

        var persistor = new FileBasedProjectPersistor();
        var project = persistor.open(projectFile.getAbsolutePath());
        var platforms = new PluginEmbeddedPlatformsImpl();
        var embeddedPlatform =  platforms.getEmbeddedPlatformFromId(project.getOptions().getEmbeddedPlatform());
        var codeGen = platforms.getCodeGeneratorFor(embeddedPlatform, project.getOptions());
        var prefsStore = new PrefsConfigurationStorage();

        System.out.println("Loading plugins");

        DefaultXmlPluginLoader loader = new DefaultXmlPluginLoader(platforms, prefsStore);
        List<CodePluginItem> plugins = new ArrayList<>();
        plugins.add(loader.loadPlugin(project.getOptions().getLastInputUuid()));
        plugins.add(loader.loadPlugin(project.getOptions().getLastDisplayUuid()));
        plugins.add(loader.loadPlugin(project.getOptions().getLastRemoteCapabilitiesUuid()));
        if(project.getOptions().getLastThemeUuid() != null) {
            plugins.add(loader.loadPlugin(project.getOptions().getLastRemoteCapabilitiesUuid()));
        }

        System.out.format("Executing code generator");

        var saveSrc = project.getOptions().isSaveToSrc();
        var nameAndKey = new NameAndKey(
                project.getOptions().getApplicationUUID().toString(),
                project.getOptions().getApplicationName()
        );
        var location = Paths.get(projectFile.getParent());
        codeGen.setLoggerFunction((level, s) -> {
            System.out.format("Gen: %s: %s", level, s);
        });
        codeGen.startConversion(location, plugins, project.getMenuTree(), nameAndKey, Collections.emptyList(), saveSrc);

        return 0;
    }
}
