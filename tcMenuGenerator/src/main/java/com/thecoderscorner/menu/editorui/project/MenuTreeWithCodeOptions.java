/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.state.MenuTree;

public class MenuTreeWithCodeOptions {
    private final MenuTree menuTree;
    private final CodeGeneratorOptions options;

    public MenuTreeWithCodeOptions(MenuTree menuTree, CodeGeneratorOptions options) {
        this.menuTree = menuTree;
        this.options = options;
    }

    public MenuTree getMenuTree() {
        return menuTree;
    }

    public CodeGeneratorOptions getOptions() {
        return options;
    }
}
