/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

/**
 * Describes which subsystem a particular plugin or property belongs to. DISPLAY shows components that
 * are controlling a display. INPUT components that gather input from user. REMOTE, anything that is
 * gathering information from users remotely (ie interfacing off the device).
 */
public enum SubSystem {
    DISPLAY,
    INPUT,
    REMOTE,
    THEME
}
