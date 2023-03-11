package com.thecoderscorner.menu.editorui.simui;

import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.TreeComponentManager;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class SimulatorUI {
    private JfxNavigationHeader navMgr;
    private MenuTree menuTree;
    private SimTreeComponentManager treeComponentManager;
    private Scene scene;
    private Stage dialogStage;
    private Consumer<WindowEvent> closeConsumer;

    public void presentSimulator(MenuTree menuTree, String fileName, UUID appUuid, Stage stage, LocaleMappingHandler handler) {
        this.menuTree = menuTree;
        MenuItemFormatter.setDefaultLocalHandler(handler);

        ScrollPane scrollPane = new ScrollPane();
        var border = new BorderPane();
        border.setCenter(scrollPane);
        border.setMaxSize(9999,9999);
        border.setPrefSize(600, 400);

        dialogStage = new Stage();
        dialogStage.setTitle(appUuid.toString());
        dialogStage.initOwner(stage);
        scene = new Scene(border);
        dialogStage.setScene(scene);
        dialogStage.show();
        dialogStage.setOnCloseRequest(event -> closeConsumer.accept(event));

        var settings = new GlobalSettings(SimulatorUI.class);
        var dialogMgr = new DoNothingDialogManager();
        var layoutPersistence = new ScreenLayoutPersistence(menuTree, settings, appUuid, Path.of(fileName), 18);
        navMgr = new JfxNavigationHeader(layoutPersistence);
        var control = new SimulatorUIControl();

        treeComponentManager = new SimTreeComponentManager(settings, Executors.newSingleThreadScheduledExecutor(),
                Platform::runLater, control, layoutPersistence);
        navMgr.initialiseUI(treeComponentManager, dialogMgr, control, scrollPane);
        navMgr.pushMenuNavigation(MenuTree.ROOT);

        VBox vbox = new VBox(navMgr.initialiseControls());
        border.setTop(vbox);
    }

    public void setCloseConsumer(Consumer<WindowEvent> eventConsumer) {
        this.closeConsumer = eventConsumer;
    }

    public void itemHasChanged(MenuItem item) {
        treeComponentManager.menuItemHasChanged(item);
    }

    public class SimulatorUIControl implements MenuComponentControl {
        @Override
        public CorrelationId editorUpdatedItem(MenuItem menuItem, Object val) {
            return CorrelationId.EMPTY_CORRELATION;
        }

        @Override
        public CorrelationId editorUpdatedItemDelta(MenuItem menuItem, int delta) {
            return CorrelationId.EMPTY_CORRELATION;
        }

        @Override
        public void connectionStatusChanged(AuthStatus authStatus) {
            // doesn't really apply locally
        }

        @Override
        public MenuTree getMenuTree() {
            return menuTree;
        }

        @Override
        public String getConnectionName() {
            return "Simulator";
        }

        @Override
        public JfxNavigationManager getNavigationManager() {
            return navMgr;
        }

        @Override
        public void presentIoTAuthPanel() {
        }
    }

    class DoNothingDialogManager extends DialogManager {

        @Override
        protected void dialogDidChange() {
        }
    }

    private class SimTreeComponentManager extends TreeComponentManager<Node> {
        public SimTreeComponentManager(GlobalSettings settings, ScheduledExecutorService scheduledExecutorService, ThreadMarshaller marshaller,
                                       SimulatorUIControl control, ScreenLayoutPersistence layoutPersistence) {
            super(settings, scheduledExecutorService, marshaller, control, layoutPersistence);
        }

        public void menuItemHasChanged(MenuItem item) {
            if(item == null) {

            } else if (editorComponents.containsKey(item.getId())) {
                EditorComponent<Node> nodeEditorComponent = editorComponents.get(item.getId());
                nodeEditorComponent.onItemUpdated(controller.getMenuTree().getMenuState(item));
                nodeEditorComponent.structuralChange(item);
            }
        }
    }
}
