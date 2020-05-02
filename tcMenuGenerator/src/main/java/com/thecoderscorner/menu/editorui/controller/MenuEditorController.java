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
import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.dialog.RegistrationDialog;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject.EditorSaveMode;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import com.thecoderscorner.menu.editorui.project.MenuItemChange.Command;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.LIBRARY_DOCS_URL;
import static java.lang.System.Logger.Level.ERROR;

@SuppressWarnings("unused")
public class MenuEditorController {
    public static final String REGISTRATION_URL = "http://www.thecoderscorner.com/tcc/app/registerTcMenu";
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

    public Menu examplesMenu;
    public TextArea prototypeTextArea;
    public BorderPane rootPane;
    public TreeView<MenuItem> menuTree;
    public Button menuTreeAdd;
    public Button menuTreeRemove;
    public Button menuTreeCopy;
    public Button menuTreeUp;
    public Button menuTreeDown;
    public BorderPane editorBorderPane;
    public MenuBar mainMenu;

    private List<Button> toolButtons;
    private Optional<UIMenuItem> currentEditor = Optional.empty();
    private ArduinoLibraryInstaller installer;
    private CurrentProjectEditorUI editorUI;
    private CodePluginManager pluginManager;
    private ConfigurationStorage configStore;
    private LinkedList<String> recentItems = new LinkedList<>();
    private LibraryVersionDetector libVerDetector;

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

        menuTree.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            if (newItem != null) {
                onTreeChangeSelection(newItem.getValue());
            }
        });

        loadPreferences();

        Platform.runLater(() -> {
            sortOutToolButtons();
            sortOutMenuForMac();
            redrawTreeControl();
            redrawStatus();
            populateMenu(examplesMenu, installer.findLibraryInstall("tcMenu"), "examples");
            populateMenu(menuSketches, installer.getArduinoDirectory(), "");
        });
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
            try {
                if(OSXAdapter.setAboutHandler(this, getClass().getMethod("onAboutOsX"))) {
                    aboutMenuItem.setVisible(false);
                }

                OSXAdapter.setQuitHandler(this, getClass().getMethod("onExitOsX"));
                exitMenuItem.setVisible(false);
            } catch (NoSuchMethodException e) {
                logger.log(ERROR, "Unable to set Mac menu properly", e);
            }
        }
    }

    public void onExitOsX() {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void onAboutOsX() {
        aboutMenuPressed(null);
    }

    private void sortOutToolButtons() {
        toolButtons = Arrays.asList(menuTreeAdd, menuTreeRemove, menuTreeCopy, menuTreeUp, menuTreeDown);

        toolButtons.forEach(button -> button.getStyleClass().setAll("tool-button"));
    }

    private void onEditorChange(MenuItem original, MenuItem changed) {
        if (!original.equals(changed)) {
            menuTree.getSelectionModel().getSelectedItem().setValue(changed);
            editorProject.applyCommand(Command.EDIT, changed);
            redrawPrototype();
        }
    }

    public void onTreeChangeSelection(MenuItem newValue) {
        editorUI.createPanelForMenuItem(newValue, editorProject.getMenuTree(), this::onEditorChange)
                .ifPresentOrElse((uiMenuItem) -> {
                    editorBorderPane.setCenter(uiMenuItem.initPanel());
                    currentEditor = Optional.of(uiMenuItem);
                },
                () -> {
                    AppInformationPanel panel = new AppInformationPanel(installer, this, pluginManager, editorUI, libVerDetector);
                    editorBorderPane.setCenter(panel.showEmptyInfoPanel());
                    currentEditor = Optional.empty();
                }
        );

        // we cannot modify root.
        toolButtons.stream().filter(b -> b != menuTreeAdd)
                .forEach(b -> b.setDisable(MenuTree.ROOT.equals(newValue)));

        // We cannot copy sub menus whole. Only value items
        if(newValue.hasChildren()) menuTreeCopy.setDisable(true);
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

    public void registerMenuPressed(ActionEvent actionEvent) {
        RegistrationDialog.showRegistration(configStore, getStage(), REGISTRATION_URL);
    }

    public void onTreeCopy(ActionEvent actionEvent) {
        MenuItem selected = menuTree.getSelectionModel().getSelectedItem().getValue();
        MenuIdChooser chooser = new MenuIdChooserImpl(editorProject.getMenuTree());
        MenuItem item = MenuItemHelper.createFromExistingWithId(selected, chooser.nextHighestId());
        SubMenuItem subMenu = getSelectedSubMenu();
        editorProject.applyCommand(Command.NEW, item, subMenu);

        // select the newly created item and render it.
        redrawTreeControl();
        selectChildInTreeById(menuTree.getRoot(), item.getId());
    }

    public void onTreeMoveUp(ActionEvent event) {
        MenuItem selected = menuTree.getSelectionModel().getSelectedItem().getValue();
        editorProject.applyCommand(Command.UP, selected);
        redrawTreeControl();
    }

    public void onTreeMoveDown(ActionEvent event) {
        MenuItem selected = menuTree.getSelectionModel().getSelectedItem().getValue();
        editorProject.applyCommand(Command.DOWN, selected);
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
        if (toRemove.hasChildren()) {
            if(!editorUI.questionYesNo("Remove ALL items within [" + toRemove.getName() + "]?",
                    "If you click yes and proceed, you will remove all items under " + toRemove.getName())) {
                return;
            }
        }

        editorProject.applyCommand(Command.REMOVE, toRemove);
        redrawTreeControl();
    }

    public void onFileNew(ActionEvent event) {
        editorProject.newProject();
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
        recentItems.addAll(configStore.loadRecents());

        Platform.runLater(this::handleRecents);

        if(configStore.getRegisteredKey().isEmpty()) {
            Platform.runLater(()-> {
                RegistrationDialog.showRegistration(configStore, getStage(), REGISTRATION_URL);
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
        configStore.saveUniqueRecents(recentItems);
    }

    public void onCut(ActionEvent event) {
        currentEditor.ifPresent(UIMenuItem::handleCut);
    }

    public void onCopy(ActionEvent event) {
        currentEditor.ifPresent(UIMenuItem::handleCopy);
    }

    public void onPaste(ActionEvent event) {
        currentEditor.ifPresent(UIMenuItem::handlePaste);
    }

    public void onShowEditMenu(Event event) {
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
            recentItems.addFirst(editorProject.getFileName());
        }

        recentItems = recentItems.stream()
                .filter(name -> !name.equals(ConfigurationStorage.RECENT_DEFAULT))
                .distinct()
                .collect(Collectors.toCollection(LinkedList::new));

        menuRecents.getItems().clear();
        recentItems.forEach(path-> {
            var item = new javafx.scene.control.MenuItem(path);
            item.setOnAction(e-> {
                editorProject.openProject(path);
                redrawTreeControl();
            });
            menuRecents.getItems().add(item);
        });
    }
}
