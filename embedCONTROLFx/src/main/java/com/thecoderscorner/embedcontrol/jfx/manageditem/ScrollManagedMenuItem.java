/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfx.manageditem;

import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.CurrentScrollPositionMenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.Optional;

public class ScrollManagedMenuItem extends IntegerBaseManagedMenuItem<ScrollChoiceMenuItem, CurrentScrollPosition> {
    private CurrentScrollPosition currentPosition;

    public ScrollManagedMenuItem(ScrollChoiceMenuItem item) {
        super(item);
        currentPosition = new CurrentScrollPosition(0, "");
    }

    @Override
    public void internalChangeItem(AnyMenuState state) {
        if(state instanceof CurrentScrollPositionMenuState posState) {
            currentPosition = posState.getValue();
            itemLabel.setText(posState.getValue().getValue());
        }
    }

    protected Optional<CorrelationId> handleAdjustment(RemoteMenuController menuController, int mode) {
        var newPosition = new CurrentScrollPosition(currentPosition.getPosition() + mode, "");
        return Optional.of(menuController.sendAbsoluteUpdate(item, newPosition.toString()));
    }
}