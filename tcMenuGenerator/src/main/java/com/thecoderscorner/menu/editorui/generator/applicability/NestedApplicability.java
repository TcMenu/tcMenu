/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.applicability;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.List;

public class NestedApplicability implements CodeApplicability {
    public enum NestingMode { OR, AND }
    private final NestingMode nestingMode;
    private final List<CodeApplicability> applicabilityList;

    public NestedApplicability(NestingMode nestingMode, List<CodeApplicability> applicabilityList) {
        this.nestingMode = nestingMode;
        this.applicabilityList = applicabilityList;
    }

    @Override
    public boolean isApplicable(List<CreatorProperty> properties)
    {
        var ret = (nestingMode == NestingMode.AND) ? true : false;

        if (nestingMode == NestingMode.AND)
        {
            for (var applicability : applicabilityList) ret = ret && applicability.isApplicable(properties);
        }
        else
        {
            for (var applicability : applicabilityList) ret = ret || applicability.isApplicable(properties);
        }
        return ret;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder(255);
        sb.append("Nested Applicability ");
        sb.append(nestingMode);
        sb.append(" [");
        for (var applicability : applicabilityList)
        {
            sb.append(applicability.toString());
            sb.append(' ');
        }
        sb.append("]");
        return sb.toString();
    }
}
