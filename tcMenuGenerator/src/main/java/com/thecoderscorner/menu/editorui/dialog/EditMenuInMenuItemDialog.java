package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.EditMenuInMenuItemController;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition;
import javafx.stage.Stage;

import java.util.Optional;

public class EditMenuInMenuItemDialog extends BaseDialogSupport<EditMenuInMenuItemController> {
    private final MenuInMenuDefinition definition;
    private final MenuTree tree;

    public EditMenuInMenuItemDialog(Stage stage, MenuInMenuDefinition definition, MenuTree tree) {
        this.definition = definition;
        this.tree = tree;
        tryAndCreateDialog(stage, "/ui/menuInMenuItemEditor.fxml", "Edit Menu In Menu Item", true);
    }

    @Override
    protected void initialiseController(EditMenuInMenuItemController controller) throws Exception {
        controller.initialise(definition, tree);
    }

    public Optional<MenuInMenuDefinition> getResult() {
        return controller.getResult();
    }
}
