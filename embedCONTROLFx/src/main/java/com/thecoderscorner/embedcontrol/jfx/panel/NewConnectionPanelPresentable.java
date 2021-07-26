package com.thecoderscorner.embedcontrol.jfx.panel;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.dialog.BaseDialogSupport;
import com.thecoderscorner.embedcontrol.jfx.dialog.NewConnectionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class NewConnectionPanelPresentable implements PanelPresentable {
    private final PlatformSerialFactory serialFactory;
    private Consumer<ConnectionCreator> creatorConsumer;
    private GlobalSettings settings;
    private ScheduledExecutorService executorService;
    private NewConnectionController controller;

    public NewConnectionPanelPresentable(PlatformSerialFactory serialFactory, Consumer<ConnectionCreator> creatorConsumer,
                                         GlobalSettings settings, ScheduledExecutorService executorService) {
        this.serialFactory = serialFactory;
        this.creatorConsumer = creatorConsumer;
        this.settings = settings;
        this.executorService = executorService;
    }

    @Override
    public void presentPanelIntoArea(ScrollPane pane) throws Exception {
        var loader = new FXMLLoader(BaseDialogSupport.class.getResource("/newConnection.fxml"));
        Pane loadedPane = loader.load();
        controller = loader.getController();
        controller.initialise(settings, executorService, serialFactory, creatorConsumer);
        pane.setContent(loadedPane);
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
    public boolean closePanelIfPossible() {
        controller.destroy();
        return true;
    }
}
