package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;

public class NoDialogFacilities extends DialogManager {
    @Override
    protected void dialogDidChange() {
        synchronized (lock) {
            if(mode == DialogMode.SHOW) {
                buttonWasPressed(button1 == MenuButtonType.NONE ? button2 : button1);
            }
        }
    }

    @Override
    protected void buttonWasPressed(MenuButtonType btn) {
        if(delegate != null) delegate.apply(btn);
    }
}
