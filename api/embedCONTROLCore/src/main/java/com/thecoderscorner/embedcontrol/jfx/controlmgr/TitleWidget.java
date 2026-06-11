package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A title widget is generally presented in the top right corner of a display. It can represent some state such as
 * signal strength or connectivity and also on touch screens you can interact with them as they are buttons. States
 * are essentially integer values (or can be enums too) and each integer value has an associated image. Changing the
 * state results in the listener being called, and normally when added to a display, the widget will update on screen.
 *
 * Each underlying display implementation has a different way of expressing images, so this class is generic to allow
 * for each implementation
 * @param <T> the image implementation for the display technology.
 */
public class TitleWidget<T> {
    private final List<T> images;
    private final int numberOfStates;
    private final AtomicInteger currentState;
    private final List<TitleWidgetListener<T>> changeListeners = new CopyOnWriteArrayList<>();
    private final int appId;

    /**
     * Create a title widget given a list of images for each state, the number of states and the initial state.
     * @param images the list of images to use for each state
     * @param numberOfStates the number of allowed states
     * @param initialState the initial state to start in
     */
    public TitleWidget(List<T> images, int numberOfStates, int initialState) {
        this.images = images;
        this.numberOfStates = numberOfStates;
        this.currentState = new AtomicInteger(initialState);
        this.appId = 0;
    }

    /**
     * Create a title widget given a list of images for each state, the number of states and the initial state. Also
     * allows the user to set an ID for this widget that can be used to identify it later in callbacks.
     * @param images the list of images to use for each state
     * @param numberOfStates the number of allowed states
     * @param initialState the initial state to start in
     * @param appId an ID that can be compared against later using `getAppId`
     */
    public TitleWidget(List<T> images, int numberOfStates, int initialState, int appId) {
        this.images = images;
        this.numberOfStates = numberOfStates;
        this.currentState = new AtomicInteger(initialState);
        this.appId = appId;
    }

    /**
     * Adds a listener that is notified whenever the current state of this widget is changed.
     * @param listener the listener to receive change events
     */
    public void addWidgetChangeListener(TitleWidgetListener<T> listener) {
        changeListeners.add(listener);
    }

    /**
     * @return the image for the current state
     */
    public T getCurrentImage() {
        return images.get(currentState.get());
    }

    /**
     * Changes the current state and notifies listeners
     * @param state the new state
     */
    public void setCurrentState(int state) {
        if(state > numberOfStates) throw new IllegalArgumentException("Out of range");
        currentState.set(state);
        valueHasChanged();
    }

    /**
     * Actually calls the listeners to indicate that the value has changed, usually only used internally
     */
    public void valueHasChanged() {
        for(var l : changeListeners) {
            l.titleWidgetHasChanged(this);
        }
    }

    /**
     * An override of set current state for enumerations. It uses the ordinal of the enumeration to pick the state.
     * @param enumState the enum value
     */
    public void setCurrentState(Enum<?> enumState) {
        if(enumState.ordinal() > numberOfStates) throw new IllegalArgumentException("Out of range");
        currentState.set(enumState.ordinal());
        valueHasChanged();
    }

    /**
     * @return the app ID provided during construction
     */
    public int getAppId() {
        return appId;
    }
}
