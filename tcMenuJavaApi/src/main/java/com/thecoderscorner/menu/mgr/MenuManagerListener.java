package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.domain.MenuItem;

/**
 * MenuManager listeners get notification when any menu item has changed, this works by the `menuItemHasChanged` method
 * being called for each change. Further to this, you can also mark any method in your class with `@MenuCallback(id=n)`
 * where n is the menu ID of the item you are interested in, then this callback method will be called in addition to the
 * global menu callback. If you use designer UI, it will create such a method for each callback function.
 *
 * List selection notifications have a special form `@MenuCallback(id=n, listResult=true)` that takes an extra parameter
 * for the list response.
 *
 * ScrollChoiceMenuItem value retrievers can also be added {@link ScrollChoiceValueRetriever}, these will be called
 * whenever a value is needed for a scroll choice item,
 *
 * @see MenuCallback
 * @see ScrollChoiceValueRetriever
 */
public interface MenuManagerListener {
    /**
     * Called whenever there is a change in any menu item, it will indicate if the change is local or remote.
     * @param item the item ID
     * @param remoteChange if the change is local or remote.
     */
    void menuItemHasChanged(MenuItem item, boolean remoteChange);

    /**
     * Indicates that the manager (and therefore the application) is about to start. You can do any tasks that are
     * needed at start up here. For example loading state back from storage
     */
    void managerWillStart();

    void managerWillStop();
}
