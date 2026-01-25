package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.arduino.MenuItemToEmbeddedGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StaticMenuTreeCodeGeneratorImpl implements MenuTreeCodeGenerator {
    private Collection<BuildStructInitializer> menusInOrder;

    @Override
    public void initialise(MenuTree tree) throws TcMenuConversionException {
        menusInOrder = generateMenusInOrder(tree);
    }

    @Override
    public List<HeaderDefinition> headersToGenerate() {
        return menusInOrder.stream().map(BuildStructInitializer::getHeaderRequirements)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String getRootMenuCode() {
        return "";
    }

    @Override
    public String getHeaderMenuCode() {
        return "";
    }

    protected Collection<BuildStructInitializer> generateMenusInOrder(MenuTree menuTree) throws TcMenuConversionException {
        List<MenuItem> root = menuTree.getMenuItems(MenuTree.ROOT);
        List<List<BuildStructInitializer>> itemsInOrder = renderMenu(menuTree, root);
        Collections.reverse(itemsInOrder);
        return itemsInOrder.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    protected List<List<BuildStructInitializer>> renderMenu(MenuTree menuTree, Collection<MenuItem> itemsColl) throws TcMenuConversionException {
        ArrayList<MenuItem> items = new ArrayList<>(itemsColl);
        List<List<BuildStructInitializer>> itemsInOrder = new ArrayList<>(100);
        for (int i = 0; i < items.size(); i++) {

            MenuItem item = items.get(i);
            if (item.hasChildren()) {
                int nextIdx = i + 1;
                String nextSub = (nextIdx < items.size()) ? menuNameFor(items.get(nextIdx)) : MenuItemToEmbeddedGenerator.CPP_NULL_PTR;

                List<MenuItem> childItems = menuTree.getMenuItems(item);
                String nextChild = (!childItems.isEmpty()) ? menuNameFor(childItems.getFirst()) : MenuItemToEmbeddedGenerator.CPP_NULL_PTR;
                itemsInOrder.add(MenuItemHelper.visitWithResult(item,
                                new MenuItemToEmbeddedGenerator(menuNameFor(item), nextSub, nextChild,
                                        false, localeHandler, menuTree))
                        .orElse(Collections.emptyList()));
                itemsInOrder.addAll(renderMenu(menuTree, childItems));
            } else {
                int nextIdx = i + 1;
                Object defVal = MenuItemHelper.getValueFor(item, menuTree, MenuItemHelper.getDefaultFor(item));
                String next = (nextIdx < items.size()) ? menuNameFor(items.get(nextIdx)) : MenuItemToEmbeddedGenerator.CPP_NULL_PTR;
                itemsInOrder.add(MenuItemHelper.visitWithResult(item,
                                new MenuItemToEmbeddedGenerator(menuNameFor(item), next, null,
                                        toEmbeddedCppValue(item, defVal), localeHandler, menuTree))
                        .orElse(Collections.emptyList()));
            }
        }
        return itemsInOrder;
    }

}
