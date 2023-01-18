/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.stateForMenuItem;

/**
 * Menu tree holds all the menu items for a specific remote connection or local session. It holds a hierarchy of
 * items, where items of type submenu can hold other items. As menu items are immutable, the state for each item is
 * held separately, and can be accessed from here for each item. There are many helper methods on `MenuItemHelper`
 * that make working with menu items easier.
 * @see MenuItemHelper
 */
public class MenuTree {
    /**
     * Some operations support moving items up or down in the tree, when they do they use this enumeration to
     * describe the direction of the move.
     */
    public enum MoveType { MOVE_UP, MOVE_DOWN }

    /**
     * This is the root menu item, the top level item on the display basically
     */
    public static final SubMenuItem ROOT = new SubMenuItem("Root", null, 0, -1, false, true, false, false);

    /**
     * The maximum expected items in a typical menu.
     */
    private static final int EXPECTED_MAX_VALUES = 256;

    /**
     * This map holds the state for each item, it's the only semi immutable part of the library, even though
     * the actual state objects are immutable, and are replaced on change.
     */
    private final Map<Integer, AnyMenuState> menuStates = new ConcurrentHashMap<>(EXPECTED_MAX_VALUES);

    /**
     * Submenus are organised as a sub menu containing a list of items. There is a lock around this object to
     * it is thread safe.
     */
    private final Map<MenuItem, ArrayList<MenuItem>> subMenuItems = new HashMap<>(EXPECTED_MAX_VALUES / 8);

    /**
     * Create a basic tree that is initially empty
     */
    public MenuTree() {
        subMenuItems.put(ROOT, new ArrayList<>());
    }

    /**
     * add a new menu item to a sub menu, for the top level menu use ROOT.
     * @param parent the submenu where this should appear
     * @param item the item to be added
     */
    public void addMenuItem(SubMenuItem parent, MenuItem item) {
        SubMenuItem subMenu = (parent != null) ? parent : ROOT;

        synchronized (subMenuItems) {
            ArrayList<MenuItem> subMenuChildren = subMenuItems.computeIfAbsent(subMenu, sm -> new ArrayList<>());
            subMenuChildren.add(item);

            if (item.hasChildren()) {
                subMenuItems.put(item, new ArrayList<>());
            }
        }
    }

    /**
     * This will either add or update an existing item, depending on the ID is already present.
     * @param parentId the parent where it should be placed / already exists
     * @param item the item to either add or update.
     */
    public void addOrUpdateItem(int parentId, MenuItem item) {
        synchronized (subMenuItems) {
            getSubMenuById(parentId).ifPresent(subMenu-> {
                if(getMenuItems(subMenu).stream().anyMatch(it-> it.getId() == item.getId())) {
                    replaceMenuById(asSubMenu(subMenu), item);
                }
                else {
                    addMenuItem(asSubMenu(subMenu), item);
                }
            });
        }
    }

    /**
     * gets a submenu by its ID. Returns an optional that will be empty when not present
     * @param parentId the parent to obtain
     * @return an optional that will be populated when present with the sub menu.
     */
    public Optional<SubMenuItem> getSubMenuById(int parentId) {
        return  getAllSubMenus().stream().filter(subMenu->subMenu.getId() == parentId).map(m->(SubMenuItem)m).findFirst();
    }

    /**
     * Gets the menu item with the specified ID, finding the submenu if needed. In most cases the linkage between
     * ID and item will be cached and therefore fast, if you don't know the sub menu set it to null, and it will be
     * determined.
     * @param id the id of the object to find.
     * @return the menu at the given id
     */
    public Optional<MenuItem> getMenuById(int id) {
        var state = menuStates.get(id);
        if(state != null) {
            return Optional.of(state.getItem());
        }

        // shortcut to find the submenu by ID if possible before going through everything.
        var maybeSubMenuId = getAllSubMenus().stream().filter(item -> item.getId() == id).findFirst();
        if(maybeSubMenuId.isPresent()) return maybeSubMenuId;

        return getAllMenuItems().stream().filter(item -> item.getId() == id).findFirst();
    }

