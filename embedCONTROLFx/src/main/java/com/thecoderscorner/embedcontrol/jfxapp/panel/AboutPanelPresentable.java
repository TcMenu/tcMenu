package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.BaseDialogSupport;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class AboutPanelPresentable implements PanelPresentable<Node> {
    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(BaseDialogSupport.class.getResource("/aboutPage.fxml"));
        return loader.<Pane>load();
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
