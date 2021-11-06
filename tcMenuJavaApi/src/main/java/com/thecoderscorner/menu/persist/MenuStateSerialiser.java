package com.thecoderscorner.menu.persist;

import com.thecoderscorner.menu.domain.state.AnyMenuState;

import java.util.List;

public interface MenuStateSerialiser {
    void loadMenuStatesAndApply();
    List<AnyMenuState> loadMenuStates();
    void saveMenuStates();
}
