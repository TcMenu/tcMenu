package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class WaitingForConnectionPanel implements PanelPresentable<Node> {
    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(WaitingForConnectionPanel.class.getResource("/ecui/waitingForConnection.fxml"));
        loader.setResources(MenuEditorApp.getBundle());
        return loader.<Pane>load();
    }

    @Override
    public String getPanelName() {
        return "Waiting for connection";
    }

    @Override
    public boolean canBeRemoved() {
        return true;
    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public void closePanel() {

    }
}
