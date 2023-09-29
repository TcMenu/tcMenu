package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import javafx.stage.Stage;

public class FormManagerDialog extends BaseDialogSupport<FormManagerController> {
    private final EmbedControlContext context;

    public FormManagerDialog(Stage stage, EmbedControlContext context) {
        this.context = context;
        tryAndCreateDialog(stage, "/ecui/formManager.fxml", "Edit Connection", true);
    }

    @Override
    protected void initialiseController(FormManagerController controller) throws Exception {
        controller.initialise(context);
    }
}
