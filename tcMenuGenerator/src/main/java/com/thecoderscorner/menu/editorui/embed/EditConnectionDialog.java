package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import javafx.stage.Stage;

import java.util.Optional;

public class EditConnectionDialog extends BaseDialogSupport<NewConnectionController> {
    private final Optional<TcMenuPersistedConnection> existingConnection;
    private final EmbedControlContext context;

    public EditConnectionDialog(Stage stage, EmbedControlContext context, TcMenuPersistedConnection creator, boolean modal) {
        this.context = context;
        this.existingConnection = Optional.of(creator);
        tryAndCreateDialog(stage, "/ecui/newConnection.fxml", "Edit Connection", modal);

    }
    public EditConnectionDialog(Stage stage, EmbedControlContext context, boolean modal) {
        existingConnection = Optional.empty();
        this.context = context;
        tryAndCreateDialog(stage, "/ecui/newConnection.fxml", "Edit Connection", modal);
    }

    @Override
    protected void initialiseController(NewConnectionController controller) throws Exception {
        controller.initialise(context, existingConnection);
    }

    public Optional<TcMenuPersistedConnection> checkResult() {
        controller.destroy();
        return controller.getResult();
    }

}
