package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.RemotePanelDisplayable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.TcMenuFormPersistence;
import com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxMenuPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.TitleWidget;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ColorSettingsPresentable;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.mgr.DialogShowMode;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import static com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader.*;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;
import static java.lang.System.Logger.Level.*;

public class RemoteConnectionPanel implements PanelPresentable<Node>, RemotePanelDisplayable {
    private final System.Logger logger = System.getLogger(RemoteConnectionPanel.class.getSimpleName());
    private TcMenuPersistedConnection persistedConnection;
    private RemoteMenuComponentControl control;
    private GlobalSettings settings;
    private EmbedControlContext context;
    private MenuItem rootItem;
    private ConnectionCreator creator;
    private JfxNavigationHeader navigationManager;
    private GridPane dialogPane;
    private Label headerLabel;
    private Label messageLabel;
    private Button dlgButton1;
    private Button dlgButton2;
    private ScrollPane scrollPane;
    private BorderPane rootPanel;
    private RemoteDialogManager dialogManager;
    private TitleWidget<Image> connectStatusWidget;
    private boolean pairingInProgress = false;
    private RemoteMenuController controller;
    private MenuItemStore itemStore;
    private RemoteControllerListener remoteListener;
    private RemoteInformation remoteInformation = RemoteInformation.NOT_CONNECTED;
    private ContextMenu layoutContextMenu = null;
    private TcMenuFormPersistence selectedForm;
    private Button formWidgetButton = null;


    public RemoteConnectionPanel(EmbedControlContext context, MenuItem item,
                                 ScheduledExecutorService executorService, TcMenuPersistedConnection connection) {
        try {
            this.persistedConnection = connection;
            this.creator = context.connectionFromDescription(connection);
            this.navigationManager = new JfxNavigationHeader(executorService, settings);
            this.settings = context.getSettings();
            this.context = context;
            this.rootItem = item;
            control = new RemoteMenuComponentControl(controller, navigationManager);
            dialogManager = new RemoteDialogManager();
        } catch (Exception e) {
            logger.log(ERROR, "Failed to start controller " + persistedConnection.getName(), e);
        }
    }

    @Override
    public Node getPanelToPresent(double width) {
        if(rootPanel == null) {
            rootPanel = new BorderPane();
            initialiseConnectionComponents();
            VBox topLayout = new VBox();
            topLayout.getChildren().add(getDialogComponents(rootPanel));
            topLayout.getChildren().add(navigationManager.initialiseControls());
            rootPanel.setTop(topLayout);
            generateWidgets();

            createNewController();
        }
        return rootPanel;
    }

    private void initialiseConnectionComponents() {
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        rootPanel.setCenter(scrollPane);
        scrollPane.setContent(new Label("Waiting for connection"));
        dialogManager = new RemoteDialogManager();
    }

    private void generateWidgets() {
        var settingsWidget = standardSettingsWidget();
        navigationManager.addTitleWidget(settingsWidget);
        connectStatusWidget = standardStatusLedWidget();
        navigationManager.addTitleWidget(connectStatusWidget);

        ContextMenu settingsMenu = generateSettingsContextMenu();
        navigationManager.getButtonFor(settingsWidget).ifPresent(b -> b.setContextMenu(settingsMenu));

        if(getUuid() != null) {
            var formWidget = standardLayoutWidget();
            navigationManager.addTitleWidget(formWidget);
            ContextMenu layoutMenu = generateLayoutMenu();
            navigationManager.getButtonFor(formWidget).ifPresent(b -> {
                b.setContextMenu(layoutMenu);
                formWidgetButton = b;
            });
        }
    }

    private ContextMenu generateLayoutMenu() {
        layoutContextMenu = new ContextMenu();
        buildLayoutItems();
        return layoutContextMenu;
    }

    private void buildLayoutItems() {
        var forms = context.getDataStore().getAllFormsForUuid(getUuid().toString());
        var items = forms.stream()
                .map(f -> {
                    var selIndiciator = selectedForm != null && selectedForm.getFormId() == f.getFormId() ? " *" : "";
                    var it = new javafx.scene.control.MenuItem(f.getFormName() + " [" + f.getFormId() + "]" + selIndiciator);
                    it.setOnAction(event -> changeSelectedForm(f));
                    return it;
                })
                .toList();

        layoutContextMenu.getItems().clear();
        layoutContextMenu.getItems().addAll(items);

        if(selectedForm != null) {
           var editExisiting = new javafx.scene.control.MenuItem("Clear Form");
           editExisiting.setOnAction(event -> changeSelectedForm(null));
           layoutContextMenu.getItems().add(editExisiting);
        }
        if(formWidgetButton != null) formWidgetButton.setDisable(layoutContextMenu.getItems().isEmpty());
    }

