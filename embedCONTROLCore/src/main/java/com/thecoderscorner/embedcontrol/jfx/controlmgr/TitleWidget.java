package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TitleWidget<T> {
    private final List<T> images;
    private final int numberOfStates;
    private final AtomicInteger currentState;
    private final List<TitleWidgetListener<T>> changeListeners = new CopyOnWriteArrayList<>();

    public TitleWidget(List<T> images, int numberOfStates, int initialState) {
        this.images = images;
        this.numberOfStates = numberOfStates;
        this.currentState = new AtomicInteger(initialState);
    }

    public void addWidgetChangeListener(TitleWidgetListener<T> listener) {
        changeListeners.add(listener);
    }

    public T getCurrentImage() {
        return images.get(currentState.get());
    }

    public void setCurrentState(int state) {
        if(state > numberOfStates) throw new IllegalArgumentException("Out of range");
        currentState.set(state);
        valueHasChanged();
    }

    public void valueHasChanged() {
        for(var l : changeListeners) {
            l.titleWidgetHasChanged(this);
        }
    }

    public void setCurrentState(Enum<?> enumState) {
        if(enumState.ordinal() > numberOfStates) throw new IllegalArgumentException("Out of range");
        currentState.set(enumState.ordinal());
        valueHasChanged();
    }
}
