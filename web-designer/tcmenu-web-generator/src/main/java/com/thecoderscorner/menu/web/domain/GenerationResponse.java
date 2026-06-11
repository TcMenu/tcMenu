package com.thecoderscorner.menu.web.domain;

import com.thecoderscorner.menu.editorui.generator.logger.GeneratedFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class GenerationResponse {
    private boolean successful;
    private List<GeneratedFile> generatedFiles;
    private List<LogEntry> logLines;
    private String buildId;

    public static GenerationResponse badResponse(List<LogEntry> logs) {
        return new GenerationResponse(false, List.of(), logs, null);
    }

    public static GenerationResponse okResponse(List<GeneratedFile> files, List<LogEntry> logs, UUID buildId) {
        return new GenerationResponse(true, sanitiseFilesInOutput(files), logs, buildId.toString());
    }

    private static List<GeneratedFile> sanitiseFilesInOutput(List<GeneratedFile> files) {
        return files.stream()
                .map(gf -> new GeneratedFile(Path.of(gf.getFileName()).getFileName().toString(), gf.getContent(), gf.isAlwaysOverwrite()))
                .toList();
    }

    public GeneratedFile getFileByName(String fileName) {
        return generatedFiles.stream()
                .filter(f -> f.getFileName().endsWith(fileName))
                .findFirst().orElseThrow();
    }
}