    private void changeSelectedForm(TcMenuFormPersistence form) {
        selectedForm = form;
        if(selectedForm != null) {
            itemStore.loadLayout(form.getXmlData(), getUuid());
            buildLayoutItems();
            persistedConnection = persistedConnection.withFormChange(selectedForm.getFormName());
        } else {
            itemStore.reset("Empty");
            buildLayoutItems();
            persistedConnection = persistedConnection.withFormChange("");
        }
        try {
            context.getDataStore().updateConnection(persistedConnection);
            restartConnection(null);
        } catch (DataException e) {
            logger.log(ERROR, "Save form failed", e);
        }
    }

    private ContextMenu generateSettingsContextMenu() {
        var colorConfig = new javafx.scene.control.MenuItem("Color Settings");
        colorConfig.setOnAction(evt -> navigationManager.pushNavigation(new ColorSettingsPresentable(
                settings, navigationManager, GlobalColorCustomizable.KEY_NAME,
                new MenuItemStore(settings, new MenuTree(), "", 1, 1, true), false)
        ));
        var editConfig = new javafx.scene.control.MenuItem("Edit Connection");
        editConfig.setOnAction(this::editConnection);
        var deleteConnection = new javafx.scene.control.MenuItem("Delete Connection");
        deleteConnection.setOnAction(this::deleteConnection);
        var restartConnection = new javafx.scene.control.MenuItem("Restart Connection");
        restartConnection.setOnAction(this::restartConnection);
        return new ContextMenu(colorConfig, editConfig, deleteConnection, restartConnection);
    }

    void connectionWasEdited(TcMenuPersistedConnection newConnection) {
        persistedConnection = newConnection;
        context.updateConnection(newConnection);
        restartConnection(null);
    }

    private void editConnection(ActionEvent actionEvent) {
        var dlg = new EditConnectionDialog((Stage)headerLabel.getScene().getWindow(), this.context, persistedConnection, true);
        dlg.checkResult().ifPresent(this::connectionWasEdited);

    }

