package com.thecoderscorner.embedcontrol.jfx.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.DialogViewer;
import com.thecoderscorner.embedcontrol.core.controlmgr.TreeComponentManager;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxScreenManager;
import com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController.*;

public class RemoteConnectionPanel implements PanelPresentable, DialogViewer {
    private final ConnectionCreator creator;
    private final GlobalSettings settings;
    private final ScheduledExecutorService executorService;
    private final UUID uuid;
    private RemoteMenuController controller;
    private TreeComponentManager treeManager;
    private JfxScreenManager screenManager;
    private ScheduledFuture<?> taskRef;
    private GridPane dialogPane;
    private Label headerLabel;
    private Label messageLabel;
    private Button dlgButton1;
    private Button dlgButton2;
    private MenuButtonType dlg1ButtonType = MenuButtonType.NONE;
    private MenuButtonType dlg2ButtonType = MenuButtonType.NONE;

    public RemoteConnectionPanel(ConnectionCreator creator, GlobalSettings settings, ScheduledExecutorService executorService,
                                 UUID panelUuid) {
        this.creator = creator;
        this.settings = settings;
        this.executorService = executorService;
        this.uuid = panelUuid;
    }

    @Override
    public void presentPanelIntoArea(BorderPane pane) throws Exception {
        Label waitingLabel = new Label("Waiting for connection...");
        var scrollPane = new ScrollPane();
        pane.setCenter(scrollPane);
        scrollPane.setContent(waitingLabel);
        controller = creator.start();
        screenManager = new JfxScreenManager(controller, scrollPane, Platform::runLater, 2);
        treeManager = new TreeComponentManager(screenManager, controller, settings, this, executorService, Platform::runLater);

        generateDialogComponents(pane);

        taskRef = executorService.schedule(() -> treeManager.timerTick(), 100, TimeUnit.MILLISECONDS);
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
        taskRef.cancel(false);
        controller.stop();
        screenManager.clear();
        treeManager.dispose();
        treeManager = null;
        screenManager = null;
        controller = null;
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

    }

    public UUID getUuid() {
        return uuid;
    }

    public ConnectionCreator getCreator() {
        return creator;
    }
}
