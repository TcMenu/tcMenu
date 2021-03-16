/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoDirectoryStructureHelper;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.LibraryStatus;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.project.*;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.uimodel.UISubMenuItem;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector.ReleaseType;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoDirectoryStructureHelper.DirectoryPath.SKETCHES_DIR;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoDirectoryStructureHelper.DirectoryPath.TCMENU_DIR;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.*;
import static com.thecoderscorner.menu.editorui.uitests.UiUtils.pushCtrlAndKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class MenuEditorTestCases {

    private CurrentProjectEditorUI editorProjectUI;
    private ProjectPersistor persistor;
    private ArduinoLibraryInstaller installer;
    private CurrentEditorProject project;
    private Stage stage;
    private CodePluginManager simulatedCodeManager;
    private ArduinoDirectoryStructureHelper dirHelper = new ArduinoDirectoryStructureHelper();

    @Start
    public void onStart(Stage stage) throws Exception {

        dirHelper.initialise();
        dirHelper.createSketch(TCMENU_DIR, "exampleSketch1", true);
        dirHelper.createSketch(TCMENU_DIR, "exampleSketch2", true);
        dirHelper.createSketch(SKETCHES_DIR, "sketches1", true);
        dirHelper.createSketch(SKETCHES_DIR, "sketches2", true);
        dirHelper.createSketch(SKETCHES_DIR, "sketchesIgnore", false);

        // load the main window FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/menuEditor.fxml"));
        Pane myPane = loader.load();

        // we need to mock a few things around the edges to make testing easier.
        editorProjectUI = mock(CurrentProjectEditorUI.class);
        when(editorProjectUI.createPanelForMenuItem(any(), any(), any(), any())).thenReturn(Optional.empty());

        persistor = mock(ProjectPersistor.class);
        installer = mock(ArduinoLibraryInstaller.class);

        simulatedCodeManager = mock(CodePluginManager.class);
        when(simulatedCodeManager.getLoadedPlugins()).thenReturn(Arrays.asList(generateCodePluginConfig()));

        // and we are always up to date library wise in unit test land
        when(installer.statusOfAllLibraries()).thenReturn(new LibraryStatus(true, true, true, true));
        when(installer.findLibraryInstall("tcMenu")).thenReturn(dirHelper.getTcMenuPath());
        when(installer.getArduinoDirectory()).thenReturn(dirHelper.getSketchesDir());
        when(installer.getVersionOfLibrary("module.name", AVAILABLE_PLUGIN)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.getVersionOfLibrary("module.name", CURRENT_PLUGIN)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.getVersionOfLibrary("tcMenu", AVAILABLE_LIB)).thenReturn(new VersionInfo("1.0.1"));
        when(installer.getVersionOfLibrary("tcMenu", CURRENT_LIB)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.getVersionOfLibrary("IoAbstraction", AVAILABLE_LIB)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.getVersionOfLibrary("IoAbstraction", CURRENT_LIB)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.getVersionOfLibrary("LiquidCrystalIO", AVAILABLE_LIB)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.getVersionOfLibrary("LiquidCrystalIO", CURRENT_LIB)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.getVersionOfLibrary("TaskManagerIO", AVAILABLE_LIB)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.getVersionOfLibrary("TaskManagerIO", CURRENT_LIB)).thenReturn(new VersionInfo("1.0.0"));
        when(installer.statusOfAllLibraries()).thenReturn(new LibraryStatus(false, true, true, true));

        // create a basic project, that has a few menu items in it.
        project = new CurrentEditorProject(
                editorProjectUI,
                persistor
        );

        ConfigurationStorage storage = mock(ConfigurationStorage.class);
        when(storage.loadRecents()).thenReturn(List.of("recentItem1", "recentItem2"));
        when(storage.getRegisteredKey()).thenReturn("UnitTesterII");
        when(storage.isUsingArduinoIDE()).thenReturn(true);
        when(storage.getArduinoOverrideDirectory()).thenReturn(Optional.empty());
        when(storage.getVersion()).thenReturn("1.1.1");

        // set up the controller and stage..
        MenuEditorController controller = loader.getController();
        LibraryVersionDetector libDetector = mock(LibraryVersionDetector.class);
        when(libDetector.getReleaseType()).thenReturn(ReleaseType.STABLE);
        controller.initialise(project, installer, editorProjectUI, simulatedCodeManager, storage, libDetector);
        this.stage = stage;

        Scene myScene = new Scene(myPane);
        stage.setScene(myScene);
        stage.show();
    }



    private CodePluginConfig generateCodePluginConfig() {
        return new CodePluginConfig("module.name", "PluginName", "1.0.0",
                                    Arrays.asList());
    }

    @AfterEach
    public void tidyUp() throws IOException {
        dirHelper.cleanUp();
        Platform.runLater(()-> stage.close());
    }

    @Test
    public void testNewProjectAddItemThenUndo(FxRobot robot) throws IOException {
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        // we stub out the new item dialog as it has its
        MenuItem itemToAdd = addItemToTheTreeUsingPlusButton(robot, aNewMenuItem());

        // make sure the item is in the tree and make sure it's been drawn properly
        assertOnItemInTree(itemToAdd, true);
        checkTheTreeMatchesMenuTree(robot, itemToAdd);

        // pressing Undo should remove the item from the list, after pressing check its not in MenuTree or on display
        pushCtrlAndKey(robot, KeyCode.Z);
        assertOnItemInTree(itemToAdd, false);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        // pressing Redo should bring the item back
        pushCtrlAndKey(robot, KeyCode.Y);
        assertOnItemInTree(itemToAdd, true);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        when(editorProjectUI.findFileNameFromUser(false)).thenReturn(Optional.of("fileName"));
        assertTrue(project.isDirty());
        pushCtrlAndKey(robot, KeyCode.A);
        verify(persistor, atLeastOnce()).save("fileName", project.getMenuTree(), project.getGeneratorOptions());

        pushCtrlAndKey(robot, KeyCode.L);
        verify(editorProjectUI, atLeastOnce()).showRomLayoutDialog(project.getMenuTree());

        pushCtrlAndKey(robot, KeyCode.B);
        verify(editorProjectUI, atLeastOnce()).showAboutDialog(installer);

        pushCtrlAndKey(robot, KeyCode.G);
        verify(editorProjectUI, atLeastOnce()).showCodeGeneratorDialog(project, installer);
    }

    @Test
    public void testAddingRemovingAndMovingOnOpenedProject(FxRobot robot) throws Exception {
        openTheCompleteMenuTree(robot);

        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        // smoke test of the prototype area, there is a proper test of the text rendering elsewhere
        assertTrue(((TextArea)robot.lookup("#prototypeTextArea").query()).getText().contains("FloatTest         -12345.1235"));

        // we stub out the new item dialog as it has its
        MenuItem itemToAdd = aNewMenuItem();
        Mockito.when(editorProjectUI.showNewItemDialog(project.getMenuTree())).thenReturn(Optional.ofNullable(itemToAdd));

        // now we get hold of the sub menu and the items in the submenu
        SubMenuItem subItem = project.getMenuTree().getSubMenuById(100).get();
        MenuItem childItem = project.getMenuTree().getMenuById(2).get();
        TreeView<MenuItem> treeView = robot.lookup("#menuTree").query();

        // change selection in the tree to the submenu and press the add item button
        assertTrue(recursiveSelectTreeItem(treeView, treeView.getRoot(), subItem));
        robot.clickOn("#menuTreeAdd");

        // make sure the item is in the tree and make sure it's been drawn properly
        assertOnItemInTree(itemToAdd, true);
        checkTheTreeMatchesMenuTree(robot, itemToAdd);

        // now we are going to move the new item up then back down. Before this we will check
        // the ordering to ensure it started off in the right order.
        assertThat(project.getMenuTree().getMenuItems(subItem)).containsExactly(
                childItem,
                itemToAdd
        );
        // move it up and see that the menu changes order and gets drawn properly
        robot.clickOn("#menuTreeUp");
        assertThat(project.getMenuTree().getMenuItems(subItem)).containsExactly(
                itemToAdd,
                childItem
        );
        checkTheTreeMatchesMenuTree(robot, itemToAdd);

        // and back again to the previous ordering.
        robot.clickOn("#menuTreeDown");
        assertThat(project.getMenuTree().getMenuItems(subItem)).containsExactly(
                childItem,
                itemToAdd
        );
        checkTheTreeMatchesMenuTree(robot, itemToAdd);

        // and then lastly lets get rid of the item we just added. Root should be selected afterwards.
        robot.clickOn("#menuTreeRemove");
        assertOnItemInTree(itemToAdd, false);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        // at this point the project should be dirty
        assertTrue(project.isDirty());

        // save the project
        pushCtrlAndKey(robot, KeyCode.S);
        Mockito.verify(persistor, atLeastOnce()).save("fileName", project.getMenuTree(), project.getGeneratorOptions());

        // now project should be clean
        assertFalse(project.isDirty());
    }

    @Test
    public void testRecentsExamplesAndSketches(FxRobot robot) throws InterruptedException {
        var recentItems = TestUtils.findItemsInMenuWithId(robot, "menuRecents");
        var itemStrings = recentItems.stream().map(r-> r.getText()).collect(Collectors.toList());
        assertThat(itemStrings).containsExactlyInAnyOrder("recentItem1", "recentItem2");
        project.applyCommand(EditedItemChange.Command.NEW, aNewMenuItem());
        when(editorProjectUI.questionYesNo(any(), any())).thenReturn(Boolean.FALSE);
        TestUtils.clickOnMenuItemWithText(robot, "recentItem1");
        verify(editorProjectUI).questionYesNo(any(), any());
        clearInvocations(editorProjectUI);

        var exampleItems = TestUtils.findItemsInMenuWithId(robot, "examplesMenu");
        var exampleStrings = exampleItems.stream().map(r-> r.getText()).collect(Collectors.toList());
        assertThat(exampleStrings).containsExactlyInAnyOrder("exampleSketch1", "exampleSketch2");
        project.applyCommand(EditedItemChange.Command.NEW, aNewMenuItem());
        when(editorProjectUI.questionYesNo(any(), any())).thenReturn(Boolean.FALSE);
        TestUtils.clickOnMenuItemWithText(robot, "exampleSketch2");
        verify(editorProjectUI).questionYesNo(any(), any());

        var sketchItems = TestUtils.findItemsInMenuWithId(robot, "menuSketches");
        var sketchStrings = sketchItems.stream().map(r-> r.getText()).collect(Collectors.toList());
        assertThat(sketchStrings).containsExactlyInAnyOrder("sketches1", "sketches2");
    }

    @Test
    public void testRemovingASubMenuThenUndoIt(FxRobot robot) throws Exception {
        // open the usual complete menu and then check it
        openTheCompleteMenuTree(robot);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);


        TreeView<MenuItem> treeView = robot.lookup("#menuTree").query();
        SubMenuItem subItem = project.getMenuTree().getSubMenuById(100).get();
        MenuItem subChildItem = project.getMenuTree().getMenuById(2).get();
        recursiveSelectTreeItem(treeView, treeView.getRoot(), subItem);

        robot.clickOn("#menuTreeRemove");

        // shouldn't have done anything
        assertOnItemInTree(subItem, false);
        assertOnItemInTree(subChildItem, false);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);
        TestUtils.runOnFxThreadAndWait(treeView::requestFocus);

        // now do it again and press yes this time..
        pushCtrlAndKey(robot, KeyCode.Z);

        assertOnItemInTree(subItem, true);
        assertOnItemInTree(subChildItem, true);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        // now do it again and press yes this time..
        pushCtrlAndKey(robot, KeyCode.Y);

        assertOnItemInTree(subItem, false);
        assertOnItemInTree(subChildItem, false);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);
    }

    @Test
    void testNewProjectWithOverrideWhenDirty(FxRobot robot) throws Exception {
        openTheCompleteMenuTree(robot);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);
        MenuItem rgbItem = addItemToTheTreeUsingPlusButton(robot, new Rgb32MenuItemBuilder()
                .withId(223).withName("RgbForMe").withAlpha(true).menuItem());

        // now the tree should be dirty
        assertTrue(project.isDirty());

        // send a menu -> file -> new which should ask first as structure is dirty, say no this time around. Should prevent action
        when(editorProjectUI.questionYesNo("Changes will be lost", "Do you want to discard the current menu?")).thenReturn(false);
        pushCtrlAndKey(robot, KeyCode.N);
        assertOnItemInTree(rgbItem, true);

        // send a menu -> file -> new which should ask first as structure is dirty, say yes this time around. Action show go through
        when(editorProjectUI.questionYesNo("Changes will be lost", "Do you want to discard the current menu?")).thenReturn(true);
        pushCtrlAndKey(robot, KeyCode.N);
        assertOnItemInTree(rgbItem, false);
    }


    @Test
    void testCopyingSingleItemInTree(FxRobot robot) throws Exception {
        // open the usual suspect.
        openTheCompleteMenuTree(robot);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        // select the item with ID 100 which is a submenu.
        TreeView<MenuItem> treeView = robot.lookup("#menuTree").query();
        SubMenuItem subItem = project.getMenuTree().getSubMenuById(100).get();
        assertTrue(recursiveSelectTreeItem(treeView, treeView.getRoot(), subItem));

        Thread.sleep(5000);
        // sub menus can not be copied
        verifyThat("#menuTreeCopy", node -> !node.isDisabled());

        // now select the first sub item of the sub menu, which can be copied
        MenuItem itemToCopy = project.getMenuTree().getMenuById(2).get();
        assertTrue(recursiveSelectTreeItem(treeView, treeView.getRoot(), itemToCopy));

        // make sure there isn't an ID of 101 already in the tree and then copy it.
        assertFalse(project.getMenuTree().getMenuById(101).isPresent());

        when(persistor.itemsToCopyText(any(), any())).thenReturn("tcMenuCopy:[{\"parentId\":0,\"type\":\"analogItem\",\"item\":{\"maxValue\":255,\"offset\":0,\"divisor\":100,\"unitName\":\"A\",\"name\":\"Current\",\"id\":2,\"eepromAddress\":4,\"functionName\":\"onCurrentChange\",\"readOnly\":false,\"localOnly\":false,\"visible\":true}}]");
        when(persistor.copyTextToItems(any())).thenReturn(List.of(new PersistedMenu(MenuTree.ROOT, itemToCopy)));

        robot.clickOn("#menuTreeCopy");
        robot.clickOn("#menuTreePaste");

        // now check that the new duplicate is created.
        Optional<MenuItem> maybeItem = project.getMenuTree().getMenuById(101);
        assertTrue(maybeItem.isPresent());
        assertThat(itemToCopy).isExactlyInstanceOf(maybeItem.get().getClass());

        checkTheTreeMatchesMenuTree(robot, itemToCopy);
    }

    @Test
    void testAddOnSubmenuNotActiveAddsToParent(FxRobot robot) throws Exception {
        // open the usual suspect.
        openTheCompleteMenuTree(robot);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        // select item 1, in the ROOT menu
        TreeView<MenuItem> treeView = robot.lookup("#menuTree").query();
        MenuItem item = project.getMenuTree().getMenuById(1).get();
        assertTrue(recursiveSelectTreeItem(treeView, treeView.getRoot(), item));

        MenuItem addedItem = addItemToTheTreeUsingPlusButton(robot, new ScrollChoiceMenuItemBuilder()
                .withId(293).withName("123").withVariableName("abc123").withFunctionName("ed209")
                .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM)
                .withItemWidth(10).withNumEntries(5).withEepromOffset(100)
                .withVariable("ramVariable").menuItem());
        checkTheTreeMatchesMenuTree(robot, addedItem);

        assertTrue(project.getMenuTree().getMenuById(addedItem.getId()).isPresent());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPuttingUpSubMenuEditorPane(FxRobot robot) throws Exception {
        // open the usual menu
        openTheCompleteMenuTree(robot);
        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);

        // get hold of the tree and the sub menu item
        TreeView<MenuItem> treeView = robot.lookup("#menuTree").query();
        SubMenuItem subItem = project.getMenuTree().getSubMenuById(100).get();

        ArgumentCaptor<BiConsumer> captor = ArgumentCaptor.forClass(BiConsumer.class);

        // set up the editorUI to return a panel when the submenu is chosen.
        VariableNameGenerator vng = new VariableNameGenerator(project.getMenuTree(), false);
        UISubMenuItem panel = new UISubMenuItem(subItem, new MenuIdChooserImpl(project.getMenuTree()), vng, (item1, item2) -> {});
        Mockito.when(editorProjectUI.createPanelForMenuItem(eq(subItem), eq(project.getMenuTree()), any(), any()))
                .thenReturn(Optional.ofNullable(panel));
        recursiveSelectTreeItem(treeView, treeView.getRoot(), subItem);

        // get the consumer that takes change from the UIMenuItem back to the main controller.
        // and send a simulated update to it, ensure it is processed and the tree updated.
        verify(editorProjectUI).createPanelForMenuItem(eq(subItem), eq(project.getMenuTree()), any(), captor.capture());
        SubMenuItem adjustedItem = SubMenuItemBuilder.aSubMenuItemBuilder()
                .withExisting(subItem)
                .withName("AdjustedName")
                .menuItem();
        captor.getValue().accept(subItem, adjustedItem);

        // check it's been processed
        MenuItem readBackAdjusted = project.getMenuTree().getMenuById(100).get();
        assertEquals(readBackAdjusted, adjustedItem);
        assertEquals("AdjustedName", readBackAdjusted.getName());

        // check the tree is updated
        checkTheTreeMatchesMenuTree(robot, adjustedItem);
    }

    @Test
    void testTheRootAreaInformationPanel(FxRobot robot) throws Exception {
        openTheCompleteMenuTree(robot);
        SubMenuItem subItem = project.getMenuTree().getSubMenuById(100).get();
        TreeView<MenuItem> treeView = robot.lookup("#menuTree").query();

        assertTrue(recursiveSelectTreeItem(treeView, treeView.getRoot(), subItem));

        assertTrue(recursiveSelectTreeItem(treeView, treeView.getRoot(), MenuTree.ROOT));

        verifyThat("#libdocsurl", (Hyperlink hl) -> hl.getText().equals("Browse docs and watch starter videos (F1 at any time)"));
        verifyThat("#githuburl", (Hyperlink hl) -> hl.getText().equals("Please give us a star on github if you like this tool"));

        robot.clickOn("#libdocsurl");
        verify(editorProjectUI).browseToURL(AppInformationPanel.LIBRARY_DOCS_URL);
        robot.clickOn("#githuburl");
        verify(editorProjectUI).browseToURL(AppInformationPanel.GITHUB_PROJECT_URL);

        List<String> pluginSet = robot.lookup(".pluginInfoLbl").queryAll().stream()
                .map(p -> ((Label)p).getText())
                .collect(Collectors.toList());
        assertThat(pluginSet).containsExactlyInAnyOrder("- PluginName. Installed: 1.0.0, Available: 1.0.0");

        when(installer.statusOfAllLibraries()).thenReturn(new LibraryStatus(true, true, true, true));
        when(installer.getVersionOfLibrary("tcMenu", AVAILABLE_LIB)).thenReturn(new VersionInfo("1.0.1"));
        when(installer.getVersionOfLibrary("tcMenu", CURRENT_LIB)).thenReturn(new VersionInfo("1.0.1"));

        checkTheTreeMatchesMenuTree(robot, MenuTree.ROOT);
    }

    /**
     * Adds an item into the live tree by pressing the + button, but first setting
     * up the required dependencies.
     * @param robot the fx robot
     * @return the new item created.
     */
    private MenuItem addItemToTheTreeUsingPlusButton(FxRobot robot, MenuItem itemToAdd) {
        // create a new item in the tree.
        Mockito.when(editorProjectUI.showNewItemDialog(project.getMenuTree())).thenReturn(Optional.ofNullable(itemToAdd));
        robot.clickOn("#menuTreeAdd");
        return itemToAdd;
    }

    /**
     * Recursively find and then select an item in the menu item tree
     * @param treeView the tree view
     * @param treeItem the first item (recursive call start at root normally)
     * @param subItem the item to find.
     * @return true if the item was found.
     */
    @SuppressWarnings("Duplicates") // because the duplicate is not trivial to fix and factoring out looks worse.
    private boolean recursiveSelectTreeItem(TreeView<MenuItem> treeView, TreeItem<MenuItem> treeItem, MenuItem subItem) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        if(treeItem.getValue().equals(subItem)) {
            Platform.runLater(()-> {
                treeView.getSelectionModel().select(treeItem);
                latch.countDown();
            });
            latch.await(1000, TimeUnit.MILLISECONDS);
            return true;
        }

        for (TreeItem<MenuItem> item : treeItem.getChildren()) {
            if(item.getValue().equals(subItem)) {
                Platform.runLater(()-> {
                    treeView.getSelectionModel().select(item);
                    latch.countDown();
                });
                latch.await(1000, TimeUnit.MILLISECONDS);
                return true;
            }
            if(item.getValue().hasChildren()) {
                if(recursiveSelectTreeItem(treeView, item, subItem)) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Assert if the item was (or was not) found anywhere in the tree
     * @param itemToAdd item to check
     * @param shouldBeThere if it should be expected to be there or not.
     */
    private void assertOnItemInTree(MenuItem itemToAdd, boolean shouldBeThere) {
        MenuTree tree = project.getMenuTree();
        Set<MenuItem> items = tree.getAllSubMenus().stream()
                .flatMap(sub-> tree.getMenuItems(sub).stream())
                .collect(Collectors.toSet());

        assertEquals(shouldBeThere, items.contains(itemToAdd));
    }

    /**
     * Helper to create a menuitem
     * @return a menu item
     */
    private MenuItem aNewMenuItem() {
        return EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(105)
                .withName("Smokin")
                .withEepromAddr(22)
                .menuItem();
    }

    /**
     * Simulates file > open being pressed and simulates the load by loading the complete menu set.
     * @param robot fx robot.
     * @throws Exception
     */
    private void openTheCompleteMenuTree(FxRobot robot) throws Exception {

        // we are simulating the persistence so just mock out the calls to open the file.
        when(editorProjectUI.findFileNameFromUser(true)).thenReturn(Optional.of("fileName"));
        when(persistor.open("fileName")).thenReturn(new MenuTreeWithCodeOptions(
                TestUtils.buildCompleteTree(), project.getGeneratorOptions()
        ));

        // Perform file open then make sure the file opened.
        pushCtrlAndKey(robot, KeyCode.O);
        assertTrue(project.isFileNameSet());
        assertEquals("fileName", project.getFileName());
    }

    /**
     * Perform a deep check on the top two levels of the tree
     * @param robot the fx robot
     * @param selected the item that should be selected.
     */
    private void checkTheTreeMatchesMenuTree(FxRobot robot, MenuItem selected) {
        // First get the tree and make sure it has a ROOT menu item
        TreeView<MenuItem> treeView = robot.lookup("#menuTree").query();
        assertEquals(MenuTree.ROOT, treeView.getRoot().getValue());

        assertEquals(selected, treeView.getSelectionModel().getSelectedItem().getValue());

        // now we check the top level entries.
        MenuTree menuTree = project.getMenuTree();
        List<MenuItem> childItems = treeView.getRoot().getChildren().stream()
                .map(TreeItem::getValue)
                .collect(Collectors.toList());
        assertEquals(menuTree.getMenuItems(MenuTree.ROOT), childItems);

        // now check any elements in the first level down by comparing the tree view against
        // the menuTree.
        menuTree.getMenuItems(MenuTree.ROOT).stream()
                .filter(MenuItem::hasChildren)
                .forEach(subMenu -> {
                    Optional<TreeItem<MenuItem>> subTree =  treeView.getRoot().getChildren().stream()
                            .filter(item -> item.getValue().getId() == subMenu.getId())
                            .findFirst();
                    assertTrue(subTree.isPresent());

                    List<MenuItem> subChildTree = subTree.get().getChildren().stream()
                            .map(TreeItem::getValue)
                            .collect(Collectors.toList());

                    assertEquals(menuTree.getMenuItems(subMenu), subChildTree);
                });
    }

}
