package com.thecoderscorner.embedcontrol.jfx.panel;

import com.thecoderscorner.embedcontrol.jfx.dialog.BaseDialogSupport;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class AboutPanelPresentable implements PanelPresentable {
    @Override
    public void presentPanelIntoArea(ScrollPane pane) throws Exception {
        var loader = new FXMLLoader(BaseDialogSupport.class.getResource("/aboutPage.fxml"));
        Pane loadedPane = loader.load();
        pane.setContent(loadedPane);
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
    public boolean closePanelIfPossible() {
        return true;
    }
}
