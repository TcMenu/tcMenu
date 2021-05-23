/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.cli.StartUICommand;
import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.dialog.RegistrationDialog;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.project.*;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject.EditorSaveMode;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.*;
import static com.thecoderscorner.menu.editorui.project.EditedItemChange.Command;
import static com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor.TCMENU_COPY_PREFIX;
import static java.lang.System.Logger.Level.ERROR;

@SuppressWarnings({"unused", "rawtypes"})
public class MenuEditorController {
    public static final String REGISTRATION_URL = "https://www.thecoderscorner.com/tcc/app/registerTcMenu";
    private final System.Logger logger = System.getLogger(MenuEditorController.class.getSimpleName());
    public Label statusField;
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
    public javafx.scene.control.MenuItem menuItemUp;
    public javafx.scene.control.MenuItem menuItemDown;

    public Menu examplesMenu;
    public TextArea prototypeTextArea;
    public BorderPane rootPane;
    public TreeView<MenuItem> menuTree;
    public Button menuTreeAdd;
    public Button menuTreeRemove;
    public Button menuTreeCopy;
    public Button menuTreePaste;
    public Button menuTreeUp;
    public Button menuTreeDown;
    public BorderPane editorBorderPane;
    public MenuBar mainMenu;

    private Optional<UIMenuItem> currentEditor = Optional.empty();
    private ArduinoLibraryInstaller installer;
    private CurrentProjectEditorUI editorUI;
    private CodePluginManager pluginManager;
    private ConfigurationStorage configStore;
    private LinkedList<RecentlyUsedItem> recentItems = new LinkedList<>();
    private LibraryVersionDetector libVerDetector;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

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

        if(StartUICommand.didUserSelectProject()) {
            editorProject.openProject(StartUICommand.userSelectedProject());
        }

