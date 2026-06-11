import {
    fieldEepromRequirement,
    BooleanMenuItem,
    EnumMenuItem,
    AnalogMenuItem,
    EditableLargeNumberMenuItem,
    Rgb32MenuItem,
    ScrollChoiceMenuItem,
    EditableTextMenuItem,
    TextEditMode,
    ActionMenuItem,
    SubMenuItem,
    nextAvailableEepromLocation
} from './MenuItem';
import {MenuTree} from "./MenuTree";

describe('fieldEepromRequirement', () => {
    it('should return 0 for null', () => {
        expect(fieldEepromRequirement(null as any)).toBe(0);
    });

    it('should return 2 for BooleanMenuItem', () => {
        expect(fieldEepromRequirement(new BooleanMenuItem("1"))).toBe(2);
    });

    it('should return 2 for EnumMenuItem', () => {
        expect(fieldEepromRequirement(new EnumMenuItem("1"))).toBe(2);
    });

    it('should return 2 for AnalogMenuItem', () => {
        expect(fieldEepromRequirement(new AnalogMenuItem("1"))).toBe(2);
    });

    it('should return 8 for EditableLargeNumberMenuItem', () => {
        expect(fieldEepromRequirement(new EditableLargeNumberMenuItem("1"))).toBe(8);
    });

    it('should return 4 for Rgb32MenuItem', () => {
        expect(fieldEepromRequirement(new Rgb32MenuItem("1"))).toBe(4);
    });

    it('should return 2 for ScrollChoiceMenuItem', () => {
        expect(fieldEepromRequirement(new ScrollChoiceMenuItem("1"))).toBe(2);
    });

    it('should return text length for EditableTextMenuItem in PLAIN_TEXT mode', () => {
        const item = new EditableTextMenuItem("1");
        item.setEditMode(TextEditMode.PLAIN_TEXT);
        item.setTextLength(15);
        expect(fieldEepromRequirement(item)).toBe(15);
    });

    it('should return 4 for EditableTextMenuItem in non-PLAIN_TEXT mode', () => {
        const item = new EditableTextMenuItem("1");
        item.setEditMode(TextEditMode.IP_ADDRESS);
        expect(fieldEepromRequirement(item)).toBe(4);
    });

    it('should return 0 for ActionMenuItem', () => {
        expect(fieldEepromRequirement(new ActionMenuItem("1"))).toBe(0);
    });
});

describe('MenuItem base functionality', () => {
    it('should track changed flag correctly', () => {
        const item = new AnalogMenuItem("Test");
        item.clearChanged();
        expect(item.isChanged()).toBe(false);

        item.setItemName("New Name");
        expect(item.isChanged()).toBe(true);
        item.clearChanged();

        item.setEEPROMLocation(10);
        expect(item.isChanged()).toBe(true);
        item.clearChanged();

        item.setVariableName("NewVar");
        expect(item.isChanged()).toBe(true);
        item.clearChanged();

        item.setCallbackFnName("onCallback");
        expect(item.isChanged()).toBe(true);
        item.clearChanged();

        item.setReadOnly(true);
        expect(item.isChanged()).toBe(true);
        item.clearChanged();

        item.setVisible(true);
        expect(item.isChanged()).toBe(true);
        item.clearChanged();

        item.setLocalOnly(true);
        expect(item.isChanged()).toBe(true);
        item.clearChanged();

        item.setStaticDataInRAM(true);
        expect(item.isChanged()).toBe(true);
    });

    it('should identify basic properties correctly', () => {
        const item = new AnalogMenuItem("Test");
        item.setMenuId("123");
        expect(item.getMenuId()).toBe("123");
        item.setItemName("TestName");
        expect(item.getItemName()).toBe("TestName");
    });
});

describe('nextAvailableEepromLocation', () => {
    it('should find the next available location', () => {
        const tree = new MenuTree(() => {});
        const item1 = new AnalogMenuItem("Item 1");
        item1.setMenuId("1");
        item1.setEEPROMLocation(0); // size 2
        tree.addMenuItem("0", item1);

        const item2 = new AnalogMenuItem("Item 2");
        item2.setMenuId("2");
        item2.setEEPROMLocation(10); // size 2
        tree.addMenuItem("0", item2);

        expect(nextAvailableEepromLocation(tree)).toBe(12);
    });

    it('should ignore items with eeprom -1 or 0xFFFF', () => {
        const tree = new MenuTree(() => {});
        const item1 = new AnalogMenuItem("Item 1");
        item1.setMenuId("1");
        item1.setEEPROMLocation(10); // size 2
        tree.addMenuItem("0", item1);

        const item2 = new AnalogMenuItem("Item 2");
        item2.setMenuId("2");
        item2.setEEPROMLocation(-1);
        tree.addMenuItem("0", item2);

        const item3 = new AnalogMenuItem("Item 3");
        item3.setMenuId("3");
        item3.setEEPROMLocation(0xFFFF);
        tree.addMenuItem("0", item3);

        expect(nextAvailableEepromLocation(tree)).toBe(12);
    });
});

describe('SubMenuItem movement', () => {
    it('should move children up and down', () => {
        const sub = new SubMenuItem("Parent", "1");
        const child1 = new ActionMenuItem("2");
        const child2 = new ActionMenuItem("3");
        const child3 = new ActionMenuItem("4");

        sub.addChildItem(child1);
        sub.addChildItem(child2);
        sub.addChildItem(child3);

        expect(sub.getChildren()[0]).toBe(child1);
        expect(sub.getChildren()[1]).toBe(child2);
        expect(sub.getChildren()[2]).toBe(child3);

        sub.moveChildDown(child1);
        expect(sub.getChildren()[0]).toBe(child2);
        expect(sub.getChildren()[1]).toBe(child1);

        sub.moveChildUp(child3);
        expect(sub.getChildren()[1]).toBe(child3);
        expect(sub.getChildren()[2]).toBe(child1);

        // Boundary cases
        sub.moveChildUp(child2); // already at top
        expect(sub.getChildren()[0]).toBe(child2);

        sub.moveChildDown(child1); // already at bottom
        expect(sub.getChildren()[2]).toBe(child1);
    });
});

describe('MenuTree movement', () => {
    it('should move items between submenus', () => {
        const tree = new MenuTree(() => {});
        const sub1 = new SubMenuItem("Sub1", "1");
        const sub2 = new SubMenuItem("Sub2", "2");
        const item = new ActionMenuItem("3");

        tree.addMenuItem("0", sub1);
        tree.addMenuItem("0", sub2);
        tree.addMenuItem("1", item);

        expect(tree.findParent(item)).toBe(sub1);
        expect(sub1.getChildren()).toContain(item);

        tree.moveItem(item, "2");
        expect(tree.findParent(item)).toBe(sub2);
        expect(sub1.getChildren()).not.toContain(item);
        expect(sub2.getChildren()).toContain(item);
    });

    it('should detect cycles when moving', () => {
        const tree = new MenuTree(() => {});
        const sub1 = new SubMenuItem("Sub1", "1");
        const sub2 = new SubMenuItem("Sub2", "2");

        tree.addMenuItem("0", sub1);
        tree.addMenuItem("1", sub2);

        // try to move sub1 into sub2 (sub2 is already in sub1)
        expect(() => tree.moveItem(sub1, "2")).toThrow("Cycle detected");
    });
});

