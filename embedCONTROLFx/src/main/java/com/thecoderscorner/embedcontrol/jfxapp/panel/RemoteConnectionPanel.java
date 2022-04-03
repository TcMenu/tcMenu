package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.RemotePanelDisplayable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxPanelLayoutEditorPresenter;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.TitleWidget;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ColorSettingsPresentable;
import com.thecoderscorner.embedcontrol.jfxapp.EmbedControlContext;
import com.thecoderscorner.embedcontrol.jfxapp.RemoteAppScreenLayoutPersistence;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.PairingController;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.util.Optional;
import java.util.UUID;

import static com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class RemoteConnectionPanel implements PanelPresentable<Node>, RemotePanelDisplayable {
    private final System.Logger logger = System.getLogger(RemoteConnectionPanel.class.getSimpleName());
    private RemoteAppScreenLayoutPersistence layoutPersistence;
    private RemoteMenuComponentControl control;
    private RemoteTreeComponentManager remoteTreeComponentManager;
    private GlobalSettings settings;
    private EmbedControlContext context;
    private MenuItem rootItem;
    private ConnectionCreator creator;
    private JfxNavigationHeader navigationManager;
    private RemoteMenuController controller;
    private GridPane dialogPane;
    private Label headerLabel;
    private Label messageLabel;
    private Button dlgButton1;
    private Button dlgButton2;
    private ScrollPane scrollPane;
    private BorderPane rootPanel;
    private RemoteDialogManager dialogManager;
    private TitleWidget<Image> connectStatusWidget;

    public RemoteConnectionPanel(GlobalSettings settings, EmbedControlContext context, RemoteAppScreenLayoutPersistence layoutPersistence,
                                 MenuItem item) {
        try {
            this.creator = layoutPersistence.getConnectionCreator();
            this.navigationManager = new JfxNavigationHeader(layoutPersistence);
            this.settings = settings;
            this.context = context;
            this.layoutPersistence = layoutPersistence;
            this.rootItem = item;
            dialogManager = new RemoteDialogManager();
        } catch (Exception e) {
            logger.log(ERROR, "Failed to start controller " + layoutPersistence.getPanelName(), e);
        }
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        rootPanel = new BorderPane();
        initialiseConnectionComponents();
        VBox topLayout = new VBox();
        topLayout.getChildren().add(getDialogComponents(rootPanel));
        topLayout.getChildren().add(navigationManager.initialiseControls());
        rootPanel.setTop(topLayout);
        generateWidgets();

        controller = layoutPersistence.getConnectionCreator().start();
        this.control = new RemoteMenuComponentControl(controller, navigationManager);
        this.control.setAuthStatusChangeConsumer(this::statusHasChanged);
        if(settings.isSetupLayoutModeEnabled()) {
            this.navigationManager.setItemEditorPresenter(new JfxPanelLayoutEditorPresenter(layoutPersistence, control.getMenuTree(),
                    navigationManager, settings));
        }
        remoteTreeComponentManager = new RemoteTreeComponentManager(controller, settings, dialogManager,
                layoutPersistence.getExecutorService(), Platform::runLater, control, layoutPersistence);
        navigationManager.initialiseUI(remoteTreeComponentManager, dialogManager, control, scrollPane);

        return rootPanel;
    }

    private void initialiseConnectionComponents() throws Exception {
        scrollPane = new ScrollPane();
        rootPanel.setCenter(scrollPane);
        scrollPane.setContent(new Label("Waiting for connection"));
        controller = creator.start();
        dialogManager = new RemoteDialogManager();
    }

    private void generateWidgets() {
        var settingsWidget = standardSettingsWidget();
        navigationManager.addTitleWidget(settingsWidget);
        var saveWidget = standardSaveWidget();
        navigationManager.addTitleWidget(saveWidget);
        connectStatusWidget = standardStatusLed();
        navigationManager.addTitleWidget(connectStatusWidget);

        ContextMenu settingsMenu = generateSettingsContextMenu();
        navigationManager.getButtonFor(settingsWidget).ifPresent(b -> b.setContextMenu(settingsMenu));

        navigationManager.addWidgetClickedListener((actionEvent, widget) -> {
            if(widget == saveWidget) {
                layoutPersistence.serialiseAll();
                dialogManager.withTitle(creator.getName() + " Saved", false)
                        .withMessage("Successfully saved project", false)
                        .showDialogWithButtons(MenuButtonType.NONE, MenuButtonType.CLOSE);
            }
        });
    }

    private ContextMenu generateSettingsContextMenu() {
        var colorConfig = new javafx.scene.control.MenuItem("Color Settings");
        colorConfig.setOnAction(evt -> navigationManager.pushNavigation(new ColorSettingsPresentable(
                settings, navigationManager,layoutPersistence, control.getMenuTree())
        ));
        var editConfig = new javafx.scene.control.MenuItem("Edit Connection");
        editConfig.setOnAction(this::editConnection);
        var deleteConnection = new javafx.scene.control.MenuItem("Delete Connection");
        deleteConnection.setOnAction(this::deleteConnection);
        var restartConnection = new javafx.scene.control.MenuItem("Restart Connection");
        restartConnection.setOnAction(this::restartConnection);
        return new ContextMenu(colorConfig, editConfig, deleteConnection, restartConnection);
    }

    private void editConnection(ActionEvent actionEvent) {
        navigationManager.pushNavigation(new NewConnectionPanelPresentable(settings, context, Optional.of(creator)));
    }

    private void restartConnection(ActionEvent actionEvent) {
        // force a connection restart but only if already connected
        if (controller != null && controller.getConnector().getAuthenticationStatus() != AuthStatus.NOT_STARTED) {
            try {
                logger.log(INFO, "Trying to restart the connection " + creator);
                controller.stop();
                controller = creator.start();
                logger.log(INFO, "Restarted the connection " + creator);
            } catch (Exception e) {
                logger.log(ERROR, "Could not restart connection" + creator, e);
            }
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
            if (btn == ButtonType.YES) {
                context.deleteConnection(getUuid());
            }
        });
    }

    private Node getDialogComponents(BorderPane pane) {
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
        for (int i = 0; i < 2; i++) {
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
        showDialog(false);

        dlgButton1.setOnAction(evt -> dialogManager.buttonWasPressed(dialogManager.getButtonType(1)));
        dlgButton2.setOnAction(evt -> dialogManager.buttonWasPressed(dialogManager.getButtonType(2)));
        return dialogPane;
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
    public boolean canClose() {
        return false;
    }

    @Override
    public void closePanel() {
        try {
            if (controller != null) controller.stop();
            controller = null;
            navigationManager.destroy();
        } catch (Exception ex) {
            logger.log(ERROR, "Exception while closing panel", ex);
        }
    }

    public void statusHasChanged(AuthStatus status) {
        Platform.runLater(() -> {
            if (status == AuthStatus.CONNECTION_READY) {
                connectStatusWidget.setCurrentState(StandardLedWidgetStates.GREEN);
                layoutPersistence.remoteApplicationDidLoad(controller.getConnector().getRemoteParty().getUuid(), controller.getManagedMenu());
                navigationManager.pushMenuNavigation(MenuItemHelper.asSubMenu(rootItem), true);
            } else if (status == AuthStatus.FAILED_AUTH) {
                connectStatusWidget.setCurrentState(StandardLedWidgetStates.RED);
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
            } else {
                boolean noConnection = status == AuthStatus.AWAITING_CONNECTION || status == AuthStatus.CONNECTION_FAILED;
                connectStatusWidget.setCurrentState(noConnection ? StandardLedWidgetStates.RED : StandardLedWidgetStates.ORANGE);
                scrollPane.setDisable(noConnection);
            }
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
        } catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR, "Connection not restarted", ButtonType.CLOSE);
            alert.showAndWait();
            logger.log(ERROR, "Unable to restart connection after pairing", e);
        }
    }

    public UUID getUuid() {
        return layoutPersistence.getRemoteUuid();
    }

    public ConnectionCreator getCreator() {
        return creator;
    }

    private void showDialog(boolean visible) {
        dialogPane.setVisible(visible);
        dialogPane.setManaged(visible);
    }

    public RemoteAppScreenLayoutPersistence getLayoutPersistence() {
        return layoutPersistence;
    }

    class RemoteDialogManager extends DialogManager {
        @Override
        protected void dialogDidChange() {
            Platform.runLater(() -> {
                headerLabel.setText(title);
                messageLabel.setText(message);
                dlgButton1.setText(toPrintableText(button1));
                dlgButton1.setVisible(button1 != MenuButtonType.NONE);
                dlgButton2.setText(toPrintableText(button2));
                dlgButton2.setVisible(button2 != MenuButtonType.NONE);
                showDialog(mode == DialogMode.SHOW);
            });
        }

        @Override
        protected void buttonWasPressed(MenuButtonType btn) {
            controller.sendDialogAction(btn);
        }
    }

}
