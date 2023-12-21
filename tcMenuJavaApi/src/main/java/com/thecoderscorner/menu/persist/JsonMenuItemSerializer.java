package com.thecoderscorner.menu.persist;

import com.google.gson.*;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.persist.PersistedMenu.*;
import static java.lang.System.Logger.Level.ERROR;

/**
 * Creates a menu serializer instance that can convert menu structures to and from JSON format. In the simplest case
 * just create a new instance of the class and you can use it to convert between formats.
 *
 * <pre>
 *     var jsonSerializer = new JsonMenuItemSerializer();
 *     var tree = jsonSerializer.newMenuTreeWithItems(textCopiedFromTcMenuDesigner);
 *     var json = jsonSerializer.itemsToCopyText(MenuTree.ROOT, tree);
 * </pre>
 */
public class JsonMenuItemSerializer {
    private final static System.Logger logger = System.getLogger(JsonMenuItemSerializer.class.getSimpleName());
    private static final String PARENT_ID = "parentId";
    private static final String TYPE_ID = "type";
    private static final String DEF_VALUE_ID = "defaultValue";
    private static final String ITEM_ID = "item";

    private final Gson gson;

    public JsonMenuItemSerializer() {
        this(null);
    }
    public JsonMenuItemSerializer(Consumer<GsonBuilder> builder) {
        this.gson = makeGsonProcessor(builder);
    }

    public Gson getGson() {
        return gson;
    }

    public List<PersistedMenu> populateListInOrder(SubMenuItem node, MenuTree menuTree, boolean processingRoot) {
        ArrayList<PersistedMenu> list = new ArrayList<>();
        List<MenuItem> items = menuTree.getMenuItems(node);
        if(!node.equals(MenuTree.ROOT) && processingRoot) {
            PersistedMenu sub = new PersistedMenu(menuTree.findParent(node), node);
            sub.setDefaultValue("false");
            list.add(sub);
        }
        for (MenuItem item : items) {
            PersistedMenu persistedMenu = new PersistedMenu(node, item);
            if(menuTree.getMenuState(item) != null) {
                persistedMenu.setDefaultValue(MenuItemHelper.getValueFor(item, menuTree, MenuItemHelper.getDefaultFor(item)).toString());
            }
            list.add(persistedMenu);
            if (item.hasChildren()) {
                list.addAll(populateListInOrder(MenuItemHelper.asSubMenu(item), menuTree, false));
            }
        }
        return list;
    }

    public String itemsToCopyText(MenuItem startingPoint, MenuTree tree) {
        List<PersistedMenu> items;
        if(startingPoint instanceof SubMenuItem) {
            items = populateListInOrder((SubMenuItem) startingPoint, tree, true);
        }
        else {
            items = new ArrayList<>(); // has to be an array list.
            PersistedMenu menu = new PersistedMenu(tree.findParent(startingPoint), startingPoint);
            menu.setDefaultValue(Objects.toString(MenuItemHelper.getValueFor(startingPoint, tree)));
            items.add(menu);
        }
        return TCMENU_COPY_PREFIX + gson.toJson(items);
    }

    @SuppressWarnings("unchecked")
    public List<PersistedMenu> copyTextToItems(String items) {
        if(!items.startsWith(TCMENU_COPY_PREFIX)) return Collections.emptyList();
        var jsonStr = items.substring(TCMENU_COPY_PREFIX.length());
        return gson.fromJson(jsonStr, ArrayList.class);
    }

    public MenuTree newMenuTreeWithItems(String tcMenuCopy) {
        var tree = new MenuTree();
        var items = copyTextToItems(tcMenuCopy);
        for (var item : items) {
            tree.addMenuItem(tree.getSubMenuById(item.getParentId()).orElse(MenuTree.ROOT), item.getItem());
            if(item.getDefaultValue() != null) {
                MenuItemHelper.setMenuState(item.getItem(), item.getDefaultValue(), tree);
            }
        }
        return tree;
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Gson makeGsonProcessor(Consumer<GsonBuilder> builderConsumer) {
        ArrayList<PersistedMenu> example = new ArrayList<>();

        var builder = new GsonBuilder();
        if(builderConsumer!=null) builderConsumer.accept(builder);
        return builder.registerTypeAdapter(example.getClass(), new MenuItemSerialiser())
                .registerTypeAdapter(example.getClass(), new MenuItemDeserialiser())
                .registerTypeAdapter(Instant.class, new CompatibleDateTimePersistor())
                .setPrettyPrinting()
                .create();
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
                if(checkItemValueCanPersist(itm)) {
                    ele.addProperty(DEF_VALUE_ID, itm.getDefaultValue());
                }
                ele.add(ITEM_ID, ctx.serialize(itm.getItem()));
                arr.add(ele);
            });
            return arr;
        }
    }


    /**
     * There are some menu types that should not have a default value, these generally don't have a value associated with
     * them that can be easily saved, such as lists, action items, builder items and submenus.
     * @param persistedMenu the item to check
     * @return true if the item value can be persisted, otherwise false
     */
    public static boolean checkItemValueCanPersist(PersistedMenu persistedMenu) {
        var item = persistedMenu.getItem();
        return !(item instanceof SubMenuItem || item instanceof ActionMenuItem || item instanceof RuntimeListMenuItem ||
                item instanceof CustomBuilderMenuItem);
    }

    static class MenuItemDeserialiser implements JsonDeserializer<ArrayList<PersistedMenu>> {

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
                String defVal = null;
                if(ele.getAsJsonObject().has(DEF_VALUE_ID)) {
                    defVal = ele.getAsJsonObject().get(DEF_VALUE_ID).getAsString();
                }
                Class<? extends MenuItem> c = mapOfTypes.get(ty);
                if (c != null) {
                    MenuItem item = ctx.deserialize(ele.getAsJsonObject().getAsJsonObject(ITEM_ID), c);
                    PersistedMenu m = new PersistedMenu();
                    m.setItem(item);
                    m.setParentId(parentId);
                    m.setDefaultValue(defVal);
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
