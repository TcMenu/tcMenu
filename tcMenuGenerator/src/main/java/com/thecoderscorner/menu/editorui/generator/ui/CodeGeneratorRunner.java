package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import javafx.stage.Stage;

import java.util.List;

public interface CodeGeneratorRunner {
    void startCodeGeneration(Stage stage, EmbeddedPlatform platform, String  path, List<EmbeddedCodeCreator> creators);
}
