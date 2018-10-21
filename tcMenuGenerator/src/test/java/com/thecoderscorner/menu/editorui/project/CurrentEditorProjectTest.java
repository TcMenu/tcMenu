package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.project.MenuItemChange.Command.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

public class CurrentEditorProjectTest {

    CurrentEditorProject project;
    private CurrentProjectEditorUI editorUI;
    private ProjectPersistor persistor;
    private BooleanMenuItem item1;
    private BooleanMenuItem item2;
    private SubMenuItem subMenu;

    @Before
    public void setUp() throws Exception {
        editorUI = Mockito.mock(CurrentProjectEditorUI.class);
        persistor = Mockito.mock(ProjectPersistor.class);
        project = new CurrentEditorProject(editorUI, persistor);

        item1 = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(1)
                .withEepromAddr(12)
                .withName("name")
                .withNaming(BooleanMenuItem.BooleanNaming.ON_OFF)
                .menuItem();
        item2 = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withExisting(item1)
                .withId(2)
                .menuItem();
        subMenu = SubMenuItemBuilder.aSubMenuItemBuilder()
                .withId(100)
                .withName("abcd")
                .menuItem();
    }

    @Test
    public void testInsertFollowedByCleanDown() {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        assertTrue(project.getMenuTree().getMenuById(MenuTree.ROOT, 1).isPresent());
        Mockito.when(editorUI.questionYesNo(any(), any())).thenReturn(true);
        assertTrue(project.isDirty());
        project.newProject();
        assertFalse(project.isFileNameSet());
        assertFalse(project.isDirty());
        Mockito.verify(editorUI, Mockito.atLeastOnce()).setTitle("New TcMenu Designer");
    }

    @Test
    public void testAddFollowedByUndo() {
        project.applyCommand(NEW, item1);
        assertTrue(project.getMenuTree().getMenuById(MenuTree.ROOT, 1).isPresent());
        project.undoChange();
        assertFalse(project.getMenuTree().getMenuById(MenuTree.ROOT, 1).isPresent());
        project.redoChange();
        assertTrue(project.getMenuTree().getMenuById(MenuTree.ROOT, 1).isPresent());

        assertTrue(project.isDirty());
    }

    @Test
    public void testMovingItemsAround() {
        assertFalse(project.canRedo());

        project.applyCommand(NEW, item1, MenuTree.ROOT);
        project.applyCommand(NEW, item2, MenuTree.ROOT);
        assertThat(project.getMenuTree().getMenuItems(MenuTree.ROOT), CoreMatchers.is(Arrays.asList(item1, item2)));

        project.applyCommand(DOWN, item1, MenuTree.ROOT);
        assertThat(project.getMenuTree().getMenuItems(MenuTree.ROOT), CoreMatchers.is(Arrays.asList(item2, item1)));

        project.applyCommand(UP, item1, MenuTree.ROOT);
        assertThat(project.getMenuTree().getMenuItems(MenuTree.ROOT), CoreMatchers.is(Arrays.asList(item1, item2)));

        project.undoChange();
        assertThat(project.getMenuTree().getMenuItems(MenuTree.ROOT), CoreMatchers.is(Arrays.asList(item2, item1)));

        assertTrue(project.canRedo());
        assertTrue(project.canUndo());

        project.undoChange();
        assertThat(project.getMenuTree().getMenuItems(MenuTree.ROOT), CoreMatchers.is(Arrays.asList(item1, item2)));

        assertTrue(project.isDirty());
    }

    @Test
    public void testRemovingItemsThenUndo() {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        assertTrue(project.getMenuTree().getMenuById(MenuTree.ROOT, 1).isPresent());
        project.applyCommand(REMOVE, item1, MenuTree.ROOT);
        assertFalse(project.getMenuTree().getMenuById(MenuTree.ROOT, 1).isPresent());

        project.undoChange();
        assertTrue(project.getMenuTree().getMenuById(MenuTree.ROOT, 1).isPresent());
    }

    @Test
    public void testEditingAnItemAndUndoIt() {
        project.applyCommand(NEW, item1, MenuTree.ROOT);
        assertTrue(project.getMenuTree().getMenuById(MenuTree.ROOT, 1).isPresent());

        BooleanMenuItem itemEdit = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withExisting(item1)
                .withName("Hello")
                .menuItem();
        project.applyCommand(EDIT, itemEdit, MenuTree.ROOT);
        Optional<MenuItem> itemReadBack = project.getMenuTree().getMenuById(MenuTree.ROOT, 1);
        assertTrue(itemReadBack.isPresent());
        assertEquals("Hello", itemReadBack.get().getName());

        project.undoChange();
        itemReadBack = project.getMenuTree().getMenuById(MenuTree.ROOT, 1);
        assertTrue(itemReadBack.isPresent());
        assertEquals("name", itemReadBack.get().getName());
    }

    @Test
    public void testSaving() throws IOException {
        project.applyCommand(NEW, item1, MenuTree.ROOT);

        assertEquals("New", project.getFileName());
        Mockito.when(editorUI.findFileNameFromUser(false)).thenReturn(Optional.of("filename"));
        project.saveProject(CurrentEditorProject.EditorSaveMode.SAVE);
        assertFalse(project.isDirty());
        Mockito.verify(persistor).save("filename", project.getMenuTree(), project.getGeneratorOptions());

        Mockito.when(editorUI.findFileNameFromUser(false)).thenReturn(Optional.of("filename2"));
        project.saveProject(CurrentEditorProject.EditorSaveMode.SAVE_AS);
        assertFalse(project.isDirty());
        Mockito.verify(persistor).save("filename2", project.getMenuTree(), project.getGeneratorOptions());

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
        Mockito.when(editorUI.findFileNameFromUser(true)).thenReturn(Optional.of("filename"));
        MenuTree replacementMenu = TestUtils.buildSimpleTree();
        Mockito.when(persistor.open("filename")).thenReturn(new MenuTreeWithCodeOptions(
                replacementMenu, CurrentEditorProject.BLANK_GEN_OPTIONS
        ));
        project.openProject();

        assertEquals(replacementMenu, project.getMenuTree());
    }
}