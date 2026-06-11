package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.domain.MenuItem;

/**
 * Indicates that there has been a structural change in the list, for example addition or removal of a menu item
 * in the tree. You normally subscribe to this from MenuManagerServer
 * @see MenuManagerServer
 */
@FunctionalInterface
public interface MenuTreeStructureChangeListener {
    /**
     * The tree has structurally changed.
     * @param parentHint a hint as to where the change occurred.
     */
    void treeStructureChanged(MenuItem parentHint);
}
