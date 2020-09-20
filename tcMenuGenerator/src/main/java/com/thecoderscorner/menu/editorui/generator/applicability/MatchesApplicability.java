/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.applicability;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;

import java.util.Collection;
import java.util.regex.Pattern;

public class MatchesApplicability implements CodeApplicability {
    private final String propertyId;
    private final Pattern compiledMatch;

    public MatchesApplicability(String propertyId, String match)
    {
        this.propertyId = propertyId;
        this.compiledMatch = Pattern.compile(match);
    }

    @Override
    public boolean isApplicable(Collection<CreatorProperty> properties)
    {
        var prop = properties.stream().filter(p -> p.getName().equals(propertyId))
            .map(CreatorProperty::getLatestValue)
            .findFirst().orElse("");

        return compiledMatch.matcher(prop).matches();
    }

    @Override
    public String toString() {
        return "MatchesApplicability{" +
                "propertyId='" + propertyId + '\'' +
                ", compiledMatch=" + compiledMatch +
                '}';
    }
}
