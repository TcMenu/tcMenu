/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;

import java.util.List;

public class RequiredSourceFile {
    private final String fileName;
    private final List<CodeReplacement> replacementList;
    private final CodeApplicability applicability;

    public RequiredSourceFile(String fileName, List<CodeReplacement> replacementList, CodeApplicability applicability) {
        this.fileName = fileName;
        this.replacementList = replacementList;
        this.applicability = applicability;
    }

    public String getFileName() {
        return fileName;
    }

    public List<CodeReplacement> getReplacementList() {
        return replacementList;
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
