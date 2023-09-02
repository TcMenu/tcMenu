/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.cli.StartUICommand;
import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.dialog.ChooseIoExpanderDialog;
import com.thecoderscorner.menu.editorui.dialog.EditMenuInMenuDialog;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.project.*;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject.EditorSaveMode;
import com.thecoderscorner.menu.editorui.simui.SimulatorUI;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import com.thecoderscorner.menu.persist.VersionInfo;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.*;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_LIB;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.project.EditedItemChange.Command;
import static com.thecoderscorner.menu.persist.PersistedMenu.TCMENU_COPY_PREFIX;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

@SuppressWarnings({"unused", "rawtypes"})
public class MenuEditorController {
    private final static DataFormat objectDataFormat = new DataFormat("application/x-java-serialized-object");
    private final System.Logger logger = System.getLogger(MenuEditorController.class.getSimpleName());
    public Label statusField;
    public CheckMenuItem darkModeMenuFlag;
    public Label currentEditLabel;
    public MenuButton localeMenuButton;
    private CurrentEditorProject editorProject;
    public javafx.scene.control.MenuItem menuCut;
    public javafx.scene.control.MenuItem menuCopy;
    public javafx.scene.control.MenuItem menuPaste;
    public javafx.scene.control.MenuItem menuUndo;
    public javafx.scene.control.MenuItem menuRedo;
    public javafx.scene.control.Menu menuRecents;
    public javafx.scene.control.Menu menuSketches;
    public javafx.scene.control.MenuItem exitMenuItem;
    public javafx.scene.control.MenuItem aboutMenuItem;
    public javafx.scene.control.MenuItem menuAddItem;
    public javafx.scene.control.MenuItem menuRemoveItem;

    public Menu examplesMenu;
    public BorderPane rootPane;
    public TreeView<MenuItemWithDescription> menuTree;
    public Button menuTreeAdd;
    public Button menuTreeRemove;
    public Button menuTreeCopy;
    public Button menuTreePaste;
    public BorderPane editorBorderPane;
    public MenuBar mainMenu;

    private Optional<UIMenuItem> currentEditor = Optional.empty();
    private Optional<AppInformationPanel> appInfoPanel = Optional.empty();
    private ArduinoLibraryInstaller installer;
    private CurrentProjectEditorUI editorUI;
    private CodePluginManager pluginManager;
    private ConfigurationStorage configStore;
    private LinkedList<RecentlyUsedItem> recentItems = new LinkedList<>();
    private LibraryVersionDetector libVerDetector;
    private int menuToProjectMaxLevels = 1;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ResourceBundle bundle = MenuEditorApp.getBundle();
    private SimulatorUI simulatorUI = null;

