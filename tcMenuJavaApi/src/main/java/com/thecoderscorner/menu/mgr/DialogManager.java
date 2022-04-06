package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.remote.commands.*;

import java.util.function.Function;

public abstract class DialogManager implements DialogViewer {
    protected final Object lock = new Object();
    protected DialogMode mode = DialogMode.HIDE;
    protected String title = "";
    protected String message = "";
    protected MenuButtonType button1 = MenuButtonType.NONE;
    protected MenuButtonType button2 = MenuButtonType.NONE;
    protected Function<MenuButtonType, Boolean> delegate;
    private DialogShowMode dialogShowMode;

    @Override
    public boolean isDialogVisible() {
        synchronized (lock) {
            return mode == DialogMode.SHOW;
        }
    }

    @Override
    public void updateStateFromCommand(MenuCommand cmd) {
        // if it is not a dialog update or the dialog is currently locally locked do not update.
        if(cmd.getCommandType() != MenuCommandType.DIALOG_UPDATE || dialogShowMode == DialogShowMode.LOCAL_DELEGATE_LOCKED) return;

        var dlgCmd = (MenuDialogCommand) cmd;
        synchronized (lock) {
            if(dlgCmd.getDialogMode() == DialogMode.ACTION ) {
                // we received an action event, IE user has pressed a button remotely
                buttonWasPressed(dlgCmd.getButton1());
            }
            else {
                mode = dlgCmd.getDialogMode();
                title = dlgCmd.getHeader();
                message = dlgCmd.getBuffer();
                button1 = dlgCmd.getButton1();
                button2 = dlgCmd.getButton2();
                dialogShowMode = DialogShowMode.REGULAR;
                delegate = null;
            }
        }
        dialogDidChange();
    }

    @Override
    public DialogManager withTitle(String title, boolean silent) {
        synchronized (lock) {
            this.title = title;
        }
        if(!silent) dialogDidChange();
        return this;
    }

    @Override
    public DialogManager withMessage(String message, boolean silent) {
        synchronized (lock) {
            this.message = message;
        }
        if(!silent) dialogDidChange();
        return this;
    }

    public DialogManager withDelegate(DialogShowMode mode, Function<MenuButtonType, Boolean> delegate) {
        synchronized (lock) {
            this.dialogShowMode = mode;
            this.delegate = delegate;
        }
        return this;
    }

    @Override
    public void showDialogWithButtons(MenuButtonType b1, MenuButtonType b2) {
        synchronized (lock) {
            this.button1 = b1;
            this.button2 = b2;
            this.mode = DialogMode.SHOW;
        }
        dialogDidChange();
    }

    @Override
    public void hideDialog() {
        synchronized (lock) {
            this.mode = DialogMode.HIDE;
            this.dialogShowMode = DialogShowMode.REGULAR;
        }
        dialogDidChange();
    }

    protected String toPrintableText(MenuButtonType type) {
        switch (type) {
            case NONE:
                return "";
            case OK:
                return "OK";
            case ACCEPT:
                return "Accept";
            case CANCEL:
                return "Cancel";
            case CLOSE:
                return "Close";
            default:
                throw new IllegalArgumentException();
        }
    }

    public DialogShowMode getDialogShowMode() {
        synchronized (lock) {
            return this.dialogShowMode;
        }
    }

    public MenuButtonType getButtonType(int btnNum) {
        return btnNum == 1 ? button1 : button2;
    }

    protected abstract void dialogDidChange();
    protected void buttonWasPressed(MenuButtonType btn) {
        var proceed = (delegate != null) ? delegate.apply(btn) : true;
        if(proceed) {
            hideDialog();
        }
    }
}
