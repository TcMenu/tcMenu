/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;

public class CodeParameter {
    private final String type;
    private final boolean paramUsed;
    private final String value;
    private final String defaultValue;

    public CodeParameter(String type, boolean paramUsed, String value) {
        this.type = type;
        this.paramUsed = paramUsed;
        this.value = value;
        this.defaultValue = null;
    }

    public CodeParameter(String type, boolean paramUsed, String value, String defaultValue) {
        this.type = type;
        this.paramUsed = paramUsed;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public String expandExpression(CodeConversionContext context, String text)
    {
        if (text == null) return "NULL";
        var sb = new StringBuilder();

        int i = 0;
        while(i < text.length())
        {
            boolean escape = false;
            if(text.charAt(i) == '\\' && (i + 1) < text.length())
            {
                escape = true;
                ++i;
            }
            if(text.charAt(i) == '$' && !escape)
            {
                i = ParseVariable(text, ++i, sb, context) + 1;
            }
            else
            {
                sb.append(text.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    private int ParseVariable(String text, int start, StringBuilder sb, CodeConversionContext context)
    {
        if (start >= text.length()) return start;

        StringBuilder variableName = new StringBuilder();
        int i = start;
        if (text.charAt(i) == '{')
        {
            while (++i < text.length() && text.charAt(i) != '}')
            {
                variableName.append(text.charAt(i));
            }
        }
        else
        {
            sb.append(text.charAt(i));
            return start;
        }

        var varStr = variableName.toString();

        var varValue = context.getProperties().stream()
                .filter(prop -> prop.getName().equals(varStr))
                .map(CreatorProperty::getLatestValue)
                .findFirst().orElse("");
        sb.append(varValue);
        return i;
    }

    public String getType() {
        return type;
    }

    public boolean isParamUsed() {
        return paramUsed;
    }

    public String getValue() {
        return value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