        menuTree.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            if (newItem != null) {
                onTreeChangeSelection(newItem.getValue());
            }
        });

        loadPreferences();

        Platform.runLater(() -> {
            sortOutMenuForMac();
            redrawTreeControl();
            redrawStatus();
            populateAllMenus();
        });

        storage.addArduinoDirectoryChangeListener((ard, lib, libsChanged) -> {
            if(libsChanged) Platform.runLater(this::populateAllMenus);
        });

        executor.scheduleAtFixedRate(this::checkOnClipboard, 3000, 3000, TimeUnit.MILLISECONDS);
    }

    public void checkOnClipboard() {
        Platform.runLater(() -> menuTreePaste.setDisable(!isClipboardContentValid()));
    }

    private void populateAllMenus() {
        if(configStore.isUsingArduinoIDE()) {
            populateMenu(examplesMenu, installer.findLibraryInstall("tcMenu"), "examples");
            populateMenu(menuSketches, installer.getArduinoDirectory(), "");
        }
    }

    public CurrentEditorProject getProject() {
        return editorProject;
    }

    private void populateMenu(Menu toPopulate, Optional<Path> maybeDir, String subDir) {
        if(maybeDir.isPresent()) {
            try {
                toPopulate.getItems().clear();
                Files.list(maybeDir.get().resolve(subDir))
                        .filter(Files::isDirectory)
                        .filter(this::hasEmfFile)
                        .forEach(path -> {
                            var item = new javafx.scene.control.MenuItem(path.getFileName().toString());
                            item.setOnAction(e-> openFirstEMF(path));
                            toPopulate.getItems().add(item);
                        });
            } catch (IOException e) {
                logger.log(ERROR, "Unable to populate menus due to exception", e);
            }

        }
        else {
            logger.log(ERROR, "Examples directory not found");
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

    private void redrawStatus() {
        statusField.setText("TcMenu Designer " + configStore. getVersion()
                + " \u00A9 thecoderscorner.com. Registered to " + configStore.getRegisteredKey());
    }

    private void sortOutMenuForMac() {
        final String os = System.getProperty ("os.name");
        if (os != null && os.startsWith ("Mac")) {
            mainMenu.useSystemMenuBarProperty().set(true);
            exitMenuItem.setVisible(false);
        }
    }

    private void onEditorChange(MenuItem original, MenuItem changed) {
        if (!original.equals(changed)) {
            menuTree.getSelectionModel().getSelectedItem().setValue(changed);
            editorProject.applyCommand(Command.EDIT, changed);
            redrawPrototype();
        }
    }

    public void presentInfoPanel() {
        AppInformationPanel panel = new AppInformationPanel(installer, this, pluginManager, editorUI, libVerDetector, configStore);
        editorBorderPane.setCenter(panel.showEmptyInfoPanel());
        currentEditor = Optional.empty();
    }

    public void onTreeChangeSelection(MenuItem newValue) {
        VariableNameGenerator gen = new VariableNameGenerator(
                editorProject.getMenuTree(),
                editorProject.getGeneratorOptions().isNamingRecursive(),
                editorProject.getUncommittedItems()
        );
        editorUI.createPanelForMenuItem(newValue, editorProject.getMenuTree(), gen, this::onEditorChange)
                .ifPresentOrElse((uiMenuItem) -> {
                    ScrollPane scrollPane = new ScrollPane(uiMenuItem.initPanel());
                    scrollPane.setFitToWidth(true);
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    editorBorderPane.setCenter(scrollPane);
                    currentEditor = Optional.of(uiMenuItem);
                }, this::presentInfoPanel
        );

        // we cannot modify root.
        var isRoot = MenuTree.ROOT.equals(newValue);
        menuRemoveItem.setDisable(isRoot);
        menuItemUp.setDisable(isRoot);
        menuItemDown.setDisable(isRoot);
        menuTreeRemove.setDisable(isRoot);
        menuTreeCopy.setDisable(isRoot);
        menuTreeUp.setDisable(isRoot);
        menuTreeDown.setDisable(isRoot);
        menuTreePaste.setDisable(!isClipboardContentValid());

        // We cannot copy ROOT. Only value items
        if(newValue.equals(MenuTree.ROOT)) menuTreeCopy.setDisable(true);
    }

    private boolean isClipboardContentValid() {
        var clipboard = Clipboard.getSystemClipboard();
        if(clipboard.hasContent(DataFormat.PLAIN_TEXT)) {
            var data = clipboard.getContent(DataFormat.PLAIN_TEXT);
            return (data != null && data.toString().startsWith(TCMENU_COPY_PREFIX));
        }
        return false;
    }

    private void redrawTreeControl() {
        TreeItem<MenuItem> selectedItem = menuTree.getSelectionModel().getSelectedItem();
        int sel = MenuTree.ROOT.getId();
        if (selectedItem != null && selectedItem.getValue() != null) {
            sel = selectedItem.getValue().getId();
        }

        TreeItem<MenuItem> rootItem = new TreeItem<>(MenuTree.ROOT);
        rootItem.setExpanded(true);

        SubMenuItem root = MenuTree.ROOT;
        recurseTreeItems(editorProject.getMenuTree().getMenuItems(root), rootItem);
        menuTree.setRoot(rootItem);
        menuTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        menuTree.getSelectionModel().selectFirst();

        redrawPrototype();
        selectChildInTreeById(rootItem, sel);
    }

    private void selectChildInTreeById(TreeItem<MenuItem> item, int id) {
        // if we had a selection before the rebuild, honour it
        for (TreeItem<MenuItem> child : item.getChildren()) {
            if (child.getValue().getId() == id) {
                menuTree.getSelectionModel().select(child);
                return;
            } else if (!child.getChildren().isEmpty()) {
                selectChildInTreeById(child, id);
            }
        }
    }

    private void redrawPrototype() {
        prototypeTextArea.setText(new TextTreeItemRenderer(editorProject.getMenuTree()).getTreeAsText());
    }

    private void recurseTreeItems(List<MenuItem> menuItems, TreeItem<MenuItem> treeItem) {
        if (menuItems == null) return;

        for (MenuItem<?> item : menuItems) {
            TreeItem<MenuItem> child = new TreeItem<>(item);
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

    public void registerMenuPressed(ActionEvent actionEvent) {
        RegistrationDialog.showRegistration(configStore, getStage(), REGISTRATION_URL);
    }

    public void onTreeCopy(ActionEvent actionEvent) {
        var selected = menuTree.getSelectionModel().getSelectedItem();
        if(selected == null || selected.getValue().equals(MenuTree.ROOT)) return;

        var cp = editorProject.getProjectPersistor().itemsToCopyText(selected.getValue(), editorProject.getMenuTree());
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

    public void onTreeMoveUp(ActionEvent event) {
        MenuItem selected = menuTree.getSelectionModel().getSelectedItem().getValue();
        if(selected == null) return;
        editorProject.applyCommand(new UpDownItemChange(selected, getProject().getMenuTree().findParent(selected), true));
        redrawTreeControl();
    }

    public void onTreeMoveDown(ActionEvent event) {
        MenuItem selected = menuTree.getSelectionModel().getSelectedItem().getValue();
        editorProject.applyCommand(new UpDownItemChange(selected, getProject().getMenuTree().findParent(selected), false));
        redrawTreeControl();
    }

    public void onAddToTreeMenu(ActionEvent actionEvent) {
        SubMenuItem subMenu = getSelectedSubMenu();

        Optional<MenuItem> maybeItem = editorUI.showNewItemDialog(editorProject.getMenuTree());
        maybeItem.ifPresent((menuItem) -> {
            editorProject.applyCommand(Command.NEW, menuItem, subMenu);
            redrawTreeControl();
            selectChildInTreeById(menuTree.getRoot(), menuItem.getId());
        });

    }

    private SubMenuItem getSelectedSubMenu() {
        MenuItem selMenu = menuTree.getSelectionModel().getSelectedItem().getValue();
        if (!selMenu.hasChildren()) {
            selMenu = editorProject.getMenuTree().findParent(selMenu);
        }
        return MenuItemHelper.asSubMenu(selMenu);
    }

    private Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

    public void onRemoveTreeMenu(ActionEvent actionEvent) {
        MenuItem toRemove = menuTree.getSelectionModel().getSelectedItem().getValue();

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

    public void onFileNew(ActionEvent event) {
        editorUI.showCreateProjectDialog(editorProject);
        redrawTreeControl();
        handleRecents();
    }

    public void onFileOpen(ActionEvent event) {
        handleRecents();
        if (editorProject.openProject()) {
            redrawTreeControl();
            handleRecents();
        }
    }

    public void onRecent(ActionEvent event) {
        javafx.scene.control.MenuItem item = (javafx.scene.control.MenuItem) event.getSource();
        String recent = item.getText();
        if (!PrefsConfigurationStorage.RECENT_DEFAULT.equals(recent)) {
            editorProject.openProject(recent);
            redrawTreeControl();
        }
    }

    public void onFileSave(ActionEvent event) {
        editorProject.saveProject(EditorSaveMode.SAVE);
        redrawTreeControl();
        handleRecents();
    }

    public void onFileSaveAs(ActionEvent event) {
        editorProject.saveProject(EditorSaveMode.SAVE_AS);
        redrawTreeControl();
        handleRecents();
    }

    public void onFileExit(ActionEvent event) {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void onCodeShowLayout(ActionEvent actionEvent) {
        editorUI.showRomLayoutDialog(editorProject.getMenuTree());
    }

    public void onGenerateCode(ActionEvent event) {
        editorUI.showCodeGeneratorDialog(editorProject, installer);
        editorProject.saveProject(EditorSaveMode.SAVE);
        redrawTreeControl();
        handleRecents();
    }

    public void loadPreferences() {
        recentItems.clear();
        List<String> recentPaths = configStore.loadRecents();

        var recentList = recentPaths.stream()
                .map(recentPath -> new RecentlyUsedItem(Paths.get(recentPath).getFileName().toString(), recentPath))
                .collect(Collectors.toList());

        recentItems.addAll(recentList);

        Platform.runLater(this::handleRecents);

        var current = new VersionInfo(configStore.getVersion());
        if(!configStore.getLastRunVersion().equals(current)) {
            Platform.runLater(()-> {
                configStore.setLastRunVersion(current);
                editorUI.showSplashScreen();
                TreeItem<MenuItem> item = menuTree.getSelectionModel().getSelectedItem();
                if(item != null) {
                    onTreeChangeSelection(item.getValue());
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
                .filter(recent -> !recent.name().equals(ConfigurationStorage.RECENT_DEFAULT))
                .distinct()
                .collect(Collectors.toCollection(LinkedList::new));

        menuRecents.getItems().clear();
        recentItems.forEach(recentlyUsedItem -> {
            var item = new javafx.scene.control.MenuItem(recentlyUsedItem.name());
            item.setOnAction(e-> {
                editorProject.openProject(recentlyUsedItem.path());
                redrawTreeControl();
            });
            menuRecents.getItems().add(item);
        });
    }

    public void onGeneralSettings(ActionEvent actionEvent) {
        editorUI.showGeneralSettings();
    }

    private record RecentlyUsedItem(String name, String path) {
        public String toString() {
            return name;
        }
    }
}
