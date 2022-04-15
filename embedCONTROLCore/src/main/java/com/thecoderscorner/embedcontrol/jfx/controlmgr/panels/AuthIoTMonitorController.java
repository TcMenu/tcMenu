package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.ServerConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.Logger.Level.INFO;

/**
 * This is the controller for `authIoTMonitor.fxml`. It provides a simple panel to manage the core IoT settings, including
 * adding and removing authentication pairs and checking active connections, and closing a connection if required.
 * You can read more about connections in the tcMenu documentation -
 */
public class AuthIoTMonitorController {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    public ListView<ServerConnection> connectionList;
    public ListView<String> authenticatedUsersList;
    public Button removeAuthButton;
    public Button closeConnectionButton;
    private MenuAuthenticator authenticator;
    private MenuManagerServer manager;

    public void initialise(MenuAuthenticator authenticator, MenuManagerServer manager) {
        this.authenticator = authenticator;
        this.manager = manager;

        refreshAuthentication();
        refreshClientConnections();
        authenticatedUsersList.getSelectionModel().selectedItemProperty().addListener((observable, o, n) -> removeAuthButton.setDisable(n == null));
        connectionList.getSelectionModel().selectedItemProperty().addListener((observable, o, n) -> closeConnectionButton.setDisable(n == null));
    }

    private void refreshClientConnections() {
        connectionList.setItems(FXCollections.observableList(this.manager.getAllServerConnections()));
        connectionList.getSelectionModel().selectFirst();
        closeConnectionButton.setDisable(connectionList.getSelectionModel().getSelectedItem() == null);
    }

    private void refreshAuthentication() {
        List<String> allNames = authenticator.getAllNames();
        authenticatedUsersList.setItems(FXCollections.observableList(allNames));
        authenticatedUsersList.getSelectionModel().selectFirst();
        removeAuthButton.setDisable(authenticatedUsersList.getSelectionModel().getSelectedItem() == null);
    }

    public void addAuthPressed(ActionEvent actionEvent) {
        logger.log(INFO, "Add authentication pressed");
        Stage stage = (Stage) authenticatedUsersList.getScene().getWindow();
        AtomicReference<AddAuthEntryController> controllerRef = new AtomicReference<>(null);
        BaseDialogSupport.tryAndCreateDialog(stage, "/core_fxml/addAuthEntry.fxml", "Add Authentication", true,
                controllerRef::set);
        if(controllerRef.get() != null && controllerRef.get().getUserName() != null) {
            logger.log(INFO, "Added new authentication for " + controllerRef.get().getUserName());
            authenticator.addAuthentication(controllerRef.get().getUserName(), controllerRef.get().getUuid(), false)
                    .thenApply(success -> {
                        Platform.runLater(this::refreshAuthentication);
                        return true;
                    });
        }

    }

    public void removeAuthPressed(ActionEvent actionEvent) {
        var sel = authenticatedUsersList.getSelectionModel().getSelectedItem();
        if(sel == null) return;
        logger.log(INFO, "Remove authentication for " + sel);
        authenticator.removeAuthentication(sel);
        refreshAuthentication();
    }

    public void closeConnectionPressed(ActionEvent actionEvent) {
        var con = connectionList.getSelectionModel().getSelectedItem();
        if(con == null) return;
        logger.log(INFO, "Close connection for " + con);
        con.closeConnection();
        refreshClientConnections();
    }

    public void refreshWasPressed(ActionEvent actionEvent) {
        refreshClientConnections();
    }
}
