package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfxapp.EmbedControlContext;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.BaseDialogSupport;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.NewConnectionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

public class NewConnectionPanelPresentable implements PanelPresentable<Node> {
    private final GlobalSettings settings;
    private final ScheduledExecutorService executorService;
    private final EmbedControlContext context;
    private NewConnectionController controller;

    public NewConnectionPanelPresentable(GlobalSettings settings, EmbedControlContext context) {
        this.context = context;
        this.settings = settings;
        this.executorService = context.getExecutorService();
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(BaseDialogSupport.class.getResource("/newConnection.fxml"));
        Pane loadedPane = loader.load();
        controller = loader.getController();
        controller.initialise(settings, context, Optional.empty());
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "New Connection";
    }

    @Override
    public boolean canBeRemoved() {
        return false;
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void closePanel() {
        controller.destroy();
    }
}
