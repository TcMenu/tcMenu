package com.thecoderscorner.menu.domain.build;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.*;
import static com.thecoderscorner.menu.domain.CustomBuilderMenuItem.*;
import static com.thecoderscorner.menu.domain.RuntimeListMenuItem.*;

public class MenuTreeBuilder {
    public static final int ROM_SAVE = 0x20;
    public static final int DONT_SAVE = -1;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final MenuTree tree;
    private final Deque<SubMenuItem> subMenuStack = new LinkedList<>();
    private final AtomicInteger nextId = new AtomicInteger(MenuTree.ROOT.getId() + 1);

    public MenuTreeBuilder() {
        this.tree = new MenuTree();
        this.subMenuStack.push(tree.getSubMenuById(MenuTree.ROOT.getId()).orElseThrow());
    }

    public MenuTreeBuilder(MenuTree tree, SubMenuItem root) {
        this.tree = tree;
        this.subMenuStack.push(root);
    }

    public MenuTreeBuilder subMenu(int id, String name, CallbackDefinition callback, MenuBuilderFlag... flags) {
        var newSubMenu = new SubMenuItem(name, null, id, -1, callback.cbText(),
                hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY), !hasFlag(flags, MenuBuilderFlag.HIDDEN),
                hasFlag(flags, MenuBuilderFlag.SECURED), hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), newSubMenu);
        subMenuStack.push(newSubMenu);
        MenuItemHelper.setMenuState(newSubMenu, false, tree);
        return this;
    }

    public MenuTreeBuilder endSub() {
        assertNoDuplicates();
        subMenuStack.pop();
        return this;
    }

    public MenuTreeBuilder booleanItem(int id, String name, int eeprom, BooleanNaming naming, boolean defValue,
                                       CallbackDefinition callback, MenuBuilderFlag... flags) {
        var newBooleanItem = new BooleanMenuItem(name, null, id, eeprom, callback.cbText(), naming,
                hasFlag(flags, MenuBuilderFlag.READ_ONLY), hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY),
                !hasFlag(flags, MenuBuilderFlag.HIDDEN), hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), newBooleanItem);
        MenuItemHelper.setMenuState(newBooleanItem, defValue, tree);
        return this;
    }

    public MenuTreeBuilder floatItem(int id, String name, int decPlaces, int defVal, CallbackDefinition callback,
                                     MenuBuilderFlag... flags) {
        var newFloatItem = new FloatMenuItem(name, null, id, callback.cbText(), -1, decPlaces,
                hasFlag(flags, MenuBuilderFlag.READ_ONLY), hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY),
                !hasFlag(flags, MenuBuilderFlag.HIDDEN), hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), newFloatItem);
        MenuItemHelper.setMenuState(newFloatItem, defVal, tree);
        return this;
    }

    public MenuTreeBuilder enumItem(int id, String name, int eeprom, List<String> values, int defVal,
                                    CallbackDefinition callback, MenuBuilderFlag... flags) {
        var newEnum = new EnumMenuItem(name, null, id, eeprom, callback.cbText(), values,
                hasFlag(flags, MenuBuilderFlag.READ_ONLY), hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY),
                !hasFlag(flags, MenuBuilderFlag.HIDDEN), hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), newEnum);
        MenuItemHelper.setMenuState(newEnum, defVal, tree);
        return this;
    }

    public MenuTreeBuilder actionItem(int id, String name, CallbackDefinition callback, MenuBuilderFlag... flags) {
        var newAction = new ActionMenuItem(name, null, id, callback.cbText(), -1,
                hasFlag(flags, MenuBuilderFlag.READ_ONLY), hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY),
                !hasFlag(flags, MenuBuilderFlag.HIDDEN), hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), newAction);
        MenuItemHelper.setMenuState(newAction, false, tree);
        return this;
    }

    public MenuTreeBuilder rgb32Item(int id, String name, int eeprom, boolean hasAlpha, PortableColor defVal,
                                     CallbackDefinition callback, MenuBuilderFlag... flags) {
        var rgb32 = new Rgb32MenuItem(name, null, id, eeprom, callback.cbText(), hasAlpha,
                hasFlag(flags, MenuBuilderFlag.READ_ONLY), hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY),
                !hasFlag(flags, MenuBuilderFlag.HIDDEN), hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), rgb32);
        MenuItemHelper.setMenuState(rgb32, defVal, tree);
        return this;
    }
    
    public AnalogMenuItemBuilder analogItem(int id, String name, int eeprom, int defVal, CallbackDefinition callback,
                                            MenuBuilderFlag... flags) {
        return new AnalogMenuItemBuilder(this, defVal)
                .withName(name).withId(id).withEepromAddr(eeprom).withFunctionName(callback.cbText())
                .withReadOnly(hasFlag(flags, MenuBuilderFlag.READ_ONLY))
                .withLocalOnly(hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY))
                .withVisible(!hasFlag(flags, MenuBuilderFlag.HIDDEN))
                .withStaticDataInRAM(hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
    }

    public ScrollChoiceMenuItemBuilder scrollChoiceItem(int id, String name, int eeprom, int defVal,
                                                        CallbackDefinition callback, MenuBuilderFlag... flags) {
        return new ScrollChoiceMenuItemBuilder(this, defVal)
                .withName(name).withId(id).withEepromAddr(eeprom).withFunctionName(callback.cbText())
                .withReadOnly(hasFlag(flags, MenuBuilderFlag.READ_ONLY))
                .withLocalOnly(hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY))
                .withVisible(!hasFlag(flags, MenuBuilderFlag.HIDDEN))
                .withStaticDataInRAM(hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
    }
    
    public MenuTreeBuilder customItem(int id, String name, CustomMenuType type, CallbackDefinition callback,
                                      MenuBuilderFlag... flags) {
        var custom = new CustomBuilderMenuItem(name, null, id, -1, callback.cbText(),
                hasFlag(flags, MenuBuilderFlag.READ_ONLY), hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY),
                !hasFlag(flags, MenuBuilderFlag.HIDDEN), type, hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), custom);
        return this;
    }

    public MenuTreeBuilder largeNumItem(int id, String name, int eeprom, LargeNumberDefinition numDef, BigDecimal def,
                                        CallbackDefinition callback, MenuBuilderFlag... flags) {
        var lgeNum = new EditableLargeNumberMenuItem(name, null, id, eeprom, callback.cbText(), numDef.getDigitsAllowed(), numDef.getDecPlaces(), numDef.isNeg(),
                hasFlag(flags, MenuBuilderFlag.READ_ONLY), hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY), 
                !hasFlag(flags, MenuBuilderFlag.HIDDEN), hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), lgeNum);
        MenuItemHelper.setMenuState(lgeNum, def, tree);
        return this;
    }

    public MenuTreeBuilder textItem(int id, String name, int eeprom,  int chars, String def,
                                    CallbackDefinition callback, MenuBuilderFlag... flags) {
        var text = new EditableTextMenuItem(name, null, id, eeprom, callback.cbText(), chars,
                EditItemType.PLAIN_TEXT, hasFlag(flags, MenuBuilderFlag.READ_ONLY),
                hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY), !hasFlag(flags, MenuBuilderFlag.HIDDEN),
                hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), text);
        MenuItemHelper.setMenuState(text, def, tree);
        return this;
    }

    public MenuTreeBuilder ipAddressItem(int id, String name, int eeprom, String ipAddr,
                                         CallbackDefinition callback, MenuBuilderFlag... flags) {
        var text = new EditableTextMenuItem(name, null, id, eeprom, callback.cbText(), 4,
                EditItemType.IP_ADDRESS, hasFlag(flags, MenuBuilderFlag.READ_ONLY),
                hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY), !hasFlag(flags, MenuBuilderFlag.HIDDEN),
                hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), text);
        MenuItemHelper.setMenuState(text, ipAddr, tree);
        return this;
    }

    public MenuTreeBuilder dateItem(int id, String name, int eeprom, LocalDate date, CallbackDefinition callback, MenuBuilderFlag... flags) {
        var text = new EditableTextMenuItem(name, null, id, eeprom, callback.cbText(), 3,
                EditItemType.GREGORIAN_DATE, hasFlag(flags, MenuBuilderFlag.READ_ONLY),
                hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY), !hasFlag(flags, MenuBuilderFlag.HIDDEN),
                hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), text);
        MenuItemHelper.setMenuState(text, date.format(DateTimeFormatter.ISO_DATE), tree);
        return this;
    }

    public MenuTreeBuilder timeItem(int id, String name, int eeprom, EditItemType type, LocalTime time, CallbackDefinition callback, MenuBuilderFlag... flags) {
        var text = new EditableTextMenuItem(name, null, id, eeprom, callback.cbText(), 3,
                type, hasFlag(flags, MenuBuilderFlag.READ_ONLY),
                hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY), !hasFlag(flags, MenuBuilderFlag.HIDDEN),
                hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM));
        tree.addMenuItem(subMenuStack.peek(), text);
        MenuItemHelper.setMenuState(text, time.format(timeFormatter), tree);
        return this;
    }

    public MenuTreeBuilder listFromArray(int id, String name, ListCreationMode mode, List<String> rows, CallbackDefinition callback, MenuBuilderFlag... flags) {
        var list = new RuntimeListMenuItem(name, null, id, DONT_SAVE, callback.cbText(), hasFlag(flags, MenuBuilderFlag.READ_ONLY),
                hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY), !hasFlag(flags, MenuBuilderFlag.HIDDEN), rows.size(),
                hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM), mode);
        tree.addMenuItem(subMenuStack.peek(), list);
        MenuItemHelper.setMenuState(list, rows, tree);
        return this;
    }

    public MenuTreeBuilder listFromCustom(int id, String name, int rowCount, CallbackDefinition callback, MenuBuilderFlag... flags) {
        var list = new RuntimeListMenuItem(name, null, id, DONT_SAVE, callback.cbText(), hasFlag(flags, MenuBuilderFlag.READ_ONLY),
                hasFlag(flags, MenuBuilderFlag.LOCAL_ONLY), !hasFlag(flags, MenuBuilderFlag.HIDDEN), rowCount,
                hasFlag(flags, MenuBuilderFlag.STATIC_IN_RAM), ListCreationMode.CUSTOM_RTCALL);
        tree.addMenuItem(subMenuStack.peek(), list);
        MenuItemHelper.setMenuState(list, List.of(), tree);
        return this;
    }

    public MenuTreeBuilder adjustingVariableName(int id, String variableName) {
        MenuItem item = tree.getMenuById(id).orElseThrow();
        var builder = MenuItemHelper.builderWithExisting(item);
        var parent = tree.findParent(item);
        builder.withVariableName(variableName);
        tree.addOrUpdateItem(parent.getId(), builder.menuItem());
        return this;
    }

    private void assertNoDuplicates() {
        var visited = new java.util.HashSet<Integer>();

        for (MenuItem item : tree.getAllMenuItems()) {
            if(!visited.add(item.getId())) {
                throw new IllegalStateException("Duplicate menu item id: " + item.getId());
            }
        }
    }

    private boolean hasFlag(MenuBuilderFlag[] flags, MenuBuilderFlag menuBuilderFlag) {
        return Arrays.stream(flags).anyMatch(flag -> flag == menuBuilderFlag);
    }

    public void rawPushItem(MenuItem m) {
        tree.addMenuItem(subMenuStack.peek(), m);
    }

    public MenuTree asTree() {
        assertNoDuplicates();
        return tree;
    }

    public int nextId() {
        return nextId.getAndIncrement();
    }
}
