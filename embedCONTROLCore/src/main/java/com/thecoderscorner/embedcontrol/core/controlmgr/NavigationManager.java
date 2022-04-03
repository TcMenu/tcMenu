package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.jfx.controlmgr.TitleWidget;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.mgr.DialogManager;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.Image;

import java.util.function.BiConsumer;

/**
 * Provides the core navigational capabilities within the app, it handles the stack of panels that have been presented
 * and the ability to go backwards out of them using pop. It is also possible to reset the navigation back to a single
 * panel. Title widgets are also managed by the navigation maanger, where you can add widgets and also register for
 * clicks on the widget's underlying button.
 * @param <T> The UI display type
 * @param <I> The UI Image type required by title widgets
 */
public interface NavigationManager<T, I> {

    /**
     * Go back one level in the navigation. Usually there is a back button that will control going back one level, it
     * will be enabled whenever canClose returns true.
     */
    void popNavigation();

    /**
     * Reset the navigation stack to just this single item, removing all other items from the list.
     * @param navigation the navigation item to switch to.
     * @see PanelPresentable
     */
    void resetNavigationTo(PanelPresentable<T> navigation);

    /**
     * Push a new item onto the navigation queue, so that the panel is displayed and at the top of the stack.
     * @param navigation the navigation item to add
     * @see PanelPresentable
     */
    void pushNavigation(PanelPresentable<T> navigation);

    /**
     * Push a new menu onto the display with the option to reset the layout at the same time by passing true as the
     * second parameter.
     * @param subMenuItem the submenu to present
     * @param resetNavigation true to completely reset the navigation stack, otherwise false.
     */
    void pushMenuNavigation(SubMenuItem subMenuItem, boolean resetNavigation);

    /**
     * Gets the panel that is currently being displayed
     * @return the panel presentable that is being display
     */
    PanelPresentable<Node> currentNavigationPanel();

    /**
     * Override the current title with the new title, by default the title is set during push/pop operations
     * @param newTitle the title text
     */
    void setTitle(String newTitle);

    /**
     * Adds a title widget to the widgets displayed generally on the right hand side of the menu. On touch screen
     * systems it is possible to interact with this and clicking one results in a clicked listener callback.
     * @param titleWidget the title widget to add
     * @see TitleWidget
     */
    void addTitleWidget(TitleWidget<I> titleWidget);

    /**
     * Adds a listener that is informed when a title widget is clicked.
     * @param listener to be informed when a title widget is clicked in the UI
     */
    void addWidgetClickedListener(BiConsumer<ActionEvent, TitleWidget<Image>> listener);

    /**
     * Push a new submenu to be displayed, a shortcut for pushNavigation that prepares a new menu panel.
     * @param subMenuItem the submenu to present.
     */
    void pushMenuNavigation(SubMenuItem subMenuItem);

    /**
     * Get hold of the dialog manager instance that can present a simple one or two button dialog on the display,
     * even remotely.
     */
    DialogManager getDialogManager();
}
