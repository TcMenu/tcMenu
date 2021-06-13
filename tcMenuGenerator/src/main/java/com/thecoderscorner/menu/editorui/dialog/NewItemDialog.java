/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.NewItemController;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.stage.Stage;

import java.util.Optional;

public class NewItemDialog extends BaseDialogSupport<NewItemController> {
    private final MenuTree tree;
    private final CurrentProjectEditorUI editorUI;

    public NewItemDialog(Stage stage, MenuTree tree, CurrentProjectEditorUI editorUI, boolean modal) {
        this.tree = tree;
        this.editorUI = editorUI;

        tryAndCreateDialog(stage, "/ui/newItemDialog.fxml", "Create new item", modal);
    }

    public Optional<MenuItem> getResultOrEmpty() {
        return controller.getResult();
    }

    @Override
    protected void initialiseController(NewItemController controller) {
        controller.initialise(new MenuIdChooserImpl(tree), editorUI);
    }
}
