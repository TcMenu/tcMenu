/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator;

public class CppAndHeader {
    private final String cppText;
    private final String headerText;

    public CppAndHeader(String cppText, String headerText) {
        this.cppText = cppText;
        this.headerText = headerText;
    }

    public String getCppText() {
        return cppText;
    }

    public String getHeaderText() {
        return headerText;
    }
}
