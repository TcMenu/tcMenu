package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredSourceFile;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredZipFile;
import com.thecoderscorner.menu.editorui.util.ZipUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.PROJECT_TO_CURRENT_WITH_GENERATED;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.PROJECT_TO_SRC_WITH_GENERATED;

public class PluginRequiredFileProcessor extends PluginFileProcessor {
    private final ProjectSaveLocation saveLocation;
    private List<RequiredSourceFile> allSrcFiles;

    public PluginRequiredFileProcessor(CodeConversionContext context, UserFeedbackLogger logger, ProjectSaveLocation saveLocation,
                                       List<RequiredSourceFile> allSrcFiles) {
        super(context, logger);
        this.saveLocation = saveLocation;
        this.allSrcFiles = allSrcFiles;
    }

    public void dealWithRequiredPlugins(List<CodePluginItem> generators, Path directory, Path projectHome, ProjectSaveLocation psl,
                                        List<String> previousPluginFiles) throws TcMenuConversionException {
        logger.info("Checking if any plugin files need removal because of plugin changes");

        var newPluginFileSet = getAllApplicableFilesToProcess(allSrcFiles);

        for (var plugin : previousPluginFiles) {
            if (!newPluginFileSet.contains(plugin)) {
                var fileNamePart = Paths.get(plugin).getFileName().toString();
                var actualFile = directory.resolve(fileNamePart);
                try {
                    if (Files.exists(actualFile)) {
                        logger.warn("Removing unused plugin: " + actualFile);
                        Files.delete(actualFile);
                    }
                } catch (IOException e) {
                    logger.error("Could not delete plugin: " + actualFile + " error ", e);
                }
            }
        }

        logger.info("Adding all files required by selected plugins");

        for (var gen : generators) {
            generatePluginsForCreator(gen, directory, projectHome);
        }
    }

    protected void internalProcessPluginFile(String fileNamePart, RequiredSourceFile srcFile, byte[] fileDataBytes,
                                             Path directory, Path projectHome) throws IOException {
        Path resolvedOutputFile = directory;
        if (isGeneratedDirOn(saveLocation) && srcFile.isOverwritable()) {
            resolvedOutputFile = resolvedOutputFile.resolve("generated");
        }
        resolvedOutputFile = resolvedOutputFile.resolve(fileNamePart);
        if (!srcFile.isOverwritable() && Files.exists(resolvedOutputFile)) {
            logger.warn("Source file " + srcFile.getFileName() + " already exists and overwrite is false, skipping");
        } else if (srcFile instanceof RequiredZipFile zipFile) {
            try (var stream = new ByteArrayInputStream(fileDataBytes)) {
                var destDir = projectHome.resolve(expando.expandExpression(context, zipFile.getDest()));
                if (zipFile.isCleanFirst() && Files.exists(destDir)) {
                    logger.info("Clean zip dest directory " + destDir + " for " + zipFile.getFileName());
                    boolean processFailed;
                    try (var destDirStream = Files.walk(destDir)) {
                        processFailed = destDirStream.sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .map(File::delete)
                                .anyMatch(retVal -> !retVal);

                    }
                    if (processFailed) {
                        logger.error("Failed to clean the zip dest directory " + destDir + " for " + zipFile.getFileName());
                    }
                }
                Files.createDirectories(destDir);
                logger.info("Extract zip into dest directory " + destDir + " for " + zipFile.getFileName());
                ZipUtils.extractFilesFromZip(destDir, stream);
            }
        } else {
            emitPluginFileToDir(srcFile, fileDataBytes, resolvedOutputFile);
        }
    }

    private boolean isGeneratedDirOn(ProjectSaveLocation psl) {
        return psl == PROJECT_TO_CURRENT_WITH_GENERATED || psl == PROJECT_TO_SRC_WITH_GENERATED;
    }

}
