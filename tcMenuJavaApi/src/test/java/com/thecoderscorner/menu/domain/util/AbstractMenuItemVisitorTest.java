/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;
import org.junit.Test;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.domain.DomainFixtures.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AbstractMenuItemVisitorTest {

    @Test
    public void testAnyItemCall() {
        SubMenuItem subItem = aSubMenu("123", 1);

        AbstractMenuItemVisitor<Integer> visitor = new AbstractMenuItemVisitor<>() {
            @Override
            public void anyItem(MenuItem item) {
                setResult(1);
            }
        };
        subItem.accept(visitor);
        assertThat(visitor.getResult().orElse(0), is(1));
    }

    @Test
    public void testAllOutrightCall() {
        AbstractMenuItemVisitor<String> visitor = new AbstractMenuItemVisitor<>() {
            @Override
            public void visit(SubMenuItem item) {
                setResult(getResult().orElse("") + "1");
            }

            @Override
            public void visit(AnalogMenuItem item) {
                setResult(getResult().orElse("") + "2");
            }

            @Override
            public void visit(EnumMenuItem item) {
                setResult(getResult().orElse("") + "3");
            }

            @Override
            public void visit(BooleanMenuItem item) {
                setResult(getResult().orElse("") + "4");
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                setResult(getResult().orElse("") + "5");
            }

            @Override
            public void visit(FloatMenuItem item) {
                setResult(getResult().orElse("") + "6");
            }

            @Override
            public void visit(ActionMenuItem item) {
                setResult(getResult().orElse("") + "7");
            }

            @Override
            public void visit(RuntimeListMenuItem item) {
                setResult(getResult().orElse("") + "8");
            }

            @Override
            public void visit(EditableLargeNumberMenuItem item) {
                setResult(getResult().orElse("") + "9");
            }
        };

        AnalogMenuItem analog = anAnalogItem("123", 1);
        SubMenuItem subItem = aSubMenu("321", 2);
        EnumMenuItem enumItem = anEnumItem("111", 3);
        BooleanMenuItem boolItem = aBooleanMenu("222", 2, BooleanNaming.TRUE_FALSE);
        EditableTextMenuItem textItem = aTextMenu("123", 2);
        FloatMenuItem floatItem = aFloatMenu("123", 223);
        ActionMenuItem actionItem = anActionMenu("123", 347);
        RuntimeListMenuItem runList = aRuntimeListMenu("1232", 153, 1);
        EditableLargeNumberMenuItem numItem = aLargeNumber("1232", 153, 4, true);

        subItem.accept(visitor);
        analog.accept(visitor);
        enumItem.accept(visitor);
        boolItem.accept(visitor);
        textItem.accept(visitor);
        floatItem.accept(visitor);
        actionItem.accept(visitor);
        runList.accept(visitor);
        numItem.accept(visitor);

        assertThat(visitor.getResult().orElse(""), is("123456789"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testExceptionOnUnimplementedCase() {
        AbstractMenuItemVisitor<String> visitor = new AbstractMenuItemVisitor<>() {
            // intentionally empty
        };
        SubMenuItem item = aSubMenu("123", 1);
        item.accept(visitor);
    }
}