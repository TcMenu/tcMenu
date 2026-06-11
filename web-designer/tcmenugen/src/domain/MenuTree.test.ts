// MenuTree.test.ts
import {MenuTree} from './MenuTree';
import {MenuItem, SubMenuItem} from './MenuItem';

describe('MenuTree - addMenuItem', () => {
    let treeStructureChangedMock: jest.Mock;
    let menuTree: MenuTree;

    beforeEach(() => {
        treeStructureChangedMock = jest.fn();
        menuTree = new MenuTree(treeStructureChangedMock);
    });

    it('should add a menu item to a valid submenu', () => {
        const subMenu = new SubMenuItem("SubMenu", "1");
        menuTree.addMenuItem("0", subMenu); // Adding a submenu to the root

        const menuItem = new MenuItem<string>("Item");
        menuItem.setMenuId("2");

        const result = menuTree.addMenuItem("1", menuItem);
        expect(result).toBe(true);
        expect(menuTree.getMenuItemFor("2")).toBe(menuItem);
        expect(subMenu.getChildren()).toContain(menuItem);
        expect(treeStructureChangedMock).toHaveBeenCalledWith(menuTree, "2");
    });

    it('should not add the menu item if it already exists', () => {
        const existingItem = new MenuItem<number>(10);
        existingItem.setMenuId("3");
        menuTree.addMenuItem("0", existingItem);

        const duplicateItem = new MenuItem<number>(20);
        duplicateItem.setMenuId("3");

        jest.clearAllMocks();
        const result = menuTree.addMenuItem("0", duplicateItem);
        expect(result).toBe(false);
        expect(treeStructureChangedMock).not.toHaveBeenCalled();
        expect(menuTree.getMenuItemFor("3")).toBe(existingItem);
    });

    it('should throw an error if the parent is not a submenu', () => {
        const standardItem = new MenuItem<boolean>(true);
        standardItem.setMenuId("4");
        menuTree.addMenuItem("0", standardItem);

        const newItem = new MenuItem<string>("New");
        newItem.setMenuId("5");

        expect(() => menuTree.addMenuItem("4", newItem)).toThrow("Non submenu parent 4 for 5");
        expect(menuTree.getMenuItemFor("5")).toBeUndefined();
    });

    it('should return false if trying to add a menu item with no valid submenu parent', () => {
        const menuItem = new MenuItem<string>("LonelyItem");
        menuItem.setMenuId("10");

        expect(() => menuTree.addMenuItem("99", menuItem)).toThrow("Non submenu parent 99 for 10"); // Non-existent parent
        expect(menuTree.getMenuItemFor("10")).toBeUndefined();
        expect(treeStructureChangedMock).not.toHaveBeenCalled();
    });
});