package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.state.MenuTree;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.UUID;

public class FormBuilderPresentable implements PanelPresentable<Node> {
    private final MenuTree tree;
    private final JfxNavigationManager navMgr;
    private final UUID appUuid;
    private FormEditorController formEditorController;
    private final GlobalSettings settings;
    private final MenuItemStore store;
    private Node loadedPane;

    public FormBuilderPresentable(GlobalSettings settings, UUID uuid, MenuTree tree, JfxNavigationManager navMgr, MenuItemStore store) {
        this.settings = settings;
        this.tree = tree;
        this.navMgr = navMgr;
        this.appUuid = uuid;
        this.store = store;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(FormBuilderPresentable.class.getResource("/core_fxml/formEditor.fxml"));
        if (loadedPane == null) {
            loadedPane = loader.load();
            formEditorController = loader.getController();
            formEditorController.initialise(settings, tree, appUuid, navMgr, store);
        } else {
            formEditorController.initialise(settings, tree, appUuid, navMgr, store);
        }
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "Form Editor for " + tree.getMenuById(store.getRootItemId()).orElseThrow();
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
        formEditorController.closePressed();
    }

    public MenuItemStore getCurrentStore() {
        return store;
    }
}
