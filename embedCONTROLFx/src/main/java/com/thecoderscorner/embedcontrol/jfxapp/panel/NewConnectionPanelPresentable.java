package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfxapp.EmbedControlContext;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.BaseDialogSupport;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.NewConnectionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

public class NewConnectionPanelPresentable implements PanelPresentable<Node> {
    private final GlobalSettings settings;
    private final ScheduledExecutorService executorService;
    private final EmbedControlContext context;
    private final Optional<ConnectionCreator> maybeCreator;
    private NewConnectionController controller;

    public NewConnectionPanelPresentable(GlobalSettings settings, EmbedControlContext context, Optional<ConnectionCreator> maybeCreator) {
        this.maybeCreator = maybeCreator;
        this.context = context;
        this.settings = settings;
        this.executorService = context.getExecutorService();
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(BaseDialogSupport.class.getResource("/newConnection.fxml"));
        Pane loadedPane = loader.load();
        controller = loader.getController();
        controller.initialise(settings, context, maybeCreator);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return maybeCreator.isPresent() ? ("Edit" + maybeCreator.get().getName()) :  "New Connection";
    }

    @Override
    public boolean canBeRemoved() {
        return maybeCreator.isPresent();
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
