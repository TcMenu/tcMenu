package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Optional;

public interface JavaPluginItem {
    CodeApplicability ALWAYS_APPLICABLE = new AlwaysApplicable();
    CodePluginItem getPlugin();
    List<CreatorProperty> getRequiredProperties();
    List<FunctionDefinition> getFunctions();
    List<HeaderDefinition> getHeaderDefinitions();
    List<RequiredSourceFile> getRequiredSourceFiles();
    List<CodeVariable> getVariables();
    Optional<Image> getImage();
    default void beforeGenerationStarts(CodeConversionContext context) {}
}
