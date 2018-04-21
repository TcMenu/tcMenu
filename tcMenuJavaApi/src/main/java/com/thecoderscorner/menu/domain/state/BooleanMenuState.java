package com.thecoderscorner.menu.domain.state;

public class BooleanMenuState extends MenuState<Boolean> {
    public BooleanMenuState(boolean changed, boolean active, boolean value) {
        super(changed, active, value);
    }
}
