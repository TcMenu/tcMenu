/*
 * Copyright (c)  2016-2021 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.ActionMenuItem;
import com.thecoderscorner.menu.domain.ActionMenuItemBuilder;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItemBuilder;
import com.thecoderscorner.menu.domain.state.MenuTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariableNameGeneratorTest {

    private SubMenuItem sub1;
    private SubMenuItem sub2;
    private MenuTree tree;
    private ActionMenuItem action1;
    private ActionMenuItem action2;
    private SubMenuItem sub3;
    private ActionMenuItem action3;

    @BeforeEach
    void setUp() {
        tree = new MenuTree();

        // two top level sub menus
        sub1 = SubMenuItemBuilder.aSubMenuItemBuilder().withName("Sub Menu 1 Öôóò").withId(100).menuItem();
        tree.addMenuItem(MenuTree.ROOT, sub1);
        sub2 = SubMenuItemBuilder.aSubMenuItemBuilder().withName("Sub 2 Öôóò").withId(101).withVariableName("OverrideÖôóò").menuItem();
        tree.addMenuItem(MenuTree.ROOT, sub2);

        // an action item under each of the above sub menus
        action1 = ActionMenuItemBuilder.anActionMenuItemBuilder().withName("Action 1").withId(102).menuItem();
        tree.addMenuItem(sub1, action1);
        action2 = ActionMenuItemBuilder.anActionMenuItemBuilder().withName("Action 2").withId(103).withVariableName("OverrideAbc").menuItem();
        tree.addMenuItem(sub2, action2);

        // put another sub menu under sub2 so now we have sub2 -> sub3
        sub3 = SubMenuItemBuilder.aSubMenuItemBuilder().withName("Sub 3").withId(105).menuItem();
        tree.addMenuItem(sub2, sub3);

        // put an item under sub 3 so now we have sub2 -> sub3 -> action3
        action3 = ActionMenuItemBuilder.anActionMenuItemBuilder().withName("Action 3").withId(106).menuItem();
        tree.addMenuItem(sub3, action3);
    }

    @Test
    public void testGenerationFlatNamesTopLevel() {
        VariableNameGenerator generator = new VariableNameGenerator(tree, false);
        assertEquals("SubMenu1Öôóò", generator.makeNameToVar(sub1));
        assertEquals("OverrideÖôóò", generator.makeNameToVar(sub2));
        assertEquals("Sub3", generator.makeNameToVar(sub3));

        assertEquals("NewName", generator.makeNameToVar(sub1, "NewName"));
        assertEquals("NewName", generator.makeNameToVar(sub2, "NewName"));
    }

    @Test
    public void testGenerationFlatNamesSubLevel() {
        VariableNameGenerator generator = new VariableNameGenerator(tree, false);
        assertEquals("Action1", generator.makeNameToVar(action1));
        assertEquals("OverrideAbc", generator.makeNameToVar(action2));

        assertEquals("NewName", generator.makeNameToVar(action1, "NewName"));
        assertEquals("NewName", generator.makeNameToVar(action2, "NewName"));
    }

    @Test
    public void testGenerationRecursiveNames() {
        VariableNameGenerator generator = new VariableNameGenerator(tree, true);

        assertEquals("SubMenu1ÖôóòAction1", generator.makeNameToVar(action1));
        assertEquals("OverrideAbc", generator.makeNameToVar(action2));

        assertEquals("SubMenu1ÖôóòNewName", generator.makeNameToVar(action1, "NewName"));
        assertEquals("OverrideÖôóòNewName", generator.makeNameToVar(action2, "NewName"));

        assertEquals("SubMenu1Öôóò", generator.makeNameToVar(sub1));
        assertEquals("OverrideÖôóò", generator.makeNameToVar(sub2));

        assertEquals("NewName", generator.makeNameToVar(sub1, "NewName"));
        assertEquals("NewName", generator.makeNameToVar(sub2, "NewName"));
    }

    @Test
    public void testDoubleRecursiveNames() {
        VariableNameGenerator generator = new VariableNameGenerator(tree, true);

        assertEquals("OverrideÖôóòSub3", generator.makeNameToVar(sub3));
        assertEquals("OverrideÖôóòSub3Action3", generator.makeNameToVar(action3));

        assertEquals("OverrideÖôóòNewSubName", generator.makeNameToVar(sub3, "New Sub Name"));
        assertEquals("OverrideÖôóòSub3LightsCamera", generator.makeNameToVar(action3, "Lights Camera"));
    }
}