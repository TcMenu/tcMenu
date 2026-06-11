import {
    ActionMenuItem,
    AnalogMenuItem,
    BooleanMenuItem, BooleanNaming, CustomBuilderMenuItem, CustomMenuType,
    EditableLargeNumberMenuItem,
    EditableTextMenuItem,
    EnumMenuItem,
    FloatMenuItem, ListCreationMode,
    ListMenuItem,
    MenuItem,
    Rgb32MenuItem,
    ScrollChoice,
    ScrollChoiceMenuItem, ScrollChoiceMode,
    SubMenuItem, TextEditMode
} from "./MenuItem";
import {MenuTree} from "./MenuTree";
import {
    ACTION_PERSIST_TYPE,
    ANALOG_PERSIST_TYPE,
    BOOLEAN_PERSIST_TYPE, CUSTOM_ITEM_PERSIST_TYPE,
    ENUM_PERSIST_TYPE,
    FLOAT_PERSIST_TYPE,
    getPersistType,
    PersistedMenu,
    RGB32_COLOR_PERSIST_TYPE,
    RUNTIME_LARGE_NUM_PERSIST_TYPE,
    RUNTIME_LIST_PERSIST_TYPE,
    SCROLL_CHOICE_PERSIST_TYPE,
    SUB_PERSIST_TYPE,
    TCMENU_COPY_PREFIX,
    TEXT_PERSIST_TYPE
} from "./PersistedMenu";

export class JsonMenuItemSerializer {

    private getPersistableValue(item: MenuItem<any>): string | undefined {
        if (item instanceof EnumMenuItem) {
            const listLen = item.getItemList().length;
            const current = item.getCurrentValue();
            return (current >= 0 && current < listLen) ? current.toString() : "0";
        } else if (item instanceof ListMenuItem) {
            return "";
        } else {
            return item.getCurrentValue()?.toString();
        }
    }

    public populateListInOrder(node: SubMenuItem, menuTree: MenuTree, processingRoot: boolean): Array<PersistedMenu> {
        let list: Array<PersistedMenu> = [];
        let items = node.getChildren();

        if (node.getMenuId() !== "0" && processingRoot) {
            let parentId = "0"; 
            let persistedSub = new PersistedMenu(parentId, SUB_PERSIST_TYPE, this.serializeItem(node), "false");
            list.push(persistedSub);
        }

        for (let item of items) {
            let persistedMenu = new PersistedMenu(node.getMenuId(), getPersistType(item), this.serializeItem(item), this.getPersistableValue(item));
            list.push(persistedMenu);

            if (item instanceof SubMenuItem) {
                list = list.concat(this.populateListInOrder(item, menuTree, false));
            }
        }
        return list;
    }

    public serializeItem(item: MenuItem<any>): any {
        let itemObj: any = {};
        // Use a whitelist of common fields to avoid internal state leaking
        itemObj.name = item.getItemName();
        itemObj.id = parseInt(item.getMenuId());
        itemObj.eepromAddress = item.getEEPROMLocation();
        itemObj.readOnly = item.isReadOnly();
        itemObj.localOnly = item.isLocalOnly();
        itemObj.visible = item.isVisible();
        itemObj.staticDataInRAM = item.isStaticDataInRAM();

        itemObj.functionName = item.getCallbackFnName() || "";
        itemObj.variableName = item.getVariableName() || "";

        if (item instanceof AnalogMenuItem) {
            itemObj.maxValue = item.getMaxValue();
            itemObj.offset = item.getOffset();
            itemObj.divisor = item.getDivisor();
            itemObj.unitName = item.getUnitName();
            itemObj.step = item.getStep();
        } else if (item instanceof EnumMenuItem) {
            itemObj.enumEntries = item.getItemList();
        } else if (item instanceof BooleanMenuItem) {
            itemObj.naming = BooleanNaming[item.getNaming()];
        } else if (item instanceof EditableTextMenuItem) {
            itemObj.textLength = item.getTextLength();
            itemObj.itemType = TextEditMode[item.getEditMode()];
        } else if (item instanceof FloatMenuItem) {
            itemObj.numDecimalPlaces = item.getDecimalPlaces();
        } else if (item instanceof SubMenuItem) {
            itemObj.secured = item.isSecuredMenu();
        } else if (item instanceof EditableLargeNumberMenuItem) {
            itemObj.digitsAllowed = item.getDigitsAllowed();
            itemObj.decimalPlaces = item.getDecimalPlaces();
            itemObj.negativeAllowed = item.isNegativeAllowed();
        } else if (item instanceof ScrollChoiceMenuItem) {
            itemObj.itemWidth = item.getFixedItemWidth();
            itemObj.eepromOffset = item.getEepromOffset();
            itemObj.numEntries = item.getNumberOfEntries();
            itemObj.choiceMode = ScrollChoiceMode[item.getChoiceMode()];
            if (item.getVariable()) itemObj.variable = item.getVariable();
            if (item.getEepromOffset() !== 0) itemObj.eepromOffset = item.getEepromOffset();
        } else if (item instanceof Rgb32MenuItem) {
            itemObj.includeAlphaChannel = item.isAlphaChannelOn();
        } else if (item instanceof ListMenuItem) {
            itemObj.initialRows = item.getNumberOfItems();
            itemObj.listCreationMode = ListCreationMode[item.getListCreationMode()];
        } else if (item instanceof CustomBuilderMenuItem) {
            itemObj.menuType = CustomMenuType[item.getMenuType()];
        } else if (item instanceof ActionMenuItem) {
            // Action items only have base fields, which are already handled
        }

        return itemObj;
    }

