package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

/**
 * Represents the interface that is used to work with dialogs, it supports updating a dialog from an incoming message,
 *
 */
public interface DialogViewer {
    boolean isDialogVisible();
    DialogViewer withTitle(String title, boolean silent);
    DialogManager withMessage(String message, boolean silent);
    void showDialogWithButtons(MenuButtonType b1, MenuButtonType b2);
    void hideDialog();
    void updateStateFromCommand(MenuCommand cmd);
}
