/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.NoLocaleEnabledLocalHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import com.thecoderscorner.menu.persist.SafeBundleLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.System.Logger.Level.ERROR;

/**
 * {@link CurrentEditorProject} represents the current project that is being edited by the UI. It supports the controller
 * with all the required functionality to both alter and persist project files. The controller should never perform any
 * write operations directly on the menu tree. Also controls the undo and redo buffers.
 */
public class CurrentEditorProject {

    public static final String NO_CREATOR_SELECTED = "";
    public static final String MENU_PROJECT_LANG_FILENAME = "project-lang";

    public enum EditorSaveMode { SAVE_AS, SAVE}

    private static final String TITLE = "TcMenu Designer";
    private static final int UNDO_BUFFER_SIZE = 200;
    private final CurrentProjectEditorUI editorUI;
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final ProjectPersistor projectPersistor;
    private final ConfigurationStorage configStore;
    private final Set<Integer> uncommittedItems = new HashSet<>();

    private MenuTree menuTree;
    private Optional<String> fileName = Optional.empty();
    private String description;
    private boolean dirty = true; // always assume dirty at first..
    private CodeGeneratorOptions generatorOptions;
    private LocaleMappingHandler localeHandler = null;

    private final Deque<MenuItemChange> changeHistory = new LinkedList<>();
    private final Deque<MenuItemChange> redoHistory = new LinkedList<>();

    public CurrentEditorProject(CurrentProjectEditorUI editorUI, ProjectPersistor persistor, ConfigurationStorage storage) {
        this.editorUI = editorUI;
        projectPersistor = persistor;
        configStore = storage;
        cleanDown();
    }

    private void cleanDown() {
        menuTree = new MenuTree();
        fileName = Optional.empty();
        description = "";
        uncommittedItems.clear();
        generatorOptions = makeBlankGeneratorOptions();
        localeHandler = new NoLocaleEnabledLocalHandler();
        setDirty(false);
        updateTitle();
    }

    public ProjectPersistor getProjectPersistor() { return projectPersistor; }

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
                description = openedProject.getDescription();
                generatorOptions = openedProject.getOptions();
                if (generatorOptions == null) generatorOptions = makeBlankGeneratorOptions();
                setDirty(false);
                updateTitle();
                changeHistory.clear();
                uncommittedItems.clear();
                return true;
            }
        } catch (IOException e) {
            fileName = Optional.empty();
            logger.log(ERROR, "open operation failed on " + file, e);
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
            var name = editorUI.findFileNameFromUser(false);
            if(name.isEmpty()) {
                editorUI.alertOnError("No selected file", "No file was selected so project has not been saved.");
                return;
            } else {
                fileName = name;
            }
        }

        fileName.ifPresent((file)-> {
            try {
                uncommittedItems.clear();
                projectPersistor.save(file, description, menuTree, generatorOptions, new NoLocaleEnabledLocalHandler());
                setDirty(false);
            } catch (IOException e) {
                logger.log(ERROR, "save operation failed on " + file, e);
                editorUI.alertOnError("Unable to save file", "Could not save file to chosen location");
            }
        });
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        setDirty(true);
        this.description = description;
    }

    public Set<Integer> getUncommittedItems() {
        return uncommittedItems;
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

    public void setDirty(boolean dirty) {
        if(this.dirty != dirty) {
            this.dirty = dirty;
            updateTitle();
        }
    }

    private void updateTitle() {
        var titlePrettyFile = "New";
        if(fileName.isPresent()) {
            titlePrettyFile = Paths.get(fileName.get()).getFileName().toString();
        }
        editorUI.setTitle(titlePrettyFile + (isDirty()?"* - ":" - ") + TITLE);
    }

    public MenuTree getMenuTree() {
        return menuTree;
    }

    public void applyCommand(EditedItemChange.Command command, MenuItem newItem) {
        applyCommand(command, newItem, menuTree.findParent(newItem));
    }

    public void applyCommand(EditedItemChange.Command command, MenuItem newItem, SubMenuItem parent) {
        if(command == EditedItemChange.Command.NEW) {
            uncommittedItems.add(newItem.getId());
        }
        else if(command == EditedItemChange.Command.REMOVE) {
            uncommittedItems.remove(newItem.getId());
        }

        MenuItem oldItem = changeHistory.stream()
                .filter(item-> item.getItem() != null && item.getItem().getId() == newItem.getId())
                .reduce((first, second) -> second)
                .map(MenuItemChange::getItem)
                .orElse(newItem);
        MenuItemChange change = new EditedItemChange(newItem, oldItem, parent, command);
        applyCommand(change);
    }

    public void applyCommand(MenuItemChange change) {
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

    public LocaleMappingHandler getLocaleHandler() {
        if(localeHandler != null && localeHandler.isLocalSupportEnabled()) return localeHandler;

        if(fileName.isPresent() && Files.exists(Paths.get(fileName.get()).getParent())) {
            var dir = Paths.get(fileName.get()).getParent();
            var i18nDir = dir.resolve("i18n");
            var rootProperties = i18nDir.resolve(MENU_PROJECT_LANG_FILENAME + ".properties");
            if(Files.exists(rootProperties)) {
                localeHandler = new PropertiesLocaleEnabledHandler(new SafeBundleLoader(i18nDir, MENU_PROJECT_LANG_FILENAME));
            } else {
                localeHandler = new NoLocaleEnabledLocalHandler();
            }
        } else {
            localeHandler = new NoLocaleEnabledLocalHandler();
        }
        return localeHandler;
    }

    public void enableLocaleHandler() {
        // short circuit - check if already enabled and skip
        if(localeHandler != null && localeHandler.isLocalSupportEnabled()) return;

        // otherwise try and enable but only if the project has already been saved.
        if (fileName.isPresent() && Files.exists(Paths.get(fileName.get()).getParent())) {
            var dir = Paths.get(fileName.get()).getParent();
            var i18nDir = dir.resolve("i18n");
            var rootProperties = i18nDir.resolve(MENU_PROJECT_LANG_FILENAME + ".properties");
            try {
                Files.writeString(rootProperties, "# Locale file created by tcMenu");
                localeHandler = new PropertiesLocaleEnabledHandler(new SafeBundleLoader(i18nDir, MENU_PROJECT_LANG_FILENAME));
            } catch (IOException e) {
                logger.log(ERROR, "Error creating resource bundle for languages", e);
            }
        }
    }

    public CodeGeneratorOptions makeBlankGeneratorOptions() {
        return new CodeGeneratorOptionsBuilder()
                .withRecursiveNaming(configStore.isDefaultRecursiveNamingOn())
                .withSaveToSrc(configStore.isDefaultSaveToSrcOn())
                .withUseSizedEEPROMStorage(configStore.isDefaultSizedEEPROMStorage())
                .codeOptions();
    }
}
