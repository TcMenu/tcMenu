package com.thecoderscorner.embedcontrol.jfx.controlmgr;

@FunctionalInterface
public interface TitleWidgetListener<T> {
    void titleWidgetHasChanged(TitleWidget<T> widget);
}
