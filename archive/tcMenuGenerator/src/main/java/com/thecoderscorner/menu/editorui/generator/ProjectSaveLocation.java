package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.MenuEditorApp;

/**
 * Indicates to the code generator where the source and generated source should be generated (or where to find it)
 */
public enum ProjectSaveLocation {
    /** All files including generated to the src directory */
    ALL_TO_SRC("app.info.save.gen.loc2"),
    /** All files including generated to the current directory */
    ALL_TO_CURRENT("app.info.save.gen.loc1"),
    /** All editable project files to current, generated non-editable files to generated */
    PROJECT_TO_CURRENT_WITH_GENERATED("app.info.save.gen.loc3"),
    /** All editable project files to src, generated non-editable files to generated */
    PROJECT_TO_SRC_WITH_GENERATED("app.info.save.gen.loc4");


    private final String bundleName;

    ProjectSaveLocation(String bundleName) {
        this.bundleName = bundleName;
    }

    @Override
    public String toString() {
        if(MenuEditorApp.getBundle() != null) {
            return MenuEditorApp.getBundle().getString(bundleName);
        } else {
            return bundleName;
        }
    }
}