    public itemsToCopyText(startingPoint: MenuItem<any>, tree: MenuTree): string {
        let items: Array<PersistedMenu>;
        if (startingPoint instanceof SubMenuItem) {
            items = this.populateListInOrder(startingPoint, tree, true);
        } else {
            items = [];
            // Finding parent in MenuTree is not direct in TS version, might need to improve MenuTree.
            // For now, let's assume it's under root if we don't know better.
            items.push(new PersistedMenu("0", getPersistType(startingPoint), this.serializeItem(startingPoint), this.getPersistableValue(startingPoint)));
        }
        return TCMENU_COPY_PREFIX + JSON.stringify(items, (key, value) => {
            return value;
        }, 2);
    }

    public copyTextToItems(itemsStr: string): Array<PersistedMenu> {
        if (!itemsStr.startsWith(TCMENU_COPY_PREFIX)) return [];
        let jsonStr = itemsStr.substring(TCMENU_COPY_PREFIX.length);
        let rawList = JSON.parse(jsonStr);
        let result: Array<PersistedMenu> = [];

        for (let obj of rawList) {
            let item = this.deserializeItem(obj.type, obj.item);
            if (item) {
                result.push(new PersistedMenu(obj.parentId.toString(), obj.type, item, obj.defaultValue));
            }
        }
        return result;
    }

