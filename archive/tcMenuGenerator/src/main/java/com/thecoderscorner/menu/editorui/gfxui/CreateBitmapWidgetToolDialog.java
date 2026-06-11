package com.thecoderscorner.menu.editorui.gfxui;

import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.stage.Stage;

public class CreateBitmapWidgetToolDialog extends BaseDialogSupport<CreateBitmapWidgetController> {
    private final CurrentProjectEditorUI editorUI;
    private final ConfigurationStorage storage;

    public CreateBitmapWidgetToolDialog(Stage mainStage, CurrentProjectEditorUI editorUI, ConfigurationStorage storage) {
        this.editorUI = editorUI;
        this.storage = storage;
        tryAndCreateDialog(mainStage, "/ui/ImageToNativeBitmapConverter.fxml", bundle.getString("bitmap.create.title"), true, 0.96);
    }

    @Override
    protected void initialiseController(CreateBitmapWidgetController controller) throws Exception {
        controller.initialise(editorUI, storage);
    }
}
