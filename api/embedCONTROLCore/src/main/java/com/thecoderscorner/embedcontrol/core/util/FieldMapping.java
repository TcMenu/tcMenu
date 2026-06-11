package com.thecoderscorner.embedcontrol.core.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMapping {
    String fieldName();
    boolean primaryKey() default false;
    FieldType fieldType() default FieldType.VARCHAR;
}
