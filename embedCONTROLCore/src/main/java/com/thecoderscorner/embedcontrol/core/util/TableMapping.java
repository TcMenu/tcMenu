package com.thecoderscorner.embedcontrol.core.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TableMapping {
    String tableName();
    String uniqueKeyField();
}
