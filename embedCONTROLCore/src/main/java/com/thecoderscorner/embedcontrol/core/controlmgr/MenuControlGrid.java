package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.customization.LayoutEditorSettingsPresenter;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;

import java.util.function.Consumer;

/**
 * A menu control grid is responsible for actually placing controls onto the UI. It has a method for each type of
 * control that can be added, and has helper methods to clear down the grid, and to nest menu items, this allows for
 * cases where recursive menu rendering is used to give visual clues such as indentation.
 * @param <T>
 */
public interface MenuControlGrid<T>
{
    /**
     * Completely clear down the UI so that any controls that have been added are removed.
     */
    void clear();

    /**
     * This is generally used in recursive mode, as we go into another submenu to tell the UI that a visual clue
     * is needed to represent the nesting.
     */
    void startNesting();

    /**
     * This is generally used in recursive mode to go back a level of nesting.
     */
    void endNesting();

    /**
     * Add a static label that is not representing a menu item, purely for display purposes
     * @param label the text of the label
     * @param position the settings to use for the rendering
     * @param isHeader if the item is a header item or regular item
     */
    void addStaticLabel(String label, ComponentSettings position, boolean isHeader);

    /**
     * Creates a control that acts somewhat like a spinner, where there is an up and a down button, and somewhere to
     * present the current value. It is normally used for items with a finite set of values like Analog, Enum and Scroll
     * menu item types.
     * @param item the item to present
     * @param settings the settings to use to display it.
     * @return the created component
     */
    EditorComponent<T> addUpDownControl(MenuItem item, ComponentSettings settings);

    /**
     * Creates a button item that can either represent an action or boolean menu item, in the case of boolean items it
     * will act like a toggle, in the case of action items, it will activate the item.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component
     */
    EditorComponent<T> addBooleanButton(MenuItem item, ComponentSettings settings);

    /**
     * Creates a text item that can represent nearly every menu item. It can also edit most editable item types by
     * presenting a suitable control for editing if need be.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @param prototype A prototype value so we can find the right editor. See MenuItemHelper.getDefaultFor(item)
     * @param <P> the type for editing, will normally be inferred from the default.
     * @return the created component.
     */
    <P> EditorComponent<T> addTextEditor(MenuItem item, ComponentSettings settings, P prototype);

    /**
     * Creates a list item that represents a list of values, at the moment only String values are possible
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> addListEditor(MenuItem item, ComponentSettings settings);

    /**
     * Creates an editable date item that represents a gregorian date, a suitable date editor is provided during editing.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> addDateEditorComponent(MenuItem item, ComponentSettings settings);

    /**
     * Creates an editable time item that represents a time or duration, with an editor if needed for edit operations
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> addTimeEditorComponent(MenuItem item, ComponentSettings settings);

    /**
     * Creates a horizontal slider that presents the percentage of the value graphically along with as text. The user can
     * modify the value by moving the slider.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> addHorizontalSlider(MenuItem item, ComponentSettings settings);

    /**
     * Creates a rgb color item that can be edited using a color picker.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> addRgbColorControl(MenuItem item, ComponentSettings settings);

    /**
     * Creates a button that will run the actionConsumer upon being activated. This allows for custom actions to be
     * attached to menu items.
     * @param item the item to present
     * @param text the text to use on the button
     * @param settings the settings to use to display it
     * @param actionConsumer the consumer to be called upon activation of the button
     * @return the created component.
     */
    EditorComponent<T> addButtonWithAction(MenuItem item, String text, ComponentSettings settings,
                                           Consumer<MenuItem> actionConsumer);

    /**
     * Creates a control that is associated with the IoT Monitor action, in many cases this would be a button that
     * when pressed opens the IoT panel.
     * @param item the IoT item
     * @param componentSettings the component settings
     * @return the created component
     */
    EditorComponent<T> addIoTMonitor(MenuItem item, ComponentSettings componentSettings);

    /**
     * A layout editor allows us to redefine the layout of a menu item, or even an entire submenu. When a layout editor
     * exists, an extra button is added to each item that can bring up an editor panel to edit
     * the settings for the item
     * @param editorPresenter the editor presenter
     */
    void setLayoutEditor(LayoutEditorSettingsPresenter editorPresenter);

}