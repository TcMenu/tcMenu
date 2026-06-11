import React, {useState} from 'react';
import {setCurrentlyOpenProject, useCurrentlyOpenProject} from "../App";
import {ProjectWelcome} from "./ProjectWelcome";
import {MenuTreeComponent} from "./MenuTreeComponent";
import {
    ActionMenuItem,
    AnalogMenuItem,
    BooleanMenuItem, CustomBuilderMenuItem,
    EditableLargeNumberMenuItem,
    EditableTextMenuItem,
    EnumMenuItem,
    FloatMenuItem,
    ListMenuItem,
    MenuItem,
    Rgb32MenuItem,
    ScrollChoiceMenuItem,
    SubMenuItem,
    TcMenuItemError,
    nextAvailableEepromLocation
} from "../domain/MenuItem";
import {EepromSaveMode, MenuTreeWithCodeOptions, RoundTripMode} from "../domain/ProjectStruct";
import {RootEditor} from "./RootEditor";
import {JsonMenuItemSerializer} from "../domain/JsonMenuItemSerializer";
import {tcMenuStarers} from "./BulkAddTemplates";
import {AnalogItemEditor, BooleanItemEditor, EnumItemEditor} from "./EditValueItems";
import {
    LargeNumberItemEditor,
    ListItemEditor,
    Rgb32ItemEditor,
    ScrollChoiceItemEditor,
    TextItemEditor
} from "./EditRuntimeItems";
import {CustomBuilderMenuItemEditor, FloatItemEditor, SubMenuItemEditor} from "./EditOtherMenuItem";
import {EditCallbackDialog} from "./EditCallbackDialog";

export function openModeDesc(roundTripMode: RoundTripMode): string {
    switch(roundTripMode) {
        case RoundTripMode.NEW_PROJECT: return "New project to be created";
        case RoundTripMode.EMF_FILE_DRAGGED_IN: return "EMF file dragged in";
        case RoundTripMode.DIRECTORY_IN_BROWSER: return "Fully open local project";
    }
}

const MENU_MENU_ITEM_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/menu-item-types/";

function getRightDocsLink(selectedItem: MenuItem<any>): string {
    switch(selectedItem.messageType) {
        case "Analog": return `${MENU_MENU_ITEM_URL}analog-menu-item/`;
        case "Action": return `${MENU_MENU_ITEM_URL}action-menu-item/`;
        case "Sub": return `${MENU_MENU_ITEM_URL}sub-menu-item/`;
        case "Enum": return `${MENU_MENU_ITEM_URL}enum-menu-item/`;
        case "Boolean": return `${MENU_MENU_ITEM_URL}boolean-menu-item/`;
        case "Float": return `${MENU_MENU_ITEM_URL}float-menu-item/`;
        case "List": return `${MENU_MENU_ITEM_URL}list-menu-item/`;
        case "Rgb32": return `${MENU_MENU_ITEM_URL}rgb32-menu-item/`;
        case "Scroll": return `${MENU_MENU_ITEM_URL}scrollchoice-menu-item/`;
        case "Text": return `${MENU_MENU_ITEM_URL}editabletext-menu-item/`;
        case "LargeNum": return `${MENU_MENU_ITEM_URL}largenum-menu-item/`;
        case "CustomBuilder": return "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-iot/menu-library-remote-connectivity/";
        default: return MENU_MENU_ITEM_URL;
    }
}

function documentationLinkForItem(selectedItem: MenuItem<any>) {
    return <div className="docs-link">
        <span><a href={getRightDocsLink(selectedItem)} target="tcdocs">Specific docs for {selectedItem.messageType}</a></span>
        <span><a href={MENU_MENU_ITEM_URL} target="tcdocs">Core menu item docs</a></span>
    </div>;
}

