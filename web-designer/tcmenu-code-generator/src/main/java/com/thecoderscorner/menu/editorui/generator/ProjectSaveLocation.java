package com.thecoderscorner.menu.editorui.generator;

/**
 * Indicates to the code generator where the source and generated source should be generated (or where to find it)
 */
public enum ProjectSaveLocation {
    /** All files including generated to the src directory */
    ALL_TO_SRC,
    /** All files including generated to the current directory */
    ALL_TO_CURRENT,
    /** All editable project files to current, generated non-editable files to generated */
    PROJECT_TO_CURRENT_WITH_GENERATED,
    /** All editable project files to src, generated non-editable files to generated */
    PROJECT_TO_SRC_WITH_GENERATED,
    /** The project_menu file contains all plugins along menu code and setup */
    ONE_SINGLE_FILE,
    /** The project_menu file contains all plugins, but menu in main */
    ONE_SINGLE_FILE_MENU_MAIN
}
