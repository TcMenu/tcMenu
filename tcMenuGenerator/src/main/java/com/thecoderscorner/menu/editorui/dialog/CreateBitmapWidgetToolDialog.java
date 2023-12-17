package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.CreateBitmapWidgetController;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.stage.Stage;

public class CreateBitmapWidgetToolDialog extends BaseDialogSupport<CreateBitmapWidgetController> {
    private final String homeDirectory;
    private CurrentProjectEditorUI editorUI;

    public CreateBitmapWidgetToolDialog(Stage mainStage, CurrentProjectEditorUI editorUI, String homeDirectory) {
        this.editorUI = editorUI;
        this.homeDirectory = homeDirectory;
        tryAndCreateDialog(mainStage, "/ui/ImageToNativeBitmapConverter.fxml", bundle.getString("bitmap.create.title"), true, 0.96);
    }

    @Override
    protected void initialiseController(CreateBitmapWidgetController controller) throws Exception {
        controller.initialise(editorUI, homeDirectory);
    }
}
