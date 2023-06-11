package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class FormEditorPropertiesPresentable implements PanelPresentable<Node> {
    private final MenuItemStore store;
    private final FormEditorController formEditor;
    private FormEditorPropertiesController formEditorController;
    private JfxNavigationManager navMgr;
    private Node loadedPane;

    public FormEditorPropertiesPresentable(MenuItemStore store, FormEditorController controller, JfxNavigationManager navMgr) {
        this.store = store;
        this.formEditor = controller;
        this.navMgr = navMgr;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        if(formEditorController != null) {
            return loadedPane;
        } else {
            var loader = new FXMLLoader(FormEditorPropertiesPresentable.class.getResource("/core_fxml/formEditorProperties.fxml"));
            loadedPane = loader.load();
            formEditorController = loader.getController();
            formEditorController.initialise(store, formEditor, navMgr);
            return loadedPane;
        }
    }

    @Override
    public String getPanelName() {
        return "Form Editor Properties";
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
