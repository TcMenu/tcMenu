package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

import java.util.Set;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;
import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus;
import static com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController.asFxColor;

public class JfxScreenManager implements ScreenManager {
    public static final int DEFAULT_FONT_SIZE = 16;

    private final ScrollPane scrollView;
    private final RemoteMenuController controller;
    private final int cols;
    private int level;
    private GridPane currentGrid;
    private final ThreadMarshaller threadMarshaller;

    public JfxScreenManager(RemoteMenuController controller, ScrollPane scrollView, ThreadMarshaller marshaller, int cols) {
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
    public EditorComponent addUpDownInteger(MenuItem item, ComponentSettings settings) {
        var analogEditor = new IntegerUpDownEditorComponent(item, controller, settings, threadMarshaller);
        addToGridInPosition(settings, analogEditor.createComponent());
        return analogEditor;
    }

    @Override
    public EditorComponent addUpDownScroll(MenuItem item, ComponentSettings settings) {
        var scrollEditor = new ScrollUpDownEditorComponent(item, controller, settings, threadMarshaller);
        addToGridInPosition(settings, scrollEditor.createComponent());
        return scrollEditor;
    }

    @Override
    public EditorComponent addBooleanButton(MenuItem item, ComponentSettings settings) {
        var boolBtn = new BoolButtonEditorComponent(item, controller, settings, threadMarshaller);
        addToGridInPosition(settings, boolBtn.createComponent());
        return boolBtn;
    }

    @Override
    public EditorComponent addRgbColorControl(MenuItem item, ComponentSettings settings) {
        var colorRgb = new RgbColorEditorComponent(item, controller, settings, threadMarshaller);
        addToGridInPosition(settings, colorRgb.createComponent());
        return colorRgb;
    }

    @Override
    public <T> EditorComponent addTextEditor(MenuItem item, ComponentSettings settings, T prototype) {
        var textEd = new TextFieldEditorComponent<T>(controller, settings, item, threadMarshaller);
        addToGridInPosition(settings, textEd.createComponent());
        return textEd;
    }

    @Override
    public EditorComponent addDateEditorComponent(MenuItem item, ComponentSettings settings) {
        if (item instanceof EditableTextMenuItem textFld && textFld.getItemType() == EditItemType.GREGORIAN_DATE) {
            var dateEditor = new DateFieldEditorComponent(controller, settings, item, threadMarshaller);
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
    public EditorComponent addTimeEditorComponent(MenuItem item, ComponentSettings settings) {
        if (item instanceof EditableTextMenuItem textFld && ALLOWED_TIME_TYPES.contains(textFld.getItemType())) {
            var dateEditor = new TimeFieldEditorComponent(controller, settings, item, threadMarshaller);
            addToGridInPosition(settings, dateEditor.createComponent());
            return dateEditor;
        } else {
            throw new IllegalArgumentException("Not of time type: " + item);
        }
    }

    @Override
    public EditorComponent addListEditor(MenuItem item, ComponentSettings settings) {
        var listEd = new ListEditorComponent(controller, settings, item, threadMarshaller);
        addToGridInPosition(settings, listEd.createComponent());
        return listEd;
    }

    @Override
    public EditorComponent addHorizontalSlider(MenuItem item, ComponentSettings settings) {
        var slider = new HorizontalSliderAnalogComponent(controller, settings, item, controller.getManagedMenu(), threadMarshaller);
        addToGridInPosition(settings, slider.createComponent());
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

    private static void setGridPositioning(ComponentSettings settings, Node lbl) {
        GridPane.setRowIndex(lbl, settings.getPosition().getRow());
        GridPane.setColumnIndex(lbl, settings.getPosition().getCol());
        GridPane.setRowSpan(lbl, settings.getPosition().getRowSpan());
        GridPane.setColumnSpan(lbl, settings.getPosition().getColSpan());
    }

    public static TextAlignment toTextAlignment(PortableAlignment justification) {
        switch (justification) {
            case RIGHT:
                return TextAlignment.RIGHT;
            case CENTER:
                return TextAlignment.CENTER;
            case LEFT:
            default:
                return TextAlignment.LEFT;
        }
    }
}