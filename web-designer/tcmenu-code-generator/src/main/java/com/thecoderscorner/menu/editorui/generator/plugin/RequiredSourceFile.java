/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;

import java.util.List;

public class RequiredSourceFile {
    private final String fileName;
    private final List<CodeReplacement> replacementList;
    private final CodeApplicability applicability;
    private final boolean overwritable;
    private final String content;

    public RequiredSourceFile(String fileName, List<CodeReplacement> replacementList, CodeApplicability applicability, boolean overwrite) {
        this.fileName = fileName;
        this.replacementList = replacementList;
        this.applicability = applicability;
        this.overwritable = overwrite;
        this.content = null;
    }

    public RequiredSourceFile(String fileName, String content, List<CodeReplacement> replacementList, boolean overwrite) {
        this.fileName = fileName;
        this.replacementList = replacementList;
        this.applicability = new AlwaysApplicable();
        this.overwritable = overwrite;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isPrepopulated() { return content != null; }

    public String getContent() { return content; }

    public List<CodeReplacement> getReplacementList() {
        return replacementList;
    }

    public boolean isOverwritable() {
        return overwritable;
    }

    public CodeApplicability getApplicability() { return applicability; }

    @Override
    public String toString() {
        return "RequiredSourceFile{" +
                "fileName='" + fileName + '\'' +
                ", replacementList=" + replacementList +
                ", applicability=" + applicability +
                '}';
    }
}
