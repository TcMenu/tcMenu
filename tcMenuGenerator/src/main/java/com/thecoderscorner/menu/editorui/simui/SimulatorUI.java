package com.thecoderscorner.menu.editorui.simui;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.TcMenuFormPersistence;
import com.thecoderscorner.embedcontrol.core.util.DataException;
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
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;

public class SimulatorUI {
    public static final int WIDGET_ID_FORM = 1;
    public static final int WIDGET_ID_SETTINGS = 2;

    private JfxNavigationHeader navMgr;
    private MenuTree menuTree;
    private Scene scene;
    private Stage dialogStage;
    private Consumer<WindowEvent> closeConsumer;
    private FormBuilderPresentable formEditorPanel;
    private CurrentEditorProject project;
    private GlobalSettings settings;
    private AppDataStore dataStore;
    private ContextMenu contextMenu;
    private String uuid;
    private TcMenuFormPersistence currentLayout;
    private MenuItemStore itemStore;

    public void presentSimulator(MenuTree menuTree, CurrentEditorProject project, Stage stage) {
        this.menuTree = menuTree;
        this.project = project;
        this.uuid = project.getGeneratorOptions().getApplicationUUID().toString();
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

        var appContext = MenuEditorApp.getInstance().getAppContext();
        settings = appContext.getGlobalSettings();
        dataStore = appContext.getEcDataStore();
        var dialogMgr = new DoNothingDialogManager();
        navMgr = new JfxNavigationHeader(appContext.getExecutorService(), settings);
        var control = new SimulatorUIControl();
        navMgr.initialiseUI(dialogMgr, control, scrollPane);
        itemStore = new MenuItemStore(settings, menuTree, "Untitled", 7, 4, true);
        navMgr.pushMenuNavigation(MenuTree.ROOT, itemStore);

        VBox vbox = new VBox(navMgr.initialiseControls());
        border.setTop(vbox);

        var editorImage = new Image(Objects.requireNonNull(AppDataStore.class.getResourceAsStream("/img-core/layout-off.png")));
        var settingsImage = new Image(Objects.requireNonNull(AppDataStore.class.getResourceAsStream("/img-core/settings-cog.png")));
        TitleWidget<Image> formWidget = new TitleWidget<>(List.of(editorImage), 1, 0, WIDGET_ID_FORM);
        navMgr.addTitleWidget(formWidget);
        navMgr.addTitleWidget(new TitleWidget<>(List.of(settingsImage), 1, 0, WIDGET_ID_SETTINGS));
        navMgr.addWidgetClickedListener((actionEvent, titleWidget) -> widgetClickListener(titleWidget));
        navMgr.getButtonFor(formWidget).ifPresent(button -> button.setContextMenu(contextMenuForLayout()));
        formEditorPanel = new FormBuilderPresentable(settings, opts.getApplicationUUID(), menuTree, navMgr, itemStore, this::saveFormConsumer);
    }

    private void saveFormConsumer(String xml) {
        if(currentLayout == null) return;
        currentLayout = new TcMenuFormPersistence(currentLayout.getFormId(), currentLayout.getUuid(), currentLayout.getFormName(), xml);
        try {
            dataStore.updateForm(currentLayout);
        } catch (DataException e) {
            System.getLogger("Simulator").log(ERROR, "Form Save failed");
        }
        rebuildGrid();
    }

    private ContextMenu contextMenuForLayout() {
        if(contextMenu == null) {
            contextMenu = new ContextMenu();
        }

        contextMenu.getItems().clear();

        for(var form : dataStore.getAllFormsForUuid(uuid)) {
            var selText = currentLayout != null && currentLayout.getFormId() == form.getFormId() ? " *" : "";
            var itemLayout = new javafx.scene.control.MenuItem(form.getFormName() + " [" +form.getFormId() + "]" + selText);
            itemLayout.setOnAction(event -> {
                currentLayout = form;
                itemStore.loadLayout(currentLayout.getXmlData(), UUID.fromString(uuid));
                rebuildGrid();
                contextMenuForLayout();
            });
            contextMenu.getItems().add(itemLayout);
        }

        contextMenu.getItems().add(new SeparatorMenuItem());
        var createNew = new javafx.scene.control.MenuItem("Create New Layout");
        createNew.setOnAction(event -> createActivateNewLayout());
        contextMenu.getItems().add(createNew);

        if(currentLayout != null) {
            var editLayout = new javafx.scene.control.MenuItem("Edit " + currentLayout.getFormName());
            editLayout.setOnAction(event -> navMgr.pushNavigationIfNotOnStack(formEditorPanel));
            contextMenu.getItems().add(editLayout);
        }

        return contextMenu;
    }

    private void createActivateNewLayout() {
        var form = TcMenuFormPersistence.anEmptyFormPersistence(uuid);
        try {
            dataStore.updateForm(form);
        } catch (DataException e) {
            System.getLogger("Simulator").log(ERROR, "Create form failed", e);
        }
        currentLayout = form;
        rebuildGrid();
        contextMenuForLayout();
    }

    private void rebuildGrid() {
        if (navMgr.currentNavigationPanel() instanceof JfxMenuPresentable menuPanel) {
            menuPanel.entirelyRebuildGrid();
        }
    }

    private void widgetClickListener(TitleWidget<Image> titleWidget) {
        if(titleWidget.getAppId() == WIDGET_ID_SETTINGS) {
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
