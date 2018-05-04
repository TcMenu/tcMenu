/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.state.MenuTree;

import java.io.IOException;

public interface ProjectPersistor {

    MenuTree open(String fileName) throws IOException;
    void save(String fileName, MenuTree tree) throws IOException;
}
