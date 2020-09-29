/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.*;

import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.ARDUINO_AVR;

/**
 * {@link CurrentEditorProject} represents the current project that is being edited by the UI. It supports the controller
 * with all the required functionality to both alter and persist project files. The controller should never perform any
 * write operations directly on the menu tree. Also controls the undo and redo buffers.
 */
public class CurrentEditorProject {

    private static final String NO_CREATOR_SELECTED = "";
    public static final CodeGeneratorOptions BLANK_GEN_OPTIONS = new CodeGeneratorOptions(
            ARDUINO_AVR.getBoardId(),
            NO_CREATOR_SELECTED, NO_CREATOR_SELECTED, NO_CREATOR_SELECTED,
            Collections.emptyList(), UUID.randomUUID(), "New Device",
            true, false, false
    );


    public enum EditorSaveMode { SAVE_AS, SAVE}

    private static final String TITLE = "TcMenu Designer";
    private static final int UNDO_BUFFER_SIZE = 200;
    private final CurrentProjectEditorUI editorUI;
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final ProjectPersistor projectPersistor;

    private MenuTree menuTree;
    private Optional<String> fileName;
    private boolean dirty = true; // always assume dirty at first..
    private CodeGeneratorOptions generatorOptions = BLANK_GEN_OPTIONS;
    private Deque<MenuItemChange> changeHistory = new LinkedList<>();
    private Deque<MenuItemChange> redoHistory = new LinkedList<>();

    public CurrentEditorProject(CurrentProjectEditorUI editorUI, ProjectPersistor persistor) {
        this.editorUI = editorUI;
        projectPersistor = persistor;
        cleanDown();
    }

    private void cleanDown() {
        menuTree = new MenuTree();
        fileName = Optional.empty();
        setDirty(false);
        updateTitle();
    }

    public void newProject() {
        if(checkIfWeShouldOverwrite()) {
            cleanDown();
            updateTitle();
            changeHistory.clear();
        }
    }

    private boolean checkIfWeShouldOverwrite() {
        if(isDirty()) {
            return editorUI.questionYesNo("Changes will be lost", "Do you want to discard the current menu?");
        }

        return true;
    }

    public CodeGeneratorOptions getGeneratorOptions() {
        return generatorOptions;
    }

    public void setGeneratorOptions(CodeGeneratorOptions generatorOptions) {
        setDirty(true);
        this.generatorOptions = generatorOptions;
    }

    public boolean openProject(String file) {
        try {
            if(checkIfWeShouldOverwrite()) {
                fileName = Optional.ofNullable(file);
                MenuTreeWithCodeOptions openedProject = projectPersistor.open(file);
                menuTree = openedProject.getMenuTree();
                generatorOptions = openedProject.getOptions();
                if (generatorOptions == null) generatorOptions = BLANK_GEN_OPTIONS;
                setDirty(false);
                updateTitle();
                changeHistory.clear();
                return true;
            }
        } catch (IOException e) {
            fileName = Optional.empty();
            logger.log(Level.ERROR, "open operation failed on " + file, e);
            editorUI.alertOnError("Unable to open file", "The selected file could not be opened");
        }
        return false;
    }

    public boolean openProject() {
        if (checkIfWeShouldOverwrite()) {
            fileName = editorUI.findFileNameFromUser(true);
            fileName.ifPresent(this::openProject);
            return true;
        }
        return false;
    }

    public void saveProject(EditorSaveMode saveMode) {
        if(fileName.isEmpty() || saveMode == EditorSaveMode.SAVE_AS) {
            fileName = editorUI.findFileNameFromUser(false);
        }

        fileName.ifPresent((file)-> {
            try {
                projectPersistor.save(file, menuTree, generatorOptions);
                setDirty(false);
            } catch (IOException e) {
                logger.log(Level.ERROR, "save operation failed on " + file, e);
                editorUI.alertOnError("Unable to save file", "Could not save file to chosen location");
            }
        });
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
            updateTitle();
        }
    }

    private void updateTitle() {
        editorUI.setTitle(getFileName() + (isDirty()?"* ":" ") + TITLE);
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
