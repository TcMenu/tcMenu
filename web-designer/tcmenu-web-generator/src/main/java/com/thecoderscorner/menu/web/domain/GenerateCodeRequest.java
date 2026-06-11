package com.thecoderscorner.menu.web.domain;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.logger.GeneratedFile;
import com.thecoderscorner.menu.editorui.project.PersistedProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenerateCodeRequest {
    private PersistedProject project;
    private List<CreatorProperty> existingProperties;
    private List<GeneratedFile> requiredFiles;
}
