package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.remote.commands.*;

import java.util.function.Function;

/**
 * Dialog Manager provides the capability to work with dialogs, to present them, change the values of them and also
 * to update them from a remote command arriving. This includes being able to deal with activation from a remote.
 *
 * Usually, for local or remote activities this class is extended and the dialogDidChange method is implemented to update
 * the UI accordingly and optionally, the buttonWasPressed method may need to be overridden.
 */
public abstract class DialogManager {
    protected final Object lock = new Object();
    protected DialogMode mode = DialogMode.HIDE;
    protected String title = "";
    protected String message = "";
    protected MenuButtonType button1 = MenuButtonType.NONE;
    protected MenuButtonType button2 = MenuButtonType.NONE;
    protected Function<MenuButtonType, Boolean> delegate;
    private DialogShowMode dialogShowMode;

    /**
     * @return true if the dialog is on display, otherwise false
     */
    public boolean isDialogVisible() {
        synchronized (lock) {
            return mode == DialogMode.SHOW;
        }
    }

    /**
     * Update the dialog from an incoming remote command, checking first if the command is a dialog event, and then
     * updating all the fields and calling dialogDidChange.
     * @param cmd the command
     */
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

    /**
     * Using builder syntax you can show dialog using the with commands, this sets the title
     * @param title the new title
     * @param silent if an update should be triggered
     * @return itself for chaining
     */
    public DialogManager withTitle(String title, boolean silent) {
        synchronized (lock) {
            this.title = title;
        }
        if(!silent) dialogDidChange();
        return this;
    }

    /**
     * Using builder syntax you can show dialog using the with commands this sets the message
     * @param message the message field
     * @param silent if an update should be triggered
     * @return itself for chaining
     */
    public DialogManager withMessage(String message, boolean silent) {
        synchronized (lock) {
            this.message = message;
        }
        if(!silent) dialogDidChange();
        return this;
    }

    /**
     * Using builder syntax you can show dialog using the with commands this sets the delegate and mode
     * @param mode the mode in which the dialog should show, regular, or locally
     * @param delegate the delegate to call on a button being pressed
     * @return itself for chaining
     */
    public DialogManager withDelegate(DialogShowMode mode, Function<MenuButtonType, Boolean> delegate) {
        synchronized (lock) {
            this.dialogShowMode = mode;
            this.delegate = delegate;
        }
        return this;
    }

    /**
     * Actually shows the dialog with the buttons provided
     * @param b1 one of the button types
     * @param b2 one of the button types
     */
    public void showDialogWithButtons(MenuButtonType b1, MenuButtonType b2) {
        synchronized (lock) {
            this.button1 = b1;
            this.button2 = b2;
            this.mode = DialogMode.SHOW;
        }
        dialogDidChange();
    }

    /**
     * Remove the dialog from display
     */
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

    /**
     * @return the mode in which the dialog is being shown
     */
    public DialogShowMode getDialogShowMode() {
        synchronized (lock) {
            return this.dialogShowMode;
        }
    }

    /**
     * the button type for a given button number - 0 or 1
     * @param btnNum the button number
     * @return the button type
     */
    public MenuButtonType getButtonType(int btnNum) {
        return btnNum == 1 ? button1 : button2;
    }

    /**
     * this should be overridden to update the UI, it signifies that the dialog has changed
     */
    protected abstract void dialogDidChange();

    /**
     * This can be overridden if needed, it will be called whenever a button is pressed.
     * @param btn the button type
     */
    protected void buttonWasPressed(MenuButtonType btn) {
        var proceed = (delegate != null) ? delegate.apply(btn) : true;
        if(proceed) {
            hideDialog();
        }
    }
}
