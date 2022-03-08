package com.thecoderscorner.embedcontrol.jfx.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.DialogViewer;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.TreeComponentManager;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.RemotePanelDisplayable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.EmbedControlContext;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxScreenManager;
import com.thecoderscorner.embedcontrol.jfx.dialog.PairingController;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class RemoteConnectionPanel implements PanelPresentable, RemotePanelDisplayable, DialogViewer {
    private final System.Logger logger = System.getLogger(RemoteConnectionPanel.class.getSimpleName());
    private ConnectionCreator creator;
    private final GlobalSettings settings;
    private final EmbedControlContext context;
    private final UUID uuid;
    private RemoteMenuController controller;
    private RemoteTreeComponentManager treeManager;
    private JfxScreenManager screenManager;
    private ScheduledFuture<?> taskRef;
    private GridPane dialogPane;
    private Label headerLabel;
    private Label messageLabel;
    private Button dlgButton1;
    private Button dlgButton2;
    private MenuButtonType dlg1ButtonType = MenuButtonType.NONE;
    private MenuButtonType dlg2ButtonType = MenuButtonType.NONE;
    private ScrollPane scrollPane;
    private Label statusLabel;
    private BorderPane rootPanel;

    public RemoteConnectionPanel(ConnectionCreator creator, GlobalSettings settings, EmbedControlContext context,
                                 UUID panelUuid) {
        this.creator = creator;
        this.settings = settings;
        this.context = context;
        this.uuid = panelUuid;
    }

    @Override
    public void presentPanelIntoArea(BorderPane pane) throws Exception {
        rootPanel = pane;
        initialiseConnectionComponents();
        generateDialogComponents(pane);
        generateButtonBar(pane);

    }

    private void initialiseConnectionComponents() throws Exception {
        Label waitingLabel = new Label("Waiting for connection...");
        scrollPane = new ScrollPane();
        rootPanel.setCenter(scrollPane);
        scrollPane.setContent(waitingLabel);
        controller = creator.start();
        RemoteMenuComponentControl theMenuController = new RemoteMenuComponentControl();
        screenManager = new JfxScreenManager(theMenuController, scrollPane, Platform::runLater, 2);
        treeManager = new RemoteTreeComponentManager(screenManager, controller, settings, this,
                context.getExecutorService(), Platform::runLater, theMenuController);
        taskRef = context.getExecutorService().schedule(() -> treeManager.timerTick(), 100, TimeUnit.MILLISECONDS);

    }

    private void generateButtonBar(BorderPane pane) {
        var bar = new ToolBar();
        var editButton = new Button("Edit");
        editButton.setOnAction(this::editConnection);
        var delButton = new Button("Delete");
        delButton.setOnAction(this::deleteConnection);
        var restartButton = new Button("Restart");
        restartButton.setOnAction(this::restartConnection);
        statusLabel = new Label("No status yet");
        delButton.setStyle("-fx-background-color: #b31818;-fx-text-fill: #e8dddd");
        editButton.setStyle("-fx-background-color: #b31818;-fx-text-fill: #e8dddd");
        restartButton.setStyle("-fx-background-color: #b31818;-fx-text-fill: #e8dddd");
        bar.getItems().add(editButton);
        bar.getItems().add(delButton);
        bar.getItems().add(restartButton);
        bar.getItems().add(statusLabel);

        pane.setBottom(bar);
    }

    private void editConnection(ActionEvent actionEvent) {
        context.editConnection(getUuid());
    }

    private void restartConnection(ActionEvent actionEvent) {
        // force a connection restart but only if already connected
        if(controller != null && controller.getConnector().getAuthenticationStatus() != AuthStatus.NOT_STARTED) {
            controller.getConnector().close();
        }
    }

    private void deleteConnection(ActionEvent actionEvent) {
        // confirm and then delete the connection information.
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete " + getPanelName());
        alert.setHeaderText("Really delete " + getPanelName());
        alert.setContentText("This will remove all associated information about this connection");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if(btn == ButtonType.YES) {
                context.deleteConnection(uuid);
            }
        });

    }

    private void generateDialogComponents(BorderPane pane) {
        headerLabel = new Label("");
        headerLabel.setAlignment(Pos.TOP_CENTER);
        headerLabel.setTextAlignment(TextAlignment.CENTER);
        headerLabel.setStyle("-fx-font-size: 24px;-fx-font-weight: bold;-fx-text-fill: " + asHtml(settings.getDialogColor().getFg()));
        messageLabel = new Label("");
        messageLabel.setAlignment(Pos.TOP_CENTER);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setStyle("-fx-font-size: 20px;-fx-text-fill: " + asHtml(settings.getDialogColor().getFg()));
        dlgButton1 = new Button("");
        dlgButton1.setStyle("-fx-font-size: 20px; -fx-text-fill: " + asHtml(settings.getButtonColor().getFg()) + ";-fx-background-color: " + asHtml(settings.getButtonColor().getBg()));
        dlgButton1.setMaxWidth(9999);
        dlgButton2 = new Button("");
        dlgButton2.setStyle("-fx-font-size: 20px; -fx-text-fill: " + asHtml(settings.getButtonColor().getFg()) + ";-fx-background-color: " + asHtml(settings.getButtonColor().getBg()));
        dlgButton2.setMaxWidth(9999);
        dialogPane = new GridPane();
        dialogPane.setVgap(4);
        dialogPane.setHgap(4);
        for(int i=0;i<2;i++) {
            var constraint = new ColumnConstraints();
            constraint.setPercentWidth(50);
            dialogPane.getColumnConstraints().add(constraint);
        }
        dialogPane.setStyle("-fx-background-color: " + asHtml(settings.getDialogColor().getBg()));
        dialogPane.prefWidthProperty().bind(pane.widthProperty());
        dialogPane.add(headerLabel, 0, 0, 2, 1);
        dialogPane.add(messageLabel, 0, 1, 2, 1);
        dialogPane.add(dlgButton1, 0, 2);
        dialogPane.add(dlgButton2, 1, 2);
        GridPane.setHalignment(headerLabel, HPos.CENTER);
        GridPane.setHalignment(messageLabel, HPos.CENTER);
        show(false);
        pane.setTop(dialogPane);

        dlgButton1.setOnAction(evt -> controller.sendDialogAction(dlg1ButtonType));
        dlgButton2.setOnAction(evt -> controller.sendDialogAction(dlg2ButtonType));
    }

    private String asHtml(PortableColor col) {
        return String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
    }

    @Override
    public String getPanelName() {
        return creator.getName();
    }

    @Override
    public boolean canBeRemoved() {
        return true;
    }

    @Override
    public boolean closePanelIfPossible() {
        try {
            taskRef.cancel(false);
            if (controller != null) controller.stop();
            if(screenManager != null) screenManager.clear();
            if(treeManager != null) treeManager.dispose();
            treeManager = null;
            screenManager = null;
            controller = null;
        }
        catch (Exception ex) {
            logger.log(ERROR, "Exception while closing panel", ex);
        }
        return true;
    }

    @Override
    public void setButton1(MenuButtonType type) {
        dlg1ButtonType = type;
        dlgButton1.setText(toPrintableText(type));
        dlgButton1.setVisible(type != MenuButtonType.NONE );
    }

    private String toPrintableText(MenuButtonType type) {
        return switch (type) {
            case NONE -> "";
            case OK -> "OK";
            case ACCEPT -> "Accept";
            case CANCEL -> "Cancel";
            case CLOSE -> "Close";
        };
    }

    @Override
    public void setButton2(MenuButtonType type) {
        dlg2ButtonType = type;
        dlgButton2.setText(toPrintableText(type));
        dlgButton2.setVisible(type != MenuButtonType.NONE );
    }

    @Override
    public void show(boolean visible) {
        dialogPane.setVisible(visible);
        dialogPane.setManaged(visible);

    }

    @Override
    public void setText(String title, String subject) {
        headerLabel.setText(title);
        messageLabel.setText(subject);
    }

    @Override
    public void statusHasChanged(AuthStatus status) {
        if(status == AuthStatus.FAILED_AUTH) {
            scrollPane.setDisable(false);
            try {
                logger.log(INFO, "Pairing needed, stopping controller and showing pairing window");
                controller.stop();
                controller = null;
                Platform.runLater(this::doPairing);
            } catch (Exception e) {
                var alert = new Alert(Alert.AlertType.ERROR, "Pairing has failed", ButtonType.CLOSE);
                alert.showAndWait();
            }
        }
        else {
            scrollPane.setDisable(status == AuthStatus.AWAITING_CONNECTION || status == AuthStatus.CONNECTION_FAILED);
        }
        Platform.runLater(() -> {
            var name = "No connection";
            if(controller != null) {
                name = controller.getConnector().getConnectionName();
            }
            statusLabel.setText(name + " - " + status.getDescription());
        });
    }

    private void doPairing() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pairingDialog.fxml"));
            Pane myPane = loader.load();
            PairingController pairingController = loader.getController();
            pairingController.initialise(creator, context.getExecutorService(), this::pairingHasFinished);
            scrollPane.setContent(myPane);
        } catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR, "Pairing failed", ButtonType.CLOSE);
            alert.showAndWait();
            logger.log(ERROR, "Could not start the remote connector", e);
        }
    }

    private void pairingHasFinished(Boolean aBoolean) {
        try {
            scrollPane.setContent(new Label("Please wait.."));
            controller = creator.start();
            screenManager = new JfxScreenManager(new RemoteMenuComponentControl(), scrollPane, Platform::runLater, 2);
            treeManager = new RemoteTreeComponentManager(screenManager, controller, settings, this,
                    context.getExecutorService(), Platform::runLater, new RemoteMenuComponentControl());
        } catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR, "Connection not restarted", ButtonType.CLOSE);
            alert.showAndWait();
            logger.log(ERROR, "Unable to restart connection after pairing", e);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public ConnectionCreator getCreator() {
        return creator;
    }

    public void changeConnectionCreator(ConnectionCreator connectionCreator) {
        try {
            closePanelIfPossible();
            creator = connectionCreator;
            initialiseConnectionComponents();
        } catch (Exception e) {
            logger.log(ERROR, "Failed to re-initialise connection", e);
        }
    }

    class RemoteMenuComponentControl implements MenuComponentControl {
        @Override
        public CorrelationId editorUpdatedItem(MenuItem item, Object val) {
            return controller.sendAbsoluteUpdate(item, val);
        }

        @Override
        public CorrelationId editorUpdatedItemDelta(MenuItem item, int delta) {
            return controller.sendDeltaUpdate(item, delta);
        }

        @Override
        public MenuTree getMenuTree() {
            return controller.getManagedMenu();
        }

        @Override
        public String getConnectionName() {
            var rp = controller.getConnector().getRemoteParty();
            if(rp != null) {
                return rp.getName() + " - " + rp.getPlatform().getDescription() + " V" + rp.getMajorVersion() + "." + rp.getMinorVersion();
            }
            else return controller.getConnector().getConnectionName();
        }
    }
}
