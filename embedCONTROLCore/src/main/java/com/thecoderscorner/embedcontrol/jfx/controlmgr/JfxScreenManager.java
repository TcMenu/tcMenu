package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.PortableColor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

import java.util.Set;
import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;
import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class JfxScreenManager implements ScreenManager<Node> {
    public static final int DEFAULT_FONT_SIZE = 16;

    private final ScrollPane scrollView;
    private final MenuComponentControl controller;
    private final int cols;
    private int level;
    private GridPane currentGrid;
    private final ThreadMarshaller threadMarshaller;

    public JfxScreenManager(MenuComponentControl controller, ScrollPane scrollView, ThreadMarshaller marshaller, int cols) {
        this.threadMarshaller = marshaller;
        this.scrollView = scrollView;
        this.controller = controller;
        this.cols = cols;

        clear();
    }

    @Override
    public int getDefaultFontSize() {
        return DEFAULT_FONT_SIZE;
    }

    @Override
    public void clear() {
        currentGrid = new GridPane();
        currentGrid.setHgap(5);
        currentGrid.setVgap(5);
        currentGrid.setMaxWidth(9999);
        currentGrid.setPrefWidth(scrollView.widthProperty().doubleValue() - 30.0);
        scrollView.widthProperty().addListener((cl, oldVal, newVal) ->
                currentGrid.setPrefWidth(newVal.doubleValue() - 30.0));

        for(int i=0; i<cols; i++) {
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
        addToGridInPosition(settings, lbl);
    }

    @Override
    public EditorComponent<Node> addUpDownInteger(MenuItem item, ComponentSettings settings) {
        var analogEditor = new IntegerUpDownEditorComponent(item, controller, settings, threadMarshaller);
        addToGridInPosition(settings, analogEditor.createComponent());
        return analogEditor;
    }

    @Override
    public EditorComponent<Node> addUpDownScroll(MenuItem item, ComponentSettings settings) {
        var scrollEditor = new ScrollUpDownEditorComponent(item, controller, settings, threadMarshaller);
        addToGridInPosition(settings, scrollEditor.createComponent());
        return scrollEditor;
    }

    @Override
    public EditorComponent<Node> addBooleanButton(MenuItem item, ComponentSettings settings) {
        var boolBtn = new BoolButtonEditorComponent(item, controller, settings, threadMarshaller);
        addToGridInPosition(settings, boolBtn.createComponent());
        return boolBtn;
    }

    @Override
    public EditorComponent<Node> addRgbColorControl(MenuItem item, ComponentSettings settings) {
        var colorRgb = new TextFieldEditorComponent<PortableColor>(controller, settings, item, threadMarshaller);
        addToGridInPosition(settings, colorRgb.createComponent());
        return colorRgb;
    }

    @Override
    public EditorComponent<Node> addButtonWithAction(SubMenuItem subItem, String text, ComponentSettings componentSettings, Consumer<SubMenuItem> actionConsumer) {
        var btnEd = new SubMenuSelectButtonEditorComponent(subItem, text, controller, componentSettings, threadMarshaller, actionConsumer);
        addToGridInPosition(componentSettings, btnEd.createComponent());
        return btnEd;
    }

    @Override
    public <P> EditorComponent<Node> addTextEditor(MenuItem item, ComponentSettings settings, P prototype) {
        var textEd = new TextFieldEditorComponent<P>(controller, settings, item, threadMarshaller);
        addToGridInPosition(settings, textEd.createComponent());
        return textEd;
    }

    @Override
    public EditorComponent<Node> addDateEditorComponent(MenuItem item, ComponentSettings settings) {
        if (item instanceof EditableTextMenuItem textFld && textFld.getItemType() == EditItemType.GREGORIAN_DATE) {
            var dateEditor = new TextFieldEditorComponent<String>(controller, settings, item, threadMarshaller);
            addToGridInPosition(settings, dateEditor.createComponent());
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
            addToGridInPosition(settings, dateEditor.createComponent());
            return dateEditor;
        } else {
            throw new IllegalArgumentException("Not of time type: " + item);
        }
    }

    @Override
    public EditorComponent<Node> addListEditor(MenuItem item, ComponentSettings settings) {
        var listEd = new ListEditorComponent(controller, settings, item, threadMarshaller);
        addToGridInPosition(settings, listEd.createComponent());
        return listEd;
    }

    @Override
    public EditorComponent<Node> addHorizontalSlider(MenuItem item, ComponentSettings settings) {
        var slider = new HorizontalSliderAnalogComponent(controller, settings, item, controller.getMenuTree(), threadMarshaller);

        Canvas component = (Canvas) slider.createComponent();
        component.setWidth(scrollView.getWidth() - 30);
        component.setHeight(25);
        addToGridInPosition(settings, component);
        return slider;
    }

    private void addToGridInPosition(ComponentSettings settings, Node sp) {
        ComponentPositioning pos = settings.getPosition();
        currentGrid.add(sp, pos.getCol(), pos.getRow(), pos.getColSpan(), pos.getRowSpan());
    }

    @Override
    public void endNesting() {
        if (--level == 0) {
            scrollView.setContent(currentGrid);
        }
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
}