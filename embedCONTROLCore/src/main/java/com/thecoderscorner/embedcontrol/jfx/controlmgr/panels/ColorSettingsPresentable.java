package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.embedcontrol.customization.formbuilder.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ColorSettingsPresentable implements PanelPresentable<Node> {
    private final Map<String, ColorCustomizable> colorRanges = new HashMap<>();
    private final MenuItemStore store;
    private ColorSettingsController colorSettingsController;
    private final GlobalSettings settings;
    private final JfxNavigationManager manager;
    private final String name;

    public ColorSettingsPresentable(GlobalSettings settings, JfxNavigationManager manager, String name,
                                    MenuItemStore store) {
        this.store = store;
        this.manager = manager;
        this.settings = settings;
        this.name = name;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(ColorSettingsPresentable.class.getResource("/core_fxml/generalSettings.fxml"));
        Pane loadedPane = loader.load();
        colorSettingsController = loader.getController();
        colorSettingsController.initialise(manager, settings, store, name);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "Color settings";
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
