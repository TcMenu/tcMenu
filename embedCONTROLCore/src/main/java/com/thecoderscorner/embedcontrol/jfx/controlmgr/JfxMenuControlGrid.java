package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.LayoutEditorSettingsPresenter;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;
import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class JfxMenuControlGrid implements MenuControlGrid<Node>, PanelPresentable<Node> {
    private static final int DEFAULT_INDENTATION = 8;
    private final MenuComponentControl controller;
    private final ScreenLayoutPersistence layoutPersistence;
    private final TreeComponentManager<Node> treeComponentManager;
    private final ThreadMarshaller threadMarshaller;
    private final int levelIndentation;
    private final MenuItem presentedItem;
    private final boolean recursive;
    private GridPane currentGrid;
    private int level;
    private double presentableWidth = 999.99;
    private boolean createdLayout = false;
    private LayoutEditorSettingsPresenter layoutEditingPresenter;

    public JfxMenuControlGrid(MenuComponentControl controller, ThreadMarshaller marshaller, TreeComponentManager<Node> componentManager,
                              ScreenLayoutPersistence layoutPersistence, MenuItem presentedItem) {
        this.threadMarshaller = marshaller;
        this.treeComponentManager = componentManager;
        this.presentedItem = presentedItem;
        this.recursive = layoutPersistence.isRecursive(presentedItem);
        this.controller = controller;
        this.layoutPersistence = layoutPersistence;
        this.levelIndentation = DEFAULT_INDENTATION;
    }

    @Override
    public Node getPanelToPresent(double preferredWidth) throws Exception {
        if(!createdLayout) {
            createdLayout = true;
            presentableWidth = preferredWidth;
            layoutPersistence.resetAutoLayout();
            clear();
            treeComponentManager.renderMenuRecursive(this, MenuItemHelper.asSubMenu(presentedItem), recursive);
        }
        return currentGrid;
    }

    @Override
    public String getPanelName() {
        if(controller == null) return "empty";
        return presentedItem == MenuTree.ROOT ? controller.getConnectionName() : presentedItem.getName();
    }

    @Override
    public boolean canBeRemoved() {
        return true;
    }

    @Override
    public boolean canClose() {
        return presentedItem != MenuTree.ROOT;
    }

    @Override
    public void closePanel() {
        clear();
    }

    @Override
    public void setLayoutEditor(LayoutEditorSettingsPresenter presenter) {
        layoutEditingPresenter = presenter;
    }

    @Override
    public void clear() {
        level = 0;
        currentGrid = new GridPane();
        currentGrid.setHgap(5);
        currentGrid.setVgap(5);
        currentGrid.setMaxWidth(9999);
        currentGrid.setPrefWidth(presentableWidth);

        for(int i=0; i<layoutPersistence.getGridSize(); i++) {
            var column1 = new ColumnConstraints();
            column1.setPercentWidth(50);
            currentGrid.getColumnConstraints().add(column1);
        }
    }

    @Override
    public void addStaticLabel(String label, ComponentSettings settings, boolean isHeader) {
        var lbl = new Label(label);
        lbl.setTextAlignment(toTextAlignment(settings.getJustification()));
        if (isHeader) lbl.setStyle("-fx-font-weight: bold;");
        var col = asFxColor(settings.getColors().foregroundFor(RenderingStatus.NORMAL, ConditionalColoring.ColorComponentType.TEXT_FIELD));
        lbl.setTextFill(col);
        addToGridInPosition(Optional.empty(), settings, lbl);
    }

    @Override
    public EditorComponent<Node> addUpDownControl(MenuItem item, ComponentSettings settings) {
        UpDownEditorComponentBase<?> editor;
        if(item instanceof ScrollChoiceMenuItem) {
            editor = new ScrollUpDownEditorComponent(item, controller, settings, threadMarshaller);
        } else {
            editor = new IntegerUpDownEditorComponent(item, controller, settings, threadMarshaller);
        }
        addToGridInPosition(Optional.of(item), settings, editor.createComponent());
        return editor;
    }

    @Override
    public EditorComponent<Node> addBooleanButton(MenuItem item, ComponentSettings settings) {
        var boolBtn = new BoolButtonEditorComponent(item, controller, settings, threadMarshaller);
        addToGridInPosition(Optional.of(item), settings, boolBtn.createComponent());
        return boolBtn;
    }

    @Override
    public EditorComponent<Node> addRgbColorControl(MenuItem item, ComponentSettings settings) {
        var colorRgb = new TextFieldEditorComponent<PortableColor>(controller, settings, item, threadMarshaller);
        addToGridInPosition(Optional.of(item), settings, colorRgb.createComponent());
        return colorRgb;
    }

    @Override
    public EditorComponent<Node> addButtonWithAction(SubMenuItem subItem, String text, ComponentSettings componentSettings, Consumer<SubMenuItem> actionConsumer) {
        var btnEd = new SubMenuSelectButtonEditorComponent(subItem, text, controller, componentSettings, threadMarshaller, actionConsumer);
        addToGridInPosition(Optional.empty(), componentSettings, btnEd.createComponent());
        return btnEd;
    }

    @Override
    public <P> EditorComponent<Node> addTextEditor(MenuItem item, ComponentSettings settings, P prototype) {
        var textEd = new TextFieldEditorComponent<P>(controller, settings, item, threadMarshaller);
        addToGridInPosition(Optional.of(item), settings, textEd.createComponent());
        return textEd;
    }

    @Override
    public EditorComponent<Node> addDateEditorComponent(MenuItem item, ComponentSettings settings) {
        if (item instanceof EditableTextMenuItem textFld && textFld.getItemType() == EditItemType.GREGORIAN_DATE) {
            var dateEditor = new TextFieldEditorComponent<String>(controller, settings, item, threadMarshaller);
            addToGridInPosition(Optional.of(item), settings, dateEditor.createComponent());
            return dateEditor;
        } else {
            throw new IllegalArgumentException("Not of gregorian date type: " + item);
        }
    }

    private static final Set<EditItemType> ALLOWED_TIME_TYPES = Set.of(
            EditItemType.TIME_12H, EditItemType.TIME_24H, EditItemType.TIME_24_HUNDREDS, EditItemType.TIME_12H_HHMM,
            EditItemType.TIME_24H_HHMM, EditItemType.TIME_DURATION_HUNDREDS, EditItemType.TIME_DURATION_SECONDS
    );

    @Override
    public EditorComponent<Node> addTimeEditorComponent(MenuItem item, ComponentSettings settings) {
        if (item instanceof EditableTextMenuItem textFld && ALLOWED_TIME_TYPES.contains(textFld.getItemType())) {
            var dateEditor = new TextFieldEditorComponent<String>(controller, settings, item, threadMarshaller);
            addToGridInPosition(Optional.of(item), settings, dateEditor.createComponent());
            return dateEditor;
        } else {
            throw new IllegalArgumentException("Not of time type: " + item);
        }
    }

    @Override
    public EditorComponent<Node> addListEditor(MenuItem item, ComponentSettings settings) {
        var listEd = new ListEditorComponent(controller, settings, item, threadMarshaller);
        addToGridInPosition(Optional.of(item), settings, listEd.createComponent());
        return listEd;
    }

    @Override
    public EditorComponent<Node> addHorizontalSlider(MenuItem item, ComponentSettings settings) {
        var slider = new HorizontalSliderAnalogComponent(controller, settings, item, controller.getMenuTree(), threadMarshaller);

        Canvas component = (Canvas) slider.createComponent();
        component.setWidth(presentableWidth - 30);
        component.setHeight(25);
        addToGridInPosition(Optional.of(item), settings, component);
        return slider;
    }

    private void addToGridInPosition(Optional<MenuItem> item, ComponentSettings settings, Node sp) {
        if(layoutEditingPresenter != null && item.isPresent()) {
            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(sp);
            var openLayoutButton = new Button("*");
            openLayoutButton.setOnAction(evt -> {
                if(layoutEditingPresenter!=null) layoutEditingPresenter.layoutEditorRequired(item.get());
            });
            borderPane.setRight(openLayoutButton);
            sp = borderPane;
        }
        ComponentPositioning pos = settings.getPosition();
        currentGrid.add(sp, pos.getCol(), pos.getRow(), pos.getColSpan(), pos.getRowSpan());
        GridPane.setMargin(sp, new Insets(0, 0, 0, level * levelIndentation));
    }

    @Override
    public void endNesting() {
        --level;
    }

    @Override
    public void startNesting() {
        level++;
    }

    public static TextAlignment toTextAlignment(PortableAlignment justification) {
        return switch (justification) {
            case RIGHT -> TextAlignment.RIGHT;
            case CENTER -> TextAlignment.CENTER;
            default -> TextAlignment.LEFT;
        };
    }

    public void connectionIsUp(boolean up) {
        currentGrid.setDisable(!up);
    }
}