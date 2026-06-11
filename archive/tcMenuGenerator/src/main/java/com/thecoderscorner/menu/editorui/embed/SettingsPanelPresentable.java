package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class SettingsPanelPresentable implements PanelPresentable<Node> {
    private final GlobalSettings settings;
    private final AppDataStore dataStore;

    public SettingsPanelPresentable(GlobalSettings settings, AppDataStore dataStore) {
        this.settings = settings;
        this.dataStore = dataStore;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(SettingsPanelPresentable.class.getResource("/generalSettings.fxml"));
        Pane loadedPane = loader.load();
        GeneralSettingsController controller = loader.getController();
        controller.initialise(settings, dataStore);
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
