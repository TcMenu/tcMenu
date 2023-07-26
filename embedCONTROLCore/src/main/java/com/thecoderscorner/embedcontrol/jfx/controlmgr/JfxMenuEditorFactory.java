package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.mgr.DialogManager;
import javafx.scene.Node;

import java.util.Set;
import java.util.function.Consumer;

/**
 * The menu control grid is the JavaFX component that renders menu items into a GridPanel. It creates instances of
 * the editor components and places them into the panel. To place them into the panel, each item that is added  takes
 * a ComponentSettings object that has the positioning and rendering data.
 *
 * @see ComponentSettings
 */
public class JfxMenuEditorFactory implements MenuEditorFactory<Node> {
    private final MenuComponentControl controller;
    private final DialogManager dlgMgr;
    private final ThreadMarshaller threadMarshaller;

    public JfxMenuEditorFactory(MenuComponentControl controller, ThreadMarshaller marshaller, DialogManager dlgMgr) {
        this.threadMarshaller = marshaller;
        this.controller = controller;
        this.dlgMgr = dlgMgr;
    }

    @Override
    public EditorComponent<Node> createUpDownControl(MenuItem item, ComponentSettings settings) {
        UpDownEditorComponentBase<?> editor;
        if(item instanceof ScrollChoiceMenuItem) {
            editor = new ScrollUpDownEditorComponent(item, controller, settings, threadMarshaller);
        } else {
            editor = new IntegerUpDownEditorComponent(item, controller, settings, threadMarshaller);
        }
        return editor;
    }

    @Override
    public EditorComponent<Node> createBooleanButton(MenuItem item, ComponentSettings settings) {
        return new BoolButtonEditorComponent(item, controller, settings, threadMarshaller);
    }

    @Override
    public EditorComponent<Node> createRgbColorControl(MenuItem item, ComponentSettings settings) {
        return new TextFieldEditorComponent<PortableColor>(controller, settings, item, dlgMgr, threadMarshaller);
    }

    @Override
    public EditorComponent<Node> createButtonWithAction(MenuItem subItem, String text, ComponentSettings componentSettings, Consumer<MenuItem> actionConsumer) {
        return new MenuSelectButtonEditorComponent(subItem, text, controller, componentSettings, threadMarshaller, actionConsumer);
    }

    @Override
    public EditorComponent<Node> createIoTMonitor(MenuItem item, ComponentSettings componentSettings) {
        return createButtonWithAction(item, MenuItemFormatter.defaultInstance().getItemName(item), componentSettings, menuItem -> controller.presentIoTAuthPanel());
    }

    @Override
    public <P> EditorComponent<Node> createTextEditor(MenuItem item, ComponentSettings settings, P prototype) {
        return new TextFieldEditorComponent<P>(controller, settings, item, dlgMgr, threadMarshaller);
    }

    @Override
    public EditorComponent<Node> createDateEditorComponent(MenuItem item, ComponentSettings settings) {
        if (item instanceof EditableTextMenuItem textFld && textFld.getItemType() == EditItemType.GREGORIAN_DATE) {
            return new TextFieldEditorComponent<String>(controller, settings, item, dlgMgr, threadMarshaller);
        } else {
            throw new IllegalArgumentException("Not of gregorian date type: " + item);
        }
    }

    private static final Set<EditItemType> ALLOWED_TIME_TYPES = Set.of(
            EditItemType.TIME_12H, EditItemType.TIME_24H, EditItemType.TIME_24_HUNDREDS, EditItemType.TIME_12H_HHMM,
            EditItemType.TIME_24H_HHMM, EditItemType.TIME_DURATION_HUNDREDS, EditItemType.TIME_DURATION_SECONDS
    );

    @Override
    public EditorComponent<Node> createTimeEditorComponent(MenuItem item, ComponentSettings settings) {
        if (item instanceof EditableTextMenuItem textFld && ALLOWED_TIME_TYPES.contains(textFld.getItemType())) {
            return new TextFieldEditorComponent<String>(controller, settings, item, dlgMgr, threadMarshaller);
        } else {
            throw new IllegalArgumentException("Not of time type: " + item);
        }
    }

    @Override
    public EditorComponent<Node> createListEditor(MenuItem item, ComponentSettings settings) {
        return new ListEditorComponent(controller, settings, item, threadMarshaller);
    }

    @Override
    public EditorComponent<Node> createHorizontalSlider(MenuItem item, ComponentSettings settings, double presentableWidth) {
        var slider = new HorizontalSliderAnalogComponent(controller, settings, item, controller.getMenuTree(), threadMarshaller);
        return slider;
    }
}