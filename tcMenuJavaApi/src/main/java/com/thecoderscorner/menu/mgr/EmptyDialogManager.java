package com.thecoderscorner.menu.mgr;

/**
 * A no-operation implementation of menu item that meets the interface but does nothing.
 */
public class EmptyDialogManager extends DialogManager {
    @Override
    protected void dialogDidChange() {
    }
}
