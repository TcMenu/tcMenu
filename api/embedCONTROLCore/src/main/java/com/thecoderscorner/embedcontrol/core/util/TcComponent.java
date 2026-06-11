package com.thecoderscorner.embedcontrol.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods within a MenuConfig annotated with this method will be called during construction and the returned result
 * will be added to componentMap so as to be available with its getBean method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TcComponent {
}
