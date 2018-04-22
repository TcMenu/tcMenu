package com.thecoderscorner.menu.editorui.controller;

import com.google.common.collect.ImmutableList;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.dialog.*;
import com.thecoderscorner.menu.editorui.generator.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import com.thecoderscorner.menu.editorui.project.MenuItemChange.Command;
import com.thecoderscorner.menu.editorui.uimodel.UIEditorFactory;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.LIBRARY_DOCS_URL;

import com.thecoderscorner.menu.domain.MenuItem;

public class MenuEditorController {
    public static final String RECENT_DEFAULT = "Recent";
    public static final String REGISTERED_KEY = "Registered";
    private final Logger logger = LoggerFactory.getLogger(MenuEditorController.class);
    private CurrentEditorProject editorProject;
    public javafx.scene.control.MenuItem menuCut;
    public javafx.scene.control.MenuItem menuCopy;
    public javafx.scene.control.MenuItem menuPaste;
    public javafx.scene.control.MenuItem menuUndo;
    public javafx.scene.control.MenuItem menuRedo;
    public javafx.scene.control.MenuItem menuRecent1;
    public javafx.scene.control.MenuItem menuRecent2;
    public javafx.scene.control.MenuItem menuRecent3;
    public javafx.scene.control.MenuItem menuRecent4;

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

