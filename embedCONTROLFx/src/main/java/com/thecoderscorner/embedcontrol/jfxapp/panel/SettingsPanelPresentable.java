package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.BaseDialogSupport;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.GeneralSettingsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class SettingsPanelPresentable implements PanelPresentable {
    private final GlobalSettings settings;

    public SettingsPanelPresentable(GlobalSettings settings) {
        this.settings = settings;
    }

    @Override
    public void presentPanelIntoArea(BorderPane pane) throws IOException {
        var loader = new FXMLLoader(BaseDialogSupport.class.getResource("/generalSettings.fxml"));
        Pane loadedPane = loader.load();
        pane.setCenter(loadedPane);
        GeneralSettingsController controller = loader.getController();
        controller.initialise(settings);
    }

    @Override
    public String getPanelName() {
        return "Settings";
    }

    @Override
    public boolean canBeRemoved() {
        return false;
    }

    @Override
    public boolean closePanelIfPossible() {
        return true;
    }
}
