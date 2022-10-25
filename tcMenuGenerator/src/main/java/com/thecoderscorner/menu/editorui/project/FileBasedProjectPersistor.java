/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.google.gson.*;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import com.thecoderscorner.menu.persist.PersistedMenu;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;
import static java.lang.System.Logger.Level.INFO;

/**
 * An implementation of the ProjectPersisor that is based on GSON based JSON library.
 * It saves a human readable JSON file containing all the settings, and can load back
 * equally.
 *
 * The file open / save dialog is based on JAva FX.
 */
public class FileBasedProjectPersistor implements ProjectPersistor {

    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final JsonMenuItemSerializer serializer = new JsonMenuItemSerializer((gsonBuilder) ->
            gsonBuilder.registerTypeAdapter(EepromDefinition.class, new EepromDefinitionSerialiser())
                    .registerTypeAdapter(EepromDefinition.class, new EepromDefinitionDeseriailiser())
                    .registerTypeAdapter(AuthenticatorDefinition.class, new AuthDefinitionSerialiser())
                    .registerTypeAdapter(AuthenticatorDefinition.class, new AuthDefinitionDeseriailiser())
                    .registerTypeAdapter(IoExpanderDefinitionCollection.class, new IoExpanderDefinitionSerialiser())
                    .registerTypeAdapter(IoExpanderDefinitionCollection.class, new IoExpanderDefinitionDeseriailiser()));

    @Override
    public MenuTreeWithCodeOptions open(String fileName) throws IOException {
        logger.log(INFO, "Open file " + fileName);

        try (Reader reader = new BufferedReader(new FileReader(fileName))) {
            PersistedProject prj = serializer.getGson().fromJson(reader, PersistedProject.class);
            MenuTree tree = new MenuTree();
            prj.getItems().forEach((item) -> {
                tree.addMenuItem(fromParentId(tree, item.getParentId()), item.getItem());
                if(item.getDefaultValue() != null && JsonMenuItemSerializer.checkItemValueCanPersist(item)) {
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
    public void save(String fileName, String desc, MenuTree tree, CodeGeneratorOptions options) throws IOException {
        logger.log(INFO, "Save file starting for: " + fileName);

        List<PersistedMenu> itemsInOrder = serializer.populateListInOrder(MenuTree.ROOT, tree);

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

    static class EepromDefinitionSerialiser implements JsonSerializer<EepromDefinition> {
        @Override
        public JsonElement serialize(EepromDefinition eepromDefinition, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(eepromDefinition != null ? eepromDefinition.writeToProject() : "");
        }
    }

    static class EepromDefinitionDeseriailiser implements JsonDeserializer<EepromDefinition> {
        @Override
        public EepromDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return EepromDefinition.readFromProject(jsonElement.getAsString());
        }
    }

    static class AuthDefinitionSerialiser implements JsonSerializer<AuthenticatorDefinition> {
        @Override
        public JsonElement serialize(AuthenticatorDefinition authDefinition, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(authDefinition != null ? authDefinition.writeToProject() : "");
        }
    }

    static class AuthDefinitionDeseriailiser implements JsonDeserializer<AuthenticatorDefinition> {
        @Override
        public AuthenticatorDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return AuthenticatorDefinition.readFromProject(jsonElement.getAsString());
        }
    }

    static class IoExpanderDefinitionSerialiser implements JsonSerializer<IoExpanderDefinitionCollection> {
        @Override
        public JsonElement serialize(IoExpanderDefinitionCollection definitionCollection, Type type, JsonSerializationContext jsonSerializationContext) {
            var array = new JsonArray();
            for(var def : definitionCollection.getAllExpanders()) {
                array.add(def.toString());
            }
            return array;
        }
    }

    static class IoExpanderDefinitionDeseriailiser implements JsonDeserializer<IoExpanderDefinitionCollection> {
        @Override
        public IoExpanderDefinitionCollection deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            var list = new ArrayList<IoExpanderDefinition>();
            var jsonArray = jsonElement.getAsJsonArray();
            for(var def : jsonArray) {
                IoExpanderDefinition.fromString(def.getAsString()).ifPresent(list::add);
            }
            return new IoExpanderDefinitionCollection(list);
        }
    }

}