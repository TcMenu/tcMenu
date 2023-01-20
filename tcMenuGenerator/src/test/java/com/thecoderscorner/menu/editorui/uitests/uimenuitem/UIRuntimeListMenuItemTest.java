package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItem;
import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.UILargeNumberMenuItem;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
