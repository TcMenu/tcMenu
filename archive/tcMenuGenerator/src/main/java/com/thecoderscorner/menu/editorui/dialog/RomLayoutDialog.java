/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.RomLayoutController;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import javafx.stage.Stage;

public class RomLayoutDialog extends BaseDialogSupport<RomLayoutController> {

    private final MenuTree menuTree;
    private final LocaleMappingHandler localeHandler;

    public RomLayoutDialog(Stage stage, MenuTree menuTree, LocaleMappingHandler localeHandler, boolean modal) {
        this.menuTree = menuTree;
        this.localeHandler = localeHandler;
        tryAndCreateDialog(stage, "/ui/romLayoutDialog.fxml", bundle.getString("rom.layout.title"), modal, .7);
    }

    @Override
    protected void initialiseController(RomLayoutController controller) {
        controller.init(menuTree, localeHandler);
    }
}
