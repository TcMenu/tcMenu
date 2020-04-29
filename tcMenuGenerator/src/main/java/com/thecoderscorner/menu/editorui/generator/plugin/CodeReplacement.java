/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;

public class CodeReplacement {
    private final String find;
    private final String replace;
    private final CodeApplicability applicability;

    public CodeReplacement(String find, String replace, CodeApplicability applicability) {
        this.find = find;
        this.replace = replace;
        this.applicability = applicability;
    }

    public String getFind() {
        return find;
    }

    public String getReplace() {
        return replace;
    }

    public CodeApplicability getApplicability() {
        return applicability;
    }
}
