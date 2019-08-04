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
    /** This is a warning that the value was out of range */
    VALUE_RANGE_WARNING("Value out of range", -1),
    /** The operation was successful */
    SUCCESS("OK", 0),
    /** The requested ID was not found */
    ID_NOT_FOUND("ID not found", 1),
    /** The credentials provided were incorrect */
    INVALID_CREDENTIALS("Invalid Credentials", 2),
    /** There was an error that is not categorised. */
    UNKNOWN_ERROR("Unknown Error", 10000);

    private String description;
    private int statusCode;

    AckStatus(String description, int statusCode) {

        this.description = description;
        this.statusCode = statusCode;
    }

    /**
     * @return description of this status
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the integer wire code for this status.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return true if the status code is an error, otherwise false.
     */
    public boolean isError() {
        return statusCode > 0;
    }
}