    public void initialise(CurrentEditorProject editorProject) {
        this.editorProject = editorProject;
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
        });
    }

    private void sortOutMenuForMac() {
        final String os = System.getProperty ("os.name");
        if (os != null && os.startsWith ("Mac"))
            mainMenu.useSystemMenuBarProperty ().set (true);

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

    private void onTreeChangeSelection(MenuItem newValue) {
        UIEditorFactory.createPanelForMenuItem(newValue, editorProject.getMenuTree(), this::onEditorChange)
                .ifPresentOrElse((uiMenuItem) -> {
                    editorBorderPane.setCenter(uiMenuItem.initPanel());
                    currentEditor = Optional.of(uiMenuItem);
                },
                () -> {
                    editorBorderPane.setCenter(AppInformationPanel.showEmptyInfoPanel());
                    currentEditor = Optional.empty();
                }
        );

        toolButtons.stream().filter(b -> b != menuTreeAdd)
                .forEach(b -> b.setDisable(MenuTree.ROOT.equals(newValue)));
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

    private void recurseTreeItems(ImmutableList<MenuItem> menuItems, TreeItem<MenuItem> treeItem) {
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
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.showSplash(getStage());
    }

    public void onMenuDocumentation(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().browse(new URI(LIBRARY_DOCS_URL));
        } catch (IOException | URISyntaxException e) {
            // not much we can do here really!
            logger.error("Could not open browser", e);
        }
    }

    public void registerMenuPressed(ActionEvent actionEvent) {
        RegistrationDialog.showRegistration(getStage());
    }

    public void onTreeCopy(ActionEvent actionEvent) {
        MenuItem selected = menuTree.getSelectionModel().getSelectedItem().getValue();
        MenuIdChooser chooser = new MenuIdChooserImpl(editorProject.getMenuTree());
        MenuItem item = MenuItemHelper.createFromExistingWithId(selected, chooser.nextHighestId());

        redrawTreeControl();
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
        MenuItem selMenu = menuTree.getSelectionModel().getSelectedItem().getValue();
        if (!selMenu.hasChildren()) {
            selMenu = editorProject.getMenuTree().findParent(selMenu);
        }
        SubMenuItem subMenu = MenuItemHelper.asSubMenu(selMenu);

        Optional<MenuItem> maybeItem = NewItemDialog.showNewItemRequest(getStage(), editorProject.getMenuTree());
        maybeItem.ifPresent((menuItem) -> {
            editorProject.applyCommand(Command.NEW, menuItem, subMenu);
            redrawTreeControl();
            selectChildInTreeById(menuTree.getRoot(), menuItem.getId());
        });

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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove all items in submenu");
            alert.setHeaderText("Remove ALL items within [" + toRemove + "]?");
            alert.setContentText("If you click yes and proceed, you will remove all items under " + toRemove);
            Optional<ButtonType> maybeButtonType = alert.showAndWait();
            if (!maybeButtonType.isPresent() || maybeButtonType.get() != ButtonType.OK) {
                // get outa here.
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
        if (editorProject.openProject()) {
            redrawTreeControl();
            handleRecents();
        }
    }

    public void onRecent(ActionEvent event) {
        javafx.scene.control.MenuItem item = (javafx.scene.control.MenuItem) event.getSource();
        String recent = item.getText();
        if (!RECENT_DEFAULT.equals(recent)) {
            editorProject.openProject(recent);
            redrawTreeControl();
        }
    }

    public void onFileSave(ActionEvent event) {
        editorProject.saveProject(CurrentEditorProject.EditorSaveMode.SAVE);
        redrawTreeControl();
        handleRecents();
    }

    public void onFileSaveAs(ActionEvent event) {
        editorProject.saveProject(CurrentEditorProject.EditorSaveMode.SAVE_AS);
        redrawTreeControl();
        handleRecents();
    }

    public void onFileExit(ActionEvent event) {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void onCodeShowLayout(ActionEvent actionEvent) {
        RomLayoutDialog.showLayoutDialog(getStage(), editorProject.getMenuTree());
    }

    public void onGenerateCode(ActionEvent event) {
        CodeGeneratorDialog.showCodeGenerator(getStage(), editorProject);
    }

    public void onInstallLibrary(ActionEvent actionEvent) {
        ArduinoLibraryInstaller instaler = new ArduinoLibraryInstaller();
        instaler.tryToInstallLibrary();
    }

    public void loadPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());

        menuRecent1.setText(prefs.get(RECENT_DEFAULT + "1", RECENT_DEFAULT));
        menuRecent2.setText(prefs.get(RECENT_DEFAULT + "2", RECENT_DEFAULT));
        menuRecent3.setText(prefs.get(RECENT_DEFAULT + "3", RECENT_DEFAULT));
        menuRecent4.setText(prefs.get(RECENT_DEFAULT + "4", RECENT_DEFAULT));

        if(prefs.get(REGISTERED_KEY, "").isEmpty()) {
            Platform.runLater(()-> {
                RegistrationDialog.showRegistration(getStage());
                TreeItem<MenuItem> item = menuTree.getSelectionModel().getSelectedItem();
                if(item != null) {
                    onTreeChangeSelection(item.getValue());
                }
            });
        }
    }

    public void persistPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        prefs.put(RECENT_DEFAULT + "1", menuRecent1.getText());
        prefs.put(RECENT_DEFAULT + "2", menuRecent2.getText());
        prefs.put(RECENT_DEFAULT + "3", menuRecent3.getText());
        prefs.put(RECENT_DEFAULT + "4", menuRecent4.getText());
    }

    public void onUndo(ActionEvent event) {
        editorProject.undoChange();
        redrawTreeControl();
    }

    public void onRedo(ActionEvent event) {
        editorProject.redoChange();
        redrawTreeControl();
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
        if (!editorProject.isFileNameSet()) return;

        List<String> recents = Arrays.asList(editorProject.getFileName(), menuRecent1.getText(), menuRecent2.getText(), menuRecent3.getText(), menuRecent4.getText());
        LinkedList<String> cleanedRecents = recents.stream()
                .filter(name -> !name.equals(RECENT_DEFAULT))
                .distinct()
                .collect(Collectors.toCollection(LinkedList::new));

        List<javafx.scene.control.MenuItem> recentItems = Arrays.asList(menuRecent1, menuRecent2, menuRecent3, menuRecent4);
        recentItems.forEach(menuItem -> {
            if (cleanedRecents.isEmpty()) {
                menuItem.setText(RECENT_DEFAULT);
            } else {
                menuItem.setText(cleanedRecents.removeFirst());
            }
        });


    }
}
