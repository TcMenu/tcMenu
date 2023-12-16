package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

public class ConnectionPairingPresentable implements PanelPresentable<Node> {

    private final ConnectionCreator creator;
    private final EmbedControlContext context;
    private final Consumer<Boolean> pairingHasFinished;
    private final JfxNavigationManager navigationManager;
    private final String name;

    public ConnectionPairingPresentable(JfxNavigationManager navigationManager, ConnectionCreator creator,
                                        EmbedControlContext context, String name, Consumer<Boolean> pairingHasFinished) {
        this.creator = creator;
        this.context = context;
        this.name = name;
        this.pairingHasFinished = pairingHasFinished;
        this.navigationManager = navigationManager;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ecui/pairingDialog.fxml"));
        loader.setResources(MenuEditorApp.getBundle());
        Pane myPane = loader.load();
        PairingController pairingController = loader.getController();
        pairingController.initialise(navigationManager, creator, context.getExecutorService(), pairingHasFinished);
        return myPane;
    }

    @Override
    public String getPanelName() {
        return "Pair with " + name;
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

    }
}
