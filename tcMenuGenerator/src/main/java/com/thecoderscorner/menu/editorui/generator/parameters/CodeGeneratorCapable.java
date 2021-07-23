package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;

import java.util.Optional;

public interface CodeGeneratorCapable {
    Optional<String> generateCode();
    Optional<String> generateGlobal();
    Optional<HeaderDefinition> generateHeader();
}
