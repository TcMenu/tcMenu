/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.dialog.*;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.ui.DefaultCodeGeneratorRunner;
import com.thecoderscorner.menu.editorui.generator.ui.GenerateCodeDialog;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class CurrentProjectEditorUIImpl implements CurrentProjectEditorUI {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final LibraryVersionDetector versionDetector;
    private final String homeDirectory;
    private final Stage mainStage;
    private final CodePluginManager manager;
    private final EmbeddedPlatforms platforms;
    private final ArduinoLibraryInstaller installer;
    private final ConfigurationStorage configStore;
    private final ResourceBundle designerBundle;
    private CurrentEditorProject editorProject;

    public CurrentProjectEditorUIImpl(CodePluginManager manager, Stage mainStage, EmbeddedPlatforms platforms,
                                      ArduinoLibraryInstaller installer, ConfigurationStorage storage,
                                      LibraryVersionDetector versionDetector, String home, ResourceBundle designerBundle) {
        this.manager = manager;
        this.mainStage = mainStage;
        this.platforms = platforms;
        this.installer = installer;
        this.configStore = storage;
        this.versionDetector = versionDetector;
        this.homeDirectory = home;
        this.designerBundle = designerBundle;
    }

    public void setEditorProject(CurrentEditorProject project) {
        this.editorProject = project;
    }

    @Override
    public Optional<String> findFileNameFromUser(Optional<Path> initialDir, boolean open, String allowedExtensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a Menu File");
        initialDir.ifPresentOrElse(
                dir -> fileChooser.setInitialDirectory(new File(dir.toString())),
                () -> installer.getArduinoDirectory().ifPresent(path-> fileChooser.setInitialDirectory(path.toFile()))
        );

        String allowedExtensionsDesc;
        String allowedExtensionsFmt;
        if(allowedExtensions.contains("|")) {
            var extParts = allowedExtensions.split("\\|");
            allowedExtensionsDesc = extParts[0];
            allowedExtensionsFmt = extParts[1];
        } else {
            allowedExtensionsDesc = allowedExtensionsFmt = allowedExtensions;
        }

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(allowedExtensionsDesc, allowedExtensionsFmt));
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
        return findFileNameFromUser(Optional.empty(), open, designerBundle.getString("menu.file.fmt.name") +  "|*.emf");
    }

    @Override
    public void alertOnError(String heading, String description) {
        logger.log(ERROR, "Show error with heading: {0}, description: {1}", heading, description);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
        heading = decodeBundleIfNeeded(heading);
        alert.setTitle(heading);
        alert.setHeaderText(heading);
        alert.setContentText(decodeBundleIfNeeded(description));
        alert.showAndWait();
    }

    private String decodeBundleIfNeeded(String s) {
        if(s != null && s.startsWith("%") && s.length() > 1) {
            return designerBundle.getString(s.substring(1));
        } else {
            return s;
        }
    }

    @Override
    public boolean questionYesNo(String title, String header) {
        logger.log(INFO, "Showing question for confirmation title: {0}, header: {1}", title, header);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, decodeBundleIfNeeded(header), ButtonType.YES, ButtonType.NO);
        BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
        title = decodeBundleIfNeeded(title);
        alert.setTitle(title);
        alert.setHeaderText(title);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
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
    public void showSplashScreen(Consumer<String> themeChangeListener) {
        logger.log(INFO, "Showing splash screen");
        new SplashScreenDialog(mainStage, this, themeChangeListener, configStore, true);
    }

    @Override
    public void showCreateProjectDialog() {
        logger.log(INFO, "Create project dialog show");
        new NewProjectDialog(mainStage, configStore, platforms, editorProject, true);
    }

    @Override
    public void showAboutDialog(ArduinoLibraryInstaller installer) {
        logger.log(INFO, "Showing about dialog");
        new AboutDialog(configStore, mainStage, true);
    }

    @Override
    public Optional<MenuItem> showSearchDialog(MenuTree tree) {
        logger.log(INFO, "Showing search dialog");
        var d = new SearchMenuItemDialog(tree, mainStage, true);
        return d.getResult();
    }

    @Override
    public void showCodeGeneratorDialog(ArduinoLibraryInstaller installer) {
        logger.log(INFO, "Start - show code generator dialog");
        if(!editorProject.isFileNameSet()) {
            this.alertOnError("%core.no.filename.set", "%core.please.select.file.first");
            throw new IllegalArgumentException("No filename provided");
        }

        try {
            DefaultCodeGeneratorRunner codeGeneratorRunner = new DefaultCodeGeneratorRunner(editorProject, platforms, manager);
            GenerateCodeDialog dialog = new GenerateCodeDialog(manager, this, editorProject, codeGeneratorRunner, platforms);
            dialog.showCodeGenerator(mainStage, true);
        }
        catch (Exception ex) {
            logger.log(ERROR, "Did not present code generator", ex);
            this.alertOnError(designerBundle.getString("core.code.gen.dlg.failure"), designerBundle.getString("core.code.gen.dlg.failure.desc"));
        }

        logger.log(INFO, "End - show code generator dialog");
    }

    @Override
    public CurrentEditorProject getCurrentProject() {
        return editorProject;
    }

    @Override
    public void browseToURL(String urlToVisit) {
        SafeNavigator.safeNavigateTo(urlToVisit);
    }

    @Override
    public void showGeneralSettings() {
        new GeneralSettingsDialog(mainStage, configStore, versionDetector, installer, manager, homeDirectory);
    }

    @Override
    public Optional<EepromDefinition> showEditEEPROMDialog(Optional<EepromDefinition> current) {
        var dlg = new SelectEepromTypeDialog(mainStage, current.orElse(new NoEepromDefinition()), true);
        return dlg.getResultOrEmpty();
    }

    @Override
    public Optional<String> showFontEditorDialog(String currentChoice, boolean tcUnicodeEnabled) {
        var dlg = new ChooseFontDialog(mainStage, currentChoice, tcUnicodeEnabled, true);
        return dlg.getResultOrEmpty();
    }

    public Optional<UIMenuItem<?>> createPanelForMenuItem(MenuItem menuItem, MenuTree tree, VariableNameGenerator generator,
                                                          BiConsumer<MenuItem, MenuItem> changeConsumer) {
        logger.log(INFO, "creating new panel for menu item editing " + menuItem.getId());
        RenderingChooserVisitor renderingChooserVisitor = new RenderingChooserVisitor(changeConsumer, tree, generator);
        var ret = MenuItemHelper.visitWithResult(menuItem, renderingChooserVisitor);
        ret.ifPresent(uiMenuItem -> logger.log(INFO, "created panel " + uiMenuItem.getClass().getSimpleName()));
        return ret;
    }

    static class RenderingChooserVisitor extends AbstractMenuItemVisitor<UIMenuItem<?>> {

        private final BiConsumer<MenuItem,MenuItem> changeConsumer;
        private final MenuIdChooser menuIdChooser;
        private final VariableNameGenerator nameGenerator;

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
        public void visit(CustomBuilderMenuItem customItem) {
            setResult(new UICustomMenuItem(customItem, menuIdChooser, nameGenerator, changeConsumer));
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

    public List<EmbeddedPlatform> getEmbeddedPlatforms() {
        return platforms.getEmbeddedPlatforms();
    }

    @Override
    public void showCreateFontUtility() {
        new CreateFontUtilityDialog(mainStage, this, homeDirectory);
    }

    @Override
    public void showBitmapEditorUtility() {
        new CreateBitmapWidgetToolDialog(mainStage, this, homeDirectory);
    }

    @Override
    public void showLocaleConfiguration(CurrentEditorProject project) {
        if(!project.isFileNameSet()) {
            alertOnError(designerBundle.getString("core.no.filename.set"), designerBundle.getString("core.please.select.file.first"));
            return;
        }

        if(!project.getLocaleHandler().isLocalSupportEnabled()) {
            if(questionYesNo(designerBundle.getString("core.enable.locale.support.message"), designerBundle.getString("core.enable.locale.support.header"))) {
                project.enableLocaleHandler();
            } else {
                return; // do nothing
            }
        }

        if(project.getLocaleHandler() instanceof PropertiesLocaleEnabledHandler handler) {
            new ConfigureLocalesDialog(mainStage, true, handler);
        } else {
            logger.log(ERROR, "Fail safe, locale handler in unexpected bad state");
        }
    }
}
