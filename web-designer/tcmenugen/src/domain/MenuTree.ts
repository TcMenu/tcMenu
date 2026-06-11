import {MenuItem, SubMenuItem} from "./MenuItem";

type TreeStructureChangedFn = (menuTree: MenuTree, id: string) => void;

export class MenuTree {
    private allItemMap: { [id: string]: MenuItem<any>; } = {};
    private readonly rootElement = new SubMenuItem("ROOT", "0");
    private treeStructureChanged: TreeStructureChangedFn;

    constructor(treeStructChangeFn: TreeStructureChangedFn) {
        this.allItemMap["0"] = this.rootElement;
        this.treeStructureChanged = treeStructChangeFn;
    }

    public setTreeStructureChanged(fn: TreeStructureChangedFn) {
        this.treeStructureChanged = fn;
    }

    public getRoot() {
        return this.rootElement;
    }

    public getAllItems(): MenuItem<any>[] {
        return Object.values(this.allItemMap);
    }

    public getMenuItemFor(key: string): MenuItem<any> {
        return this.allItemMap[key];
    }

    public addMenuItem(subMenuId: string, menuItem: MenuItem<any>): boolean {
        if(!this.getMenuItemFor(menuItem.getMenuId())) {
            let sub = this.allItemMap[subMenuId.toString()];
            if (!(sub instanceof SubMenuItem)) throw new Error(`Non submenu parent ${subMenuId} for ${menuItem.getMenuId()}`);
            sub.addChildItem(menuItem);
            this.allItemMap[menuItem.getMenuId()] = menuItem;
            this.treeStructureChanged(this, menuItem.getMenuId());
            return true;
        }
        else return false; // already existed so not re-added.
    }

    public deleteMenuItem(menuItemId: string): boolean {
        const item = this.getMenuItemFor(menuItemId);
        if(!item || item.getMenuId() === "0") {
            return false;
        }

        // Find parent and remove from parent's children
        for(let key in this.allItemMap) {
            const potentialParent = this.allItemMap[key];
            if(potentialParent instanceof SubMenuItem) {
                if(potentialParent.getChildren().some(c => c.getMenuId() === menuItemId)) {
                    potentialParent.removeChildItem(item);
                }
            }
        }

        if(item instanceof SubMenuItem) {
            for(let child of item.getChildren()) {
                this.deleteMenuItem(child.getMenuId());
            }
        }

        delete this.allItemMap[menuItemId];
        this.treeStructureChanged(this, menuItemId);
        return true;
    }

    public nextAvailableId(): number {
        let id = 0;
        for(let key in this.allItemMap) {
            const num = parseInt(key);
            if(!isNaN(num) && num > id) {
                id = num;
            }
        }
        return id + 1;
    }

    hasId(menuId: string): boolean {
        return this.allItemMap[menuId] !== undefined;
    }


    public notifyItemChanged(id: string) {
        this.treeStructureChanged(this, id);
    }

    public findParent(selectedItem: MenuItem<any>) {
        const subs = Object.values(this.allItemMap)
            .filter((item)  => item instanceof SubMenuItem);

        for (let sub of subs) {
            if (sub instanceof SubMenuItem && sub.getChildren().includes(selectedItem)) {
                return sub;
            }
        }

        return this.getRoot();
    }

    public moveItem(item: MenuItem<any>, newSubMenuId: string) {
        if(item.getMenuId() === "0") return;

        const currentParent = this.findParent(item);
        if(currentParent.getMenuId() === newSubMenuId) return;

        const newParent = this.getMenuItemFor(newSubMenuId);
        if (!(newParent instanceof SubMenuItem)) throw new Error(`Non submenu parent ${newSubMenuId}`);

        // check for cycles
        let p: SubMenuItem | null = newParent;
        while(p != null) {
            if(p.getMenuId() === item.getMenuId()) throw new Error("Cycle detected in move");
            p = this.findParent(p);
            if(p.getMenuId() === "0") break;
        }

        currentParent.removeChildItem(item);
        newParent.addChildItem(item);
        this.treeStructureChanged(this, item.getMenuId());
    }
}