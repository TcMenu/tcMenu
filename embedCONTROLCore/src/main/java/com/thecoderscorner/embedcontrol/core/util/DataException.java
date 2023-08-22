package com.thecoderscorner.embedcontrol.core.util;

public class DataException extends Exception {
    public DataException(String queryObject, Exception ex) {
        super(queryObject, ex);
    }

    public DataException(String queryObject) {
        super(queryObject);
    }
}
