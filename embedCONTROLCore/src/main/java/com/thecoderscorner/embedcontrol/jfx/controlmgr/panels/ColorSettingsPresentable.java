package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.Objects;
import java.util.function.Consumer;

public class ColorSettingsPresentable implements PanelPresentable<Node> {
    private ColorSettingsController colorSettingsController;
    private final GlobalSettings settings;
    private final JfxNavigationManager manager;
    private final String name;
    private final boolean allowAdd;
    private Consumer<Boolean> closeListener = null;

    public ColorSettingsPresentable(GlobalSettings settings, JfxNavigationManager manager, String name, boolean allowAdd) {
        this.manager = manager;
        this.settings = settings;
        this.name = name;
        this.allowAdd = allowAdd;
    }

    public void addPanelCloseListener(Consumer<Boolean> closeListener) {
        this.closeListener = closeListener;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(ColorSettingsPresentable.class.getResource("/core_fxml/generalSettings.fxml"));
        Pane loadedPane = loader.load();
        colorSettingsController = loader.getController();
        colorSettingsController.initialise(manager, settings, name, allowAdd);
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
        if(closeListener != null) closeListener.accept(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorSettingsPresentable that = (ColorSettingsPresentable) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