export function TcMenuEditor() {
    const project = useCurrentlyOpenProject();
    const [selectedItem, setSelectedItem] = useState<MenuItem<any> | null>(null);
    const [isAddingItem, setIsAddingItem] = useState(false);
    const [isAddingTemplate, setIsAddingTemplate] = useState(false);
    const [confirmDelete, setConfirmDelete] = useState<MenuItem<any> | null>(null);
    const [, setTreeHash] = useState(0);

    React.useEffect(() => {
        if (project && !selectedItem) {
            setSelectedItem(project.menuTree.getRoot());
        }
    }, [project, selectedItem]);

    React.useEffect(() => {
        if (project) {
            project.menuTree.setTreeStructureChanged(() => {
                setTreeHash(h => h + 1);
            });
        }
    }, [project]);

    function findNearestSubMenu(selectedItem: MenuItem<any> | null): SubMenuItem|null {
        if(selectedItem instanceof SubMenuItem) return selectedItem;
        if(project === null) return null;
        if(selectedItem === null) return project.menuTree.getRoot();
        return project.menuTree.findParent(selectedItem);
    }

    function countChildren(selectedItem: SubMenuItem): number {
        let count = 0;
        for (let child of selectedItem.getChildren()) {
            count++;
            if (child instanceof SubMenuItem) {
                count += countChildren(child);
            }
        }
        return count;
    }

    if (!project) {
        return <ProjectWelcome/>;
    }

    function addDefaultTemplate(project: MenuTreeWithCodeOptions) {
        setIsAddingTemplate(true);
    }


    const handleSelect = (item: MenuItem<any>) => {
        setSelectedItem(item);
        setIsAddingItem(false);
    };

    const isRootSelected = selectedItem?.getMenuId() === "0";
    
    function newItemFromType(type: string): MenuItem<any> | null {
        if (!project) return null;
        let nextAvailableId = project.menuTree.nextAvailableId().toString();
        if(type === "Analog") return new AnalogMenuItem(nextAvailableId);
        else if(type === "Enum") return new EnumMenuItem(nextAvailableId);
        else if(type === "List") return new ListMenuItem(nextAvailableId);
        else if(type === "Sub") return new SubMenuItem("New sub", nextAvailableId);
        else if(type === "Boolean") return new BooleanMenuItem(nextAvailableId);
        else if(type === "Float") return new FloatMenuItem(nextAvailableId);
        else if(type === "Action") return new ActionMenuItem(nextAvailableId);
        else if(type === "LargeNum") return new EditableLargeNumberMenuItem(nextAvailableId);
        else if(type === "Rgb32") return new Rgb32MenuItem(nextAvailableId);
        else if(type === "Scroll") return new ScrollChoiceMenuItem(nextAvailableId);
        else if(type === "Text") return new EditableTextMenuItem(nextAvailableId);
        else throw new TcMenuItemError("Add item type not recognized");
    }

    let content;
    if (isAddingItem) {
        content = <AddItemDialog onCancel={() => setIsAddingItem(false)} onCreate={(type) => {
            const newItem = newItemFromType(type);
            const insertPoint = findNearestSubMenu(selectedItem)
            if (newItem && insertPoint) {
                newItem.setItemName("New Item " + newItem.getMenuId());
                project.menuTree.addMenuItem(insertPoint.getMenuId(), newItem);
                setSelectedItem(newItem);
                setTreeHash(h => h + 1);
            }
            setIsAddingItem(false);
        }} />;
    } else if (isAddingTemplate) {
        content = <SelectTemplateDialog onCancel={() => setIsAddingTemplate(false)} onSelect={(copyText) => {
            const serializer = new JsonMenuItemSerializer();
            const insertPoint = findNearestSubMenu(selectedItem)
            serializer.putItemsIntoMenuTree(copyText, project.menuTree, insertPoint?.getMenuId() ?? "0");
            setTreeHash(h => h + 1);
            setIsAddingTemplate(false);
        }} />;
    } else if (isRootSelected) {
        content = <RootEditor project={project} onHashChange={() => setTreeHash(h => h + 1)}/>;
    } else {
        content = <ItemEditor item={selectedItem} project={project} />;
    }

    function deleteAnItem(project: MenuTreeWithCodeOptions, selectedItem: MenuItem<any> | null) {
        if(!selectedItem || selectedItem.getMenuId() === "0") return;

        if(selectedItem?.messageType === "Sub") {
            setConfirmDelete(selectedItem);
            return;
        }
        
        project.menuTree.deleteMenuItem(selectedItem.getMenuId());
        setSelectedItem(project.menuTree.getRoot());
        setTreeHash(h => h + 1);
    }

    function moveUp(project: MenuTreeWithCodeOptions, selectedItem: MenuItem<any> | null) {
        if(!selectedItem || selectedItem.getMenuId() === "0") return;
        const parent = project.menuTree.findParent(selectedItem);
        if(parent) {
            parent.moveChildUp(selectedItem);
            setTreeHash(h => h + 1);
        }
    }

    function moveDown(project: MenuTreeWithCodeOptions, selectedItem: MenuItem<any> | null) {
        if(!selectedItem || selectedItem.getMenuId() === "0") return;
        const parent = project.menuTree.findParent(selectedItem);
        if(parent) {
            parent.moveChildDown(selectedItem);
            setTreeHash(h => h + 1);
        }
    }

    async function copyItem(selectedItem: MenuItem<any> | null) {
        if(!selectedItem || selectedItem.getMenuId() === "0" || !project) return;
        const serializer = new JsonMenuItemSerializer();
        const text = serializer.itemsToCopyText(selectedItem, project.menuTree);
        await navigator.clipboard.writeText(text);
    }

    async function pasteItem(selectedItem: MenuItem<any> | null) {
        if(!project) return;
        const text = await navigator.clipboard.readText();
        const serializer = new JsonMenuItemSerializer();
        const insertPoint = findNearestSubMenu(selectedItem);
        serializer.putItemsIntoMenuTree(text, project.menuTree, insertPoint?.getMenuId() ?? "0");
        setTreeHash(h => h + 1);
    }


    return <div className="menu-editor-container">
        {confirmDelete && <YesNoDialog
            message={`Are you sure you want to delete submenu "${confirmDelete.getItemName()}" and all its ${countChildren(confirmDelete as SubMenuItem)} children?`}
            onYes={() => {
                project.menuTree.deleteMenuItem(confirmDelete.getMenuId());
                setSelectedItem(project.menuTree.getRoot());
                setTreeHash(h => h + 1);
                setConfirmDelete(null);
            }}
            onNo={() => setConfirmDelete(null)}
        />}
        <div className="editor-header">
            <h1>Menu Editor - <span style={{fontSize: '70%'}}><b>{project.options.applicationName}</b> is {openModeDesc(project.roundTripMode)}</span></h1>
            <button className="close-button" onClick={() => setCurrentlyOpenProject(null)}>Close Project</button>
        </div>
        <div className="editor-main-layout">
            <aside className="editor-sidebar">
                <MenuTreeComponent
                    menuTree={project.menuTree} 
                    selectedItem={selectedItem}
                    onSelectItem={handleSelect}
                    onAddNewItem={() => setIsAddingItem(true)}
                    onAddTemplate={() => addDefaultTemplate(project)}
                    onDeleteItem={() => deleteAnItem(project, selectedItem)}
                    onMoveUp={() => moveUp(project, selectedItem)}
                    onMoveDown={() => moveDown(project, selectedItem)}
                    onCopyItem={() => copyItem(selectedItem)}
                    onPasteItem={() => pasteItem(selectedItem)}
                />
            </aside>
            <main className="editor-content">
                {(!selectedItem || selectedItem.getMenuId() === "0") ? <div/> : <div>{documentationLinkForItem(selectedItem)}</div>}
                {content}
            </main>
        </div>
    </div>
}

