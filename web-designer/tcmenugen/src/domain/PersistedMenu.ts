import {
    AnalogMenuItem,
    BooleanMenuItem,
    EditableLargeNumberMenuItem, EditableTextMenuItem,
    EnumMenuItem, ListCreationMode,
    ListMenuItem,
    MenuItem, Rgb32MenuItem, ScrollChoiceMenuItem,
    SubMenuItem
} from "./MenuItem";
import {
    CodeGeneratorOptions,
    CreatorProperty,
    EepromSaveMode, MenuInMenuCollection,
    MenuInMenuConnectionType,
    MenuInMenuReplicationMode,
    MenuTreeWithCodeOptions,
    ProjectSaveLocation,
    RoundTripMode
} from "./ProjectStruct";
import {MenuTree} from "./MenuTree";
import {JsonMenuItemSerializer} from "./JsonMenuItemSerializer";

export const ANALOG_PERSIST_TYPE = "analogItem";
export const ENUM_PERSIST_TYPE = "enumItem";
export const SUB_PERSIST_TYPE = "subMenu";
export const ACTION_PERSIST_TYPE = "actionMenu";
export const RUNTIME_LIST_PERSIST_TYPE = "runtimeList";
export const CUSTOM_ITEM_PERSIST_TYPE = "customBuildItem";
export const BOOLEAN_PERSIST_TYPE = "boolItem";
export const TEXT_PERSIST_TYPE = "textItem";
export const FLOAT_PERSIST_TYPE = "floatItem";
export const RUNTIME_LARGE_NUM_PERSIST_TYPE = "largeNumItem";
export const SCROLL_CHOICE_PERSIST_TYPE = "scrollItem";
export const RGB32_COLOR_PERSIST_TYPE = "rgbItem";
export const TCMENU_COPY_PREFIX = "tcMenuCopy:";

export class PersistedMenu {
    parentId: string;
    readonly type: string;
    readonly item: MenuItem<any>;
    defaultValue?: string;

    constructor(parentId: string, type: string, item: MenuItem<any>, defaultValue?: string) {
        this.parentId = parentId;
        this.type = type;
        this.item = item;
        this.defaultValue = defaultValue;
    }
}

export function getPersistType(item: MenuItem<any>): string {
    // Some types might have different names in different parts of the system
    if (item.messageType === 'Analog') return ANALOG_PERSIST_TYPE;
    if (item.messageType === 'Enum') return ENUM_PERSIST_TYPE;
    if (item.messageType === 'Sub') return SUB_PERSIST_TYPE;
    if (item.messageType === 'Action') return ACTION_PERSIST_TYPE;
    if (item.messageType === 'List') return RUNTIME_LIST_PERSIST_TYPE;
    if (item.messageType === 'Boolean') return BOOLEAN_PERSIST_TYPE;
    if (item.messageType === 'Text') return TEXT_PERSIST_TYPE;
    if (item.messageType === 'Float') return FLOAT_PERSIST_TYPE;
    if (item.messageType === 'LargeNum') return RUNTIME_LARGE_NUM_PERSIST_TYPE;
    if (item.messageType === 'Scroll') return SCROLL_CHOICE_PERSIST_TYPE;
    if (item.messageType === 'Rgb32') return RGB32_COLOR_PERSIST_TYPE;
    if (item.messageType === 'CustomBuilder') return CUSTOM_ITEM_PERSIST_TYPE;

    return "unknown";
}

export interface PersistableStringList {
    id: string;
    listItems: string[];
}


export class PersistedProject {
    version: string;
    projectName: string;
    author: string;
    items: PersistedMenu[];
    codeOptions: CodeGeneratorOptions;
    stringLists: PersistableStringList[];

    constructor(version: string, projectName: string, author: string, items: PersistedMenu[],
                codeOptions: CodeGeneratorOptions, stringLists: PersistableStringList[]) {
        this.version = version;
        this.projectName = projectName;
        this.author = author;
        this.items = items;
        this.codeOptions = codeOptions;
        this.stringLists = stringLists;
    }
}

export function checkItemValueCanPersist(item: MenuItem<any>): boolean {
    return item instanceof AnalogMenuItem ||
        item instanceof EnumMenuItem ||
        item instanceof BooleanMenuItem ||
        item instanceof EditableLargeNumberMenuItem ||
        item instanceof Rgb32MenuItem ||
        item instanceof EditableTextMenuItem ||
        item instanceof ScrollChoiceMenuItem ;
}

