package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.ConfigureExpanderController;
import com.thecoderscorner.menu.editorui.controller.EditMenuInMenuController;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Collection;

public class EditMenuInMenuDialog extends BaseDialogSupport<EditMenuInMenuController> {
    private final MenuInMenuCollection collection;
    private final MenuTree tree;

    public EditMenuInMenuDialog(Stage stage, CodeGeneratorOptions options, MenuTree tree, boolean modal) {
        if(!options.getEmbeddedPlatform().equals(EmbeddedPlatform.RASPBERRY_PIJ.getBoardId())) {
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(options.getEmbeddedPlatform() + " doesn't support Menu In Menu");
            alert.setHeaderText("Menu in Menu is supported only on RPI/Java");
            alert.setContentText("If you add any Menu in Menu items and the platform is not embedded Java / Raspberry PI " +
                    "they will not be generated. Continue anyway?");
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
            BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
            var answer = alert.showAndWait().orElse(ButtonType.NO);
            if(answer != ButtonType.YES) {
                this.collection = null;
                this.tree = null;
                return;
            }
        }

        this.collection = options.getMenuInMenuCollection();
        this.tree = tree;

        tryAndCreateDialog(stage, "/ui/menuInMenuEditor.fxml", "Edit Menu In Menu Configuration", modal);
    }

    @Override
    protected void initialiseController(EditMenuInMenuController controller) throws Exception {
        controller.initialise(collection, tree);
    }
}
