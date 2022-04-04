package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.domain.MenuItem;

@FunctionalInterface
public interface MenuTreeStructureChangeListener {
    void treeStructureChanged(MenuItem parentHint);
}