function getProjectFromJson(json: any): PersistedProject {
    let serializer = new JsonMenuItemSerializer();
    let convertedItems: PersistedMenu[] = [];
    for(let item of json.items) {
        let menuItem = serializer.deserializeItem(item.type, item.item);
        if(menuItem) {
            convertedItems.push(new PersistedMenu(item.parentId.toString(), item.type, menuItem, item.defaultValue));
        }
    }

    const codeOptions = json.codeOptions as CodeGeneratorOptions;
    if (codeOptions) {
        if (typeof codeOptions.saveLocation === 'string') {
            codeOptions.saveLocation = ProjectSaveLocation[codeOptions.saveLocation as keyof typeof ProjectSaveLocation];
        }
        if(codeOptions.eepromSaveMode === undefined && codeOptions.usingSizedEEPROMStorage !== undefined) {
            codeOptions.eepromSaveMode = codeOptions.usingSizedEEPROMStorage ? EepromSaveMode.WRITE_BY_POSITION_WITH_SIZE : EepromSaveMode.LEGACY_WRITE_BY_POSITION;
        } else if (typeof codeOptions.eepromSaveMode === 'string') {
            codeOptions.eepromSaveMode = EepromSaveMode[codeOptions.eepromSaveMode as keyof typeof EepromSaveMode];
        }
        if (codeOptions.menuInMenuCollection && codeOptions.menuInMenuCollection.menuDefinitions) {
            for (let def of codeOptions.menuInMenuCollection.menuDefinitions) {
                if (typeof def.connectionType === 'string') {
                    def.connectionType = MenuInMenuConnectionType[def.connectionType as keyof typeof MenuInMenuConnectionType];
                }
                if (typeof def.replicationMode === 'string') {
                    def.replicationMode = MenuInMenuReplicationMode[def.replicationMode as keyof typeof MenuInMenuReplicationMode];
                }
            }
        }
    }

    return new PersistedProject(
        json.version, json.projectName, json.author, convertedItems,
        codeOptions, json.stringLists
    );
}

function applyCurrentValue(item: PersistedMenu, prj: PersistedProject) {
    if (item.item instanceof EnumMenuItem && item.defaultValue) {
        const idx = item.item.getItemList().indexOf(item.defaultValue);
        item.item.setCurrentValue(idx !== -1 ? idx : 0);
    } else {
        item.item.setCurrentValue(item.defaultValue);
    }

    if (item.item instanceof ListMenuItem && item.item.getListCreationMode() === ListCreationMode.FLASH_ARRAY && prj.stringLists) {
        const items = prj.stringLists
            .filter(psl => psl.id === item.item.getMenuId() && psl.listItems != null)
            .map(psl => psl.listItems)
            .at(0);
        if (items) {
            item.item.setCurrentValue(items);
        }
    }
}

export function parseEmfJsonToProject(jsonData: string, mode?: RoundTripMode) : MenuTreeWithCodeOptions {
    const json = JSON.parse(jsonData);
    const prj = getProjectFromJson(json);

    let tree = new MenuTree(menuTree => {});

    for(let item of prj.items) {
        tree.addMenuItem(item.parentId, item.item);
        if (item.defaultValue && checkItemValueCanPersist(item.item)) {
            applyCurrentValue(item, prj);
        }
    }
    return {
        menuTree: tree,
        description: prj.projectName,
        options: prj.codeOptions,
        roundTripMode: mode ?? RoundTripMode.EMF_FILE_DRAGGED_IN
    };

}

function copyOptionsToJsonSafe(options: CodeGeneratorOptions) {
    let optObj: any = {};
    Object.assign(optObj, options);
    if (options.saveLocation !== undefined) {
        optObj.saveLocation = ProjectSaveLocation[options.saveLocation];
    }
    if (options.eepromSaveMode !== undefined) {
        optObj.eepromSaveMode = EepromSaveMode[options.eepromSaveMode];
    }
    if (options.menuInMenuCollection && options.menuInMenuCollection.menuDefinitions) {
        optObj.menuInMenuCollection = {
            menuDefinitions: options.menuInMenuCollection.menuDefinitions.map(def => {
                let defObj: any = {};
                Object.assign(defObj, def);
                defObj.connectionType = MenuInMenuConnectionType[def.connectionType];
                defObj.replicationMode = MenuInMenuReplicationMode[def.replicationMode];
                return defObj;
            })
        };
    } else {
        optObj.menuInMenuCollection = {menuDefinitions: []} as MenuInMenuCollection;
    }
    return optObj;
}

function stringListsFromTree(allItems: MenuItem<any>[]) {
    const allLists = allItems.filter(item => item instanceof ListMenuItem && item.getListCreationMode() !== ListCreationMode.CUSTOM_RTCALL);
    return allLists.map(list => ({id: list.getMenuId(), listItems: list.getCurrentValue()}));
}

export function projectToPersistedJson(project: MenuTreeWithCodeOptions): any {
    const serializer = new JsonMenuItemSerializer();
    const items = serializer.populateListInOrder(project.menuTree.getRoot(), project.menuTree, false);
    return new PersistedProject(
        "1.00", // version
        project.description,
        "WebApp", // author
        items,
        copyOptionsToJsonSafe(project.options),
        stringListsFromTree(project.menuTree.getAllItems())
    );
}