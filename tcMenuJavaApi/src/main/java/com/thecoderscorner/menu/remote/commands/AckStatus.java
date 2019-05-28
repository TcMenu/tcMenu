/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

/**
 * An enumeration that represents all the possible status / error return codes from the remote.
 */
public enum AckStatus {
    /** The credentials provided were incorrect */
    VALUE_RANGE_WARNING("Value out of range", -1),
    SUCCESS("OK", 0),
    ID_NOT_FOUND("ID not found", 1),
    INVALID_CREDENTIALS("Invalid Credentials", 2),
    UNKNOWN_ERROR("Unknown Error", 10000);

    private String description;
    private int statusCode;

    AckStatus(String description, int statusCode) {

        this.description = description;
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isError() {
        return statusCode > 0;
    }
}
