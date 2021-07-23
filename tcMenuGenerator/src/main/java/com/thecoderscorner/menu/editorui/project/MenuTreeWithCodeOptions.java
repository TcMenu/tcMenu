/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;

public class MenuTreeWithCodeOptions {
    private final MenuTree menuTree;
    private final String description;
    private final CodeGeneratorOptions options;

    public MenuTreeWithCodeOptions(MenuTree menuTree, CodeGeneratorOptions options, String description) {
        this.menuTree = menuTree;
        this.description = description;
        this.options = options;
    }

    public MenuTree getMenuTree() {
        return menuTree;
    }

    public CodeGeneratorOptions getOptions() {
        return options;
    }

    public String getDescription() {
        return description != null ? description : "";
    }
}
