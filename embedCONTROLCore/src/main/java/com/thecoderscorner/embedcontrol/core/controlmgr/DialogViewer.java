package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;

public interface DialogViewer {
    void setButton1(MenuButtonType type);

    void setButton2(MenuButtonType type);

    void show(boolean visible);

    void setText(String title, String subject);

    void statusHasChanged(AuthStatus status);
}
