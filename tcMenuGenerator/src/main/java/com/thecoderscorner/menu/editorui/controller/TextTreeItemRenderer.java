/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.text.NumberFormat;
import java.util.List;

public class TextTreeItemRenderer {

    private final MenuTree tree;

    public TextTreeItemRenderer(MenuTree tree) {
        this.tree = tree;
    }

    public String getTreeAsText() {
        List<MenuItem> list = tree.getMenuItems(MenuTree.ROOT);

        if(list == null) {
            return "List is empty";
        }

        StringBuilder sb = renderMenuLevel(list, 0);
        return sb.toString();
    }

    private StringBuilder renderMenuLevel(List<MenuItem> list, int level) {
        StringBuilder sb = new StringBuilder();
        list.forEach((item) -> {
            sb.append(MenuItemHelper.visitWithResult(item, new ItemValueRenderer(level)).orElse("Empty"));
            sb.append("\n");
            if(item.hasChildren()) {
                sb.append(renderMenuLevel(tree.getMenuItems(item), level + 1));
            }
        });
        return sb;
    }

    static class ItemValueRenderer extends AbstractMenuItemVisitor<String> {
        private final String spaces = "                              ";
        private int level;

        public ItemValueRenderer(int level) {
            this.level = level;
        }

        @Override
        public void visit(AnalogMenuItem item) {
            StringBuilder sb = createBuilderWithName(item.getName());
            String it = Integer.toString((item.getMaxValue() + item.getOffset()) / item.getDivisor());
            it += item.getUnitName();
            sb.replace(spaces.length() - it.length(), spaces.length(), it);

            setResult(sb.toString());
        }

        @Override
        public void visit(EditableTextMenuItem item) {
            StringBuilder sb = createBuilderWithName(item.getName());
            String it;
            if(item.getItemType() == EditItemType.IP_ADDRESS) {
                it = "127.0.0.1";
            }
            else if(item.getItemType() == EditItemType.PLAIN_TEXT) {
                it = StringHelper.repeat("A", Math.max(1, item.getTextLength()));
            }
            else if(item.getItemType() == EditItemType.TIME_12H) {
                it = "12:03:35P";
            }
            else if(item.getItemType() == EditItemType.TIME_24H) {
                it = "23:56:53";
            }
            else if(item.getItemType() == EditItemType.TIME_24_HUNDREDS) {
                it = "23:56:53.97";
            }
            else it ="";
            sb.replace(spaces.length() - it.length(), spaces.length(), it);

            setResult(sb.toString());
        }

        private StringBuilder createBuilderWithName(String name) {
            StringBuilder sb = new StringBuilder(spaces);
            if(name.length() != 0) {
                int start = (level * 2) + 1;
                sb.replace(start, start + name.length() + 1, name);
            }
            return sb;
        }

        @Override
        public void visit(EnumMenuItem item) {
            StringBuilder sb = createBuilderWithName(item.getName());
            String it = "None";
            if(!item.getEnumEntries().isEmpty()) {
                it = item.getEnumEntries().get(0);
            }
            sb.replace(spaces.length() - it.length(), spaces.length(), it);

            setResult(sb.toString());
        }

        @Override
        public void visit(BooleanMenuItem item) {
            StringBuilder sb = createBuilderWithName(item.getName());
            String val ;
            switch(item.getNaming()) {
                case ON_OFF:
                    val = "ON";
                    break;
                case YES_NO:
                    val = "YES";
                    break;
                case TRUE_FALSE:
                default:
                    val = "TRUE";
                    break;
            }
            sb.replace(spaces.length() - val.length(), spaces.length(), val);
            setResult(sb.toString());
        }

        @Override
        public void visit(ActionMenuItem item) {
            StringBuilder sb = createBuilderWithName(item.getName());
            setResult(sb.toString());
        }

        @Override
        public void visit(FloatMenuItem item) {
            StringBuilder sb = createBuilderWithName(item.getName());
            NumberFormat fmt = NumberFormat.getInstance();
            fmt.setGroupingUsed(false);
            fmt.setMinimumFractionDigits(item.getNumDecimalPlaces());
            fmt.setMaximumFractionDigits(item.getNumDecimalPlaces());
            String s = fmt.format(-12345.123456);
            sb.replace(spaces.length() - s.length(), spaces.length(), s);
            setResult(sb.toString());
        }

        @Override
        public void visit(RuntimeListMenuItem item) {
            StringBuilder sb = createBuilderWithName(item.getName());
            String s = ">>>";
            sb.replace(spaces.length() - s.length(), spaces.length(), s);
            setResult(sb.toString());
        }

        @Override
        public void visit(SubMenuItem item) {
            StringBuilder sb = createBuilderWithName(item.getName());
            sb.replace(spaces.length() - 3, spaces.length(), ">>>");
            setResult(sb.toString());
        }
    }
}
