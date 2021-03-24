/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;

import java.util.Objects;

/**
 * Used internally by the variable builder to store header requirements.
 */
public class HeaderDefinition {
    public enum HeaderType { SOURCE, GLOBAL, CPP_FILE, CPP_SRC_FILE, FONT }
    public static final int PRIORITY_MIN = 100;
    public static final int PRIORITY_NORMAL = 50;
    public static final int PRIORITY_MAX = 0;
    private final String headerName;
    private final HeaderType headerType;
    private final int priority;
    private final CodeApplicability applicability;

    public HeaderDefinition(String headerName, HeaderType headerType, int priority, CodeApplicability applicability) {
        this.headerName = headerName;
        this.priority = priority;
        this.headerType = headerType;
        this.applicability = applicability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeaderDefinition that = (HeaderDefinition) o;
        return Objects.equals(headerName, that.headerName) && headerType == that.headerType;
    }

    public int getPriority() {
        return priority;
    }

    public String getHeaderName() {
        return headerName;
    }

    public HeaderType getHeaderType() {
        return headerType;
    }

    public CodeApplicability getApplicability() {
        return applicability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerName, headerType);
    }

    @Override
    public String toString() {
        return "HeaderDefinition{" +
                "headerName='" + headerName + '\'' +
                ", type=" + headerType +
                ", priority=" + priority +
                '}';
    }
}