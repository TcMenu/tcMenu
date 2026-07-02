import React, {useEffect, useState} from 'react';
import {MenuTree} from "../domain/MenuTree";
import {MenuItem, SubMenuItem} from "../domain/MenuItem";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
    faPlus,
    faTrash,
    faFolderPlus,
    faArrowUp,
    faArrowDown,
    faCopy,
    faPaste,
    faSearch
} from "@fortawesome/free-solid-svg-icons";
import {getInternationalization, NO_INTERNATIONALIZATION} from "../generator/I18nImpls";

interface MenuTreeComponentProps {
    menuTree: MenuTree;
    selectedItem: MenuItem<any> | null;
    onSelectItem: (item: MenuItem<any>) => void;
    onAddNewItem: () => void;
    onAddTemplate: () => void;
    onDeleteItem: () => void;
    onMoveUp: () => void;
    onMoveDown: () => void;
    onCopyItem: () => void;
    onPasteItem: () => void;
}

export function MenuTreeComponent({ menuTree, selectedItem, onSelectItem, onAddNewItem, onAddTemplate, onDeleteItem, onMoveUp, onMoveDown, onCopyItem, onPasteItem }: MenuTreeComponentProps) {
    const [filter, setFilter] = useState("");
    const root = menuTree.getRoot();
    const [i18n, setI18n] = useState(NO_INTERNATIONALIZATION);
    useEffect(() => {
        let isMounted = true;
        getInternationalization().then((intnl) => {
            if (isMounted) setI18n(intnl);
        });
        return () => {
            isMounted = false;
        };
    }, [filter]);

    const isRootSelected = selectedItem?.getMenuId() === "0";

    function emptyShowSomething(s: string): string {
        if(!s?.trim()) return "[empty]";

        return i18n.valueForKey(s) ?? s;
    }


    return (
        <div className="menu-tree">
            <div className="tree-header">
                <h3>Menu Items</h3>
                <div className="search-container">
                    <FontAwesomeIcon icon={faSearch} />
                    <input 
                        type="text" 
                        placeholder="Search..." 
                        value={filter} 
                        onChange={(e) => setFilter(e.target.value)}
                    />
                </div>
            </div>
            <div className="tree-scroll-area">
                <ul>
                    <MenuItemNode 
                        item={root} 
                        selectedItem={selectedItem} 
                        onSelect={onSelectItem}
                        filter={filter}
                        nameRenderFn={emptyShowSomething}
                    />
                </ul>
            </div>
            <div className="tree-actions">
                <button title="Add Menu Item" onClick={onAddNewItem}>
                    <FontAwesomeIcon icon={faPlus} />
                </button>
                <button title="Add Template" onClick={onAddTemplate}>
                    <FontAwesomeIcon icon={faFolderPlus} />
                </button>
                <button 
                    title="Delete Menu Item" 
                    onClick={onDeleteItem}
                    disabled={isRootSelected}
                >
                    <FontAwesomeIcon icon={faTrash} />
                </button>
                <button title="Move Up" onClick={onMoveUp} disabled={isRootSelected}>
                    <FontAwesomeIcon icon={faArrowUp} />
                </button>
                <button title="Move Down" onClick={onMoveDown} disabled={isRootSelected}>
                    <FontAwesomeIcon icon={faArrowDown} />
                </button>
                <button title="Copy Menu Item" onClick={onCopyItem} disabled={isRootSelected}>
                    <FontAwesomeIcon icon={faCopy} />
                </button>
                <button title="Paste Menu Item" onClick={onPasteItem}>
                    <FontAwesomeIcon icon={faPaste} />
                </button>
            </div>
        </div>
    );
}

interface MenuItemNodeProps {
    item: MenuItem<any>;
    selectedItem: MenuItem<any> | null;
    onSelect: (item: MenuItem<any>) => void;
    filter: string;
    nameRenderFn: (name: string) => string;
}

function MenuItemNode({ nameRenderFn, item, selectedItem, onSelect, filter }: MenuItemNodeProps) {
    const isSubMenu = item instanceof SubMenuItem;
    const isSelected = selectedItem?.getMenuId() === item.getMenuId();

    const matchesFilter = (item: MenuItem<any>): boolean => {
        if (!filter) return true;
        const search = filter.toLowerCase();
        return item.getItemName().toLowerCase().includes(search) || item.getMenuId().toString().includes(search);
    };

    const hasVisibleChildren = (item: SubMenuItem): boolean => {
        return item.getChildren().some(child => {
            if (matchesFilter(child)) return true;
            if (child instanceof SubMenuItem) return hasVisibleChildren(child);
            return false;
        });
    };

    if (item.getMenuId() !== "0" && !matchesFilter(item)) {
        if (!isSubMenu || !hasVisibleChildren(item as SubMenuItem)) {
            return null;
        }
    }

    return (
        <li>
            <span 
                className={`menu-item-name ${isSelected ? 'selected' : ''}`}
                onClick={() => onSelect(item)}
            >
                {nameRenderFn(item.getItemName()) + ": " + item.getMenuId() + " (" + item.messageType + ")"}
            </span>
            {isSubMenu && (
                <ul>
                    {(item as SubMenuItem).getChildren().map(child => (
                        <MenuItemNode 
                            key={child.getMenuId()} 
                            item={child} 
                            selectedItem={selectedItem}
                            onSelect={onSelect}
                            filter={filter}
                            nameRenderFn={nameRenderFn}
                        />
                    ))}
                </ul>
            )}
        </li>
    );
}
