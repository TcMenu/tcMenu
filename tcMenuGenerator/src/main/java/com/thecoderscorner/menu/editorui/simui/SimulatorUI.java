package com.thecoderscorner.menu.editorui.simui;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
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
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class SimulatorUI {
    public static final int WIDGET_ID_FORM = 1;
    public static final int WIDGET_ID_SETTINGS = 2;

    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private JfxNavigationHeader navMgr;
    private MenuTree menuTree;
    private Scene scene;
    private Stage dialogStage;
    private Consumer<WindowEvent> closeConsumer;
    private CurrentEditorProject project;
    private GlobalSettings settings;
    private AppDataStore dataStore;
    private ContextMenu contextMenu;
    private String uuid;
    private MenuItemStore itemStore;
    private Path formsDir;

    public void presentSimulator(MenuTree menuTree, CurrentEditorProject project, Stage stage) {
        var mainDir = Paths.get(project.getFileName()).getParent();
        formsDir = mainDir.resolve("forms");
        this.menuTree = menuTree;
        this.project = project;
        var appContext = MenuEditorApp.getContext().getAppContext();
        this.settings = appContext.getGlobalSettings();
        this.uuid = project.getGeneratorOptions().getApplicationUUID().toString();
        MenuItemFormatter.setDefaultLocalHandler(project.getLocaleHandler());

        ScrollPane scrollPane = new ScrollPane();
        var border = new BorderPane();
        border.setStyle("-fx-font-size: " + settings.getDefaultFontSize());
        border.setBackground(new Background(new BackgroundFill(asFxColor(settings.getTextColor().getBg()), null, null)));
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

        dataStore = appContext.getEcDataStore();

        var dialogMgr = new DoNothingDialogManager();
        navMgr = new JfxNavigationHeader(appContext.getExecutorService(), settings);
        var control = new SimulatorUIControl();
        navMgr.initialiseUI(dialogMgr, control, scrollPane);
        itemStore = new MenuItemStore(settings, menuTree, "Untitled", 7, 4, true);
        navMgr.pushMenuNavigation(MenuTree.ROOT, itemStore);

        VBox vbox = new VBox(navMgr.initialiseControls());
        border.setTop(vbox);

        var settingsImage = new Image(Objects.requireNonNull(AppDataStore.class.getResourceAsStream("/img-core/settings-cog.png")));
        navMgr.addTitleWidget(new TitleWidget<>(List.of(settingsImage), 1, 0, WIDGET_ID_SETTINGS));
        navMgr.addWidgetClickedListener((actionEvent, titleWidget) -> widgetClickListener(titleWidget));
    }

    private void widgetClickListener(TitleWidget<Image> titleWidget) {
        if(titleWidget.getAppId() == WIDGET_ID_SETTINGS) {
            var settingsPanel = new ColorSettingsPresentable(settings, navMgr, "Global", false);
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
