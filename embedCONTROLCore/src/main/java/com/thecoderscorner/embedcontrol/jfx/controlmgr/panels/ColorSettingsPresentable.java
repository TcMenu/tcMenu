package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.state.MenuTree;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ColorSettingsPresentable implements PanelPresentable<Node> {
    private final Map<String, ColorCustomizable> colorRanges;
    private ColorSettingsController colorSettingsController;
    private final GlobalSettings settings;
    private final JfxNavigationManager manager;
    private final boolean inSingleMode;

    public ColorSettingsPresentable(GlobalSettings settings, JfxNavigationManager manager, String name, ColorCustomizable singleMode) {
        inSingleMode = true;
        colorRanges = Map.of(name, singleMode);
        this.manager = manager;
        this.settings = settings;
    }

    public ColorSettingsPresentable(GlobalSettings settings, JfxNavigationManager manager, ScreenLayoutPersistence layoutPersistence, MenuTree tree) {
        inSingleMode = false;
        colorRanges = new HashMap<>();
        colorRanges.put(ColorSettingsController.DEFAULT_COLOR_NAME, new GlobalColorCustomizable(settings));
        for (var item : tree.getAllSubMenus()) {
            colorRanges.put("SubMenu " + item.getName(), layoutPersistence.getColorCustomizerFor(item, Optional.empty(), false));
        }
        this.settings = settings;
        this.manager = manager;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(ColorSettingsPresentable.class.getResource("/core_fxml/generalSettings.fxml"));
        Pane loadedPane = loader.load();
        colorSettingsController = loader.getController();
        if(inSingleMode) {
            var ele = colorRanges.entrySet().stream().findFirst().orElseThrow();
            colorSettingsController.initialise(manager, settings, ele.getKey(), ele.getValue());
        } else {
            colorSettingsController.initialise(manager, settings, colorRanges);
        }
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        if(inSingleMode) {
            return colorRanges.entrySet().stream().findFirst().orElseThrow().getValue().toString();
        } else {
            return "General Settings";
        }
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