export function YesNoDialog({message, onYes, onNo}: { message: string, onYes: () => void, onNo: () => void }) {
    return (
        <div className="dialog-overlay">
            <div className="dialog-content">
                <h2>{message}</h2>
                <div className="dialog-actions">
                    <button className="secondary-button" onClick={onNo}>No</button>
                    <button className="primary-button" onClick={onYes}>Yes</button>
                </div>
            </div>
        </div>
    );
}

export function AddItemDialog({ onCancel, onCreate }: { onCancel: () => void, onCreate: (type: string) => void }) {
    const [selectedType, setSelectedType] = useState('Analog');

    const itemTypes = [
        { label: 'Analog Menu Item', value: 'Analog' },
        { label: 'Enumeration Menu Item', value: 'Enum' },
        { label: 'List Menu Item', value: 'List' },
        { label: 'Sub Menu Item', value: 'Sub' },
        { label: 'Boolean Menu Item', value: 'Boolean' },
        { label: 'Float Menu Item', value: 'Float' },
        { label: 'Action Menu Item', value: 'Action' },
        { label: 'Large Number Menu Item', value: 'LargeNum' },
        { label: 'RGB Color Menu Item', value: 'Rgb32' },
        { label: 'Scroll Choice Menu Item', value: 'Scroll' },
        { label: 'Text, IpAddress or Time Item', value: 'Text' },
    ];

    return (
        <div className="add-item-dialog">
            <h2>Add New Menu Item</h2>
            <p>Select the type of menu item you want to create:</p>
            <div className="radio-group">
                {itemTypes.map((type) => (
                    <div key={type.value} className="radio-option">
                        <input
                            type="radio"
                            id={`type-${type.value}`}
                            name="itemType"
                            value={type.value}
                            checked={selectedType === type.value}
                            onChange={(e) => setSelectedType(e.target.value)}
                        />
                        <label htmlFor={`type-${type.value}`}>{type.label}</label>
                    </div>
                ))}
            </div>
            <div className="dialog-actions">
                <button className="secondary-button" onClick={onCancel}>Cancel</button>
                <button className="primary-button" onClick={() => onCreate(selectedType)}>Create Item</button>
            </div>
        </div>
    );
}

