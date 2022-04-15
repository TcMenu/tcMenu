/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

/**
 * The modes that a dialog can be in, and the transmission type for action too
 */
public enum DialogMode {
    /** the dialog is to be shown */
    SHOW,
    /** the dialog is to be hidden */
    HIDE,
    /** perform the following action on the dialog */
    ACTION
}
