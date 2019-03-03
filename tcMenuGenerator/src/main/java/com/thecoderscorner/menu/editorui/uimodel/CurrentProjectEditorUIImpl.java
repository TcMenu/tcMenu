/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.dialog.AboutDialog;
import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.dialog.RomLayoutDialog;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.ui.DefaultCodeGeneratorRunner;
import com.thecoderscorner.menu.editorui.generator.ui.GenerateCodeDialog;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class CurrentProjectEditorUIImpl implements CurrentProjectEditorUI {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private Stage mainStage;
    private CodePluginManager manager;
    private EmbeddedPlatforms platforms;

    public CurrentProjectEditorUIImpl(CodePluginManager manager, Stage mainStage, EmbeddedPlatforms platforms) {
        this.manager = manager;
        this.mainStage = mainStage;
        this.platforms = platforms;
    }

    @Override
    public Optional<String> findFileNameFromUser(boolean open) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a Menu File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Embedded menu", "*.emf"));
        File f;
        if (open) {
            f = fileChooser.showOpenDialog(mainStage);
        } else {
            f = fileChooser.showSaveDialog(mainStage);
        }

        if (f != null) {
            return Optional.of(f.getPath());
        }
        return Optional.empty();
    }

    @Override
    public void alertOnError(String heading, String description) {
        logger.log(ERROR, "Show error with heading: {0}, description: {1}", heading, description);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(heading);
        alert.setHeaderText(heading);
        alert.setContentText(description);
        alert.showAndWait();
    }

    @Override
    public boolean questionYesNo(String title, String header) {
        logger.log(INFO, "Showing question for confirmation title: {0}, header: {1}", title, header);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    @Override
    public void setTitle(String s) {
        mainStage.setTitle(s);
    }

    @Override
    public Optional<MenuItem> showNewItemDialog(MenuTree tree) {
        logger.log(INFO, "Showing new item dialog");
        NewItemDialog dlg = new NewItemDialog(mainStage, tree, this, true);
        return dlg.getResultOrEmpty();
    }

    @Override
    public void showRomLayoutDialog(MenuTree tree) {
        logger.log(INFO, "Showing rom layout dialog");
        new RomLayoutDialog(mainStage, tree, false);
    }

    @Override
    public void showAboutDialog(ArduinoLibraryInstaller installer) {
        logger.log(INFO, "Showing about dialog");
        AboutDialog ad = new AboutDialog(mainStage, installer, true);
    }

    @Override
    public void showCodeGeneratorDialog(CurrentEditorProject project, ArduinoLibraryInstaller installer) {
        logger.log(INFO, "Start - show code generator dialog");
        if(!project.isFileNameSet()) {
            this.alertOnError("No filename set", "Please set a filename to continue");
            return;
        }

        DefaultCodeGeneratorRunner codeGeneratorRunner = new DefaultCodeGeneratorRunner(project, platforms);
        GenerateCodeDialog dialog = new GenerateCodeDialog(manager, this, project, codeGeneratorRunner, platforms);
        dialog.showCodeGenerator(mainStage, true);

        logger.log(INFO, "End - show code generator dialog");
    }

    @Override
    public void browseToURL(String urlToVisit) {
        try {
            Desktop.getDesktop().browse(new URI(urlToVisit));
        } catch (IOException | URISyntaxException e) {
            // not much we can do here really!
            logger.log(ERROR, "Could not open browser", e);
        }

    }


    public Optional<UIMenuItem> createPanelForMenuItem(MenuItem menuItem, MenuTree tree, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        logger.log(INFO, "creating new panel for menu item editing " + menuItem.getId());
        RenderingChooserVisitor renderingChooserVisitor = new RenderingChooserVisitor(changeConsumer, tree);
        var ret = MenuItemHelper.visitWithResult(menuItem, renderingChooserVisitor);
        ret.ifPresent(uiMenuItem -> logger.log(INFO, "created panel " + uiMenuItem.getClass().getSimpleName()));
        return ret;
    }

    class RenderingChooserVisitor extends AbstractMenuItemVisitor<UIMenuItem> {

        private final BiConsumer<MenuItem,MenuItem> changeConsumer;
        private final MenuIdChooser menuIdChooser;

        RenderingChooserVisitor(BiConsumer<MenuItem, MenuItem> changeConsumer, MenuTree tree) {
            this.changeConsumer = changeConsumer;
            this.menuIdChooser = new MenuIdChooserImpl(tree);
        }

        @Override
        public void visit(AnalogMenuItem item) {
            setResult(new UIAnalogMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(TextMenuItem item) {
            setResult(new UITextMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(EnumMenuItem item) {
            setResult(new UIEnumMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(BooleanMenuItem item) {
            setResult(new UIBooleanMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(RemoteMenuItem item) {
            setResult(new UIRemoteMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(FloatMenuItem item) {
            setResult(new UIFloatMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(ActionMenuItem item) {
            setResult(new UIActionMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(SubMenuItem item) {
            if(!MenuTree.ROOT.equals(item)) {
                setResult(new UISubMenuItem(item, menuIdChooser, changeConsumer));
            }
        }
    }
}
