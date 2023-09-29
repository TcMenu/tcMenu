package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class FormManagerPanelPresentable implements PanelPresentable<Node> {
    private final EmbedControlContext context;
    private JfxNavigationManager navigationManager = null;
    private FormManagerController controller;

    public FormManagerPanelPresentable(EmbedControlContext context) {
        this.context = context;
    }

    public FormManagerPanelPresentable(JfxNavigationManager navigationManager, EmbedControlContext context) {
        this.context = context;
        this.navigationManager = navigationManager;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(FormManagerPanelPresentable.class.getResource("/formManager.fxml"));
        BorderPane loadedPane = loader.load();
        loadedPane.setPrefWidth(width);
        controller = loader.getController();
        controller.initialise(navigationManager, context);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "Form Manager";
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