    /**
     * Replace a menu item with the given ID. Helper to the version of the function that also needs a parent.
     * This is an infrequent operation and not optimised.
     *
     * @param toReplace the item to replace, by ID
     */
    public void replaceMenuById(MenuItem toReplace) {
        synchronized (subMenuItems) {
            replaceMenuById(findParent(toReplace), toReplace);
        }
    }

    /**
     * Replace the menu item that has a given parent with the one provided. This is an infrequent
     * operation and therefore not optimised.
     * @param subMenu the parent
     * @param toReplace the menu item to replace by ID
     */
    public void replaceMenuById(SubMenuItem subMenu, MenuItem toReplace) {
        synchronized (subMenuItems) {
            ArrayList<MenuItem> list = subMenuItems.get(subMenu);
            int idx = -1;
            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i).getId() == toReplace.getId()) {
                    idx = i;
                }
            }

            if (idx != -1) {
                // We found the original, so we now change that index to the new entry
                MenuItem oldItem = list.set(idx, toReplace);

                // Now we update the "state" which also acts like a cache of menu items for lookup
                if(menuStates.containsKey(toReplace.getId())) {
                    var oldState = menuStates.get(toReplace.getId());
                    menuStates.put(toReplace.getId(), stateForMenuItem(oldState, toReplace, oldState.getValue()));
                }

                // lastly if the item was a submenu, we need change the top level submenu list as well.
                if (toReplace.hasChildren()) {
                    ArrayList<MenuItem> items = subMenuItems.remove(oldItem);
                    subMenuItems.put(toReplace, items);
                }
            }
        }
    }

    /**
     * Moves the item either up or down in the list for that submenu
     * @param parent the parent id
     * @param newItem the item to move
     * @param moveType  the direction of the move.
     */
    public void moveItem(SubMenuItem parent, MenuItem newItem, MoveType moveType) {
        synchronized (subMenuItems) {
            ArrayList<MenuItem> items = subMenuItems.get(parent);
            int idx = items.indexOf(newItem);
            if(idx < 0) return;

            items.remove(idx);

            idx = (moveType == MoveType.MOVE_UP)? idx -1 : idx + 1;
            if(idx<0) idx=0;

            if(idx>=items.size()) {
                items.add(newItem);
            }
            else {
                items.add(idx, newItem);
            }
        }
    }

    /**
     * Remove the menu item using this menu item as a prototype (Uses the ID for comparison)
     * @param toRemove the item to remove.
     */
    public void removeMenuItem(MenuItem toRemove) {
        synchronized (subMenuItems) {
            removeMenuItem(findParent(toRemove), toRemove);
        }
    }

    /**
     * Finds the submenu that the provided object belongs to.
     * @param toFind the object to find sub menu for.
     * @return the submenu
     */
    public SubMenuItem findParent(MenuItem toFind) {
        synchronized (subMenuItems) {
            SubMenuItem parent = MenuTree.ROOT;
            for (Map.Entry<MenuItem, ArrayList<MenuItem>> entry : subMenuItems.entrySet()) {
                for (MenuItem item : entry.getValue()) {
                    if (item.getId() == toFind.getId()) {
                        parent = asSubMenu(entry.getKey());
                    }
                }
            }
            return parent;
        }
    }

    /**
     * Remove the menu item for the provided menu item in the provided sub menu.
     * @param parent the submenu to search
     * @param item the item to remove (Search By ID)
     */
    public void removeMenuItem(SubMenuItem parent, MenuItem item) {
        SubMenuItem subMenu = (parent != null) ? parent : ROOT;

        synchronized (subMenuItems) {
            ArrayList<MenuItem> subMenuChildren = subMenuItems.get(subMenu);
            if (subMenuChildren == null) {
                throw new UnsupportedOperationException("Menu element not found");
            }

            for(int i=0; i<subMenuChildren.size(); i++) {
                if(subMenuChildren.get(i).getId() == item.getId()) {
                    subMenuChildren.remove(i);
                    break;
                }
            }
            if (item.hasChildren()) {
                subMenuItems.remove(item);
            }
        }
        menuStates.remove(item.getId());
    }

    /**
     * Returns all the submenus that are currently stored
     * @return all available sub menus
     */
    public Set<MenuItem> getAllSubMenus() {
        synchronized (subMenuItems) {
            return subMenuItems.keySet();
        }
    }

    /**
     * Get a list of all menu items for a given submenu
     * @param item the submenu to use
     * @return a list of submenu items that's immutable
     */
    public List<MenuItem> getMenuItems(MenuItem item) {
        synchronized (subMenuItems) {
            ArrayList<MenuItem> menuItems = subMenuItems.get(item);
            return menuItems == null ? null : Collections.unmodifiableList(menuItems);
        }
    }

    /**
     * Gets every menu item held in this menu tree, will be unique
     * @return every menu item in the tree.
     */
    public Collection<MenuItem> getAllMenuItems() {
        var toReturn = new HashSet<MenuItem>(128);
        var subs = getAllSubMenus();
        for (MenuItem sub : subs) {
            toReturn.add(sub);
            toReturn.addAll(getMenuItems(sub));
        }
        return toReturn;
    }

    /**
     * Gets every menu item held in this menu tree from a given starting point, the starting point is a sub menu,
     * from that submenu, this method will recurse through the rest of the menu structure and provide a complete list.
     * The menu item provided itself will be the first item in the list, the rest will be in exact order as added.
     * Use this method over getAllMenuItems when the order is important, just call with `MenuTree.ROOT` to get all
     * items in the tree.
     * @param item the starting point for traversal.
     * @return every menu item in the tree from the given starting point.
     */
    public Collection<MenuItem> getAllMenuItemsFrom(SubMenuItem item) {
        var toReturn = new ArrayList<MenuItem>(128);
        var subItems = subMenuItems.get(item);
        toReturn.add(item);
        for(var it : subItems) {
            if(it.hasChildren()) {
                toReturn.addAll(getAllMenuItemsFrom(MenuItemHelper.asSubMenu(it)));
            } else {
                toReturn.add(it);
            }
        }
        return toReturn;
    }


    /**
     * Change the value that's associated with a menu item. if you are changing
     * a value, just send a command to the device, it will automatically update
     * the tree.
     *
     * @param item the item to change
     * @param menuState the new state
     */
    public void changeItem(MenuItem item, AnyMenuState menuState) {
        menuStates.put(item.getId(), menuState);
    }

    /**
     * Gets the menu state that's associated with a given menu item. This is the
     * current value for the menu item.
     * @param item the item which the state belongs to
     * @param <T> determined automatically
     * @return the state for the given menu item
     */
    @SuppressWarnings("unchecked")
    public <T extends AnyMenuState> T getMenuState(MenuItem item) {
        return (T)menuStates.get(item.getId());
    }

    /**
     * Recurse the whole menu tree calling the consumer for each item in turn. This will always be in order so that
     * a child item never comes before its parent.
     * @param root the starting point, normally ROOT
     * @param consumer the consumer that will be called for each item, providing the item and the parent
     */
    public void recurseTreeIteratingOnItems(SubMenuItem root, BiConsumer<MenuItem, SubMenuItem> consumer) {
        var sub = getMenuItems(root);
        for(var child : sub) {
            consumer.accept(child, root);
            if(child instanceof SubMenuItem) {
                recurseTreeIteratingOnItems((SubMenuItem) child, consumer);
            }
        }
    }

    /**
     * Initialise the state of each menu item to the default value, should be used during initialisation of a local
     * menu application. Will only take effect when there is no state already stored.
     */
    public void initialiseStateForEachItem() {
        recurseTreeIteratingOnItems(ROOT, (menuItem, subMenuItem) -> {
            if(getMenuState(menuItem) == null) {
                var state = stateForMenuItem(menuItem, null, false, false);
                changeItem(menuItem, state);
            }
        });
    }
}
