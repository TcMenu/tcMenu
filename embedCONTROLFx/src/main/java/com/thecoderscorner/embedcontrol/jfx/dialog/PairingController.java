package com.thecoderscorner.embedcontrol.jfx.dialog;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.menu.remote.AuthStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class PairingController {
    private ConnectionCreator creator;
    private ExecutorService executor;
    private Consumer<Boolean> completionListener;
    private boolean pairingSuccess = false;
    public Button pairingButton;
    public Label pairingStatus;

    public void initialise(ConnectionCreator creator, ExecutorService executor, Consumer<Boolean> completionListener) {
        this.creator = creator;
        this.executor = executor;
        this.completionListener = completionListener;
    }

    public void onStartPairing(ActionEvent actionEvent) {
        if (pairingSuccess) {
            completionListener.accept(pairingSuccess);
            return;
        }
        pairingButton.setDisable(true);
        executor.execute(() -> {
            try {
                pairingSuccess = creator.attemptPairing(this::pairingUpdate);
            } catch (Exception e) {
                var alert = new Alert(Alert.AlertType.ERROR, "Pairing failed to start", ButtonType.CLOSE);
                alert.showAndWait();
            }
            Platform.runLater(this::pairingHasFinished);
        });
    }

    private void pairingHasFinished() {
        if (pairingSuccess) {
            pairingStatus.setText("Successfully paired!");
            pairingButton.setText("Continue");
        }
        pairingButton.setDisable(false);
    }

    private void pairingUpdate(AuthStatus authStatus) {
        Platform.runLater(() -> pairingStatus.setText(authStatus.getDescription()));
    }
}
