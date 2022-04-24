package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.ConfigureExpanderController;
import com.thecoderscorner.menu.editorui.controller.EditMenuInMenuController;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import javafx.stage.Stage;

import java.util.Collection;

public class EditMenuInMenuDialog extends BaseDialogSupport<EditMenuInMenuController> {
    private final MenuInMenuCollection collection;
    private final MenuTree tree;

    public EditMenuInMenuDialog(Stage stage, MenuInMenuCollection collection, MenuTree tree) {
        this.collection = collection;
        this.tree = tree;
        tryAndCreateDialog(stage, "/ui/menuInMenuEditor.fxml", "Edit Menu In Menu Configuration", true);
    }

    @Override
    protected void initialiseController(EditMenuInMenuController controller) throws Exception {
        controller.initialise(collection, tree);
    }
}
