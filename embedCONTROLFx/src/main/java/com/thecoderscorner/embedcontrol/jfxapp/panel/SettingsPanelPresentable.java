package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.BaseDialogSupport;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.GeneralSettingsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class SettingsPanelPresentable implements PanelPresentable<Node> {
    private final GlobalSettings settings;

    public SettingsPanelPresentable(GlobalSettings settings) {
        this.settings = settings;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(SettingsPanelPresentable.class.getResource("/generalSettings.fxml"));
        Pane loadedPane = loader.load();
        GeneralSettingsController controller = loader.getController();
        controller.initialise(settings);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "App Settings";
    }

    @Override
    public boolean canBeRemoved() {
        return false;
    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public void closePanel() {

    }
}
