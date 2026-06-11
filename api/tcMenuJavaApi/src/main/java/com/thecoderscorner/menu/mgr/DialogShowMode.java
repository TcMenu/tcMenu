package com.thecoderscorner.menu.mgr;

/**
 * Indicates how the dialog should be shown
 */
public enum DialogShowMode {
    /** Show the dialog in a regular way */
    REGULAR,
    /** The dialog is local to the delegate function provided, no remote messages will send upon action */
    LOCAL_TO_DELEGATE,
    /** The dialog is both local to the delegate and locked so cannot be replaced with another */
    LOCAL_DELEGATE_LOCKED

}
