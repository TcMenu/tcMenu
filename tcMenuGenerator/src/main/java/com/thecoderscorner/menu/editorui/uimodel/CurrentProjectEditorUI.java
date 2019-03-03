/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * An interface that provides helper methods around displaying alert dialogs, setting display titles and rendering a
 * suitable dialog or editor pane for value object.
 *
 * This helps with decoupling of the components and facilities more accurate and easier testing as well.
 */
public interface CurrentProjectEditorUI {
    /**
     * Get a file name from the user, either for Open or Save.
     * @param open true if open, false if save.
     * @return the filename or empty.
     */
    Optional<String> findFileNameFromUser(boolean open);

    /**
     * Presents an error dialog to the user.
     * @param heading the title and heading text
     * @param description the description text
     */
    void alertOnError(String heading, String description);

    /**
     * Presents a question to the user and waits for the response
     * @param title the questions title
     * @param header the main body text
     * @return true if confirmed, otherwise false.
     */
    boolean questionYesNo(String title, String header);

    /**
     * Sets the title of the main window, decoupled from main window to prevent
     * non UI classes knowing about the UI.
     * @param s the new title
     */
    void setTitle(String s);

    /**
     * Creates a suitable menu editor panel for the main editor area of the main window.
     * @param menuItem the menu item to edit.
     * @param tree the tree it belongs to
     * @param changeConsumer the consumer that will deal with valid changes in the item.
     * @return either a suitable editor or empty.
     */
    Optional<UIMenuItem> createPanelForMenuItem(MenuItem menuItem, MenuTree tree, BiConsumer<MenuItem, MenuItem> changeConsumer);

    /**
     * Show the new item dialog for the given stage with a given menu tree.
     * @param tree the tree to use to determine the next ID
     * @return either a new item or empty
     */
    Optional<MenuItem> showNewItemDialog(MenuTree tree);

    /**
     * Shows a dialog that presents the range of ID's that are used and also the EEPROM
     * values that are in use. It will highlight any overlapping EEPROM values.
     * @param tree the tree to use to show the ranges of ID and EEPROM values.
     */
    void showRomLayoutDialog(MenuTree tree);

    /**
     * Shows the About dialog, which also shows all the version info
     * @param installer the installer object that knows about Arduino libraries
     */
    void showAboutDialog(ArduinoLibraryInstaller installer);

    /**
     * Shows the code generator dialog that can be used to build native Arduino code.
     * @param editorProject the project that is currently active in the editor
     * @param installer the arduino installer object.
     */
    void showCodeGeneratorDialog(CurrentEditorProject editorProject, ArduinoLibraryInstaller installer);

    /**
     * Browses to a URL using the default external browser
     * @param urlToVisit the URL to be visited
     */
    void browseToURL(String urlToVisit);
}
