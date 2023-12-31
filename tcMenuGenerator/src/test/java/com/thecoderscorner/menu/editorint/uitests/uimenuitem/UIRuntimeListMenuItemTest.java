package com.thecoderscorner.menu.editorint.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItemBuilder;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import com.thecoderscorner.menu.persist.SafeBundleLoader;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UIRuntimeListMenuItemTest extends UIMenuItemTestBase {
    private Path tempPath;

    @Start
    public void setup(Stage stage) throws IOException {
        tempPath = Files.createTempDirectory("i18ntest");
        init(stage);
    }

    @AfterEach
    protected void closeWindow() throws IOException {
        Platform.runLater(() -> stage.close());
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testListInCustomMode(FxRobot robot) throws Exception {
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiList = editorUI.createPanelForMenuItem(new RuntimeListMenuItemBuilder()
                .withExisting((RuntimeListMenuItem) menuTree.getMenuById(10).orElseThrow())
                .withInitialRows(10).withCreationMode(RuntimeListMenuItem.ListCreationMode.CUSTOM_RTCALL)
                .menuItem(), menuTree, vng, mockedConsumer);
        if(uiList.isEmpty()) throw new IllegalArgumentException("No menu item found");
        createMainPanel(uiList);

        performAllCommonChecks(uiList.get().getMenuItem(), false, false);

        verifyThat("#initialRowsSpinner", (Spinner<Integer> spinner) -> spinner.getValue() == 10);
    }

    @Test
    public void testListInArrayMode(FxRobot robot) throws Exception {
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        RuntimeListMenuItem menuItem = new RuntimeListMenuItemBuilder()
                .withExisting((RuntimeListMenuItem) menuTree.getMenuById(10).orElseThrow())
                .withInitialRows(10).withCreationMode(RuntimeListMenuItem.ListCreationMode.FLASH_ARRAY)
                .menuItem();
        MenuItemHelper.setMenuState(menuItem, List.of("%list1", "%list2"), menuTree);

        var uiList = editorUI.createPanelForMenuItem(menuItem, menuTree, vng, mockedConsumer);
        if(uiList.isEmpty()) throw new IllegalArgumentException("No menu item found");
        createMainPanel(uiList);

        performAllCommonChecks(uiList.get().getMenuItem(), false, false);
        verifyThat("#initialRowsSpinner", Node::isDisable);
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasListCell("%list1"));
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasListCell("%list2"));

        robot.clickOn("#addEnumEntry");
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasItems(3));
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasListCell("ChangeMe"));

        var item = menuTree.getMenuById(10).orElseThrow();
        assertThat((List<String>)MenuItemHelper.getValueFor(item, menuTree)).containsExactly("%list1", "%list2", "ChangeMe");

        var errorText = "WARNING List values: no locale entry in bundle";
        verifyThat("#uiItemErrors", (Label l)-> l.getText().contains(errorText) && l.isVisible());
        verifyThat("#initialRowsSpinner", (Spinner<Integer> s)-> s.getValue() == 3);

    }

    @Override
    protected LocaleMappingHandler getTestLocaleHandler() {
        try {
            var coreFile = tempPath.resolve("temp.properties");
            Files.writeString(coreFile, """
                    menu.item.name=hello world
                    list1=abc
                    """);
            return new PropertiesLocaleEnabledHandler(new SafeBundleLoader(tempPath, "temp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
