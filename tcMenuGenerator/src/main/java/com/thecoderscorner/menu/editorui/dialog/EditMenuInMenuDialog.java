package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.EditMenuInMenuController;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;

public class EditMenuInMenuDialog extends BaseDialogSupport<EditMenuInMenuController> {
    private final MenuInMenuCollection collection;
    private final MenuTree tree;

    public EditMenuInMenuDialog(Stage stage, CodeGeneratorOptions options, MenuTree tree, boolean modal) {
        if(!options.getEmbeddedPlatform().equals(EmbeddedPlatform.RASPBERRY_PIJ)) {
            var btn = showAlertAndWait(Alert.AlertType.CONFIRMATION,
                    options.getEmbeddedPlatform() + " doesn't support Menu In Menu",
                    "Menu in Menu is supported only on RPI/Java. If you add any Menu in Menu items they will not be generated. Continue?",
                    ButtonType.YES, ButtonType.NO);
            if(btn.orElse(ButtonType.NO) != ButtonType.YES) {
                this.collection = null;
                this.tree = null;
                return;
            }
        }

        this.collection = options.getMenuInMenuCollection();
        this.tree = tree;

        tryAndCreateDialog(stage, "/ui/menuInMenuEditor.fxml", "Edit Menu In Menu Configuration", modal, 0.75);
    }

    @Override
    protected void initialiseController(EditMenuInMenuController controller) throws Exception {
        controller.initialise(collection, tree);
    }
}
