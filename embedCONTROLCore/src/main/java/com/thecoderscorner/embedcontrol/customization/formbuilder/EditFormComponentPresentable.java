package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.state.MenuTree;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class EditFormComponentPresentable implements PanelPresentable<Node> {
    private EditFormComponentController formComponentController;
    private final GlobalSettings settings;
    private final FormMenuComponent component;
    private JfxNavigationManager navMgr;

    public EditFormComponentPresentable(GlobalSettings settings, FormMenuComponent component, JfxNavigationManager navMgr) {
        this.settings = settings;
        this.component = component;
        this.navMgr = navMgr;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(EditFormComponentPresentable.class.getResource("/core_fxml/editFormComponent.fxml"));
        Pane loadedPane = loader.load();
        formComponentController = loader.getController();
        formComponentController.initialise(settings, component, navMgr);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "Form Component " + component.getFormItem().getDescription();
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
        formComponentController.closePressed();
    }
}
