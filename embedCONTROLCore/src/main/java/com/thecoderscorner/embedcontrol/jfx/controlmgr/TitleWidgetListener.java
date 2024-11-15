package com.thecoderscorner.embedcontrol.jfx.controlmgr;

/**
 * This interface is used to listen for updates on title widgets, you register with the title widget directly.
 * @param <T> usually a javafx image
 */
@FunctionalInterface
public interface TitleWidgetListener<T> {
    /**
     * Called when a title widget has changed, you may not be on the UI thread at this point, if that matters you
     * should marshall the update accordingly.
     * @param widget the widget that has changed
     */
    void titleWidgetHasChanged(TitleWidget<T> widget);
}
