package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.NavigationManager;
import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.Map;
import java.util.Optional;

public class GeneralSettingsPresentable implements PanelPresentable<Node> {
    private ColorSettingsController colorSettingsController;
    private final Map<String, ColorCustomizable> allColorSettings;
    private final GlobalSettings settings;
    private final JfxNavigationManager manager;

    public GeneralSettingsPresentable(GlobalSettings settings, JfxNavigationManager manager, Map<String, ColorCustomizable> allColorSettings) {
        this.allColorSettings = allColorSettings;
        this.settings = settings;
        this.manager = manager;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(GeneralSettingsPresentable.class.getResource("/core_fxml/generalSettings.fxml"));
        Pane loadedPane = loader.load();
        colorSettingsController = loader.getController();
        colorSettingsController.initialise(manager, settings, allColorSettings);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "General Settings";
    }

    @Override
    public boolean canBeRemoved() {
        return true;
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void closePanel() {
        colorSettingsController.closePressed();
    }
}