    public void initialise(CurrentEditorProject editorProject, ArduinoLibraryInstaller installer,
                           CurrentProjectEditorUI editorUI, CodePluginManager pluginManager,
                           ConfigurationStorage storage,
                           LibraryVersionDetector libraryVersionDetector) {
        this.editorProject = editorProject;
        this.installer = installer;
        this.editorUI = editorUI;
        this.pluginManager = pluginManager;
        this.configStore = storage;
        this.libVerDetector = libraryVersionDetector;

        this.menuTree.setOnKeyPressed(event -> {
            if(event.isShortcutDown() && !event.isShiftDown() && !event.isAltDown()) {
                if(event.getCode() == KeyCode.C) {
                    onTreeCopy(new ActionEvent(menuTree, menuTree));
                } else if(event.getCode() == KeyCode.V) {
                    onTreePaste(new ActionEvent(menuTree, menuTree));
                }
            } else if(event.getCode() == KeyCode.ESCAPE) {
                onFocusCurrentEditor(new ActionEvent(menuTree, menuTree));
            } else if(event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                onRemoveTreeMenu(new ActionEvent(menuTree, menuTree));
            }
        });

        menuTree.setCellFactory(param -> new MenuItemTreeCell(new MenuItemTreeController()));
        menuTree.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            if (newItem != null) {
                onTreeChangeSelection(newItem.getValue().item());
            }
        });

        loadPreferences();

        Platform.runLater(() -> {
            sortOutMenuForMac();
            redrawTreeControl();
            populateAllMenus();
        });

        storage.addArduinoDirectoryChangeListener((ard, lib, libsChanged) -> {
            if(libsChanged) Platform.runLater(this::populateAllMenus);
        });

        menuToProjectMaxLevels = storage.getMenuProjectMaxLevel();

        attemptToLoadLastProject();

        executor.scheduleAtFixedRate(this::checkOnClipboard, 3000, 3000, TimeUnit.MILLISECONDS);
    }

    private void attemptToLoadLastProject() {
        try {
            if(StartUICommand.didUserSelectProject()) {
                editorProject.openProject(StartUICommand.userSelectedProject());
            } else {
                var lastPrj = configStore.getLastLoadedProject();
                if (lastPrj.isPresent()) {
                    editorProject.openProjectWithoutAlert(lastPrj.get());
                }
            }
        } catch (Exception ex) {
            logger.log(ERROR, "Last project didn't load, start fresh", ex);
            configStore.emptyLastLoadedProject();
            editorProject.newProject();
        }
    }

    public void checkOnClipboard() {
        Platform.runLater(() -> menuTreePaste.setDisable(!isClipboardContentValid()));
    }

    private void populateAllMenus() {
        if(configStore.isUsingArduinoIDE()) {
            boolean ok = populateMenu(examplesMenu, installer.findLibraryInstall("tcMenu"), "examples", 0);
            ok = ok && populateMenu(menuSketches, installer.getArduinoDirectory(), "", 0);

            if(!ok) {
                editorUI.alertOnError(
                        bundle.getString("core.arduino.or.libraries.missing"),
                        "Please check the Arduino and libraries directory from menu 'Edit -> General Settings'");
            }
        }
        darkModeMenuFlag.setSelected(BaseDialogSupport.getTheme().equals("darkMode"));
    }

    public CurrentEditorProject getProject() {
        return editorProject;
    }

    private boolean populateMenu(Menu toPopulate, Optional<Path> maybeDir, String subDir, int level) {
        if(maybeDir.isPresent()) {
            toPopulate.getItems().clear();
            Path subResolved = maybeDir.get().resolve(subDir);
            try(var fileStream = Files.list(subResolved)) {
                for(var file : fileStream.toList()) {
                    if (hasEmfFile(file)) {
                        var item = new javafx.scene.control.MenuItem(file.getFileName().toString());
                        item.setOnAction(e -> openFirstEMF(file));
                        toPopulate.getItems().add(item);
                    } else if (Files.isDirectory(file) && level < menuToProjectMaxLevels) {
                        var item = new javafx.scene.control.Menu(file.getFileName().toString());
                        populateMenu(item, Optional.of(subResolved), file.getFileName().toString(), level + 1);
                        if(!item.getItems().isEmpty()) {
                            toPopulate.getItems().add(item);
                        }
                    }
                }
                return true;
            } catch (IOException e) {
                logger.log(ERROR, "Unable to populate menus due to exception", e);
                return false;
            }

        }
        else {
            logger.log(ERROR, "Directory not found");
            return true; // not configured, but not an error
        }
    }

    private boolean hasEmfFile(Path path) {
        try {
            return Files.list(path).anyMatch(p -> p.toString().toUpperCase().endsWith(".EMF"));
        } catch (IOException e) {
            return false;
        }
    }

    private void openFirstEMF(Path path) {
        try {
            stopSimulatorIfNeeded();
            handleRecents();
            Files.list(path)
                    .filter(p -> p.toString().toUpperCase().endsWith(".EMF"))
                    .findFirst()
                    .ifPresent(example -> {
                        editorProject.openProject(example.toString());
                        redrawTreeControl();
                    });
        } catch (IOException e) {
            logger.log(ERROR, "Failed to locate ino in example " + path);
        }
    }

    private void sortOutMenuForMac() {
        final String os = System.getProperty ("os.name");
        if (os != null && os.startsWith ("Mac")) {
            mainMenu.useSystemMenuBarProperty().set(true);
            exitMenuItem.setVisible(false);
        }
    }

    private void onEditorChange(MenuItem original, MenuItem changed) {
        TreeItem<MenuItemWithDescription> selectedItem = menuTree.getSelectionModel().getSelectedItem();
        selectedItem.getValue().setItem(changed);
        menuTree.refresh();
        editorProject.applyCommand(Command.EDIT, changed);
        if(simulatorUI != null) simulatorUI.itemHasChanged(changed);
    }

    public void presentInfoPanel() {
        currentEditLabel.setText(bundle.getString("main.editor.edit.settings"));
        AppInformationPanel panel = new AppInformationPanel(installer, this, pluginManager, editorUI, libVerDetector, configStore);
        editorBorderPane.setCenter(panel.showEmptyInfoPanel());
        currentEditor = Optional.empty();
        appInfoPanel = Optional.of(panel);
    }

    public void onFocusCurrentEditor(ActionEvent actionEvent) {
        if(appInfoPanel.isPresent()) appInfoPanel.get().focusFirst();
        else currentEditor.ifPresent(UIMenuItem::focusFirst);
    }

    public void onTreeChangeSelection(MenuItem newValue) {
        VariableNameGenerator gen = new VariableNameGenerator(
                editorProject.getMenuTree(),
                editorProject.getGeneratorOptions().isNamingRecursive(),
                editorProject.getUncommittedItems()
        );

        editorUI.createPanelForMenuItem(newValue, editorProject.getMenuTree(), gen, this::onEditorChange)
                .ifPresentOrElse((uiMenuItem) -> {
                    ScrollPane scrollPane = new ScrollPane(uiMenuItem.initPanel(editorProject.getMenuTree(), editorProject.getLocaleHandler()));
                    scrollPane.setFitToWidth(true);
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    editorBorderPane.setCenter(scrollPane);
                    currentEditor = Optional.of(uiMenuItem);
                    appInfoPanel = Optional.empty();
                    currentEditLabel.setText(String.format(bundle.getString("main.editor.edit.item.fmt"),
                            newValue.getClass().getSimpleName(), newValue.getId()));
                }, this::presentInfoPanel
        );

        // we cannot modify root.
        var isRoot = MenuTree.ROOT.equals(newValue);
        menuRemoveItem.setDisable(isRoot);
        menuTreeRemove.setDisable(isRoot);
        menuTreeCopy.setDisable(newValue == null);
        menuTreePaste.setDisable(!isClipboardContentValid());
    }

    private boolean isClipboardContentValid() {
        var clipboard = Clipboard.getSystemClipboard();
        return (clipboard.hasContent(DataFormat.PLAIN_TEXT));
    }

    private void redrawTreeControl() {
        TreeItem<MenuItemWithDescription> selectedItem = menuTree.getSelectionModel().getSelectedItem();
        int sel = MenuTree.ROOT.getId();
        if (selectedItem != null && selectedItem.getValue() != null) {
            sel = selectedItem.getValue().item().getId();
        }

        TreeItem<MenuItemWithDescription> rootItem = new TreeItem<>(new MenuItemWithDescription(MenuTree.ROOT));
        rootItem.setExpanded(true);

        SubMenuItem root = MenuTree.ROOT;
        recurseTreeItems(editorProject.getMenuTree().getMenuItems(root), rootItem);
        menuTree.setRoot(rootItem);
        menuTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        menuTree.getSelectionModel().selectFirst();

        if(simulatorUI != null) simulatorUI.itemHasChanged(null);
        selectChildInTreeById(rootItem, sel);

        var items = localeMenuButton.getItems();
        items.clear();
        items.addAll(getLocaleMenuItems());
        if(editorProject.getLocaleHandler().isLocalSupportEnabled()) {
            localeMenuButton.setDisable(false);
            localeMenuButton.setMaxWidth(9999);
            localeMenuButton.setText(bundle.getString("main.editor.lang") + " " + toLang(editorProject.getLocaleHandler().getCurrentLocale()));
        } else {
            localeMenuButton.setDisable(true);
            localeMenuButton.setText(bundle.getString("main.editor.no.locale"));
        }

    }

    private void selectChildInTreeById(TreeItem<MenuItemWithDescription> item, int id) {
        // if we had a selection before the rebuild, honour it
        for (TreeItem<MenuItemWithDescription> child : item.getChildren()) {
            if (child.getValue().item().getId() == id) {
                menuTree.getSelectionModel().select(child);
                return;
            } else if (!child.getChildren().isEmpty()) {
                selectChildInTreeById(child, id);
            }
        }
    }

    private void recurseTreeItems(List<MenuItem> menuItems, TreeItem<MenuItemWithDescription> treeItem) {
        if (menuItems == null) return;

        for (MenuItem item : menuItems) {
            TreeItem<MenuItemWithDescription> child = new TreeItem<>(new MenuItemWithDescription(item));
            if (item.hasChildren()) {
                child.setExpanded(true);
                recurseTreeItems(editorProject.getMenuTree().getMenuItems(item), child);
            }
            treeItem.getChildren().add(child);
        }
    }

    public void aboutMenuPressed(ActionEvent actionEvent) {
        editorUI.showAboutDialog(installer);
    }

    public void onMenuDocumentation(ActionEvent actionEvent) {
        editorUI.browseToURL(LIBRARY_DOCS_URL);
    }

    public void onGettingStarted(ActionEvent actionEvent) {
        editorUI.browseToURL(GETTING_STARTED_PAGE_URL);
    }

    public void onMenuTCCForum(ActionEvent actionEvent) {
        editorUI.browseToURL(TCC_FORUM_PAGE);
    }

    public void onMenuDiscussions(ActionEvent actionEvent) {
        editorUI.browseToURL(GITHUB_DISCUSSION_URL);
    }

    public void onTreeCopy(ActionEvent actionEvent) {
        var selected = menuTree.getSelectionModel().getSelectedItem();
        if(selected == null) return;

        var cp = editorProject.getProjectPersistor().itemsToCopyText(selected.getValue().item(), editorProject.getMenuTree());
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(cp);
        systemClipboard.setContent(content);
    }

    public void onTreePaste(ActionEvent actionEvent) {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        if (systemClipboard.hasContent(DataFormat.PLAIN_TEXT)) {
            var data = systemClipboard.getContent(DataFormat.PLAIN_TEXT);
            if (data == null || !data.toString().startsWith(TCMENU_COPY_PREFIX)) return;
            var items = editorProject.getProjectPersistor().copyTextToItems(data.toString());
            if(items.size() == 0) return;
            editorProject.applyCommand(new PastedItemChange(items, getSelectedSubMenu(), editorProject.getMenuTree(),
                    new MenuIdChooserImpl(editorProject.getMenuTree())));
            redrawTreeControl();
        }
    }

    public void onAddToTreeMenu(ActionEvent actionEvent) {
        SubMenuItem subMenu = getSelectedSubMenu();

        Optional<MenuItem> maybeItem = editorUI.showNewItemDialog(editorProject.getMenuTree());
        maybeItem.ifPresent((menuItem) -> {
            editorProject.applyCommand(Command.NEW, menuItem, subMenu);
            redrawTreeControl();
            selectChildInTreeById(menuTree.getRoot(), menuItem.getId());
            onFocusCurrentEditor(actionEvent);
        });

    }

    private SubMenuItem getSelectedSubMenu() {
        var selMenu = menuTree.getSelectionModel().getSelectedItem().getValue().item();
        if (!selMenu.hasChildren()) {
            selMenu = editorProject.getMenuTree().findParent(selMenu);
        }
        return MenuItemHelper.asSubMenu(selMenu);
    }

    private Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

    public void onRemoveTreeMenu(ActionEvent actionEvent) {
        var toRemove = menuTree.getSelectionModel().getSelectedItem().getValue().item;

        if (toRemove.equals(MenuTree.ROOT)) {
            return; // cannot remove root.
        }

        // if there are children, confirm before removing.
        if (toRemove.hasChildren() && toRemove instanceof SubMenuItem) {
            editorProject.applyCommand(new BulkRemoveItemChange((SubMenuItem)toRemove, getProject().getMenuTree().findParent(toRemove)));
        }
        else {
            editorProject.applyCommand(Command.REMOVE, toRemove);
        }
        redrawTreeControl();
    }

    public void onFocusMenuTree(ActionEvent actionEvent) {
        menuTree.requestFocus();
    }

    public void onFileNew(ActionEvent event) {
        stopSimulatorIfNeeded();
        editorUI.showCreateProjectDialog();
        redrawTreeControl();
        handleRecents();
    }

    private void stopSimulatorIfNeeded() {
        if(simulatorUI != null) simulatorUI.closeWindow();
    }

    public void onFileOpen(ActionEvent event) {
        handleRecents();
        stopSimulatorIfNeeded();
        if (editorProject.openProject()) {
            redrawTreeControl();
            handleRecents();
        }
    }

    public void onRecent(ActionEvent event) {
        stopSimulatorIfNeeded();
        javafx.scene.control.MenuItem item = (javafx.scene.control.MenuItem) event.getSource();
        String recent = item.getText();
        if (!PrefsConfigurationStorage.RECENT_DEFAULT.equals(recent)) {
            editorProject.openProject(recent);
            redrawTreeControl();
        }
    }

    public void onFileSave(ActionEvent event) {
        editorProject.saveProject(EditorSaveMode.SAVE);
        configStore.setLastLoadedProject(editorProject.getFileName());
        redrawTreeControl();
        handleRecents();
    }

    public void onFileSaveAs(ActionEvent event) {
        editorProject.saveProject(EditorSaveMode.SAVE_AS);
        configStore.setLastLoadedProject(editorProject.getFileName());
        redrawTreeControl();
        handleRecents();
    }

    public void onFileExit(ActionEvent event) {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void onCodeShowLayout(ActionEvent actionEvent) {
        editorUI.showRomLayoutDialog(editorProject.getMenuTree(), editorProject.getLocaleHandler());
    }

    public void onGenerateCode(ActionEvent event) {
        try {
            if(!editorProject.isFileNameSet()) {
                editorUI.alertOnError(bundle.getString("core.no.filename.set"), bundle.getString("core.please.select.file.first"));
                return;
            }

            editorUI.showCodeGeneratorDialog(installer);
            editorProject.saveProject(EditorSaveMode.SAVE);
            configStore.setLastLoadedProject(editorProject.getFileName());
            redrawTreeControl();
            handleRecents();
        }
        catch (Exception e) {
            logger.log(ERROR, "Code generator caught an exception", e);
        }
    }

    public void loadPreferences() {
        recentItems.clear();
        List<String> recentPaths = configStore.loadRecents();

        var recentList = recentPaths.stream()
                .map(recentPath -> new RecentlyUsedItem(Paths.get(recentPath).getFileName().toString(), recentPath)).toList();

        recentItems.addAll(recentList);

        Platform.runLater(this::handleRecents);

        var current = new VersionInfo(configStore.getVersion());
        if(!configStore.getLastRunVersion().equals(current) || System.getProperty("alwaysShowSplash", "N").equals("Y")) {
            Platform.runLater(()-> {
                configStore.setLastRunVersion(current);
                editorUI.showSplashScreen(themeName -> {
                    darkModeMenuFlag.setSelected(themeName.equals("darkMode"));
                    BaseDialogSupport.getJMetro().setScene(menuTree.getScene());
                });
                var item = menuTree.getSelectionModel().getSelectedItem();
                if(item != null) {
                    onTreeChangeSelection(item.getValue().item());
                }
            });
        }
    }

    public void onUndo(ActionEvent event) {
        editorProject.undoChange();
        redrawTreeControl();
    }

    public void onRedo(ActionEvent event) {
        editorProject.redoChange();
        redrawTreeControl();
    }

    public void persistPreferences() {
        var recentFiles = recentItems.stream().map(RecentlyUsedItem::path).collect(Collectors.toList());
        configStore.saveUniqueRecents(recentFiles);
    }

    public void onCut(ActionEvent event) {
        currentEditor.ifPresent(UIMenuItem::handleCut);
    }

    public void onCopy(ActionEvent event) {
        if(menuTree.isFocused()) {
            onTreeCopy(event);
            menuTreePaste.setDisable(!isClipboardContentValid());
        }
        else {
            currentEditor.ifPresent(UIMenuItem::handleCopy);
        }
    }

    public void onPaste(ActionEvent event) {
        if(menuTree.isFocused()) {
            onTreePaste(event);
        }
        else {
            currentEditor.ifPresent(UIMenuItem::handlePaste);
        }
    }

    public void onShowEditMenu(Event event) {
        if(menuTree.isFocused()) {
            menuCopy.setDisable(false);
            menuCut.setDisable(true);
            menuPaste.setDisable(!isClipboardContentValid());
            return;
        }

        currentEditor.ifPresentOrElse((uiItem)-> {
            menuCopy.setDisable(!uiItem.canCopy());
            menuCut.setDisable(!uiItem.canCopy());
            menuPaste.setDisable(!uiItem.canPaste());
        }, ()-> {
            menuCopy.setDisable(true);
            menuCut.setDisable(true);
            menuPaste.setDisable(true);
        });
        menuRedo.setDisable(!editorProject.canRedo());
        menuUndo.setDisable(!editorProject.canUndo());
    }


    private void handleRecents() {
        if (editorProject.isFileNameSet()) {
            var path = Paths.get(editorProject.getFileName());
            recentItems.addFirst(new RecentlyUsedItem(path.getFileName().toString(), path.toString()));
        }

        recentItems = recentItems.stream()
                .filter(recent -> Files.exists(Paths.get(recent.path)))
                .filter(recent -> !recent.name().equals(ConfigurationStorage.RECENT_DEFAULT))
                .distinct()
                .collect(Collectors.toCollection(LinkedList::new));

        menuRecents.getItems().clear();
        recentItems.forEach(recentlyUsedItem -> {
            var item = new javafx.scene.control.MenuItem(recentlyUsedItem.name());
            item.setOnAction(e-> {
                stopSimulatorIfNeeded();
                editorProject.openProject(recentlyUsedItem.path());
                redrawTreeControl();
            });
            menuRecents.getItems().add(item);
        });
    }

    public void onGeneralSettings(ActionEvent actionEvent) {
        editorUI.showGeneralSettings();
    }

    public void onDarkModeChange(ActionEvent actionEvent) {
        BaseDialogSupport.setTheme(darkModeMenuFlag.isSelected() ?  "darkMode" : "lightMode");
        BaseDialogSupport.getJMetro().setScene(menuTree.getScene());
    }

    public void onSponsorLinkPressed(ActionEvent actionEvent) {
        editorUI.browseToURL(SPONSOR_TCMENU_PAGE);
    }

    public void onShowExpanders(ActionEvent actionEvent) {
        ChooseIoExpanderDialog dlg = new ChooseIoExpanderDialog((Stage)menuTree.getScene().getWindow(),
                Optional.empty(), editorProject, true);
    }

    public void onPrepareDiagnostics(ActionEvent actionEvent) {
        try {
            StringBuilder sb = new StringBuilder(255);
            sb.append("tcMenu diagnostics - stream ").append(libVerDetector.getReleaseType()).append(LINE_BREAK);
            sb.append("TcMenuDesigner Version - ").append(configStore.getVersion()).append(LINE_BREAK);
            sb.append("Plugin versions:").append(LINE_BREAK);
            for(var pl : pluginManager.getLoadedPlugins()) {
                sb.append(pl.getModuleName()).append(" - ").append(pl.getVersion()).append(LINE_BREAK);
            }
            if(!pluginManager.getLoadErrors().isEmpty()) {
                sb.append("Plugin load errors").append(LINE_BREAK);
                for(var err : pluginManager.getLoadErrors()) {
                    sb.append(err).append(LINE_BREAK);
                }
            }
            sb.append("Library versions:").append(LINE_BREAK);
            sb.append("tcMenu - ").append(installer.getVersionOfLibrary("tcMenu", CURRENT_LIB)).append(LINE_BREAK);
            sb.append("LiquidCrystalIO - ").append(installer.getVersionOfLibrary("LiquidCrystalIO", CURRENT_LIB)).append(LINE_BREAK);
            sb.append("TaskManagerIO - ").append(installer.getVersionOfLibrary("TaskManagerIO", CURRENT_LIB)).append(LINE_BREAK);
            sb.append("IoAbstraction - ").append(installer.getVersionOfLibrary("IoAbstraction", CURRENT_LIB)).append(LINE_BREAK);
            sb.append("Diagnostics END");

            Clipboard systemClipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(sb.toString());
            systemClipboard.setContent(content);

            var alert = new Alert(Alert.AlertType.INFORMATION, MenuEditorApp.getBundle().getString("core.diagnostics.copied"), ButtonType.CLOSE);
            alert.showAndWait();
            logger.log(INFO, "Diagnostics generated successfully");

        } catch (IOException e) {
            logger.log(ERROR, "Diagnostics failed to generate", e);
        }
    }

    public void onMenuInMenu(ActionEvent actionEvent) {
        var menuInMenuDialog = new EditMenuInMenuDialog(getStage(), editorProject.getGeneratorOptions(), editorProject.getMenuTree(), true);
    }

    public void onCreateFontDialog(ActionEvent actionEvent) {
        editorUI.showCreateFontUtility();
    }

    public void onCreateBitmapTool(ActionEvent actionEvent) {
        editorUI.showBitmapEditorUtility();
    }

    public void onConfigureLocales(ActionEvent actionEvent) {
        editorUI.showLocaleConfiguration(editorProject);
        if(editorProject.getLocaleHandler().isLocalSupportEnabled()) {
            redrawTreeControl();
        }
    }

    public void OnShowPreviewWindow(ActionEvent actionEvent) {
        if(!editorProject.isFileNameSet()) {
            editorUI.alertOnError(bundle.getString("core.no.filename.set"), bundle.getString("core.please.select.file.first"));
            return;
        }

        simulatorUI = new SimulatorUI();
        simulatorUI.presentSimulator(editorProject.getMenuTree(), editorProject, (Stage)menuTree.getScene().getWindow());
        simulatorUI.setCloseConsumer(windowEvent -> simulatorUI = null);
    }

    public void onSearchPressed(ActionEvent actionEvent) {
        var selected = editorUI.showSearchDialog(editorProject.getMenuTree());
        selected.ifPresent(item -> selectChildInTreeById(menuTree.getRoot(), item.getId()));
    }

    private record RecentlyUsedItem(String name, String path) {
        public String toString() {
            return name;
        }
    }

    public class MenuItemWithDescription {
        private String desc;
        private MenuItem item;
        public MenuItemWithDescription(MenuItem item) {
            setItem(item);
        }

        public void setItem(MenuItem item) {
            this.item = item;
            if(item.equals(MenuTree.ROOT)) {
                desc = "Root Item";
            } else {
                desc = editorProject.getLocaleHandler().getFromLocaleWithDefault(item.getName(), item.getName());
            }
        }

        public MenuItem item() {
            return item;
        }

        public String desc() {
            return desc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var that = (MenuItemWithDescription) o;
            return Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item);
        }

        @Override
        public String toString() {
            if(item.getId() == 0) return desc;

            return desc + " (ID " + item.getId() + ")";
        }
    }

    private class MenuItemTreeController implements MenuItemTreeCell.MenuItemCellController {
        @Override
        public void itemClickedLeft(TreeCell<MenuItemWithDescription> item) {}

        @Override
        public void itemClickedRight(TreeCell<MenuItemWithDescription> item) {}

        @Override
        public void menuHasMovedFromTo(MenuItem originalLocation, MenuItem newLocation, MenuItemTreeCell.MenuInsertionPoint where) {
            SubMenuItem currParent = editorProject.getMenuTree().findParent(originalLocation);
            editorProject.applyCommand(new ItemMovedChangeCommand(originalLocation, newLocation, editorProject.getMenuTree(), where));
            // once the command is applied and the drag and drop finishes, entirely redraw the tree control.
            Platform.runLater(MenuEditorController.this::redrawTreeControl);
        }
    }

    private String toLang(Locale l) {
        return switch(l.getLanguage()) {
            case "" -> bundle.getString("locale.dialog.default.bundle");
            case "--" -> "--";
            default -> l.getDisplayLanguage() + "-" + l.getCountry();
        };
    }

    private List<javafx.scene.control.MenuItem> getLocaleMenuItems() {
        var list = editorProject.getLocaleHandler().getEnabledLocales().stream()
                .map(l -> {
                    var mi = new javafx.scene.control.MenuItem(toLang(l));
                    mi.setOnAction(event -> localHasChanged(l));
                    return mi;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        list.add(new SeparatorMenuItem());
        var item = new javafx.scene.control.MenuItem(bundle.getString("menu.code.locale.configuration"));
        item.setOnAction(this::onConfigureLocales);
        list.add(item);
        return list;
    }

    private void localHasChanged(Locale l) {
        try {
            if(l == null) return;
            menuTree.getSelectionModel().select(0);
            editorProject.getLocaleHandler().changeLocale(l);
            Platform.runLater(this::redrawTreeControl);
        } catch (IOException e) {
            logger.log(ERROR, "Locale could not be changed to " + l, e);
        }

    }
}
