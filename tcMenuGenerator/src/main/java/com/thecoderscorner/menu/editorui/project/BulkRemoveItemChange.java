package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;

import java.util.ArrayList;
import java.util.List;

public class BulkRemoveItemChange extends MenuItemChange {
    private List<PersistedMenu> toRedoList = new ArrayList<>();

    public BulkRemoveItemChange(SubMenuItem newItem, SubMenuItem parent) {
        super(newItem, null, parent);
    }

    void recurseStructureRemoving(MenuTree tree, SubMenuItem sub)
    {
        var subMenusToRemove = new ArrayList<SubMenuItem>();
        for(var item : tree.getMenuItems(sub))
        {
            toRedoList.add(new PersistedMenu(sub, item));
            if (item instanceof SubMenuItem)
            {
                recurseStructureRemoving(tree, sub);
                subMenusToRemove.add(sub);
            }
        }

        for(var rm : subMenusToRemove)
        {
            tree.removeMenuItem(rm);
        }
    }


    @Override
    void unApply(MenuTree tree) {
        for(var item : toRedoList)
        {
            var par = tree.getMenuById(item.getParentId());
            if(par.isPresent() && par.get() instanceof SubMenuItem) {
                var sub = (SubMenuItem)par.get();
                tree.addOrUpdateItem(sub.getId(), item.getItem());
            }
        }
    }

    @Override
    void applyTo(MenuTree tree) {
        toRedoList.add(new PersistedMenu(parent, newItem));
        recurseStructureRemoving(tree, (SubMenuItem)newItem);
        tree.removeMenuItem(parent, newItem);
    }
}
