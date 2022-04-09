package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

public class AuthIoTMonitorPresentable implements PanelPresentable {
    private final MenuManagerServer managerServer;

    public AuthIoTMonitorPresentable(MenuManagerServer managerServer) {
        this.managerServer = managerServer;
    }

    @Override
    public Object getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(AuthIoTMonitorPresentable.class.getResource("/core_fxml/authIoTMonitor.fxml"));
        Pane loadedPane = loader.load();
        AuthIoTMonitorController controller = loader.getController();
        controller.initialise(managerServer.getAuthenticator(), managerServer);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "Authentication and IoT monitor";
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
