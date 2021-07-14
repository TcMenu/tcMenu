/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.google.gson.*;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * An implementation of the ProjectPersisor that is based on GSON based JSON library.
 * It saves a human readable JSON file containing all the settings, and can load back
 * equally.
 *
 * The file open / save dialog is based on JAva FX.
 */
public class FileBasedProjectPersistor implements ProjectPersistor {
    public static final String ANALOG_PERSIST_TYPE = "analogItem";
    public static final String ENUM_PERSIST_TYPE = "enumItem";
    public static final String SUB_PERSIST_TYPE = "subMenu";
    public static final String ACTION_PERSIST_TYPE = "actionMenu";
    public static final String RUNTIME_LIST_PERSIST_TYPE = "runtimeList";
    public static final String CUSTOM_ITEM_PERSIST_TYPE = "customBuildItem";
    public static final String BOOLEAN_PERSIST_TYPE = "boolItem";
    public static final String TEXT_PERSIST_TYPE = "textItem";
    public static final String FLOAT_PERSIST_TYPE = "floatItem";
    public static final String RUNTIME_LARGE_NUM_PERSIST_TYPE = "largeNumItem";
    public static final String SCROLL_CHOICE_PERSIST_TYPE = "scrollItem";
    public static final String RGB32_COLOR_PERSIST_TYPE = "rgbItem";
    public static final String TCMENU_COPY_PREFIX = "tcMenuCopy:";

    private static final String PARENT_ID = "parentId";
    private static final String TYPE_ID = "type";
    private static final String ITEM_ID = "item";
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Gson gson;

    public FileBasedProjectPersistor() {
        this.gson = makeGsonProcessor();
    }

