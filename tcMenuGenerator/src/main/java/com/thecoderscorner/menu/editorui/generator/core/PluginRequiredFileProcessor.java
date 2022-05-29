package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredSourceFile;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredZipFile;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
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

    public void dealWithRequiredPlugins(List<CodePluginItem> generators, Path directory, Path projectHome, List<String> previousPluginFiles) throws TcMenuConversionException {
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
            generatePluginsForCreator(gen, directory, projectHome);
        }
    }

    protected void generatePluginsForCreator(CodePluginItem item, Path directory, Path projectHome) throws TcMenuConversionException {
        var expando = new CodeParameter(CodeParameter.NO_TYPE, null, true, "");
        var filteredSourceFiles = item.getRequiredSourceFiles().stream()
                .filter(sf -> sf.getApplicability().isApplicable(context.getProperties())).toList();

        for (var srcFile : filteredSourceFiles) {
            try {
                var fileName = expando.expandExpression(context, srcFile.getFileName());
                // get the source (either from the plugin or from the tcMenu library)
                String fileNamePart;
                byte[] fileDataBytes;
                Path location = item.getConfig().getPath().resolve(fileName);
                try (var sourceInputStream = new FileInputStream(location.toFile())) {
                    fileDataBytes = sourceInputStream.readAllBytes();
                    fileNamePart = Paths.get(fileName).getFileName().toString();
                } catch (Exception e) {
                    throw new TcMenuConversionException("Unable to locate file in plugin: " + srcFile, e);
                }

                Path resolvedOutputFile = directory.resolve(fileNamePart);

                if(!srcFile.isOverwritable() && Files.exists(resolvedOutputFile)) {
                    uiLogger.accept(WARNING, "Source file " + srcFile.getFileName() + " already exists and overwrite is false, skipping");
                } else if(srcFile instanceof RequiredZipFile zipFile) {
                    try(var stream = new ByteArrayInputStream(fileDataBytes)) {
                        var destDir = projectHome.resolve(expando.expandExpression(context, zipFile.getDest()));
                        if(zipFile.isCleanFirst() && Files.exists(destDir)) {
                            uiLogger.accept(INFO, "Clean zip dest directory " + destDir + " for " + zipFile.getFileName());
                            var processFailed = Files.walk(destDir)
                                    .sorted(Comparator.reverseOrder())
                                    .map(Path::toFile)
                                    .map(File::delete)
                                    .anyMatch(retVal -> !retVal);
                            if(processFailed) {
                                uiLogger.accept(ERROR, "Failed to clean the zip dest directory " + destDir + " for " + zipFile.getFileName());
                            }
                        }
                        Files.createDirectories(destDir);
                        uiLogger.accept(INFO, "Extract zip into dest directory " + destDir + " for " + zipFile.getFileName());
                        OnlineLibraryVersionDetector.extractFilesFromZip(destDir, stream);
                    }
                } else {
                    uiLogger.accept(INFO, "Copy plugin file: " + srcFile.getFileName());
                    String fileData = new String(fileDataBytes);
                    for (var cr : srcFile.getReplacementList()) {
                        if (cr.getApplicability().isApplicable(context.getProperties())) {
                            uiLogger.accept(DEBUG, "Plugin file replacement: " + cr.getFind() + " to " + cr.getReplace());
                            var replacement = StringHelper.escapeRex(expando.expandExpression(context, cr.getReplace()));
                            fileData = new String(fileDataBytes, StandardCharsets.UTF_8).replaceAll(cr.getFind(), replacement);
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
