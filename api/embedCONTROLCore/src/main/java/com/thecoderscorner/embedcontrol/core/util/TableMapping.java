package com.thecoderscorner.embedcontrol.core.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/// Within TcMenu framework is a very basic table mapping system. It is extremely lightweight and performance is not
/// considered critical, its main purpose is for reading configuration and other settings.
/// This describes a table mapping and should be applied to a value class.
@Retention(RetentionPolicy.RUNTIME)
public @interface TableMapping {
    String tableName();
    String uniqueKeyField();
}