    @Override
    public MenuTreeWithCodeOptions open(String fileName) throws IOException {
        logger.log(INFO, "Open file " + fileName);

        try (Reader reader = new BufferedReader(new FileReader(fileName))) {
            PersistedProject prj = gson.fromJson(reader, PersistedProject.class);
            MenuTree tree = new MenuTree();
            prj.getItems().forEach((item) -> tree.addMenuItem(fromParentId(tree, item.getParentId()), item.getItem()));
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

        List<PersistedMenu> itemsInOrder = populateListInOrder(MenuTree.ROOT, tree);

        try (Writer writer = new BufferedWriter(new FileWriter(fileName))) {
            String user = System.getProperty("user.name");
            gson.toJson(new PersistedProject(desc, user, itemsInOrder, options), writer);
        }
    }

    private List<PersistedMenu> populateListInOrder(SubMenuItem node, MenuTree menuTree) {
        ArrayList<PersistedMenu> list = new ArrayList<>();
        List<MenuItem> items = menuTree.getMenuItems(node);
        for (MenuItem item : items) {
            list.add(new PersistedMenu(node, item));
            if (item.hasChildren()) {
                list.addAll(populateListInOrder(MenuItemHelper.asSubMenu(item), menuTree));
            }
        }
        return list;
    }

    public String itemsToCopyText(MenuItem startingPoint, MenuTree tree) {
        List<PersistedMenu> items;
        if(startingPoint instanceof SubMenuItem) {
            items = populateListInOrder((SubMenuItem) startingPoint, tree);
        }
        else {
            items = new ArrayList<>(); // has to be an array list.
            items.add(new PersistedMenu(tree.findParent(startingPoint), startingPoint));
        }
        return TCMENU_COPY_PREFIX + gson.toJson(items);
    }

    @SuppressWarnings("unchecked")
    public List<PersistedMenu> copyTextToItems(String items) {
        if(!items.startsWith(TCMENU_COPY_PREFIX)) return Collections.emptyList();
        var jsonStr = items.substring(TCMENU_COPY_PREFIX.length());
        return gson.fromJson(jsonStr, ArrayList.class);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Gson makeGsonProcessor() {
        ArrayList<PersistedMenu> example = new ArrayList<>();

        return new GsonBuilder()
                .registerTypeAdapter(EepromDefinition.class, new EepromDefinitionSerialiser())
                .registerTypeAdapter(EepromDefinition.class, new EepromDefinitionDeseriailiser())
                .registerTypeAdapter(AuthenticatorDefinition.class, new AuthDefinitionSerialiser())
                .registerTypeAdapter(AuthenticatorDefinition.class, new AuthDefinitionDeseriailiser())
                .registerTypeAdapter(example.getClass(), new MenuItemSerialiser())
                .registerTypeAdapter(example.getClass(), new MenuItemDeserialiser())
                .registerTypeAdapter(Instant.class, new CompatibleDateTimePersistor())
                .setPrettyPrinting()
                .create();
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

    static class MenuItemSerialiser implements JsonSerializer<ArrayList<PersistedMenu>> {

        @Override
        public JsonElement serialize(ArrayList<PersistedMenu> src, Type type, JsonSerializationContext ctx) {
            if (src == null) {
                return null;
            }
            JsonArray arr = new JsonArray();
            src.forEach((itm) -> {
                JsonObject ele = new JsonObject();
                ele.addProperty(PARENT_ID, itm.getParentId());
                ele.addProperty(TYPE_ID, itm.getType());
                ele.add(ITEM_ID, ctx.serialize(itm.getItem()));
                arr.add(ele);
            });
            return arr;
        }
    }

    class MenuItemDeserialiser implements JsonDeserializer<ArrayList<PersistedMenu>> {

        private final Map<String, Class<? extends MenuItem>> mapOfTypes = new HashMap<>();

        {
            mapOfTypes.put(ENUM_PERSIST_TYPE, EnumMenuItem.class);
            mapOfTypes.put(ANALOG_PERSIST_TYPE, AnalogMenuItem.class);
            mapOfTypes.put(BOOLEAN_PERSIST_TYPE, BooleanMenuItem.class);
            mapOfTypes.put(ACTION_PERSIST_TYPE, ActionMenuItem.class);
            mapOfTypes.put(TEXT_PERSIST_TYPE, EditableTextMenuItem.class);
            mapOfTypes.put(SUB_PERSIST_TYPE, SubMenuItem.class);
            mapOfTypes.put(RUNTIME_LIST_PERSIST_TYPE, RuntimeListMenuItem.class);
            mapOfTypes.put(RUNTIME_LARGE_NUM_PERSIST_TYPE, EditableLargeNumberMenuItem.class);
            mapOfTypes.put(CUSTOM_ITEM_PERSIST_TYPE, CustomBuilderMenuItem.class);
            mapOfTypes.put(SCROLL_CHOICE_PERSIST_TYPE, ScrollChoiceMenuItem.class);
            mapOfTypes.put(RGB32_COLOR_PERSIST_TYPE, Rgb32MenuItem.class);
            mapOfTypes.put(FLOAT_PERSIST_TYPE, FloatMenuItem.class);
        }

        @Override
        public ArrayList<PersistedMenu> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            ArrayList<PersistedMenu> list = new ArrayList<>();
            JsonArray ja = jsonElement.getAsJsonArray();

            ja.forEach(ele -> {
                String ty = ele.getAsJsonObject().get(TYPE_ID).getAsString();
                int parentId = ele.getAsJsonObject().get("parentId").getAsInt();
                Class<? extends MenuItem> c = mapOfTypes.get(ty);
                if (c != null) {
                    MenuItem item = ctx.deserialize(ele.getAsJsonObject().getAsJsonObject(ITEM_ID), c);
                    PersistedMenu m = new PersistedMenu();
                    m.setItem(item);
                    m.setParentId(parentId);
                    m.setType(ty);
                    list.add(m);
                } else {
                    logger.log(ERROR, "Item of type " + ty + " was not reloaded - skipping");
                }
            });

            return list;
        }
    }

    static class CompatibleDateTimePersistor implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant dt, Type type, JsonSerializationContext jsonSerializationContext) {
            var seconds = dt.getEpochSecond();
            var nanos = dt.getNano();
            JsonObject obj = new JsonObject();
            obj.add("seconds", new JsonPrimitive(seconds));
            obj.add("nanos", new JsonPrimitive(nanos));
            return obj;
        }

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var seconds = json.getAsJsonObject().get("seconds").getAsLong();
            var nanos = json.getAsJsonObject().get("nanos").getAsInt();
            return Instant.ofEpochSecond(seconds, nanos);
        }
    }
}