export function SelectTemplateDialog({ onCancel, onSelect }: { onCancel: () => void, onSelect: (copyText: string) => void }) {
    const [selectedTemplate, setSelectedTemplate] = useState(tcMenuStarers[0].copyText);

    return (
        <div className="add-item-dialog">
            <h2>Add Menu Items from Template</h2>
            <p>Select a template to add to your menu:</p>
            <div className="radio-group">
                {tcMenuStarers.map((template) => (
                    <div key={template.name} className="radio-option">
                        <input
                            type="radio"
                            id={`template-${template.name}`}
                            name="menuTemplate"
                            value={template.copyText}
                            checked={selectedTemplate === template.copyText}
                            onChange={(e) => setSelectedTemplate(e.target.value)}
                        />
                        <label htmlFor={`template-${template.name}`}>{template.name}</label>
                    </div>
                ))}
            </div>
            <div className="dialog-actions">
                <button className="secondary-button" onClick={onCancel}>Cancel</button>
                <button className="primary-button" onClick={() => onSelect(selectedTemplate)}>Add Template</button>
            </div>
        </div>
    );
}

export function ItemEditor({ item, project }: { item: MenuItem<any> | null, project: MenuTreeWithCodeOptions }) {
    const [, setHash] = useState(0);
    const [editCallback, setEditCallback] = useState(false);

    if (!item) return <div>Select an item to edit</div>;

    const onNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setItemName(e.target.value);
        item.markChanged();
        project.menuTree.notifyItemChanged(item.getMenuId());
        setHash(h => h + 1);
    }

    const onVariableChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setVariableName(e.target.value);
        item.markChanged();
        setHash(h => h + 1);
    }

    const onCallbackChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setCallbackFnName(e.target.value);
        item.markChanged();
        setHash(h => h + 1);
    }

    const syncVariableName = () => {
        const name = item.getItemName();
        // Simple conversion to variable name: alphanumeric only, camelCase-ish
        const varName = name.split(/[^a-zA-Z0-9]/)
            .filter(part => part.length > 0)
            .map((part, index) => {
                const cleanPart = part.replace(/[^a-zA-Z0-9]/g, '');
                if (index === 0) return cleanPart.charAt(0).toLowerCase() + cleanPart.slice(1);
                return cleanPart.charAt(0).toUpperCase() + cleanPart.slice(1).toLowerCase();
            })
            .join('');
        item.setVariableName(varName);
        item.markChanged();
        setHash(h => h + 1);
    }

    const onEepromChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        item.setEEPROMLocation(parseInt(e.target.value));
        item.markChanged();
        setHash(h => h + 1);
    }

    const onAutoEeprom = () => {
        item.setEEPROMLocation(nextAvailableEepromLocation(project.menuTree));
        item.markChanged();
        setHash(h => h + 1);
    }

    const isDynamicEeprom = project.options.eepromSaveMode === EepromSaveMode.DYNAMIC_WRITE_BY_ID;

    function extraEditorsForType() {
        switch(item?.messageType) {
            case "Boolean":
                return <BooleanItemEditor item={item as BooleanMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "Analog": 
                return <AnalogItemEditor item={item as AnalogMenuItem} onHashChange={() => setHash(h => h + 1)} />
            case "Enum":
                return <EnumItemEditor item={item as EnumMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "LargeNum":
                return <LargeNumberItemEditor item={item as EditableLargeNumberMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "Float":
                return <FloatItemEditor item={item as FloatMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "Text":
                return <TextItemEditor item={item as EditableTextMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "Sub":
                return <SubMenuItemEditor item={item as SubMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "Rgb32":
                return <Rgb32ItemEditor item={item as Rgb32MenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "Scroll":
                return <ScrollChoiceItemEditor item={item as ScrollChoiceMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "List":
                return <ListItemEditor item={item as ListMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            case "Action": return <div/>;
            case "CustomBuilder":
                return <CustomBuilderMenuItemEditor item={item as CustomBuilderMenuItem} onHashChange={() => setHash(h => h + 1)} />;
            default: return <p>There's no editor for {item?.messageType}</p>
        }
    }

    const onLocalOnlyChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setLocalOnly(e.target.checked);
        setHash(h => h + 1);
    }

    const onReadOnlyChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setReadOnly(e.target.checked);
        setHash(h => h + 1);
    }

    const onVisibleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setVisible(e.target.checked);
        setHash(h => h + 1);
    }

    const onStaticDataInRAM = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setStaticDataInRAM(e.target.checked);
        setHash(h => h + 1);
    }

    const onParentChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        try {
            project.menuTree.moveItem(item, e.target.value);
            setHash(h => h + 1);
        } catch (err: any) {
            alert(err.message);
        }
    }

    function canHaveEeprom(messageType: string) {
        return messageType === "Analog" || messageType === "Enum" || messageType === "Boolean" || messageType === "Rgb32" ||
            messageType === "LargeNum" || messageType === "Scroll" || messageType === "Text" || messageType === "Date" ||
            messageType === "Time";
    }

    function callbackHow(callbackFnName: string): string {
        if(!callbackFnName) return "None";
        let how = "";
        if(callbackFnName.startsWith("@")) how += "Manual declaration "
        if(callbackFnName.endsWith("RtCall")) {
            how += "RT render function";
        } else {
            how += "callback function";
        }
        return how;
    }

    return (
        <div className="item-editor">
            <h2>Editing: "{item.getItemName()}" (ID: {item.getMenuId()}, type {item.messageType})</h2>

            <div className="form-group">
                <label htmlFor="itemParent">Parent Sub Menu</label>
                <select id="itemParent" value={project.menuTree.findParent(item).getMenuId()} onChange={onParentChange}>
                    {project.menuTree.getAllItems()
                        .filter(it => it instanceof SubMenuItem)
                        .map(it => <option key={it.getMenuId()} value={it.getMenuId()}>{it.getItemName()} [{it.getMenuId()}]</option>)
                    }
                </select>
            </div>

            <div className="form-group">
                <label htmlFor="itemName">Item Name</label>
                <input id="itemName" type="text" value={item.getItemName()} onChange={onNameChange} />
            </div>

            <div className="form-group">
                <label htmlFor="variableName">Variable Name</label>
                <div className="input-with-button">
                    <input id="variableName" type="text" value={item.getVariableName()} onChange={onVariableChange} />
                    <button onClick={syncVariableName} title="Sync from Name">Sync</button>
                </div>
            </div>

            <div className="form-group">
                <label htmlFor="callbackFn">Notification method ({callbackHow(item.getCallbackFnName())})</label>
                <div className="input-with-button">
                    <input id="callbackFn" type="text" value={item.getCallbackFnName()} onChange={onCallbackChange} />
                    <button onClick={() => setEditCallback(true)} title="Edit Callback Type">Edit</button>
                </div>
            </div>

            {editCallback && (
                <EditCallbackDialog
                    menuItem={item}
                    initialCallback={item.getCallbackFnName()}
                    onCancel={() => setEditCallback(false)}
                    onSave={(newCallback) => {
                        item.setCallbackFnName(newCallback);
                        item.markChanged();
                        setEditCallback(false);
                        setHash(h => h + 1);
                    }}
                />
            )}

            <div className="form-group">
                {!canHaveEeprom(item.messageType) ? <div>Can't save {item.messageType} items</div> : isDynamicEeprom ? (
                        <>
                            <label htmlFor="eepromLoc">Should save to EEPROM</label>
                            <select id="eepromLoc" value={item.getEEPROMLocation()} onChange={onEepromChange}>
                                    <option value={0xFFFF}>Don't save</option>
                                    <option value={0x0020}>Save</option>
                            </select>
                        </>
                ) : (
                    <>
                    <label htmlFor="eepromLoc">Position to save in EEPROM</label>
                    <div className="input-with-button">
                        <input id="eepromLoc" type="number" min="-1" max="32768"
                               value={item.getEEPROMLocation()}
                               onChange={onEepromChange} />
                        <button onClick={onAutoEeprom} title="Auto assign">Auto</button>
                    </div>
                    </>
                )}
            </div>

            {extraEditorsForType()}

            <div className="form-group-checkbox">
                <input id="localOnly" type="checkbox" checked={item.isLocalOnly()} onChange={onLocalOnlyChange} />
                <label htmlFor="localOnly">Local Only</label>
            </div>

            <div className="form-group-checkbox">
                <input id="readOnly" type="checkbox" checked={item.isReadOnly()} onChange={onReadOnlyChange} />
                <label htmlFor="readOnly">Read Only</label>
            </div>

            <div className="form-group-checkbox">
                <input id="visible" type="checkbox" checked={item.isVisible()} onChange={onVisibleChange} />
                <label htmlFor="visible">Visible</label>
            </div>
            {project.options.useDynamicMenus ? <div/> : <div className="form-group-checkbox">
                <input id="staticDataInRAM" type="checkbox" checked={item.isStaticDataInRAM()} onChange={onStaticDataInRAM} />
                <label htmlFor="staticDataInRAM">Static INFO data in RAM</label>
            </div>}
        </div>
    );
}
