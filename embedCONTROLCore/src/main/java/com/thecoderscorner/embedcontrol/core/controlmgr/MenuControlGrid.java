package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.customization.LayoutEditorSettingsPresenter;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;

import java.util.function.Consumer;

/**
 * The screen manager object is responsible for creating components
 * @param <T>
 */
public interface MenuControlGrid<T>
{
    void clear();
    void startNesting();
    void endNesting();
    void addStaticLabel(String label, ComponentSettings position, boolean isHeader);
    EditorComponent<T> addUpDownControl(MenuItem item, ComponentSettings settings);
    EditorComponent<T> addBooleanButton(MenuItem item, ComponentSettings settings);
    <P> EditorComponent<T> addTextEditor(MenuItem item, ComponentSettings settings, P prototype);
    EditorComponent<T> addListEditor(MenuItem item, ComponentSettings settings);
    EditorComponent<T> addDateEditorComponent(MenuItem item, ComponentSettings settings);
    EditorComponent<T> addTimeEditorComponent(MenuItem item, ComponentSettings settings);
    EditorComponent<T> addHorizontalSlider(MenuItem item, ComponentSettings settings);
    EditorComponent<T> addRgbColorControl(MenuItem item, ComponentSettings settings);
    EditorComponent<T> addButtonWithAction(MenuItem subItem, String text, ComponentSettings componentSettings,
                                           Consumer<MenuItem> actionConsumer);
    EditorComponent<T> addIoTMonitor(MenuItem item, ComponentSettings componentSettings);

    /**
     * When a layout editor exists, an extra button is added to each item that can bring up an editor panel to edit
     * the settings for the item
     * @param editorPresenter the editor presenter
     */
    void setLayoutEditor(LayoutEditorSettingsPresenter editorPresenter);

}