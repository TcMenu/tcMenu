package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredSourceFile;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredZipFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AllInOnePluginFileProcessor extends PluginFileProcessor {
    private final Pattern GENERATED_FOR_PATTERN_SRC  = Pattern.compile("// Each plugin's code now follows here.*");
    private final Pattern GENERATED_FOR_PATTERN_HDR  = Pattern.compile("#define MENU_GENERATED_CODE_H");
    private final StringBuilder combinedHeaderData = new StringBuilder(8192);
    private final StringBuilder combinedSourceData = new StringBuilder(32768);
    private final Path headerFile;
    private final Path sourceFile;

    public AllInOnePluginFileProcessor(CodeConversionContext context, UserFeedbackLogger logger,
                                       Path headerFile, Path sourceFile) {
        super(context, logger);
        this.headerFile = headerFile;
        this.sourceFile = sourceFile;
    }

    @Override
    public void dealWithRequiredPlugins(List<CodePluginItem> generators, Path directory, Path projectHome, ProjectSaveLocation psl, List<String> previousPluginFiles) throws TcMenuConversionException {
        logger.info("All-in-one plugin mode combining sources into: " + headerFile.getFileName() + " and source file: " + sourceFile.getFileName());
        for(CodePluginItem generator : generators) {
            generatePluginsForCreator(generator, directory,  projectHome);
        }

        try {
            var headerText = combinedHeaderData.toString();
            if (!headerText.isEmpty()) {
                appendExtraCodeToFileAtPattern(headerText, headerFile, GENERATED_FOR_PATTERN_HDR);
            }

            var sourceText = combinedSourceData.toString();
            if (!sourceText.isEmpty()) {
                appendExtraCodeToFileAtPattern(sourceText, sourceFile, GENERATED_FOR_PATTERN_SRC);
            }
        } catch (IOException e) {
            throw new TcMenuConversionException("Could not add generated code to plugin files", e);
        }
    }

    private void appendExtraCodeToFileAtPattern(String textToAdd, Path sourceFile, Pattern generatedForPatternHdr) throws IOException {
        logger.info("Reopening and adding extra code to " + sourceFile.getFileName());
        var lines = Files.readAllLines(sourceFile);
        var textLines = Pattern.compile("\\R").split(textToAdd);
        int insertAt = -1;

        for (int i = 0; i < lines.size(); i++) {
            if (generatedForPatternHdr.matcher(lines.get(i)).matches()) {
                insertAt = i + 1;
                break;
            }
        }

        if (insertAt == -1) throw new IOException("Could not find generated for pattern in " + sourceFile);

        for (var line : textLines) {
            lines.add(insertAt++, line);
        }
        Files.write(sourceFile, lines);
    }

    @Override
    protected void internalProcessPluginFile(String fileNamePart, RequiredSourceFile srcFile, byte[] fileDataBytes, Path directory, Path projectHome) throws IOException {
        if(!srcFile.isOverwritable()) {
            emitPluginFileToDir(srcFile, fileDataBytes, projectHome.resolve(fileNamePart));
        } else if(srcFile instanceof RequiredZipFile) {
            throw new IOException("Plugins with zip files cannot be used with All-In-One mode");
        } else {
            logger.info("Processing plugin file in all-in-one mode: " + fileNamePart);
            boolean isHeaderFile = fileNamePart.endsWith(".h") || fileNamePart.endsWith(".hpp");
            var builder = isHeaderFile ? combinedHeaderData : combinedSourceData;
            builder.append(System.lineSeparator());
            builder.append("// PLUGIN FILE - ").append(fileNamePart).append(System.lineSeparator());
            String pluginFileData = processFileDataToString(srcFile, fileDataBytes);

            // when in all in one mode, we don't want #include references to the header.
            if(srcFile.getFileName().endsWith(".cpp")) {
                String fileWithoutCpp = fileNamePart.substring(0, fileNamePart.length() - 4);
                pluginFileData = pluginFileData.replace("#include \"" + fileWithoutCpp + ".h\"", "");
            }

            builder.append(pluginFileData).append(System.lineSeparator());
        }
    }

    public static List<HeaderDefinition> filteringHeaderDefinitions(List<HeaderDefinition> headerDefinitions, List<RequiredSourceFile> files) {
        var srcFiles = files.stream()
                .filter(sf -> sf.isOverwritable() && sf.getFileName().endsWith(".h"))
                .map(sf -> Path.of(sf.getFileName()).getFileName().toString())
                .toList();
        var toExclude = headerDefinitions.stream()
                .filter(def -> srcFiles.contains(def.getHeaderName()) && def.getHeaderType() == HeaderDefinition.HeaderType.SOURCE)
                .toList();
        var adjusted = new ArrayList<>(headerDefinitions);
        adjusted.removeAll(toExclude);
        return List.copyOf(adjusted);
    }
}
