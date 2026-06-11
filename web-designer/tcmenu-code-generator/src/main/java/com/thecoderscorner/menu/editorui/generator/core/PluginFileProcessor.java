package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.logger.GeneratedFile;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
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
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public abstract class PluginFileProcessor {
    protected final CodeConversionContext context;
    protected final UserFeedbackLogger logger;
    protected final CodeParameter expando = new CodeParameter(CodeParameter.NO_TYPE, null, true, "");

    public PluginFileProcessor(CodeConversionContext context, UserFeedbackLogger logger) {
        this.context = context;
        this.logger = logger;
    }

    public abstract void dealWithRequiredPlugins(List<CodePluginItem> generators,
                                        Path directory, Path projectHome, ProjectSaveLocation psl,
                                        List<String> previousPluginFiles) throws TcMenuConversionException;

    protected Set<String> getAllApplicableFilesToProcess(List<RequiredSourceFile> srcFiles) {
        return srcFiles.stream()
                .filter(sf -> sf.getApplicability().isApplicable(context.getProperties()))
                .map(RequiredSourceFile::getFileName)
                .collect(Collectors.toSet());
    }

    protected void generatePluginsForCreator(CodePluginItem item, Path directory, Path projectHome) throws TcMenuConversionException {
        var filteredSourceFiles = item.getRequiredSourceFiles().stream()
                .filter(sf -> sf.getApplicability().isApplicable(context.getProperties())).toList();
        for (var srcFile : filteredSourceFiles) {
            try {
                var fileName = expando.expandExpression(context, srcFile.getFileName());
                // get the source (either from the plugin or from the tcMenu library)
                String fileNamePart;
                byte[] fileDataBytes;

                if (srcFile.isPrepopulated()) {
                    fileDataBytes = srcFile.getContent().getBytes();
                    fileNamePart = fileName;
                } else {
                    Path location = item.getConfig().getPath().resolve(fileName);
                    try (var sourceInputStream = new FileInputStream(location.toFile())) {
                        fileDataBytes = sourceInputStream.readAllBytes();
                        fileNamePart = Paths.get(fileName).getFileName().toString();
                    } catch (Exception e) {
                        throw new TcMenuConversionException("Unable to locate file in plugin: " + srcFile, e);
                    }
                }
                internalProcessPluginFile(fileNamePart, srcFile, fileDataBytes, directory, projectHome);
            } catch (Exception e) {
                throw new TcMenuConversionException("Unexpected exception processing " + srcFile, e);
            }
        }
    }

    protected void emitPluginFileToDir(RequiredSourceFile srcFile, byte[] fileDataBytes, Path resolvedOutputFile) throws IOException {
        logger.info("Copy plugin file: " + srcFile.getFileName());
        String fileData = processFileDataToString(srcFile, fileDataBytes);

        Files.writeString(resolvedOutputFile, fileData, TRUNCATE_EXISTING, CREATE);
        logger.fileModificiation(new GeneratedFile(resolvedOutputFile.toString(), fileData, srcFile.isOverwritable()));
    }


    protected String processFileDataToString(RequiredSourceFile srcFile, byte[] fileDataBytes) {
        String fileData = new String(fileDataBytes);
        for (var cr : srcFile.getReplacementList()) {
            if (cr.getApplicability().isApplicable(context.getProperties())) {
                logger.debug("Plugin file replacement: " + cr.getFind());
                if (srcFile.isPrepopulated()) {
                    // in new content prepopulated files we don't need to deal with expressions like below.
                    fileData = fileData.replace(cr.getFind(), cr.getReplace());
                } else {
                    var replacement = StringHelper.escapeRex(expando.expandExpression(context, cr.getReplace()));
                    fileData = fileData.replaceAll(cr.getFind(), replacement);
                }
            }
        }
        return fileData;
    }

    protected abstract void internalProcessPluginFile(String fileNamePart, RequiredSourceFile srcFile, byte[] fileDataBytes,
                                             Path directory, Path projectHome) throws IOException;
}
