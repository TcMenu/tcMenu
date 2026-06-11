package com.thecoderscorner.menu.editorui.generator.ejava;

import java.util.regex.Pattern;

public interface GeneratedStatement {
    void generate(JavaClassBuilder cb, StringBuilder sb);

    default boolean isPatchable() {
        return false;
    }

    default Pattern getMatchingPattern(JavaClassBuilder cb) {
        throw new IllegalStateException("Called getMatchingPattern on un-patchable class");
    }

    default Pattern getEndOfBlockPattern() {
        throw new IllegalStateException("Called getEndOfBlockPattern on un-patchable class");
    }
}
