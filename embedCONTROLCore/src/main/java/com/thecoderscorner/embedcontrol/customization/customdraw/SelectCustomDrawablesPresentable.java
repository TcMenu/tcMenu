package com.thecoderscorner.embedcontrol.customization.customdraw;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.formbuilder.FormMenuComponent;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

public class SelectCustomDrawablesPresentable implements PanelPresentable<Node> {
    private CustomDrawablesSelectionController controller;
    private final GlobalSettings settings;
    private final FormMenuComponent component;
    private final JfxNavigationManager navMgr;
    private Consumer<Boolean> closeListener;

    public SelectCustomDrawablesPresentable(GlobalSettings settings, FormMenuComponent component, JfxNavigationManager navMgr) {
        this.settings = settings;
        this.component = component;
        this.navMgr = navMgr;
    }

    public void addPanelCloseListener(Consumer<Boolean> closeListener) {
        this.closeListener = closeListener;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(SelectCustomDrawablesPresentable.class.getResource("/core_fxml/customDrawingSelection.fxml"));
        Pane loadedPane = loader.load();
        controller = loader.getController();
        controller.initialise(settings, component, navMgr);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "Customized Drawing Manager";
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
        controller.closePressed();
        if(closeListener != null) closeListener.accept(true);
    }
}
