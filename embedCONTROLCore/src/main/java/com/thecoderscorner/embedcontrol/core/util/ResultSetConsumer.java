package com.thecoderscorner.embedcontrol.core.util;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetConsumer {
    void processResults(ResultSet rs) throws Exception;
}
