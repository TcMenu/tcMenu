package com.thecoderscorner.menu.editorui.generator.ejava;

import java.util.regex.Pattern;

public class GeneratedJavaField implements GeneratedStatement {
    private static final String END_OF_FIELDS_TEXT = "Auto generated menu fields end here. Add your own fields after here. Please do not remove this line.";
    public static final String END_OF_FIELDS_COMMENT = "// " + END_OF_FIELDS_TEXT;
    private static final Pattern END_OF_FIELDS_BLOCK_PATTERN = Pattern.compile(".*" + END_OF_FIELDS_TEXT + ".*");
    private final boolean ifMissingOnly;
    private final String fieldName;
    private final String fieldType;
    private final boolean makeFinal;

    public GeneratedJavaField(String fieldType, String fieldName) {
        this(fieldType, fieldName, true, false);
    }

    public GeneratedJavaField(String fieldType, String fieldName, boolean makeFinal, boolean ifMissingOnly) {
        this.ifMissingOnly = ifMissingOnly;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.makeFinal = makeFinal;
    }

    public void generate(JavaClassBuilder cb, StringBuilder sb) {
         sb.append(String.format("%sprivate %s%s %s;", cb.indentIt(), makeFinal ? "final " : "", fieldType, fieldName));
    }

    @Override
    public boolean isPatchable() {
        return ifMissingOnly;
    }

    @Override
    public Pattern getMatchingPattern(JavaClassBuilder cb) {
        return Pattern.compile("\\s*private.*" + fieldType + ".*" + fieldName + ".*");
    }

    @Override
    public Pattern getEndOfBlockPattern() {
        return END_OF_FIELDS_BLOCK_PATTERN;
    }
}
