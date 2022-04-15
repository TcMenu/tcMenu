package com.thecoderscorner.menu.mgr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a menu callback, when a `MenuManagerListener` is added to a menu manager `MenuManagerServer`
 * you can mark methods with this identifier if the method takes just two parameters, one for the ID and a boolean
 * that indicates if the update is local or remote. The method must take at least 2 parameters, firstly a menu ID
 * and secondly a boolean indicating if the update was local. Optionally, the third parameter is only available in
 * list mode (listResult), it is the action that took place on the list and is momentary, in that it is not stored.
 *
 * <pre>
 * // register this method to receive an item change notification
 * &at;MenuCallback(id=1)
 * public void myItemAction(BooleanMenuItem item) {
 *     // item changed
 * }
 *
 * // register this method to receive list selection changes
 * &at;MenuCallback(id=10, listResult=true)
 * public void listWasSelected(RuntimeListMenuItem item, ListResponse response) {
 *     // list item changed
 *
 * }
 * </pre>
 *
 * @see MenuManagerListener
 * @see com.thecoderscorner.menu.domain.state.ListResponse
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MenuCallback {
    int id();
    boolean listResult() default false;
}
