/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;

public class MenuTreeWithCodeOptions {
    public enum RoundTripMode { DIRECTORY_IN_BROWSER, EMF_FILE_DRAGGED_IN, NEW_PROJECT, JAVA_LOCAL }
    private final MenuTree menuTree;
    private final String description;
    private final CodeGeneratorOptions options;
    private final RoundTripMode roundTripMode;
    private final String projectFileName;

    public MenuTreeWithCodeOptions(MenuTree menuTree, CodeGeneratorOptions options, String description) {
        this(menuTree, options, description, RoundTripMode.JAVA_LOCAL, null);
    }
    public MenuTreeWithCodeOptions(MenuTree menuTree, CodeGeneratorOptions options, String description, RoundTripMode roundTripMode) {
        this(menuTree, options, description, roundTripMode, null);
    }
    public MenuTreeWithCodeOptions(MenuTree menuTree, CodeGeneratorOptions options, String description, RoundTripMode roundTripMode, String projectFileName) {
        this.menuTree = menuTree;
        this.description = description;
        this.options = options;
        this.roundTripMode = roundTripMode;
        this.projectFileName = projectFileName;
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

    public RoundTripMode getRoundTripMode() {
        return roundTripMode;
    }

    public String getProjectFileName() {
        return projectFileName;
    }
}
