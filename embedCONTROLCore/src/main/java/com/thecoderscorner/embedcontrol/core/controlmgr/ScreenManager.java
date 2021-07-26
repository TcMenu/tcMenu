package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ScreenManager
{
    int getDefaultFontSize();
    void clear();
    void startNesting();
    void endNesting();
    void addStaticLabel(String label, ComponentSettings position, boolean isHeader);
    EditorComponent addUpDownInteger(MenuItem item, ComponentSettings settings);
    EditorComponent addUpDownScroll(MenuItem item, ComponentSettings settings);
    EditorComponent addBooleanButton(MenuItem item, ComponentSettings settings);
    <T> EditorComponent addTextEditor(MenuItem item, ComponentSettings settings, T prototype);
    EditorComponent addListEditor(MenuItem item, ComponentSettings settings);
    EditorComponent addDateEditorComponent(MenuItem item, ComponentSettings settings);
    EditorComponent addTimeEditorComponent(MenuItem item, ComponentSettings settings);
    EditorComponent addHorizontalSlider(MenuItem item, ComponentSettings settings);
    EditorComponent addRgbColorControl(MenuItem item, ComponentSettings settings);
}