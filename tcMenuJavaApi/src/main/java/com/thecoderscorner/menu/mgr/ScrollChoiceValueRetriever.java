package com.thecoderscorner.menu.mgr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as being responsible for providing the value of a particular value in a scroll choice. Each time
 * a scroll choice item changes, the callback associated with it is called back. In this call-back the menu item
 * and row number are provided. It is then your responsibility to provide the item at that location.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScrollChoiceValueRetriever {
    int id();
}
