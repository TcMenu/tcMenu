/*
 * Copyright (c)  2016-2021 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.*;
import java.util.stream.Collectors;

public class VariableNameGenerator {
    private final MenuTree menuTree;
    private final boolean recursiveNaming;
    private final Set<Integer> uncommittedItems;

    public VariableNameGenerator(MenuTree menuTree, boolean recursiveNaming) {
        this.menuTree = menuTree;
        this.recursiveNaming = recursiveNaming;
        this.uncommittedItems = Set.of();
    }

    public VariableNameGenerator(MenuTree menuTree, boolean recursiveNaming, Set<Integer> uncommittedItems) {
        this.menuTree = menuTree;
        this.recursiveNaming = recursiveNaming;
        this.uncommittedItems = uncommittedItems;
    }

    public Set<Integer> getUncommittedItems() {
        return uncommittedItems;
    }

    public String makeNameToVar(MenuItem item) {
        return makeNameToVar(item, null);
    }

    public String makeNameToVar(MenuItem item, String newName) {
        // shortcut for null..
        if (item == null) return "NULL";
        if (newName == null && item.getVariableName() != null) return item.getVariableName();

        // shortcut simple naming.
        var parent = menuTree.findParent(item);
        if (!recursiveNaming || parent == null || parent.equals(MenuTree.ROOT)) {
            return makeNameFromVariable((newName != null) ? newName : item.getName());
        }

        // get all submenu names together.
        var items = new ArrayList<String>();
        var par = item;
        var name = (newName != null) ? newName : par.getName();
        while (par != null && !par.equals(MenuTree.ROOT)) {
            items.add(makeNameFromVariable(name));
            par = menuTree.findParent(par);
            if(par != null) {
                name = StringHelper.isStringEmptyOrNull(par.getVariableName()) ? par.getName() : par.getVariableName();
            }
        }

        // reverse and then join.
        Collections.reverse(items);
        return String.join("", items);

    }

    public String makeRtFunctionName(MenuItem item) {
        return "fn" + makeNameToVar(item) + "RtCall";
    }

    protected String makeNameFromVariable(String name) {
        Collection<String> parts = Arrays.asList(name.split("[\\p{P}\\p{Z}\\t\\r\\n\\v\\f^]+"));
        return parts.stream().map(this::capitaliseFirst).collect(Collectors.joining());
    }

    protected String capitaliseFirst(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

}
