package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.CreateFontUtilityController;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.stage.Stage;

public class CreateFontUtilityDialog extends BaseDialogSupport<CreateFontUtilityController> {
    private final String homeDirectory;
    private CurrentProjectEditorUI editorUI;

    public CreateFontUtilityDialog(Stage mainStage, CurrentProjectEditorUI editorUI, String homeDirectory) {
        this.editorUI = editorUI;
        this.homeDirectory = homeDirectory;
        tryAndCreateDialog(mainStage, "/ui/createFontPanel.fxml", bundle.getString("font.create.title"), true);
    }

    @Override
    protected void initialiseController(CreateFontUtilityController controller) throws Exception {
        controller.initialise(editorUI, homeDirectory);
    }
}
