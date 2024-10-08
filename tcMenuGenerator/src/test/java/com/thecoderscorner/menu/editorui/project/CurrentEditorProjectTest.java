/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static com.thecoderscorner.menu.editorui.project.EditedItemChange.Command.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrentEditorProjectTest {
    public static final String FILE_NAME_SIMULATED = "/var/tmp/fileName.emf";

    CurrentEditorProject project;
    private CurrentProjectEditorUIImpl editorUI;
    private ProjectPersistor persistor;
    private BooleanMenuItem item1;

    @BeforeEach
    public void setUp() {
        MenuEditorApp.configureBundle(MenuEditorApp.EMPTY_LOCALE);

        editorUI = mock(CurrentProjectEditorUIImpl.class);
        persistor = mock(ProjectPersistor.class);
        ConfigurationStorage configStore = mock(ConfigurationStorage.class);
        when(configStore.getVersion()).thenReturn("3.2.0");
        project = new CurrentEditorProject(editorUI, persistor, configStore, mock(ScheduledExecutorService.class), mock(TccProjectWatcher.class));

        item1 = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(1)
                .withEepromAddr(12)
                .withName("name")
                .withNaming(BooleanMenuItem.BooleanNaming.ON_OFF)
                .menuItem();
    }

    @Test
    public void testInsertFollowedByCleanDown() {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        assertTrue(project.getMenuTree().getMenuById(1).isPresent());
        Mockito.when(editorUI.questionYesNo(any(), any())).thenReturn(true);
        assertTrue(project.isDirty());
        project.newProject();
        assertFalse(project.isFileNameSet());
        assertFalse(project.isDirty());
        Mockito.verify(editorUI, Mockito.atLeastOnce()).setTitle("New - Embedded Menu Designer 3.2.0");
    }

    @Test
    public void testAddFollowedByUndo() {
        project.applyCommand(NEW, item1);
        assertTrue(project.getMenuTree().getMenuById(1).isPresent());
        project.undoChange();
        assertFalse(project.getMenuTree().getMenuById(1).isPresent());
        project.redoChange();
        assertTrue(project.getMenuTree().getMenuById(1).isPresent());

        assertTrue(project.isDirty());
    }

    @Test
    public void testRemovingItemsThenUndo() {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        assertTrue(project.getMenuTree().getMenuById(1).isPresent());
        project.applyCommand(REMOVE, item1, MenuTree.ROOT);
        assertFalse(project.getMenuTree().getMenuById(1).isPresent());

        project.undoChange();
        assertTrue(project.getMenuTree().getMenuById(1).isPresent());
    }

    @Test
    public void testEditingAnItemAndUndoIt() {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        assertTrue(project.getMenuTree().getMenuById(1).isPresent());

        BooleanMenuItem itemEdit = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withExisting(item1)
                .withName("Hello")
                .menuItem();
        project.applyCommand(EDIT, itemEdit, MenuTree.ROOT);
        Optional<MenuItem> itemReadBack = project.getMenuTree().getMenuById(1);
        assertTrue(itemReadBack.isPresent());
        assertEquals("Hello", itemReadBack.get().getName());

        project.undoChange();
        itemReadBack = project.getMenuTree().getMenuById(1);
        assertTrue(itemReadBack.isPresent());
        assertEquals("name", itemReadBack.get().getName());
    }

    @Test
    public void testSaving() throws IOException {
        project.applyCommand(NEW, item1, MenuTree.ROOT);

        assertEquals("New", project.getFileName());
        Mockito.when(editorUI.findFileNameFromUser(false)).thenReturn(Optional.of(FILE_NAME_SIMULATED));
        project.saveProject(CurrentEditorProject.EditorSaveMode.SAVE);
        assertFalse(project.isDirty());
        Mockito.verify(persistor).save(eq(FILE_NAME_SIMULATED), eq(""), eq(project.getMenuTree()), eq(project.getGeneratorOptions()), any());

        Mockito.when(editorUI.findFileNameFromUser(false)).thenReturn(Optional.of(FILE_NAME_SIMULATED + "1"));
        project.saveProject(CurrentEditorProject.EditorSaveMode.SAVE_AS);
        assertFalse(project.isDirty());
        Mockito.verify(persistor).save(eq(FILE_NAME_SIMULATED + "1"), eq(""), eq(project.getMenuTree()), eq(project.getGeneratorOptions()), any());

    }

    @Test
    public void testNewWileDirtyAsksQuestionAnswerYes() {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        Mockito.when(editorUI.questionYesNo(any(), any())).thenReturn(true);
        project.newProject();
        assertFalse(project.isDirty());
    }

    @Test
    public void testNewWileDirtyAsksQuestionAnswerNo() {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        Mockito.when(editorUI.questionYesNo(any(), any())).thenReturn(false);
        project.newProject();
        assertTrue(project.isDirty());
    }

    @Test
    public void testOpening() throws IOException {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        Mockito.when(editorUI.questionYesNo(any(), any())).thenReturn(true);
        Mockito.when(editorUI.findFileNameFromUser(true)).thenReturn(Optional.of(FILE_NAME_SIMULATED));
        MenuTree replacementMenu = TestUtils.buildSimpleTree();
        Mockito.when(persistor.open(FILE_NAME_SIMULATED)).thenReturn(new MenuTreeWithCodeOptions(
                replacementMenu, new CodeGeneratorOptionsBuilder().codeOptions(), "my project description"
        ));
        project.openProject();

        assertEquals(replacementMenu, project.getMenuTree());
    }
}