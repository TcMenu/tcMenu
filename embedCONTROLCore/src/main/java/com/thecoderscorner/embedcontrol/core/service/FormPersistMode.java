package com.thecoderscorner.embedcontrol.core.service;

/**
 * Indicates what type of form this is, IE is it externally added and then we are just
 * holding it, is it from an embedded device, or is it held within the project and
 * this is just a link
 */
public enum FormPersistMode {
    /** The form is externally managed, and this is just a local copy */
    EXTERNAL_MANAGED,
    /** The form originated from an embedded device, and this is just a local copy */
    FROM_EMBEDDED,
    /** The form is actually within the project, the XML will be empty because it is
     * assumed that the file is within the project in the forms directory. */
    WITHIN_PROJECT
}
