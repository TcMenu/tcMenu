package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Implementation that generates suitable dialogs for most common dialogs. Makes testing much easier.
 */
public interface DialogFactory {
    Optional<MenuItem> showNewItemDialog(Stage stage, MenuTree tree);

    void showRomLayoutDialog(Stage stage, MenuTree tree);

    void showAboutDialog(Stage stage, ArduinoLibraryInstaller installer);
}
