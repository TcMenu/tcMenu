package com.thecoderscorner.menu.editorui.generator.logger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GeneratedFile {
    public static GeneratedFile always(Path filepath, String content) {
        return new GeneratedFile(filepath.toString(), content, true);
    }

    private String fileName;
    private String content;
    private boolean alwaysOverwrite;
}
