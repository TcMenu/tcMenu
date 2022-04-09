package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.ServerConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;

public class AuthIoTMonitorController {
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
    }

    private void refreshAuthentication() {
        List<String> allNames = authenticator.getAllNames();
        authenticatedUsersList.setItems(FXCollections.observableList(allNames));
        authenticatedUsersList.getSelectionModel().selectFirst();
    }

    public void addAuthPressed(ActionEvent actionEvent) {
        Stage stage = (Stage) authenticatedUsersList.getScene().getWindow();
        BaseDialogSupport.tryAndCreateDialog(stage, "core_fxml/addAuthEntry.fxml", "Add Authentication", true, o -> {});
    }

    public void removeAuthPressed(ActionEvent actionEvent) {
        var sel = authenticatedUsersList.getSelectionModel().getSelectedItem();
        if(sel == null) return;
        authenticator.removeAuthentication(sel);
        refreshAuthentication();
    }

    public void closeConnectionPressed(ActionEvent actionEvent) {
        var con = connectionList.getSelectionModel().getSelectedItem();
        if(con == null) return;
        con.closeConnection();
    }
}
