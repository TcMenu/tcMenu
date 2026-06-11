import {ActionMenuItem, CustomBuilderMenuItem, CustomMenuType, SubMenuItem} from "./MenuItem";
import {MenuTree} from "./MenuTree";
import {JsonMenuItemSerializer} from "./JsonMenuItemSerializer";
import {ACTION_PERSIST_TYPE, CUSTOM_ITEM_PERSIST_TYPE, projectToPersistedJson} from "./PersistedMenu";
import {CodeGeneratorOptions, MenuTreeWithCodeOptions} from "./ProjectStruct";

describe('Action and CustomBuild Items Serialization Test', () => {
    test('should serialize ActionMenuItem and CustomBuilderMenuItem', () => {
        const tree = new MenuTree(() => {});
        const actionItem = new ActionMenuItem("100");
        actionItem.setItemName("TestAction");
        
        const customItem = new CustomBuilderMenuItem("101");
        customItem.setItemName("TestCustom");
        customItem.setMenuType(CustomMenuType.REMOTE_IOT_MONITOR);

        const subItem = new SubMenuItem("Sub", "102");
        const actionUnderSub = new ActionMenuItem("103");
        actionUnderSub.setItemName("ActionUnderSub");
        subItem.addChildItem(actionUnderSub);

        tree.addMenuItem("0", actionItem);
        tree.addMenuItem("0", customItem);
        tree.addMenuItem("0", subItem);
        tree.addMenuItem("102", actionUnderSub);

        const project: any = {
            menuTree: tree,
            options: {
                applicationName: "TestApp",
                lastProperties: []
            }
        };

        const persistedPrj = projectToPersistedJson(project);
        const items = persistedPrj.items;
        expect(items.length).toBe(5); // Root, Action, Custom, Sub, ActionUnderSub. Wait, Root is not in items.
        // populateListInOrder doesn't include ROOT itself if processingRoot is false.
        // It includes the node if processingRoot is true.
        // projectToPersistedJson calls it with (root, tree, false).
        // So it should include children of root.
        // Action (100), Custom (101), Sub (102), ActionUnderSub (103). Total 4?
        // Wait, if item is SubMenuItem, it recurses.
        
        console.log("Item IDs in output:", items.map((i: any) => i.item.id));

        const serializedAction = items.find((i: any) => i.item.id === 100);
        expect(serializedAction).toBeDefined();
        expect(serializedAction.type).toBe(ACTION_PERSIST_TYPE);
        expect(serializedAction.item.name).toBe("TestAction");

        const serializedCustom = items.find((i: any) => i.item.id === 101);
        expect(serializedCustom).toBeDefined();
        expect(serializedCustom.type).toBe(CUSTOM_ITEM_PERSIST_TYPE);
        expect(serializedCustom.item.name).toBe("TestCustom");
        
        // This is what it currently produces, but maybe Java expects something else?
        // Actually, Java GSON will try to map "menuType" string to CustomMenuType enum.
        expect(serializedCustom.item.menuType).toBe("REMOTE_IOT_MONITOR");

        // PROVE Mismatch: ActionMenuItem is missing fields that Java expects
        // Java MenuItem fields: name, variableName, id, eepromAddress, functionName, readOnly, localOnly, visible, staticDataInRAM
        // TS currently produces for ActionMenuItem: name, id, eepromAddress, readOnly, localOnly, visible, staticDataInRAM
        // MISSING in TS: variableName, functionName
        expect(serializedAction.item.variableName).toBe("");
        expect(serializedAction.item.functionName).toBe("");

        expect(serializedCustom.item.variableName).toBe("");
        expect(serializedCustom.item.functionName).toBe("");

        const subItemSerialized = items.find((i: any) => i.item.id === 102);
        expect(subItemSerialized.item.variableName).toBe("");
        expect(subItemSerialized.item.functionName).toBe("");
    });

    test('should ensure variableName and functionName are present even if null in MenuItem', () => {
        const tree = new MenuTree(() => {});
        const actionItem = new ActionMenuItem("200");
        // Manually set them to something falsy but not empty string if possible, 
        // though the setter/internal state might already default them.
        (actionItem as any).variableName = null;
        (actionItem as any).functionName = undefined;

        tree.addMenuItem("0", actionItem);

        const project: any = {
            menuTree: tree,
            options: {
                applicationName: "TestApp",
                lastProperties: []
            }
        };

        const persistedPrj = projectToPersistedJson(project);
        const item = persistedPrj.items[0].item;
        
        expect(item.variableName).toBe("");
        expect(item.functionName).toBe("");
    });
});
