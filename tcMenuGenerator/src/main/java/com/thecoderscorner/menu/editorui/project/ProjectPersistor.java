/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PersistedMenu;

import java.io.IOException;
import java.util.List;

/**
 * This class takes all file operations from the CurrentEditorProject to avoid the class being directly
 * attached to any given persistor, and also makes unit testing easier.
 */
public interface ProjectPersistor {

    /**
     * Opens a file into a project from disk.
     *
     * @param fileName the file to be loaded
     * @return a combination of the menu tree and project options
     * @throws IOException if something goes wrong while loading
     */
    MenuTreeWithCodeOptions open(String fileName) throws IOException;

    /**
     * Saves a project file to disk with the specified filename
     *
     * @param fileName name of the file to save to
     * @param desc     description for the file
     * @param tree     the tree to be saved
     * @param options  the code generator options
     * @param handler  the locale handler to ensure any locale specifics are saved too
     * @throws IOException if there is a problem saving.
     */
    void save(String fileName, String desc, MenuTree tree, CodeGeneratorOptions options, LocaleMappingHandler handler) throws IOException;

    List<PersistedMenu> copyTextToItems(String items);

    String itemsToCopyText(MenuItem startingPoint, MenuTree tree);
}