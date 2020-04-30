/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.applicability;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;

import java.util.List;

public class AlwaysApplicable implements CodeApplicability {
    @Override
    public boolean isApplicable(List<CreatorProperty> properties) {
        return true;
    }

    @Override
    public String toString() {
        return "AlwaysApplicable";
    }
}
