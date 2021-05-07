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
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.dialog.*;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.ui.DefaultCodeGeneratorRunner;
import com.thecoderscorner.menu.editorui.generator.ui.GenerateCodeDialog;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import com.thecoderscorner.menu.editorui.util.PluginUpgradeTask;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class CurrentProjectEditorUIImpl implements CurrentProjectEditorUI {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private LibraryVersionDetector versionDetector;
    private final String homeDirectory;
    private final Stage mainStage;
    private final CodePluginManager manager;
    private final EmbeddedPlatforms platforms;
    private final ArduinoLibraryInstaller installer;
    private final ConfigurationStorage configStore;

    public CurrentProjectEditorUIImpl(CodePluginManager manager, Stage mainStage, EmbeddedPlatforms platforms,
                                      ArduinoLibraryInstaller installer, ConfigurationStorage storage,
                                      LibraryVersionDetector versionDetector, String home) {
        this.manager = manager;
        this.mainStage = mainStage;
        this.platforms = platforms;
        this.installer = installer;
        this.configStore = storage;
        this.versionDetector = versionDetector;
        this.homeDirectory = home;
    }

    @Override
    public Optional<String> findFileNameFromUser(Optional<Path> initialDir, boolean open) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a Menu File");
        initialDir.ifPresentOrElse(
                dir -> fileChooser.setInitialDirectory(new File(dir.toString())),
                () -> installer.getArduinoDirectory().ifPresent(path-> fileChooser.setInitialDirectory(path.toFile()))
        );

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
    public Optional<String> findFileNameFromUser(boolean open) {
        return findFileNameFromUser(Optional.empty(), open);
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
    public void showSplashScreen() {
        logger.log(INFO, "Showing splash screen");
        new SplashScreenDialog(mainStage, this, true);
    }

    @Override
    public void showCreateProjectDialog(CurrentEditorProject editorProject) {
        logger.log(INFO, "Create project dialog show");
        new NewProjectDialog(mainStage, configStore, platforms, editorProject, true);
    }

    @Override
    public void showAboutDialog(ArduinoLibraryInstaller installer) {
        logger.log(INFO, "Showing about dialog");
        new AboutDialog(configStore, mainStage, installer, true);
    }

    @Override
    public void showCodeGeneratorDialog(CurrentEditorProject project, ArduinoLibraryInstaller installer) {
        logger.log(INFO, "Start - show code generator dialog");
        if(!project.isFileNameSet()) {
            this.alertOnError("No filename set", "Please set a filename to continue");
            return;
        }

        try {
            DefaultCodeGeneratorRunner codeGeneratorRunner = new DefaultCodeGeneratorRunner(project, platforms);
            GenerateCodeDialog dialog = new GenerateCodeDialog(manager, this, project, codeGeneratorRunner, platforms);
            dialog.showCodeGenerator(mainStage, true);
        }
        catch (Exception ex) {
            logger.log(ERROR, "Did not present code generator", ex);
            this.alertOnError("Failed to open generator", "Please check that the code plugins are installed and the project is valid");
        }

        logger.log(INFO, "End - show code generator dialog");
    }

    @Override
    public void browseToURL(String urlToVisit) {
        SafeNavigator.safeNavigateTo(urlToVisit);
    }

    @Override
    public void showGeneralSettings() {
        var updater = new PluginUpgradeTask(manager, installer, versionDetector);
        var settingsDialog = new GeneralSettingsDialog(mainStage, configStore, versionDetector, installer, manager, updater, homeDirectory);
    }

    public Optional<UIMenuItem> createPanelForMenuItem(MenuItem menuItem, MenuTree tree, VariableNameGenerator generator,
                                                       BiConsumer<MenuItem, MenuItem> changeConsumer) {
        logger.log(INFO, "creating new panel for menu item editing " + menuItem.getId());
        RenderingChooserVisitor renderingChooserVisitor = new RenderingChooserVisitor(changeConsumer, tree, generator);
        var ret = MenuItemHelper.visitWithResult(menuItem, renderingChooserVisitor);
        ret.ifPresent(uiMenuItem -> logger.log(INFO, "created panel " + uiMenuItem.getClass().getSimpleName()));
        return ret;
    }

    class RenderingChooserVisitor extends AbstractMenuItemVisitor<UIMenuItem> {

        private final BiConsumer<MenuItem,MenuItem> changeConsumer;
        private final MenuIdChooser menuIdChooser;
        private VariableNameGenerator nameGenerator;

        RenderingChooserVisitor(BiConsumer<MenuItem, MenuItem> changeConsumer, MenuTree tree, VariableNameGenerator nameGenerator) {
            this.changeConsumer = changeConsumer;
            this.menuIdChooser = new MenuIdChooserImpl(tree);
            this.nameGenerator = nameGenerator;
        }

        @Override
        public void visit(AnalogMenuItem item) {
            setResult(new UIAnalogMenuItem(item, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(EditableTextMenuItem item) {
            setResult(new UITextMenuItem(item, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(EditableLargeNumberMenuItem item) {
            setResult(new UILargeNumberMenuItem(item, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(EnumMenuItem item) {
            setResult(new UIEnumMenuItem(item, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(BooleanMenuItem item) {
            setResult(new UIBooleanMenuItem(item, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(FloatMenuItem item) {
            setResult(new UIFloatMenuItem(item, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(ActionMenuItem item) {
            setResult(new UIActionMenuItem(item, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(RuntimeListMenuItem listItem) {
            setResult(new UIRuntimeListMenuItem(listItem, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(ScrollChoiceMenuItem scrollItem) {
            setResult(new UIScrollChoiceMenuItem(scrollItem, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(Rgb32MenuItem rgbItem) {
            setResult(new UIRgb32MenuItem(rgbItem, menuIdChooser, nameGenerator, changeConsumer));
        }

        @Override
        public void visit(SubMenuItem item) {
            if(!MenuTree.ROOT.equals(item)) {
                setResult(new UISubMenuItem(item, menuIdChooser, nameGenerator, changeConsumer));
            }
        }
    }
}
