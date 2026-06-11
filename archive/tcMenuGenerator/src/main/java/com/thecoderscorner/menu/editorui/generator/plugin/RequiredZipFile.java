package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;

import java.util.List;

public class RequiredZipFile extends RequiredSourceFile {
    private final String dest;
    private final boolean cleanFirst;

    public RequiredZipFile(String fileName, List<CodeReplacement> replacementList, CodeApplicability applicability, String dest, boolean cleanFirst) {
        super(fileName, replacementList, applicability, true);
        this.dest = dest;
        this.cleanFirst = cleanFirst;
    }

    public String getDest() {
        return dest;
    }

    public boolean isCleanFirst() {
        return cleanFirst;
    }
}
