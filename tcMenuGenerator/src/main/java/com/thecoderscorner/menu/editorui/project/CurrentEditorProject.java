/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import static javafx.stage.FileChooser.ExtensionFilter;

/**
 * {@link CurrentEditorProject} represents the current project that is being edited by the UI. It supports the controller
 * with all the required functionality to both alter and persist project files. The controller should never perform any
 * write operations directly on the menu tree. Also controls the undo and redo buffers.
 */
public class CurrentEditorProject {

    public enum EditorSaveMode { SAVE_AS, SAVE }

    private static final String TITLE = "TcMenu Designer";
    private static final int UNDO_BUFFER_SIZE = 200;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ProjectPersistor projectPersistor;
    private final Stage mainStage;

    private MenuTree menuTree;
    private Optional<String> fileName;
    private boolean dirty;
    private Deque<MenuItemChange> changeHistory = new LinkedList<>();
    private Deque<MenuItemChange> redoHistory = new LinkedList<>();

    public CurrentEditorProject(Stage mainStage, ProjectPersistor persistor) {
        this.mainStage = mainStage;
        projectPersistor = persistor;
        cleanDown();
    }

    private void cleanDown() {
        menuTree = new MenuTree();
        dirty = false;
        fileName = Optional.empty();
        changeTitle();
    }

    public void newProject() {
        if(checkIfWeShouldOverwrite()) {
            cleanDown();
            changeHistory.clear();
        }
    }

    private boolean checkIfWeShouldOverwrite() {
        if(isDirty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Changes will be lost");
            alert.setHeaderText("Changes will be lost if you proceed");
            return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
        }

        return true;
    }

    public void openProject(String file) {
        try {
            fileName = Optional.ofNullable(file);
            menuTree = projectPersistor.open(file);
            dirty = false;
            changeHistory.clear();
            changeTitle();
        } catch (IOException e) {
            fileName = Optional.empty();
            logger.error("open operation failed on " + file, e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unable to open file");
            alert.setHeaderText("The selected file could not be opened");
            alert.setContentText(file + "\n\n" + e.getMessage());
        }
    }

    public boolean openProject() {
        if (checkIfWeShouldOverwrite()) {
            findFileNameFromUser(true);
            fileName.ifPresent(this::openProject);
            return true;
        }
        return false;
    }

    public void saveProject(EditorSaveMode saveMode) {
        if(!fileName.isPresent() || saveMode == EditorSaveMode.SAVE_AS) {
            findFileNameFromUser(false);
        }

        fileName.ifPresent((file)-> {
            try {
                projectPersistor.save(file, menuTree);
                dirty = false;
                changeTitle();
            } catch (IOException e) {
                logger.error("save operation failed on " + file, e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable to save file");
                alert.setHeaderText("Could not save file to chosen location");
                alert.setContentText(file + "\n\n" + e.getMessage());

            }
        });
    }

    private void findFileNameFromUser(boolean open) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a Menu File");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Embedded menu", "*.emf"));
        File f;
        if(open) {
            f = fileChooser.showOpenDialog(mainStage);
        }
        else {
            f = fileChooser.showSaveDialog(mainStage);
        }

        if(f != null) {
            fileName = Optional.of(f.getPath());
        }
    }

    private void changeTitle() {
        mainStage.setTitle(getFileName() + (isDirty()?"* ":" ") + TITLE);
    }

    public boolean isFileNameSet() {
        return fileName.isPresent();
    }

    public String getFileName() {
        return fileName.orElse("New");
    }

    public boolean isDirty() {
        return dirty;
    }

    private void setDirty(boolean dirty) {
        if(this.dirty != dirty) {
            this.dirty = dirty;
            changeTitle();
        }
        else {
            this.dirty = dirty;
        }
    }

    public MenuTree getMenuTree() {
        return menuTree;
    }

    public void applyCommand(MenuItemChange.Command command, MenuItem newItem) {
        applyCommand(command, newItem, menuTree.findParent(newItem));
    }

    public void applyCommand(MenuItemChange.Command command, MenuItem newItem, SubMenuItem parent) {
        MenuItem oldItem = changeHistory.stream()
                .filter(item-> item.getItem().getId() == newItem.getId())
                .reduce((first, second) -> second)
                .map(MenuItemChange::getItem)
                .orElse(newItem);
        MenuItemChange change = new MenuItemChange(command, newItem, oldItem, parent);
        changeHistory.add(change);

        if(changeHistory.size() > UNDO_BUFFER_SIZE) {
            changeHistory.removeFirst();
        }
        redoHistory.clear();

        change.applyTo(menuTree);
        setDirty(true);
    }

    public void undoChange() {
        if(changeHistory.isEmpty()) return;

        MenuItemChange change = changeHistory.removeLast();
        redoHistory.add(change);
        change.unApply(menuTree);
        setDirty(true);
    }

    public void redoChange() {
        if(redoHistory.isEmpty()) return;

        MenuItemChange change = redoHistory.removeLast();
        changeHistory.add(change);
        change.applyTo(menuTree);
        setDirty(true);
    }

    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    public boolean canUndo() {
        return !changeHistory.isEmpty();
    }
}
