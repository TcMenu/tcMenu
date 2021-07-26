package com.thecoderscorner.embedcontrol.jfx.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.DialogViewer;
import com.thecoderscorner.embedcontrol.core.controlmgr.TreeComponentManager;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxScreenManager;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RemoteConnectionPanel implements PanelPresentable, DialogViewer {
    private final ConnectionCreator creator;
    private final GlobalSettings settings;
    private final ScheduledExecutorService executorService;
    private RemoteMenuController controller;
    private TreeComponentManager treeManager;
    private JfxScreenManager screenManager;
    private ScheduledFuture<?> taskRef;

    public RemoteConnectionPanel(ConnectionCreator creator, GlobalSettings settings, ScheduledExecutorService executorService) {
        this.creator = creator;
        this.settings = settings;
        this.executorService = executorService;
    }

    @Override
    public void presentPanelIntoArea(ScrollPane pane) throws Exception {
        Label waitingLabel = new Label("Waiting for connection...");
        pane.setContent(waitingLabel);
        controller = creator.start();
        screenManager = new JfxScreenManager(controller, pane, Platform::runLater, 2);
        treeManager = new TreeComponentManager(screenManager, controller, settings, this, executorService, Platform::runLater);
        taskRef = executorService.schedule(() -> treeManager.timerTick(), 100, TimeUnit.MILLISECONDS);
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

    }

    @Override
    public void setButton2(MenuButtonType type) {

    }

    @Override
    public void show(boolean visible) {

    }

    @Override
    public void setText(String title, String subject) {

    }

    @Override
    public void statusHasChanged(AuthStatus status) {

    }
}
