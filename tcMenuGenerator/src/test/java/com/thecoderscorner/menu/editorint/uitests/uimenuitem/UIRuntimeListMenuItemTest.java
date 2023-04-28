package com.thecoderscorner.menu.editorint.uitests.uimenuitem;

import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(ApplicationExtension.class)
public class UIRuntimeListMenuItemTest extends UIMenuItemTestBase {
    @Start
    public void setup(Stage stage) {
        init(stage);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    public void testListCanConstruct() throws Exception {
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiList = editorUI.createPanelForMenuItem(menuTree.getMenuById(10).get(), menuTree, vng, mockedConsumer);
        if(uiList.isEmpty()) throw new IllegalArgumentException("No menu item found");
        createMainPanel(uiList);

        performAllCommonChecks(uiList.get().getMenuItem(), false, false);
    }
}
