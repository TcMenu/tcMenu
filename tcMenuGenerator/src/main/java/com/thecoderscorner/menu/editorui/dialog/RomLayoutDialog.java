/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.RomLayoutController;
import javafx.stage.Stage;

public class RomLayoutDialog extends BaseDialogSupport<RomLayoutController> {

    private final MenuTree menuTree;

    public RomLayoutDialog(Stage stage, MenuTree menuTree, boolean modal) {
        this.menuTree = menuTree;
        tryAndCreateDialog(stage, "/ui/romLayoutDialog.fxml", "Rom Layout", modal);
    }

    @Override
    protected void initialiseController(RomLayoutController controller) {
        controller.init(menuTree);
    }
}
