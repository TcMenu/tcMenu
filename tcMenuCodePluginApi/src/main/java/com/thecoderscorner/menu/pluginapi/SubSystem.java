package com.thecoderscorner.menu.pluginapi;

/**
 * Describes which subsystem a particular plugin or property belongs to. DISPLAY shows components that
 * are controlling a display. INPUT components that gather input from user. REMOTE, anything that is
 * gathering information from users remotely (ie interfacing off the device).
 */
public enum SubSystem {
    DISPLAY,
    INPUT,
    REMOTE
}
