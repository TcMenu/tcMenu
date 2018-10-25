package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import javafx.stage.Stage;

import java.util.Optional;

public class DialogFactoryImpl implements DialogFactory {
    @Override
    public Optional<MenuItem> showNewItemDialog(Stage stage, MenuTree tree) {
        NewItemDialog dlg = new NewItemDialog(stage, tree);
        return dlg.showAndWait();
    }

    @Override
    public void showRomLayoutDialog(Stage stage, MenuTree tree) {
        RomLayoutDialog rld = new RomLayoutDialog(stage, tree);
        rld.showAndWait();
    }

    @Override
    public void showAboutDialog(Stage stage, ArduinoLibraryInstaller installer) {
        AboutDialog ad = new AboutDialog(stage, installer);
        ad.showAndWait();
    }
}
