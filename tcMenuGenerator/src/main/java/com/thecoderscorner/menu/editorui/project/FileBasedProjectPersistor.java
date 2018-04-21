package com.thecoderscorner.menu.editorui.project;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;

public class FileBasedProjectPersistor implements ProjectPersistor {
    public static final String ANALOG_PERSIST_TYPE = "analogItem";
    public static final String ENUM_PERSIST_TYPE = "enumItem";
    public static final String SUB_PERSIST_TYPE = "subMenu";
    public static final String BOOLEAN_PERSIST_TYPE = "boolItem";

    private static final String PARENT_ID = "parentId";
    private static final String TYPE_ID = "type";
    private static final String ITEM_ID = "item";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;

    public FileBasedProjectPersistor() {
        this.gson = makeGsonProcessor();
    }

    @Override
    public MenuTree open(String fileName) throws IOException {
        logger.info("Open file " + fileName);

        try(Reader reader = new BufferedReader(new FileReader(fileName))) {
            PersistedProject prj = gson.fromJson(reader, PersistedProject.class);
            MenuTree tree = new MenuTree();
            prj.getItems().forEach((item) -> tree.addMenuItem(fromParentId(tree, item.getParentId()), item.getItem()));
            return tree;
        }
    }

    private SubMenuItem fromParentId(MenuTree tree, int parentId) {
        Set<MenuItem> allSubMenus = tree.getAllSubMenus();
        for (MenuItem item : allSubMenus) {
            if(item.getId() == parentId)
                return asSubMenu(item);
        }
        return MenuTree.ROOT;
    }

    @Override
    public void save(String fileName, MenuTree tree) throws IOException {
        logger.info("Save file starting for: " + fileName);

        List<PersistedMenu> itemsToStoreInOrder = populateListInOrder(MenuTree.ROOT, tree);

        try(Writer writer = new BufferedWriter(new FileWriter(fileName))) {
            gson.toJson(new PersistedProject(fileName, "author", Instant.now(), itemsToStoreInOrder), writer);
        }
    }

    private List<PersistedMenu> populateListInOrder(SubMenuItem node, MenuTree menuTree) {
        ArrayList<PersistedMenu> list = new ArrayList<>();
        ImmutableList<MenuItem> items = menuTree.getMenuItems(node);
        for (MenuItem item : items) {
            list.add(new PersistedMenu(node, item));
            if(item.hasChildren()) {
                list.addAll(populateListInOrder(MenuItemHelper.asSubMenu(item), menuTree));
            }
        }
        return list;
    }

    private Gson makeGsonProcessor() {
        ArrayList<PersistedMenu> example = new ArrayList<>();

        return new GsonBuilder()
                .registerTypeAdapter(example.getClass(), new MenuItemSerialiser())
                .registerTypeAdapter(example.getClass(), new MenuItemDeserialiser())
                .create();
    }

    class MenuItemSerialiser implements JsonSerializer<ArrayList<PersistedMenu>> {

        @Override
        public JsonElement serialize(ArrayList<PersistedMenu> src, Type type, JsonSerializationContext ctx) {
            if(src == null) {
                return null;
            }
            JsonArray arr = new JsonArray();
            src.forEach((itm) -> {
                JsonObject ele = new JsonObject();
                ele.addProperty(PARENT_ID, itm.getParentId() );
                ele.addProperty(TYPE_ID, itm.getType());
                ele.add(ITEM_ID, ctx.serialize(itm.getItem()));
                arr.add(ele);
            });
            return arr;
        }
    }

    class MenuItemDeserialiser implements JsonDeserializer<ArrayList<PersistedMenu>> {

        private Map<String, Class<? extends MenuItem>> mapOfTypes = Map.of(
                ENUM_PERSIST_TYPE, EnumMenuItem.class,
                ANALOG_PERSIST_TYPE, AnalogMenuItem.class,
                BOOLEAN_PERSIST_TYPE, BooleanMenuItem.class,
                SUB_PERSIST_TYPE, SubMenuItem.class);

        @Override
        public ArrayList<PersistedMenu> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            ArrayList<PersistedMenu> list = new ArrayList<>();
            JsonArray ja = jsonElement.getAsJsonArray();

            ja.forEach(ele -> {
                String ty = ele.getAsJsonObject().get(TYPE_ID).getAsString();
                int parentId = ele.getAsJsonObject().get("parentId").getAsInt();
                Class<? extends MenuItem> c = mapOfTypes.get(ty);
                if(c==null) throw new JsonParseException("Unknown type " + type);
                MenuItem item = ctx.deserialize(ele.getAsJsonObject().getAsJsonObject(ITEM_ID), c);

                PersistedMenu m = new PersistedMenu();
                m.setItem(item);
                m.setParentId(parentId);
                m.setType(ty);

                list.add(m);
            });

            return list;
        }
    }
}
