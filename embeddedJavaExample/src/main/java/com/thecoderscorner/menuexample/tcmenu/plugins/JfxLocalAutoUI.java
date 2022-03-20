package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.MenuAppVersion;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxHeaderBar;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxScreenManager;
import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.auth.PropertiesAuthenticator;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class JfxLocalAutoUI extends Application {
    private static final AtomicReference<ApplicationContext> GLOBAL_CONTEXT = new AtomicReference<>(null);
    private MenuManagerServer mgr;
    private LocalTreeComponentManager localTree;
    private JfxScreenManager screenManager;
    private JfxHeaderBar appHeader;
    private LocalDialogManager dlgMgr;
    private MenuAppVersion versionData;

    public static void setAppContext(ApplicationContext context) {
        GLOBAL_CONTEXT.set(context);
    }

    @Override
    public void start(Stage stage) throws Exception {
        var ctx = GLOBAL_CONTEXT.get();
        mgr = ctx.getBean(MenuManagerServer.class);
        var executor = ctx.getBean(ScheduledExecutorService.class);
        versionData = ctx.getBean(MenuAppVersion.class);

        dlgMgr = new LocalDialogManager();
        var auth = ctx.getBean(MenuAuthenticator.class);
        if(auth instanceof PropertiesAuthenticator propAuth) propAuth.setDialogManager(dlgMgr);

        stage.setTitle(mgr.getServerName());
        var scroller = new ScrollPane();
        var settings = new GlobalSettings();
        LocalMenuController localControl = new LocalMenuController();
        screenManager = new JfxScreenManager(localControl, scroller, Platform::runLater, 2);
        localTree = new LocalTreeComponentManager(screenManager, settings, dlgMgr, executor, Platform::runLater, localControl, mgr);

        var wifiWidget = JfxHeaderBar.widgetFromImages(
                JfxHeaderBar.class.getResource("/img/con-fail.png"),
                JfxHeaderBar.class.getResource("/img/con-fail.png"),
                JfxHeaderBar.class.getResource("/img/wifi-poor.png"),
                JfxHeaderBar.class.getResource("/img/wifi-low.png"),
                JfxHeaderBar.class.getResource("/img/wifi-fair.png"),
                JfxHeaderBar.class.getResource("/img/wifi-full.png")
        );
        var border = new BorderPane();
        appHeader = new JfxHeaderBar(List.of(wifiWidget));
        GridPane dialogComponents = dlgMgr.initialiseControls();
        border.setCenter(scroller);
        VBox vbox = new VBox(dialogComponents, appHeader.initialiseControls());
        border.setTop(vbox);
        BorderPane.setMargin(scroller, new Insets(4));

        Scene scene = new Scene(border, 800, 500);
        stage.setScene(scene);
        stage.show();

        executor.scheduleAtFixedRate(()-> wifiWidget.setCurrentState((int)(Math.random() * 5)), 1000, 100, TimeUnit.MILLISECONDS);
    }

    JfxHeaderBar getAppHeader() {
        return appHeader;
    }

    class LocalMenuController implements MenuComponentControl {

        @Override
        public CorrelationId editorUpdatedItem(MenuItem menuItem, Object val) {
            MenuItemHelper.setMenuState(menuItem, val, mgr.getManagedMenu());
            mgr.menuItemDidUpdate(menuItem);
            return CorrelationId.EMPTY_CORRELATION;
        }

        @Override
        public CorrelationId editorUpdatedItemDelta(MenuItem menuItem, int delta) {
            MenuItemHelper.applyIncrementalValueChange(menuItem, delta, mgr.getManagedMenu());
            mgr.menuItemDidUpdate(menuItem);
            return CorrelationId.EMPTY_CORRELATION;
        }

        @Override
        public void connectionStatusChanged(AuthStatus authStatus) {
            // doesn't really apply locally
        }

        @Override
        public MenuTree getMenuTree() {
            return mgr.getManagedMenu();
        }

        @Override
        public String getConnectionName() {
            return mgr.getServerName();
        }

        @Override
        public void menuWasDisplayed(MenuItem item) {
            var text = item == MenuTree.ROOT ? mgr.getServerName() : item.getName();
            Platform.runLater(() -> appHeader.titleChanged(text));

            if(!item.equals(MenuTree.ROOT)) {
                appHeader.showLeftButton("<");
                appHeader.setLeftButtonFunction(() -> {
                    localTree.reset();
                    localTree.presentSubMenu(getMenuTree().findParent(item), false);
                });
            }
            else {
                appHeader.showLeftButton("?");
                appHeader.setLeftButtonFunction(() -> {
                    dlgMgr.withRemoteAllowed(false).withMessage(versionData.toString(), true)
                            .withTitle(mgr.getServerName(), true)
                            .showDialogWithButtons(MenuButtonType.CLOSE, MenuButtonType.NONE);
                });
            }
        }
    }

    private class LocalDialogManager extends DialogManager {
        private boolean sendRemoteAllowed = false; // protected by DialogManager.lock
        private GridPane layoutGrid;
        private Label headerLabel;
        private Label messageLabel;
        private Button dlgButton1;
        private Button dlgButton2;

        public GridPane initialiseControls() {
            layoutGrid = new GridPane();
            layoutGrid.setMaxWidth(9999.99);
            layoutGrid.setPadding(new Insets(4));
            layoutGrid.setHgap(4);
            layoutGrid.setVgap(4);
            layoutGrid.setStyle("-fx-background-color: #39395b;");
            headerLabel = new Label("");
            headerLabel.setStyle("-fx-font-size: 24px; -fx-font-style: bold; -fx-text-fill: #ccc;");
            layoutGrid.add(headerLabel, 0, 0, 2, 1);
            messageLabel = new Label("");
            messageLabel.setStyle("-fx-font-size: 20px; -fx-font-style: bold;-fx-text-fill: #ccc;");
            layoutGrid.add(messageLabel, 0, 1, 2, 1);
            dlgButton1 = new Button("");
            dlgButton2 = new Button("");
            dlgButton1.setStyle("-fx-font-size: 24px; -fx-background-color: #2e2e60; -fx-text-fill: #ccc;");
            dlgButton2.setStyle("-fx-font-size: 24px; -fx-background-color: #2e2e60; -fx-text-fill: #ccc;");
            dlgButton1.setMaxWidth(9999.9);
            dlgButton2.setMaxWidth(9999.9);
            dlgButton1.setOnAction(event -> buttonWasPressed(button1));
            dlgButton2.setOnAction(event -> buttonWasPressed(button2));
            layoutGrid.add(dlgButton1, 0, 2);
            layoutGrid.add(dlgButton2, 1, 2);
            GridPane.setHgrow(dlgButton1, Priority.ALWAYS);
            GridPane.setHgrow(dlgButton2, Priority.ALWAYS);
            dialogDidChange();
            return layoutGrid;
        }

        public LocalDialogManager withRemoteAllowed(boolean remoteAllowed) {
            synchronized (lock) {
                sendRemoteAllowed = remoteAllowed;
            }
            return this;
        }

        @Override
        protected void dialogDidChange() {
            boolean remoteAllowed;
            synchronized (lock) {
                remoteAllowed = sendRemoteAllowed;
            }

            Platform.runLater(() -> {
                synchronized (lock) {
                    layoutGrid.setVisible(mode != DialogMode.HIDE);
                    layoutGrid.setManaged(mode != DialogMode.HIDE);
                    dlgButton1.setText(toPrintableText(button1));
                    dlgButton2.setText(toPrintableText(button2));
                    messageLabel.setText(message);
                    headerLabel.setText(title);
                }
            });

            if(remoteAllowed) {
                mgr.sendCommand(new MenuDialogCommand(mode, title, message, button1, button2, CorrelationId.EMPTY_CORRELATION));
            }
        }

        @Override
        protected void buttonWasPressed(MenuButtonType btn) {
            var proceed = (delegate != null) ? delegate.apply(btn) : true;
            if(proceed) {
                hideDialog();
            }
        }
    }
}
