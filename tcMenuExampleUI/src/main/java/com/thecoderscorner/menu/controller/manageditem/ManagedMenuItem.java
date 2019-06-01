/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.scene.Node;

import java.util.Optional;

public abstract class ManagedMenuItem<T, I extends MenuItem> {
    private static final int TICKS_HIGHLIGHT_ON_CHANGE = 10; // about 1 second of update notification.

    protected final I item;
    protected boolean animating;
    protected int ticks;
    protected Optional<CorrelationId> waitingFor = Optional.empty();

    public ManagedMenuItem(I item) {
        this.item = item;
        ticks = 0;
        animating = false;
    }


    public boolean isAnimating() {
        return animating;
    }

    public abstract Node createNodes(RemoteMenuController controller);
    public abstract void internalChangeItem(MenuState<T> change);
    public abstract void internalTick();
    protected abstract void internalCorrelationUpdate(AckStatus status);

    public synchronized void itemChanged(MenuState<T> change) {
        animating = true;
        ticks = TICKS_HIGHLIGHT_ON_CHANGE;
        internalChangeItem(change);
    }

    public void tick() {
        ticks--;
        if(ticks == 0) animating = false;

        internalTick();
    }

    public void correltationReceived(CorrelationId key, AckStatus status) {
        waitingFor.ifPresent(correlationId -> {
            if(key.equals(correlationId)) {
                waitingFor = Optional.empty();
                internalCorrelationUpdate(status);
            }
        });
    }

}
