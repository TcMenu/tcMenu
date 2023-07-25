/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.SearchMenuItemController;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import javafx.stage.Stage;

import java.util.Optional;


/** Example of displaying a splash page for a standalone JavaFX application */
public class SearchMenuItemDialog extends BaseDialogSupport<SearchMenuItemController> {

    private final MenuTree tree;
    private final LocaleMappingHandler localeHandler;

    public SearchMenuItemDialog(MenuTree tree, LocaleMappingHandler localeHandler, Stage stage, boolean modal) {
        this.tree = tree;
        this.localeHandler = localeHandler;
        tryAndCreateDialog(stage, "/ui/searchItems.fxml", bundle.getString("menu.menuitem.search.items"), modal);
    }

    @Override
    protected void initialiseController(SearchMenuItemController controller) throws Exception {
        controller.init(tree, localeHandler);
    }

    public Optional<MenuItem> getResult() {
        return controller.getResult();
    }
}