package com.thecoderscorner.menu.web.domain;

import com.thecoderscorner.menu.editorui.util.ZipUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CodeBuildInfo {
    private final String buildId;
    private final UUID projectUuid;
    private final LocalDateTime buildTime;
    private final GenerationResponse response;
    private final byte[] zipFile;

    public CodeBuildInfo(String buildId, UUID projectUuid, LocalDateTime buildTime, GenerationResponse response, Path outputDir) {
        this.buildId = buildId;
        this.projectUuid = projectUuid;
        this.buildTime = buildTime;
        this.response = response;

        try {
            zipFile = ZipUtils.createZipFileFrom(outputDir);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to zip build output", e);
        }
    }
}
