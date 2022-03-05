package com.thecoderscorner.menu.editorui.generator.ejava;

public record RegularStringStatement(String text) implements GeneratedStatement {
    @Override
    public void generate(JavaClassBuilder cb, StringBuilder sb) {
        sb.append(cb.indentIt()).append(text);
    }
}
