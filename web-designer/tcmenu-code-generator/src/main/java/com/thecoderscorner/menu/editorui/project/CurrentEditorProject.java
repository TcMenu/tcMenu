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
import com.thecoderscorner.menu.editorui.generator.EepromSaveMode;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.BackupManager;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import com.thecoderscorner.menu.persist.SafeBundleLoader;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.ALL_TO_CURRENT;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.ALL_TO_SRC;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * {@link CurrentEditorProject} represents the current project that is being edited by the UI. It supports the controller
 * with all the required functionality to both alter and persist project files. The controller should never perform any
 * write operations directly on the menu tree. Also controls the undo and redo buffers.
 */
public class CurrentEditorProject {

    public static final String NO_CREATOR_SELECTED = "";
    public static final String MENU_PROJECT_LANG_FILENAME = "project-lang";
    public static final String TCMENU_I18N_SRC_DIR = "i18n";

    public enum EditorSaveMode { SAVE_AS, SAVE}

    private static final int UNDO_BUFFER_SIZE = 200;
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    @Getter
    private final ProjectPersistor projectPersistor;
    private final ConfigurationStorage configStore;
    @Getter
    private final Set<Integer> uncommittedItems = new HashSet<>();

    @Getter
    private MenuTree menuTree;
    private Optional<String> fileName = Optional.empty();
    @Getter
    private String description;
    private boolean dirty = true; // always assume dirty at first..
    @Getter
    private CodeGeneratorOptions generatorOptions;
    private LocaleMappingHandler localeHandler = null;

    private final Deque<MenuItemChange> changeHistory = new LinkedList<>();
    private final Deque<MenuItemChange> redoHistory = new LinkedList<>();

    public CurrentEditorProject(ProjectPersistor persistor, ConfigurationStorage storage) {
        projectPersistor = persistor;
        configStore = storage;
        cleanDown();
    }

    public void close() {
    }

    private void cleanDown() {
        menuTree = new MenuTree();
        fileName = Optional.empty();
        description = "";
        uncommittedItems.clear();
        generatorOptions = makeBlankGeneratorOptions();
        localeHandler = LocaleMappingHandler.NOOP_IMPLEMENTATION;
        setDirty(false);
    }

    public void newProject() {
        cleanDown();
        changeHistory.clear();
    }

    public void setGeneratorOptions(CodeGeneratorOptions generatorOptions) {
        setDirty(true);
        this.generatorOptions = generatorOptions;
    }

    public boolean openProject(String file) {
        try {
            openProjectWithoutAlert(file);
            return true;
        } catch (IOException e) {
            cleanDown();
            changeHistory.clear();
            logger.log(ERROR, "open operation failed on " + file, e);
        }
        return false;
    }

    public void openProjectWithoutAlert(String file) throws IOException {
        MenuTreeWithCodeOptions openedProject = projectPersistor.open(file);
        menuTree = openedProject.getMenuTree();
        description = openedProject.getDescription();
        generatorOptions = openedProject.getOptions();
        if (generatorOptions == null) generatorOptions = makeBlankGeneratorOptions();
        setDirty(false);
        setFileName(file);
        checkIfLocalesPresentAndEnable();
        changeHistory.clear();
        uncommittedItems.clear();
    }

    private void checkIfLocalesPresentAndEnable() {
        // we must always reset first.
        localeHandler = LocaleMappingHandler.NOOP_IMPLEMENTATION;
        if(!isFileNameSet() || fileName.isEmpty() || Paths.get(fileName.get()).getParent() == null) {
            return;
        }
        Path projDir = Paths.get(fileName.get()).getParent();
        if(Files.exists(projDir.resolve(TCMENU_I18N_SRC_DIR))) {
            enableLocaleHandler();
        }
    }

    public void saveProject(EditorSaveMode saveMode) {
        var backupManager = new BackupManager(configStore);
        Path projFile = Paths.get(fileName.get());
        Path dir = projFile.getParent();
        try {
            backupManager.backupFile(dir, projFile);
        } catch (IOException e) {
            logger.log(ERROR, "Unable to backup project file, continuing anyway", e);
        }

        fileName.ifPresent((file)-> {
            try {
                uncommittedItems.clear();
                projectPersistor.save(file, description, menuTree, generatorOptions, localeHandler);
                setDirty(false);
            } catch (IOException e) {
                logger.log(ERROR, "save operation failed on " + file, e);
                //FIXME alertOnError("Unable to save file", "Could not save file to chosen location");
            }
        });
    }

    protected void setFileName(String name) {
        fileName = Optional.ofNullable(name);
    }

    public void setDescription(String description) {
        setDirty(true);
        this.description = description;
    }

    public boolean isFileNameSet() {
        return fileName.isPresent();
    }

    public String getFileName() {
        return fileName.orElse("New");
    }

    public boolean isDirty() {
        return dirty || localeHandler.isDirty(Optional.empty());
    }

    public void setDirty(boolean dirty) {
        if(this.dirty != dirty) {
            this.dirty = dirty;
        }
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
            var i18nDir = dir.resolve(TCMENU_I18N_SRC_DIR);
            var rootProperties = i18nDir.resolve(MENU_PROJECT_LANG_FILENAME + ".properties");
            if(Files.exists(rootProperties)) {
                localeHandler = new PropertiesLocaleEnabledHandler(new SafeBundleLoader(i18nDir, MENU_PROJECT_LANG_FILENAME));
            } else {
                localeHandler = LocaleMappingHandler.NOOP_IMPLEMENTATION;
            }
        } else {
            localeHandler = LocaleMappingHandler.NOOP_IMPLEMENTATION;
        }
        return localeHandler;
    }

    public void enableLocaleHandler() {
        // short circuit - check if already enabled and skip
        if(localeHandler != null && localeHandler.isLocalSupportEnabled()) return;

        // otherwise try and enable but only if the project has already been saved.
        if (fileName.isPresent() && Files.exists(Paths.get(fileName.get()).getParent())) {
            try {
                var dir = Paths.get(fileName.get()).getParent();
                var i18nDir = dir.resolve(TCMENU_I18N_SRC_DIR);
                if(!Files.exists(i18nDir)) {
                    Files.createDirectory(i18nDir);
                }
                var rootProperties = i18nDir.resolve(MENU_PROJECT_LANG_FILENAME + ".properties");
                if(!Files.exists(rootProperties)) {
                    Files.writeString(rootProperties, "# Locale file created by tcMenu");
                }
                localeHandler = new PropertiesLocaleEnabledHandler(new SafeBundleLoader(i18nDir, MENU_PROJECT_LANG_FILENAME));

            } catch (IOException e) {
                logger.log(ERROR, "Error creating resource bundle for languages", e);
            }
        }
    }

    public CodeGeneratorOptions makeBlankGeneratorOptions() {
        return new CodeGeneratorOptionsBuilder()
                .withRecursiveNaming(configStore.isDefaultRecursiveNamingOn())
                .withSaveLocation(configStore.isDefaultSaveToSrcOn() ? ALL_TO_SRC : ALL_TO_CURRENT)
                .withEepromSaveMode(EepromSaveMode.DYNAMIC_WRITE_BY_ID)
                .codeOptions();
    }
}
