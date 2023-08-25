package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A menu control grid is responsible for actually placing controls onto the UI. It has a method for each type of
 * control that can be added, and has helper methods to clear down the grid, and to nest menu items, this allows for
 * cases where recursive menu rendering is used to give visual clues such as indentation.
 * @param <T>
 */
public interface MenuEditorFactory<T>
{
    /**
     * Creates a control that acts somewhat like a spinner, where there is an up and a down button, and somewhere to
     * present the current value. It is normally used for items with a finite set of values like Analog, Enum and Scroll
     * menu item types.
     * @param item the item to present
     * @param settings the settings to use to display it.
     * @return the created component
     */
    EditorComponent<T> createUpDownControl(MenuItem item, ComponentSettings settings);

    /**
     * Creates a button item that can either represent an action or boolean menu item, in the case of boolean items it
     * will act like a toggle, in the case of action items, it will activate the item.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component
     */
    EditorComponent<T> createBooleanButton(MenuItem item, ComponentSettings settings);

    /**
     * Creates a text item that can represent nearly every menu item. It can also edit most editable item types by
     * presenting a suitable control for editing if need be.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @param prototype A prototype value so we can find the right editor. See MenuItemHelper.getDefaultFor(item)
     * @param <P> the type for editing, will normally be inferred from the default.
     * @return the created component.
     */
    <P> EditorComponent<T> createTextEditor(MenuItem item, ComponentSettings settings, P prototype);

    /**
     * Creates a list item that represents a list of values, at the moment only String values are possible
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> createListEditor(MenuItem item, ComponentSettings settings);

    /**
     * Creates an editable date item that represents a gregorian date, a suitable date editor is provided during editing.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> createDateEditorComponent(MenuItem item, ComponentSettings settings);

    /**
     * Creates an editable time item that represents a time or duration, with an editor if needed for edit operations
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> createTimeEditorComponent(MenuItem item, ComponentSettings settings);

    /**
     * Creates a horizontal slider that presents the percentage of the value graphically along with as text. The user can
     * modify the value by moving the slider.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> createHorizontalSlider(MenuItem item, ComponentSettings settings, double presentableWidth);

    /**
     * Creates a rgb color item that can be edited using a color picker.
     * @param item the item to present
     * @param settings the settings to use to display it
     * @return the created component.
     */
    EditorComponent<T> createRgbColorControl(MenuItem item, ComponentSettings settings);

    /**
     * Creates a button that will run the actionConsumer upon being activated. This allows for custom actions to be
     * attached to menu items.
     * @param item the item to present
     * @param text the text to use on the button
     * @param settings the settings to use to display it
     * @param actionConsumer the consumer to be called upon activation of the button
     * @return the created component.
     */
    EditorComponent<T> createButtonWithAction(MenuItem item, String text, ComponentSettings settings,
                                           Consumer<MenuItem> actionConsumer);

    /**
     * Creates a control that is associated with the IoT Monitor action, in many cases this would be a button that
     * when pressed opens the IoT panel.
     * @param item the IoT item
     * @param componentSettings the component settings
     * @return the created component
     */
    EditorComponent<T> createIoTMonitor(MenuItem item, ComponentSettings componentSettings);

    /**
     * Create a component based on the settings provided, IE it will be of a component type and style of that provided
     * @param item the item to create
     * @param componentSettings the component settings to use
     * @param subMenuAction the action to take on submenu being clicked
     * @return the created component
     */
    Optional<EditorComponent<T>> getComponentEditorItem(MenuItem item,ComponentSettings componentSettings, Consumer<MenuItem> subMenuAction);
}