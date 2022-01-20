/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;

import java.util.regex.Pattern;

public class CodeParameter {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(.*)\\/([^\\/]*)\\/");
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

        String varStr = variableName.toString();
        String varMatch = null;

        // if this expansion has a regex on the result in the form ${VAR/regex/}
        var regexSection = VARIABLE_PATTERN.matcher(varStr);
        if (regexSection.matches() && regexSection.groupCount() == 2)
        {
            varStr = regexSection.group(1);
            varMatch = regexSection.group(2);
        }

        var varName = varStr;
        var varValue = context.getProperties().stream()
                .filter(prop -> prop.getName().equals(varName))
                .map(CreatorProperty::getLatestValue)
                .findFirst().orElse("");

        if(varValue.length() > 0)
        {
            // if there is a regex on the variable, then we apply that and take the first group.
            if (varMatch != null)
            {
                var propMatch = Pattern.compile(varMatch).matcher(varValue);
                if (propMatch.matches() && propMatch.groupCount() > 0)
                {
                    varValue = propMatch.group(1);
                }
            }
            sb.append(varValue);
        }
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
