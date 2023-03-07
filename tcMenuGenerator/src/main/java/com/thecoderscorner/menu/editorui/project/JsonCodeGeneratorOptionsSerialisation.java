package com.thecoderscorner.menu.editorui.project;

import com.google.gson.*;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.*;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;

import java.lang.reflect.Type;
import java.util.*;

import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.ALL_TO_CURRENT;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.ALL_TO_SRC;

public class JsonCodeGeneratorOptionsSerialisation {
    public static final String JSON_PLATFORM = "embeddedPlatform";
    public static final String LAST_DISPLAY_UUID = "lastDisplayUuid";
    public static final String LAST_INPUT_UUID = "lastInputUuid";
    public static final String LAST_REMOTE_UUID_LEGACY = "lastRemoteUuid";
    public static final String LAST_REMOTES_UUID = "lastRemoteUuids";
    public static final String LAST_THEME_UUID = "lastThemeUuid";
    public static final String JSON_APP_UUID = "applicationUUID";
    public static final String JSON_APP_NAME = "applicationName";
    public static final String JSON_RECURSIVE_NAMING = "namingRecursive";
    public static final String JSON_SAVE_SRC_LEGACY = "saveToSrc";
    public static final String JSON_SAVE_LOCATION = "saveLocation";
    public static final String JSON_USE_CPP_MAIN = "useCppMain";
    public static final String JSON_SIZE_EEPROM = "usingSizedEEPROMStorage";
    public static final String JSON_APP_IS_MODULAR = "appIsModular";
    public static final String JSON_APP_NAMESPACE = "packageNamespace";
    public static final String JSON_EEPROM_DEFINITION = "eepromDefinition";
    public static final String JSON_AUTH_DEFINITION = "authenticatorDefinition";
    public static final String JSON_PROJECT_EXPANDERS = "projectIoExpanders";
    public static final String JSON_MENU_IN_MENU_COLL = "menuInMenuCollection";
    public static final String JSON_LAST_PROPERTIES = "lastProperties";

    private final EmbeddedPlatforms platforms;

    public JsonCodeGeneratorOptionsSerialisation(EmbeddedPlatforms platforms) {
        this.platforms = platforms;
    }

    public JsonDeserializer<CodeGeneratorOptions> getDeserialiser() { return new CodeGeneratorOptionsDeseriailiser(); }
    public JsonSerializer<CodeGeneratorOptions> getSerialiser() { return new CodeGeneratorOptionsSeriailiser(); }

    class CodeGeneratorOptionsDeseriailiser implements JsonDeserializer<CodeGeneratorOptions> {
        @Override
        public CodeGeneratorOptions deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            var builder = new CodeGeneratorOptionsBuilder();
            var jsonObj = jsonElement.getAsJsonObject();
            var platformText = optionalElement(jsonObj, JSON_PLATFORM, EmbeddedPlatform.ARDUINO_AVR.getBoardId());
            builder.withPlatform(platforms.getEmbeddedPlatformFromId(platformText))
                    .withAppName(optionalElement(jsonObj, JSON_APP_NAME, "App"))
                    .withNewId(UUID.fromString(jsonObj.get(JSON_APP_UUID).getAsString()))
                    .withRecursiveNaming(optionalElement(jsonObj, JSON_RECURSIVE_NAMING, false))
                    .withCppMain(optionalElement(jsonObj, JSON_USE_CPP_MAIN, false))
                    .withUseSizedEEPROMStorage(optionalElement(jsonObj, JSON_SIZE_EEPROM, false))
                    .withPackageNamespace(optionalElement(jsonObj, JSON_APP_NAMESPACE, ""))
                    .withModularApp(optionalElement(jsonObj, JSON_APP_IS_MODULAR, false));

            populatePluginsFromJson(builder, jsonObj);
            populateEepromAuthAndExpanders(builder, jsonObj);

            if(jsonObj.has(JSON_SAVE_SRC_LEGACY)) {
                builder.withSaveLocation(jsonObj.get(JSON_SAVE_SRC_LEGACY).getAsBoolean() ? ALL_TO_SRC : ALL_TO_CURRENT);
            } else if(jsonObj.has(JSON_SAVE_LOCATION)){
                builder.withSaveLocation(ProjectSaveLocation.valueOf(jsonObj.get(JSON_SAVE_LOCATION).getAsString()));
            }

            if(jsonObj.has(JSON_MENU_IN_MENU_COLL)) {
                builder.withMenuInMenu(ctx.deserialize(jsonObj.get(JSON_MENU_IN_MENU_COLL), MenuInMenuCollection.class));
            }

            if(jsonObj.has(JSON_LAST_PROPERTIES)) {
                var arr = jsonObj.get(JSON_LAST_PROPERTIES).getAsJsonArray();
                var props = new ArrayList<CreatorProperty>(arr.size() + 10);
                for (int i = 0; i < arr.size(); i++) {
                    var obj = arr.get(i).getAsJsonObject();
                    props.add(new CreatorProperty(
                            obj.get("name").getAsString(),
                            obj.get("latestValue").getAsString(),
                            SubSystem.valueOf(obj.get("subsystem").getAsString())
                    ));
                }
                builder.withProperties(props);
            }

            return builder.codeOptions();
        }

