/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import java.util.Objects;

/**
 * Used internally by the variable builder to store header requirements.
 */
public class HeaderDefinition {
    public static final int PRIORITY_MIN = 100;
    public static final int PRIORITY_NORMAL = 50;
    public static final int PRIORITY_MAX = 0;
    private final String headerName;
    private final boolean inSource;
    private final int priority;

    public HeaderDefinition(String headerName, boolean inSrc, int priority) {
        this.headerName = headerName;
        this.priority = priority;
        this.inSource = inSrc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeaderDefinition that = (HeaderDefinition) o;
        return Objects.equals(headerName, that.headerName) && Objects.equals(inSource, that.inSource);
    }

    public int getPriority() {
        return priority;
    }

    public String getHeaderName() {
        return headerName;
    }

    public boolean isInSource() {
        return inSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerName, inSource);
    }

    @Override
    public String toString() {
        return "HeaderDefinition{" +
                "headerName='" + headerName + '\'' +
                ", inSrc=" + inSource +
                ", priority=" + priority +
                '}';
    }
}