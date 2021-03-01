package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PastedItemChange extends MenuItemChange {

    private final List<PersistedMenu> allItems;

    public PastedItemChange(List<PersistedMenu> items, SubMenuItem subMenu, MenuTree tree, MenuIdChooser chooser)
    {
        super(null, null, subMenu);
        allItems = items;

        var oldIdToNewId = new HashMap<Integer, Integer>();
        var newId = chooser.nextHighestId();

        // now we do the re-parenting which is only done once at construction
        for (var item : allItems)
        {
            if (tree.getMenuById(item.getItem().getId()).isPresent())
            {
                var newItem = MenuItemHelper.createFromExistingWithId(item.getItem(), newId++);
                oldIdToNewId.put(item.getItem().getId(), newItem.getId());
                item.setItem(newItem);
            }
        }

        // now for every parent mapping in the list of items that's changed, we need to update it.
        for(var item : allItems)
        {
            if(oldIdToNewId.containsKey(item.getParentId()))
            {
                item.setParentId(oldIdToNewId.get(item.getParentId()));
            }
        }

        // lastly we set the ID of the starting point.
        allItems.get(0).setParentId(subMenu.getId());
    }

    @Override
    void unApply(MenuTree tree) {
        var currentlyLeft = new ArrayList<PersistedMenu>(allItems);

        // we have to iterate until all dependencies have been deleted, potentially more than one go.
        var iterations = 0;
        while (currentlyLeft.size() > 0 && ++iterations < 100)
        {
            // find all menu items that are not in any way linked to another (by parent).
            var removalSet = new ArrayList<MenuItem>();
            for(var item : allItems)
            {
                if (currentlyLeft.stream().allMatch(it -> it.getParentId() != item.getItem().getId()))
                {
                    removalSet.add(item.getItem());
                    currentlyLeft.remove(item);
                }
            }

            // delete all that can be removed.
            for(var rm : removalSet)
            {
                tree.removeMenuItem(rm);
            }
        }

    }

    @Override
    void applyTo(MenuTree tree) {
        for(var item : allItems)
        {
            var par = tree.getMenuById(item.getParentId());
            if(par.isPresent() && par.get() instanceof SubMenuItem) {
                tree.addMenuItem((SubMenuItem)par.get(), item.getItem());
            }
        }
    }
}
