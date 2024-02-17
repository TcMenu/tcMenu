package com.thecoderscorner.menu.editorui.simui;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.FormPersistMode;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.TcMenuFormPersistence;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.customization.formbuilder.FormBuilderPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.*;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ColorSettingsPresentable;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.embed.FormManagerController;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;
import static java.lang.System.Logger.Level.ERROR;

public class SimulatorUI {
    public static final int WIDGET_ID_FORM = 1;
    public static final int WIDGET_ID_SETTINGS = 2;

    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

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
        if(Files.exists(formsDir) && Files.isDirectory(formsDir)) {
            autoImportAllFormsIntoDb(formsDir);
        }

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
        formEditorPanel = new FormBuilderPresentable(settings, opts.getApplicationUUID(), menuTree, navMgr, itemStore,
                this::saveFormConsumer, new JfxMenuEditorFactory(control, Platform::runLater, dialogMgr));
    }

    private void autoImportAllFormsIntoDb(Path formsDir) {
        try(var fileList = Files.list(formsDir)) {
            // get all project forms to ensure they are still valid. This automatically prunes any project stored
            // forms that no longer exist on the disk.
            List<TcMenuFormPersistence> allProjectForms = dataStore.getAllProjectBasedForms();

            // next we get all the forms that are defined in the project forms dir
            var xmlFiles = fileList.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".xml")).toList();

            // and create project form records for any that don't exist. These are effectively symlinks to the file
            // that is on disk, and are mainly kept in sync on a best efforts basis using this method, every time the
            // preview window is loaded.
            for(var f : xmlFiles) {
                var match = allProjectForms.stream().anyMatch(form -> form.getFileNameIfPresent().orElseThrow().equals(f));
                if(!match) {
                    var data = Files.readString(f);
                    var form = FormManagerController.buildObjectFromXml(data, FormPersistMode.WITHIN_PROJECT, f).orElseThrow();
                    dataStore.updateForm(form);
                }
            }
        } catch (Exception e) {
            logger.log(ERROR, "Could not sync forms with project files in " + formsDir);
        }
    }

    private void saveFormConsumer(String xml, String newName) {
        if(currentLayout == null) return;
        currentLayout = currentLayout.projectFormLayoutUpdate(newName);
        try {
            var fileNameOpt = currentLayout.getFileNameIfPresent();
            if(fileNameOpt.isPresent()) {
                Files.writeString(fileNameOpt.get(), xml, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            dataStore.updateForm(currentLayout);
        } catch (Exception e) {
            System.getLogger("Simulator").log(ERROR, "Form Save failed");
        }
        rebuildGrid();
        contextMenuForLayout();
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
        var maybeName = acquireFormNameFromUser();
        try {
            if (maybeName.isEmpty()) return;
            var name = maybeName.get();
            var fileName = formsDir.resolve(VariableNameGenerator.makeNameFromVariable(name) + ".xml");
            if (!Files.exists(formsDir)) {
                Files.createDirectory(formsDir);
            }
            var formData = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<EmbedControl boardUuid=\"" + uuid + "\" layoutName=\"" + name + "\"><MenuLayouts/><ColorSets/></EmbedControl>";
            Files.writeString(fileName, formData);
            currentLayout = TcMenuFormPersistence.createProjectFileFormPersistence(name, uuid, fileName.toString());
            dataStore.updateForm(currentLayout);
            itemStore.loadLayout(currentLayout.getXmlData(), UUID.fromString(uuid));
            navMgr.pushNavigationIfNotOnStack(formEditorPanel);
        } catch(Exception ex) {
            logger.log(ERROR, "Could not create new layout", ex);
        }
    }

    private Optional<String> acquireFormNameFromUser() {
        var inputDlg = new TextInputDialog("");
        inputDlg.setTitle("Input name");
        inputDlg.setHeaderText("Enter name of form for " + Paths.get(project.getFileName()).getFileName());
        var maybeText = inputDlg.showAndWait();
        if(maybeText.isEmpty()) return Optional.empty();
        if(maybeText.get().trim().isEmpty()) return Optional.empty();
        return maybeText;
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