        private boolean optionalElement(JsonObject jsonObj, String tag, boolean def) {
            if(!jsonObj.has(tag)) return def;
            return jsonObj.get(tag).getAsBoolean();
        }
        private String optionalElement(JsonObject jsonObj, String tag, String def) {
            if(!jsonObj.has(tag)) return def;
            return jsonObj.get(tag).getAsString();
        }

        private void populateEepromAuthAndExpanders(CodeGeneratorOptionsBuilder builder, JsonObject jsonObj) {
            if(jsonObj.has(JSON_EEPROM_DEFINITION)) {
                builder.withEepromDefinition(EepromDefinition.readFromProject(jsonObj.get(JSON_EEPROM_DEFINITION).getAsString()));
            }
            if(jsonObj.has(JSON_AUTH_DEFINITION)) {
                builder.withAuthenticationDefinition(AuthenticatorDefinition.readFromProject(jsonObj.get(JSON_AUTH_DEFINITION).getAsString()));
            }

            if(jsonObj.has(JSON_PROJECT_EXPANDERS)) {
                var arr = jsonObj.get(JSON_PROJECT_EXPANDERS).getAsJsonArray();
                var list = new ArrayList<IoExpanderDefinition>();
                for(int i=0; i<arr.size(); i++) {
                    IoExpanderDefinition.fromString(arr.get(i).getAsString()).ifPresent(list::add);
                }
                builder.withExpanderDefinitions(new IoExpanderDefinitionCollection(list));
            }
        }

        private void populatePluginsFromJson(CodeGeneratorOptionsBuilder builder, JsonObject jsonObj) {
            var remotesList = new HashSet<String>();
            if(jsonObj.has(LAST_REMOTE_UUID_LEGACY)) {
                remotesList.add(jsonObj.get(LAST_REMOTE_UUID_LEGACY).getAsString());
            }
            if(jsonObj.has(LAST_REMOTES_UUID)) {
                var arr = jsonObj.get(LAST_REMOTES_UUID).getAsJsonArray();
                for(int i=0; i<arr.size(); i++) {
                    remotesList.add(arr.get(i).getAsString());
                }
            }
            builder.withRemotes(List.copyOf(remotesList))
                    .withDisplay(optionalElement(jsonObj, LAST_DISPLAY_UUID, ""))
                    .withInput(optionalElement(jsonObj, LAST_INPUT_UUID, ""))
                    .withTheme(optionalElement(jsonObj, LAST_THEME_UUID, ""));
        }
    }

    class CodeGeneratorOptionsSeriailiser implements JsonSerializer<CodeGeneratorOptions> {
        @Override
        public JsonElement serialize(CodeGeneratorOptions opts, Type type, JsonSerializationContext ctx) {
            var obj = new JsonObject();
            obj.addProperty(JSON_PLATFORM, opts.getEmbeddedPlatform().getBoardId());
            obj.addProperty(LAST_DISPLAY_UUID, opts.getLastDisplayUuid());
            obj.addProperty(LAST_INPUT_UUID, opts.getLastInputUuid());
            obj.add(LAST_REMOTES_UUID, serializeRemotes(opts.getLastRemoteCapabilitiesUuids()));
            obj.addProperty(LAST_THEME_UUID, opts.getLastThemeUuid());
            obj.addProperty(JSON_APP_UUID, opts.getApplicationUUID().toString());
            obj.addProperty(JSON_APP_NAME, opts.getApplicationName());
            obj.add(JSON_LAST_PROPERTIES, serializeLastProperties(opts.getLastProperties()));
            obj.addProperty(JSON_RECURSIVE_NAMING, opts.isNamingRecursive());
            obj.addProperty(JSON_USE_CPP_MAIN, opts.isUseCppMain());
            obj.addProperty(JSON_SAVE_LOCATION, opts.getSaveLocation().name());
            obj.addProperty(JSON_SIZE_EEPROM, opts.isUsingSizedEEPROMStorage());
            obj.addProperty(JSON_EEPROM_DEFINITION, opts.getEepromDefinition().writeToProject());
            obj.addProperty(JSON_AUTH_DEFINITION, opts.getAuthenticatorDefinition().writeToProject());
            obj.add(JSON_PROJECT_EXPANDERS, serializeExpanders(opts.getExpanderDefinitions().getAllExpanders()));
            obj.add(JSON_MENU_IN_MENU_COLL, ctx.serialize(opts.getMenuInMenuCollection()));
            obj.addProperty(JSON_APP_NAMESPACE, opts.getPackageNamespace());
            obj.addProperty(JSON_APP_IS_MODULAR, opts.isModularApp());
            return obj;
        }

        private JsonElement serializeExpanders(Collection<IoExpanderDefinition> allExpanders) {
            var arr = new JsonArray();
            for(var rem : allExpanders) {
                arr.add(rem.toString());
            }
            return arr;
        }

        private JsonElement serializeRemotes(List<String> lastRemoteCapabilitiesUuids) {
            var arr = new JsonArray();
            for(var rem : lastRemoteCapabilitiesUuids) {
                arr.add(rem);
            }
            return arr;
        }

        private JsonArray serializeLastProperties(List<CreatorProperty> lastProperties) {
            var arr = new JsonArray();
            for(var prop : lastProperties) {
                var obj = new JsonObject();
                obj.addProperty("name", prop.getName());
                obj.addProperty("latestValue", prop.getLatestValue());
                obj.addProperty("subsystem", prop.getSubsystem().toString());
                arr.add(obj);
            }
            return arr;
        }
    }

}
