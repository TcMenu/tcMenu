package com.thecoderscorner.menu.editorui.generator.ejava;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;

public class GeneratedJavaMethod implements GeneratedStatement {
    public enum GenerationMode { CONSTRUCTOR_IF_MISSING, CONSTRUCTOR_REPLACE, METHOD_REPLACE, METHOD_IF_MISSING }
    private static final String END_OF_METHODS_RAW_TEXT = "Auto generated menu callbacks end here. Please do not remove this line or change code after it.";
    public static final String END_OF_METHODS_TEXT = "// " + END_OF_METHODS_RAW_TEXT;
    private final static Pattern END_OF_METHOD_BLOCK = Pattern.compile(".*" + END_OF_METHODS_RAW_TEXT + ".*");

    private final GenerationMode genMode;
    private final String returnType;
    private final String name;
    private Optional<String> methodAnnotation = Optional.empty();
    private final List<String> params = new ArrayList<>();
    private final List<StringWithIndent> statements = new ArrayList<>();

    public GeneratedJavaMethod(GenerationMode mode) {
        this.genMode = mode;
        this.name = "";
        this.returnType = "";
        if(this.genMode != GenerationMode.CONSTRUCTOR_IF_MISSING && genMode != GenerationMode.CONSTRUCTOR_REPLACE) {
            throw new IllegalArgumentException("GeneratedJavaMethod constructor version used with non constructor");
        }
    }

    public GeneratedJavaMethod(GenerationMode mode, String returnType, String name) {
        this.genMode = mode;
        this.returnType = returnType;
        this.name = name;
    }

    public GenerationMode getMethodType() {
        return genMode;
    }

    public GeneratedJavaMethod withStatement(String statement) {
        statements.add(new StringWithIndent(statement, 0));
        return this;
    }

    public GeneratedJavaMethod withStatement(String statement, int indentAdjustment) {
        statements.add(new StringWithIndent(statement, indentAdjustment));
        return this;
    }

    public GeneratedJavaMethod withParameter(String param) {
        params.add(param);
        return this;
    }

    public GeneratedJavaMethod withAnnotation(String annotation) {
        this.methodAnnotation = Optional.of(annotation);
        return this;
    }

    public boolean isIfMissingOnly() {
        return genMode == GenerationMode.CONSTRUCTOR_IF_MISSING || genMode == GenerationMode.METHOD_IF_MISSING;
    }

    public String getName() {
        return name;
    }

    public void generate(JavaClassBuilder cb, StringBuilder sb) {
        var allArguments = String.join(", ", params);
        if(genMode ==  GenerationMode.CONSTRUCTOR_IF_MISSING || genMode == GenerationMode.CONSTRUCTOR_REPLACE) {
            sb.append(String.format("%spublic %s(%s) {", cb.indentIt(), cb.getClazzName(), allArguments));
        } else {
            methodAnnotation.ifPresent(ann -> sb.append(cb.indentIt()).append("@").append(ann).append(LINE_BREAK));
            sb.append(String.format("%spublic %s %s(%s) {", cb.indentIt(), returnType, name, allArguments));
        }
        sb.append(LINE_BREAK);

        cb.setIndentation(cb.getIndentation() + 1);
        for(var stmt : statements) {
            if(stmt.indentAdjust != 0) cb.setIndentation(cb.getIndentation() + stmt.indentAdjust());
            sb.append(cb.indentIt()).append(stmt.text()).append(LINE_BREAK);
        }
        cb.setIndentation(cb.getIndentation() - 1);
        sb.append(cb.indentIt()).append("}").append(LINE_BREAK);
    }

    @Override
    public boolean isPatchable() {
        return true;
    }

    @Override
    public Pattern getMatchingPattern(JavaClassBuilder cb) {
        if(genMode == GenerationMode.CONSTRUCTOR_IF_MISSING || genMode == GenerationMode.CONSTRUCTOR_REPLACE) {
            return Pattern.compile(".*public " + cb.getClazzName() + "\\(.*");
        } else {
            return Pattern.compile(".*public " + returnType + " " + name + "\\(.*");
        }
    }

    @Override
    public Pattern getEndOfBlockPattern() {
        return END_OF_METHOD_BLOCK;
    }

    record StringWithIndent(String text, int indentAdjust) {}
}