    public deserializeItem(type: string, itemObj: any): MenuItem<any> | null {
        let item: MenuItem<any>;
        const menuId = (itemObj.menuId !== undefined ? itemObj.menuId : itemObj.id).toString();
        itemObj.eepromLocation = itemObj.eepromAddress ?? - 1;
        switch (type) {
            case ANALOG_PERSIST_TYPE:
                item = new AnalogMenuItem(menuId);
                Object.assign(item, itemObj);
                break;
            case ENUM_PERSIST_TYPE:
                item = new EnumMenuItem(menuId);
                Object.assign(item, itemObj);
                if (itemObj.enumEntries) {
                    (item as EnumMenuItem).setItemList(itemObj.enumEntries);
                }
                break;
            case SUB_PERSIST_TYPE:
                item = new SubMenuItem(itemObj.name || itemObj.itemName, menuId);
                Object.assign(item, itemObj);
                if (itemObj.secured !== undefined) {
                    (item as SubMenuItem).setSecuredMenu(itemObj.secured);
                }
                (item as SubMenuItem).clearAll(); // Children are added separately in the list
                break;
            case BOOLEAN_PERSIST_TYPE:
                item = new BooleanMenuItem(menuId);
                Object.assign(item, itemObj);
                if(itemObj.naming !== undefined) {
                    const naming = BooleanNaming[itemObj.naming as keyof typeof BooleanNaming];
                    (item as BooleanMenuItem).setNaming(naming);
                }
                break;
            case TEXT_PERSIST_TYPE:
                item = new EditableTextMenuItem(menuId);
                Object.assign(item, itemObj);
                if(itemObj.itemType) {
                    const ty = TextEditMode[itemObj.itemType as keyof typeof TextEditMode];
                    (item as EditableTextMenuItem).setEditMode(ty);
                }
                break;
            case FLOAT_PERSIST_TYPE:
                item = new FloatMenuItem(menuId);
                Object.assign(item, itemObj);
                break;
            case RUNTIME_LARGE_NUM_PERSIST_TYPE:
                item = new EditableLargeNumberMenuItem(menuId);
                Object.assign(item, itemObj);
                if (itemObj.digitsAllowed !== undefined) {
                    (item as EditableLargeNumberMenuItem).setDigitsAllowed(itemObj.digitsAllowed);
                }
                if (itemObj.decimalPlaces !== undefined) {
                    (item as EditableLargeNumberMenuItem).setDecimalPlaces(itemObj.decimalPlaces);
                }
                if (itemObj.negativeAllowed !== undefined) {
                    (item as EditableLargeNumberMenuItem).setNegativeAllowed(itemObj.negativeAllowed);
                }
                break;
            case SCROLL_CHOICE_PERSIST_TYPE:
                item = new ScrollChoiceMenuItem(menuId);
                Object.assign(item, itemObj);
                (item as ScrollChoiceMenuItem).setNumberOfEntries(itemObj.numEntries ?? 0);
                (item as ScrollChoiceMenuItem).setFixedItemWidth(itemObj.itemWidth ?? 0);
                if (itemObj.currentValue) {
                    item.setCurrentValue(new ScrollChoice(itemObj.currentValue.currentPos, itemObj.currentValue.currentValue));
                }
                if(itemObj.choiceMode) {
                    const mode = ScrollChoiceMode[itemObj.choiceMode as keyof typeof ScrollChoiceMode];
                    (item as ScrollChoiceMenuItem).setChoiceMode(mode);
                }
                break;
            case RGB32_COLOR_PERSIST_TYPE:
                item = new Rgb32MenuItem(menuId);
                Object.assign(item, itemObj);
                if (itemObj.includeAlphaChannel !== undefined) {
                    (item as Rgb32MenuItem).setAlphaChannelOn(itemObj.includeAlphaChannel);
                }
                break;
            case RUNTIME_LIST_PERSIST_TYPE:
                item = new ListMenuItem(menuId);
                Object.assign(item, itemObj);
                if(itemObj.listCreationMode) {
                    const listCreationMode = ListCreationMode[itemObj.listCreationMode as keyof typeof ListCreationMode];
                    (item as ListMenuItem).setListCreationMode(listCreationMode);
                }
                if (itemObj.initialRows !== undefined) {
                    (item as ListMenuItem).setNumberOfItems(itemObj.initialRows);
                }
                break;
            case ACTION_PERSIST_TYPE:
                item = new ActionMenuItem(menuId);
                Object.assign(item, itemObj);
                break;
            case CUSTOM_ITEM_PERSIST_TYPE:
                item = new CustomBuilderMenuItem(menuId);
                Object.assign(item, itemObj);
                if(itemObj.menuType !== undefined) {
                    const customMenuType = CustomMenuType[itemObj.menuType as keyof typeof CustomMenuType];
                    (item as CustomBuilderMenuItem).setMenuType(customMenuType);
                }
                break;
            default:
                return null;
        }
        item.setMenuId(menuId);
        if (itemObj.itemName) item.setItemName(itemObj.itemName);
        else if (itemObj.name) item.setItemName(itemObj.name);

        if (itemObj.staticDataInRAM !== undefined) {
            item.setStaticDataInRAM(itemObj.staticDataInRAM);
        }
        item.clearChanged();
        return item;
    }

    private replaceAllParentIds(originalId: string, newId: string, items: Array<PersistedMenu>) {
        for(let item of items) {
            if(item.parentId === originalId) item.parentId = newId;
        }
    }

    public alterAnyDuplicateIds(items: Array<PersistedMenu>, tree: MenuTree): PersistedMenu[] {
        let usedIds = new Set<string>();
        for(let item of items) {
            let currentId = item.item.getMenuId();
            if(tree.hasId(currentId) || usedIds.has(currentId)) {
                let nextId = tree.nextAvailableId();
                while(usedIds.has(nextId.toString()) || tree.hasId(nextId.toString())) {
                    nextId++;
                }
                let newId = nextId.toString();
                item.item.setMenuId(newId);
                this.replaceAllParentIds(currentId, newId, items);
                usedIds.add(newId);
            } else {
                usedIds.add(currentId);
            }
        }
        return items;
    }

    public putItemsIntoMenuTree(tcMenuCopy: string, tree: MenuTree, parentId: string): MenuTree {
        let items = this.copyTextToItems(tcMenuCopy);
        items = this.alterAnyDuplicateIds(items, tree);
        if(items[0].parentId === "0") items[0].parentId = parentId;
        for (let pItem of items) {
            tree.addMenuItem(pItem.parentId, pItem.item);
            if (pItem.defaultValue !== undefined) {
                pItem.item.setCurrentValue(this.parseValue(pItem.item, pItem.defaultValue));
            }
        }
        return tree;
    }

    private parseValue(item: MenuItem<any>, val: string): any {
        if(item instanceof AnalogMenuItem || item instanceof EnumMenuItem) {
            return parseInt(val);
        } else if (item instanceof FloatMenuItem || item instanceof EditableLargeNumberMenuItem) {
            return parseFloat(val);
        } else if (item instanceof BooleanMenuItem) {
            return val === "true" || val === "1";
        } else if(item instanceof ScrollChoiceMenuItem) {
            return ScrollChoice.fromString(val);
        }
        return val;
    }
}
