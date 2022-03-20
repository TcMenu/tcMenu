package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredSourceFile;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class PluginRequiredFileProcessor {
    private final  CodeConversionContext context;
    private final BiConsumer<System.Logger.Level, String> uiLogger;

    public PluginRequiredFileProcessor(CodeConversionContext context, BiConsumer<System.Logger.Level, String> uiLogger) {
        this.context = context;
        this.uiLogger = uiLogger;
    }

    public void dealWithRequiredPlugins(List<CodePluginItem> generators, Path directory, List<String> previousPluginFiles) throws TcMenuConversionException {
        uiLogger.accept(INFO, "Checking if any plugin files need removal because of plugin changes");

        var newPluginFileSet = generators.stream()
                .flatMap(gen -> gen.getRequiredSourceFiles().stream())
                .filter(sf -> sf.getApplicability().isApplicable(context.getProperties()))
                .map(RequiredSourceFile::getFileName)
                .collect(Collectors.toSet());

        for (var plugin : previousPluginFiles) {
            if (!newPluginFileSet.contains(plugin)) {
                var fileNamePart = Paths.get(plugin).getFileName().toString();
                var actualFile = directory.resolve(fileNamePart);
                try {
                    if (Files.exists(actualFile)) {
                        uiLogger.accept(WARNING, "Removing unused plugin: " + actualFile);
                        Files.delete(actualFile);
                    }
                } catch (IOException e) {
                    uiLogger.accept(ERROR, "Could not delete plugin: " + actualFile + " error " + e.getMessage());
                }
            }
        }

        uiLogger.accept(INFO, "Adding all files required by selected plugins");

        for (var gen : generators) {
            generatePluginsForCreator(gen, directory);
        }
    }

    protected void generatePluginsForCreator(CodePluginItem item, Path directory) throws TcMenuConversionException {
        var expando = new CodeParameter(CodeParameter.NO_TYPE, null, true, "");
        var filteredSourceFiles = item.getRequiredSourceFiles().stream()
                .filter(sf-> sf.getApplicability().isApplicable(context.getProperties()))
                .collect(Collectors.toList());

        for (var srcFile : filteredSourceFiles) {
            try {
                var fileName = expando.expandExpression(context, srcFile.getFileName());
                // get the source (either from the plugin or from the tcMenu library)
                String fileNamePart;
                String fileData;
                Path location = item.getConfig().getPath().resolve(fileName);
                try (var sourceInputStream = new FileInputStream(location.toFile())) {
                    fileData = new String(sourceInputStream.readAllBytes());
                    fileNamePart = Paths.get(fileName).getFileName().toString();
                } catch (Exception e) {
                    throw new TcMenuConversionException("Unable to locate file in plugin: " + srcFile, e);
                }

                Path resolvedOutputFile = directory.resolve(fileNamePart);

                if(!srcFile.isOverwritable() && Files.exists(resolvedOutputFile)) {
                    uiLogger.accept(WARNING, "Source file " + srcFile.getFileName() + " already exists and overwrite is false, skipping");
                }
                else {
                    uiLogger.accept(INFO, "Copy plugin file: " + srcFile.getFileName());
                    for (var cr : srcFile.getReplacementList()) {
                        if (cr.getApplicability().isApplicable(context.getProperties())) {
                            uiLogger.accept(DEBUG, "Plugin file replacement: " + cr.getFind() + " to " + cr.getReplace());
                            var replacement = StringHelper.escapeRex(expando.expandExpression(context, cr.getReplace()));
                            fileData = fileData.replaceAll(cr.getFind(), replacement);
                        }
                    }

                    Files.write(resolvedOutputFile, fileData.getBytes(), TRUNCATE_EXISTING, CREATE);
                }

                // and copy into the destination
            } catch (Exception e) {
                throw new TcMenuConversionException("Unexpected exception processing " + srcFile, e);
            }
        }
    }

}