    private void restartConnection(ActionEvent actionEvent) {
        // force a connection restart but only if already connected
        if (controller != null && controller.getConnector().getAuthenticationStatus() != AuthStatus.NOT_STARTED) {
            try {
                createNewController();
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
                context.deleteConnection(persistedConnection);
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
        return persistedConnection.getName();
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
                navigationManager.pushMenuNavigation(asSubMenu(rootItem), itemStore, true);
                notifyControlGrid(true);
            } else if (status == AuthStatus.FAILED_AUTH) {
                connectStatusWidget.setCurrentState(StandardLedWidgetStates.RED);
                notifyControlGrid(false);
                try {
                    logger.log(INFO, "Pairing needed, stopping controller and showing pairing window");
                    if(!pairingInProgress) {
                        pairingInProgress = true;
                        if(controller != null) controller.stop();
                        controller = null;
                        Platform.runLater(this::doPairing);
                    }
                } catch (Exception e) {
                    var alert = new Alert(Alert.AlertType.ERROR, "Pairing has failed", ButtonType.CLOSE);
                    alert.showAndWait();
                }
            } else {
                boolean noConnection = status == AuthStatus.AWAITING_CONNECTION || status == AuthStatus.CONNECTION_FAILED;
                connectStatusWidget.setCurrentState(noConnection ? StandardLedWidgetStates.RED : StandardLedWidgetStates.ORANGE);
                notifyControlGrid(false);
            }
        });
    }

    private void notifyControlGrid(boolean up) {
        if(navigationManager.currentNavigationPanel() instanceof JfxMenuPresentable menuPresentable) {
            menuPresentable.connectionIsUp(up);
        }
    }

    private void doPairing() {
        navigationManager.pushNavigation(new ConnectionPairingPresentable(navigationManager, creator, context,
                persistedConnection.getName(), this::pairingHasFinished));
    }

    private void pairingHasFinished(Boolean aBoolean) {
        try {
            if(pairingInProgress) {
                navigationManager.popNavigation(); // removing pairing window and clear state
                pairingInProgress = false;
                createNewController();
            }
        } catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR, "Connection not restarted", ButtonType.CLOSE);
            alert.showAndWait();
            logger.log(ERROR, "Unable to restart connection after pairing", e);
        }
    }

    private void createNewController() {
        try {
            logger.log(INFO, "Trying to start the connection " + creator);

            controller = creator.start();

            this.control = new RemoteMenuComponentControl(controller, navigationManager);
            this.control.setAuthStatusChangeConsumer(this::statusHasChanged);

            itemStore = new MenuItemStore(settings, control.getMenuTree(), "-", 1, 4, true);
            if(!StringHelper.isStringEmptyOrNull(persistedConnection.getFormName())) {
                try {
                    logger.log(INFO, "Trying to reload form " + persistedConnection.getFormName());
                    selectedForm = persistedConnection.getSelectedForm().orElse(null);
                    if(selectedForm != null) {
                        itemStore.loadLayout(selectedForm.getXmlData(), UUID.fromString(persistedConnection.getUuid()));
                    }
                    buildLayoutItems();
                } catch (Exception ex) {
                    logger.log(ERROR, "Reload of existing form failed with exception", ex);
                    selectedForm = null;
                }
            }

            navigationManager.initialiseUI(dialogManager, control, scrollPane);
            navigationManager.pushNavigation(new WaitingForConnectionPanel());
            logger.log(INFO, "Started the connection " + creator);

            if(remoteListener != null) controller.removeListener(remoteListener);
            remoteListener = new RemoteControllerListener() {
                @Override
                public void menuItemChanged(MenuItem item, boolean valueOnly) {
                    if(navigationManager.currentNavigationPanel() instanceof JfxMenuPresentable menuPanel) {
                        menuPanel.getGridComponent().itemHasUpdated(item);
                    }
                }

                @Override
                public void treeFullyPopulated() {
                    navigationManager.pushMenuNavigation(MenuTree.ROOT, itemStore, true);
                    if(!StringHelper.isStringEmptyOrNull(persistedConnection.getUuid()) &&
                            !persistedConnection.getUuid().equals(remoteInformation.getUuid().toString())) {
                        logger.log(WARNING, "The UUID stored does not match the remote" + persistedConnection.getName());
                        Platform.runLater(() -> {
                            var alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Warning UUID mismatch");
                            alert.setHeaderText("The UUID stored does not match the remote UUID");
                            alert.setContentText("The UUID of the remote has changed from last time and could cause problems");
                            alert.showAndWait();
                        });
                    }
                    persistedConnection = persistedConnection.withUuid(remoteInformation.getUuid());
                    context.updateConnection(persistedConnection);
                }

                @Override
                public void connectionState(RemoteInformation remoteInformation, AuthStatus connected) {
                    if(remoteInformation != null) {
                        RemoteConnectionPanel.this.remoteInformation = remoteInformation;
                    }
                    statusHasChanged(connected);
                }

                @Override
                public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
                    if(item == null) return; // we ignore dialog acks at the moment.
                    if(navigationManager.currentNavigationPanel() instanceof JfxMenuPresentable menuPanel) {
                        menuPanel.getGridComponent().acknowledgementReceived(key, status);
                    }
                }

                @Override
                public void dialogUpdate(MenuDialogCommand cmd) {
                    dialogManager.updateStateFromCommand(cmd);
                }
            };
            controller.addListener(remoteListener);

            // handle the case where it's already connected really quick!
            if (controller.getConnector().getAuthenticationStatus() == AuthStatus.CONNECTION_READY) {
                if(navigationManager.currentNavigationPanel() instanceof JfxMenuPresentable) {
                    statusHasChanged(AuthStatus.CONNECTION_READY);
                }
            }
        } catch (Exception e) {
            logger.log(ERROR, "Unable to start connection " + persistedConnection.getName(), e);
        }
    }

    public UUID getUuid() {
        if(StringHelper.isStringEmptyOrNull(persistedConnection.getUuid())) return null;
        return UUID.fromString(persistedConnection.getUuid());
    }


    public String getWindowDescription() {
        var connector = controller.getConnector();
        int serNo = 0;
        if(connector != null && connector.getRemoteParty() != null) {
            serNo = connector.getRemoteParty().getSerialNumber();
        }
        return String.format("%s [%s] S/N %d", persistedConnection.getName(), persistedConnection.getUuid(), serNo);
    }


    public ConnectionCreator getCreator() {
        return creator;
    }

    private void showDialog(boolean visible) {
        dialogPane.setVisible(visible);
        dialogPane.setManaged(visible);
    }

    public TcMenuPersistedConnection getPersistence() {
        return persistedConnection;
    }

    public void toFront() {
        ((Stage)dialogPane.getScene().getWindow()).toFront();
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
            if(getDialogShowMode() == DialogShowMode.REGULAR) {
                controller.sendDialogAction(btn);
            } else {
                super.buttonWasPressed(btn);
            }
        }
    }

}
