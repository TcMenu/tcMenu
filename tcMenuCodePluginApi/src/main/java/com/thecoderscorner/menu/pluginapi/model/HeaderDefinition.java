/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model;

import java.util.Objects;

/**
 * Used internally by the variable builder to store header requirements.
 */
public class HeaderDefinition {
    public static final int PRIORITY_MIN = 0;
    public static final int PRIORITY_NORMAL = 5;
    public static final int PRIORITY_MAX = 10;
    private final String headerName;
    private final char startQuote, endQuote;
    private final int priority;

    public HeaderDefinition(String headerName, boolean useQuotes, int priority) {
        this.headerName = headerName;
        this.priority = priority;

        if (useQuotes) {
            startQuote = endQuote = '\"';
        } else {
            startQuote = '<';
            endQuote = '>';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeaderDefinition that = (HeaderDefinition) o;
        return Objects.equals(headerName, that.headerName);
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerName);
    }

    public String getHeaderCode() {
        return "#include " + startQuote + headerName + endQuote;
    }
}