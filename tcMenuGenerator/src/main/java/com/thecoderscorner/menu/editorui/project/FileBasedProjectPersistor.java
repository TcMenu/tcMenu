/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PersistedMenu;

import java.io.*;
import java.util.List;
import java.util.Set;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;
import static java.lang.System.Logger.Level.INFO;

/**
 * An implementation of the ProjectPersisor that is based on GSON based JSON library.
 * It saves a human readable JSON file containing all the settings, and can load back
 * equally.
 * <p>
 * The file open / save dialog is based on JAva FX.
 */
public class FileBasedProjectPersistor implements ProjectPersistor {


    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final JsonMenuItemSerializer serializer;
    private final EmbeddedPlatforms platforms;

    public FileBasedProjectPersistor(EmbeddedPlatforms platforms) {
        this.platforms = platforms;
        var optsSerializer = new JsonCodeGeneratorOptionsSerialisation(platforms);

        serializer = new JsonMenuItemSerializer((gsonBuilder) ->
                gsonBuilder.registerTypeAdapter(CodeGeneratorOptions.class, optsSerializer.getDeserialiser())
                           .registerTypeAdapter(CodeGeneratorOptions.class, optsSerializer.getSerialiser()));
    }

    @Override
    public MenuTreeWithCodeOptions open(String fileName) throws IOException {
        logger.log(INFO, "Open file " + fileName);

        try (Reader reader = new BufferedReader(new FileReader(fileName))) {
            PersistedProject prj = serializer.getGson().fromJson(reader, PersistedProject.class);
            MenuTree tree = new MenuTree();
            prj.getItems().forEach((item) -> {
                tree.addMenuItem(fromParentId(tree, item.getParentId()), item.getItem());
                if (item.getDefaultValue() != null && JsonMenuItemSerializer.checkItemValueCanPersist(item)) {
                    MenuItemHelper.setMenuState(item.getItem(), item.getDefaultValue(), tree);
                }
            });
            return new MenuTreeWithCodeOptions(tree, prj.getCodeOptions(), prj.getProjectName());
        }
    }

    private SubMenuItem fromParentId(MenuTree tree, int parentId) {
        Set<MenuItem> allSubMenus = tree.getAllSubMenus();
        for (MenuItem item : allSubMenus) {
            if (item.getId() == parentId)
                return asSubMenu(item);
        }
        return MenuTree.ROOT;
    }

    @Override
    public void save(String fileName, String desc, MenuTree tree, CodeGeneratorOptions options, LocaleMappingHandler localeHandler) throws IOException {
        logger.log(INFO, "Save file starting for: " + fileName);

        // make sure we save out any in flight changes to internationalisation files.
        localeHandler.saveChanges();

        List<PersistedMenu> itemsInOrder = serializer.populateListInOrder(MenuTree.ROOT, tree, true);

        try (Writer writer = new BufferedWriter(new FileWriter(fileName))) {
            String user = System.getProperty("user.name");
            serializer.getGson().toJson(new PersistedProject(desc, user, itemsInOrder, options), writer);
        }
    }

    @Override
    public List<PersistedMenu> copyTextToItems(String items) {
        return serializer.copyTextToItems(items);
    }

    @Override
    public String itemsToCopyText(MenuItem startingPoint, MenuTree tree) {
        return serializer.itemsToCopyText(startingPoint, tree);
    }

}