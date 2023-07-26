package com.thecoderscorner.menu.editorui.simui;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.customization.formbuilder.FormBuilderPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxMenuPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.TitleWidget;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ColorSettingsPresentable;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SimulatorUI {
    public static final int WIDGET_ID_FORM = 1;
    public static final int WIDGET_ID_SETTINGS = 2;

    private JfxNavigationHeader navMgr;
    private MenuTree menuTree;
    private Scene scene;
    private Stage dialogStage;
    private Consumer<WindowEvent> closeConsumer;
    private GlobalSettings settings;
    private FormBuilderPresentable formEditorPanel;
    private CurrentEditorProject project;

    public void presentSimulator(MenuTree menuTree, CurrentEditorProject project, Stage stage) {
        this.menuTree = menuTree;
        this.project = project;
        MenuItemFormatter.setDefaultLocalHandler(project.getLocaleHandler());

        ScrollPane scrollPane = new ScrollPane();
        var border = new BorderPane();
        border.setCenter(scrollPane);
        border.setMaxSize(9999,9999);
        border.setPrefSize(600, 738);
        scrollPane.setMaxSize(9999,9999);
        scrollPane.setFitToWidth(true);

        dialogStage = new Stage();
        CodeGeneratorOptions opts = project.getGeneratorOptions();
        dialogStage.setTitle("Preview for " + opts.getApplicationName() + " (" + opts.getApplicationUUID() + ")");
        dialogStage.initOwner(stage);
        scene = new Scene(border);
        dialogStage.setScene(scene);
        dialogStage.show();
        dialogStage.setOnCloseRequest(event -> closeConsumer.accept(event));

        settings = new GlobalSettings();
        var dialogMgr = new DoNothingDialogManager();
        navMgr = new JfxNavigationHeader(Executors.newSingleThreadScheduledExecutor(), settings);
        var control = new SimulatorUIControl();
        navMgr.initialiseUI(dialogMgr, control, scrollPane);
        var store = new MenuItemStore(settings, menuTree, "Untitled", 7, 4, true);
        navMgr.pushMenuNavigation(MenuTree.ROOT, store);

        VBox vbox = new VBox(navMgr.initialiseControls());
        border.setTop(vbox);

        var editorImage = new Image(Objects.requireNonNull(AppDataStore.class.getResourceAsStream("/img-core/layout-off.png")));
        var settingsImage = new Image(Objects.requireNonNull(AppDataStore.class.getResourceAsStream("/img-core/settings-cog.png")));
        navMgr.addTitleWidget(new TitleWidget<>(List.of(editorImage), 1, 0, WIDGET_ID_FORM));
        navMgr.addTitleWidget(new TitleWidget<>(List.of(settingsImage), 1, 0, WIDGET_ID_SETTINGS));
        navMgr.addWidgetClickedListener((actionEvent, titleWidget) -> showFormEditorPanel(titleWidget));
        formEditorPanel = new FormBuilderPresentable(settings, opts.getApplicationUUID(), menuTree, navMgr, store);
    }

    private void showFormEditorPanel(TitleWidget<Image> titleWidget) {
        if(titleWidget.getAppId() == WIDGET_ID_FORM) {
            navMgr.pushNavigationIfNotOnStack(formEditorPanel);
        } else if(titleWidget.getAppId() == WIDGET_ID_SETTINGS) {
            var settingsPanel = new ColorSettingsPresentable(settings, navMgr, "Global", formEditorPanel.getCurrentStore(), false);
            navMgr.pushNavigationIfNotOnStack(settingsPanel);
        }
    }

    public void setCloseConsumer(Consumer<WindowEvent> eventConsumer) {
        this.closeConsumer = eventConsumer;
    }

    public void itemHasChanged(MenuItem item) {
        Platform.runLater(() -> {
            if(navMgr.currentNavigationPanel() instanceof JfxMenuPresentable menuPanel) {
                if(item == null) {
                    menuPanel.entirelyRebuildGrid();
                } else {
                    menuPanel.getGridComponent().itemHasUpdated(item);
                }
            }
        });
    }

    public void closeWindow() {
        Platform.runLater(() -> dialogStage.close());
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
            return "Simulator " + project.getLocaleHandler().getFromLocaleOrUseSource(project.getGeneratorOptions().getApplicationName());
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
}
