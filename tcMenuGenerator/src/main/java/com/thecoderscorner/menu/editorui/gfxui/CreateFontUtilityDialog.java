package com.thecoderscorner.menu.editorui.gfxui;

import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.stage.Stage;

public class CreateFontUtilityDialog extends BaseDialogSupport<CreateFontUtilityController> {
    private final CurrentProjectEditorUI editorUI;
    private final ConfigurationStorage storage;

    public CreateFontUtilityDialog(Stage mainStage, CurrentProjectEditorUI editorUI, ConfigurationStorage storage) {
        this.editorUI = editorUI;
        this.storage = storage;
        tryAndCreateDialog(mainStage, "/ui/createFontPanel.fxml", bundle.getString("font.create.title"), true, .95);
    }

    @Override
    protected void initialiseController(CreateFontUtilityController controller) throws Exception {
        controller.initialise(editorUI, storage);
    }
}
