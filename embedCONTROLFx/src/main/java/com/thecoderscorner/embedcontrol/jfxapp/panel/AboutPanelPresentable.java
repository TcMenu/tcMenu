package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.jfxapp.VersionHelper;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.AboutController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class AboutPanelPresentable implements PanelPresentable<Node> {
    private final VersionHelper versionHelper;
    public AboutPanelPresentable(VersionHelper versionHelper) {
        this.versionHelper = versionHelper;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(AboutPanelPresentable.class.getResource("/aboutPage.fxml"));
        var panel = loader.<Pane>load();
        var aboutController = (AboutController)loader.getController();
        aboutController.initialise(versionHelper);
        return panel;
    }

    @Override
    public String getPanelName() {
        return "About";
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
    }
}
