package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxMenuEditorFactory;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.state.MenuTree;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.util.Objects;
import java.util.UUID;

public class FormBuilderPresentable implements PanelPresentable<Node> {
    private final MenuTree tree;
    private final JfxNavigationManager navMgr;
    private final UUID appUuid;
    private FormEditorController formEditorController;
    private final GlobalSettings settings;
    private final MenuItemStore store;
    private final JfxMenuEditorFactory editorFactory;
    private Node loadedPane;
    private TcMenuFormSaveConsumer saveConsumer;

    public FormBuilderPresentable(GlobalSettings settings, UUID uuid, MenuTree tree, JfxNavigationManager navMgr,
                                  MenuItemStore store, TcMenuFormSaveConsumer saveConsumer,
                                  JfxMenuEditorFactory editorFactory) {
        this.settings = settings;
        this.editorFactory = editorFactory;
        this.tree = tree;
        this.navMgr = navMgr;
        this.appUuid = uuid;
        this.store = store;
        this.saveConsumer = saveConsumer;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        var loader = new FXMLLoader(FormBuilderPresentable.class.getResource("/core_fxml/formEditor.fxml"));
        if (loadedPane == null) {
            loadedPane = loader.load();
            formEditorController = loader.getController();
            formEditorController.initialise(settings, tree, appUuid, navMgr, store, saveConsumer, editorFactory);
        } else {
            formEditorController.initialise(settings, tree, appUuid, navMgr, store, saveConsumer, editorFactory);
        }
        navMgr.bindHeightToPane(loadedPane);
        return loadedPane;
    }

    @Override
    public String getPanelName() {
        return "*BETA* Form Editor: " + tree.getMenuById(store.getRootItemId()).orElseThrow();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormBuilderPresentable that = (FormBuilderPresentable) o;
        return Objects.equals(tree, that.tree) && Objects.equals(appUuid, that.appUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tree, appUuid);
    }
}
