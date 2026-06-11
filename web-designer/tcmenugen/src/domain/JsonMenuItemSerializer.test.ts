
import {MenuTree} from './MenuTree';
import {SubMenuItem, AnalogMenuItem} from './MenuItem';
import {JsonMenuItemSerializer} from './JsonMenuItemSerializer';
import {TCMENU_COPY_PREFIX} from './PersistedMenu';

describe('JsonMenuItemSerializer - Regression Tests', () => {
    let tree: MenuTree;
    let serializer: JsonMenuItemSerializer;

    beforeEach(() => {
        tree = new MenuTree(() => {});
        serializer = new JsonMenuItemSerializer();
    });

    test('alterAnyDuplicateIds should update parentId for non-submenu items', () => {
        // Setup: A tree with an item having ID "1"
        const existingItem = new AnalogMenuItem("1");
        tree.addMenuItem("0", existingItem);

        // A template where a SubMenu has ID "1" and a child has parentId "1"
        // When we add this, SubMenu ID "1" will be changed to "2"
        // We MUST ensure the child's parentId is also updated to "2"
        const template = TCMENU_COPY_PREFIX + JSON.stringify([
            {
                parentId: "0",
                type: "subMenu",
                item: { name: "NewSub", id: 1 }
            },
            {
                parentId: "1",
                type: "analogItem",
                item: { name: "Child", id: 3 }
            }
        ]);

        // This should NOT throw "Non submenu parent 1 for 3"
        // because "1" should have been changed to "2" in both the SubMenu and the child's parentId
        expect(() => {
            serializer.putItemsIntoMenuTree(template, tree, "0");
        }).not.toThrow();

        expect(tree.hasId("2")).toBe(true);
        expect(tree.hasId("3")).toBe(true);
        
        const child = tree.getMenuItemFor("3");
        const parent = tree.findParent(child);
        expect(parent.getMenuId()).toBe("2");
    });

    test('alterAnyDuplicateIds should update parentId when a non-submenu item is renamed', () => {
        // This is a rare case but good to test. If a regular item had children (though they shouldn't)
        // or if something else referenced its ID as a parentId.
        
        const existingItem = new AnalogMenuItem("1");
        tree.addMenuItem("0", existingItem);

        // Template: Item 1 is an AnalogItem, but something else (wrongly) claims it as parent
        // Even if it's wrong in the template, if we rename 1 to 2, we should rename parentId 1 to 2.
        const template = TCMENU_COPY_PREFIX + JSON.stringify([
            {
                parentId: "0",
                type: "analogItem",
                item: { name: "Analog1", id: 1 }
            },
            {
                parentId: "1",
                type: "analogItem",
                item: { name: "ChildOfAnalog", id: 3 }
            }
        ]);

        // It will still throw when trying to ADD to tree if it's not a SubMenu, 
        // but we want to check that alterAnyDuplicateIds did its job.
        let items = serializer.copyTextToItems(template);
        items = serializer.alterAnyDuplicateIds(items, tree);

        expect(items[0].item.getMenuId()).toBe("2");
        expect(items[1].parentId).toBe("2");
    });
});
