/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.BiConsumer;

public interface SketchFileAdjuster {
    /**
     * This method is able to make changes to an INO or CPP main file that follows the approximate format of most
     * sketches to add the needed headers, function calls, and define any shell callback functions that are not already
     * defined. Not thread safe, should be created for each run.
     *
     * @param logger a consumer that handles UI logging
     * @param inoFile the file to be modified
     * @param projectName the project name
     * @param callbacks the list of callbacks.
     * @throws IOException in the event of an error
     */
    void makeAdjustments(BiConsumer<System.Logger.Level, String> logger, Path rootDir, String inoFile, String projectName,
                         Collection<CallbackRequirement> callbacks, MenuTree tree) throws IOException;

    /**
     * Checks if a project main file exists and returns the path for it, if it does not exist it attempts to create it,
     * throwing an IoException if it fails.
     *
     * @param logger the logger to handle all logging operations
     * @param path the directory where the project sources are located
     * @param projectOptions the options for the project
     * @return a path to the main file
     * @throws IOException if it could not be created
     */
    Path createFileIfNeeded(BiConsumer<System.Logger.Level, String> logger, Path path, CodeGeneratorOptions projectOptions) throws IOException;
}
