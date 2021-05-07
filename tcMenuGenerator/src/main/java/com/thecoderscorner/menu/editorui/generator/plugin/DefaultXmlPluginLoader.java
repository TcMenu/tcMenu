/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.generator.applicability.*;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.*;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.generator.validation.*;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.scene.image.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.*;

public class DefaultXmlPluginLoader implements CodePluginManager {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final EmbeddedPlatforms embeddedPlatforms;
    private final List<CodePluginConfig> allPlugins = new ArrayList<>();
    private final ConfigurationStorage configStorage;
    private List<String> loadErrrors = new CopyOnWriteArrayList<>();
    private List<Path> sourceDirs;

    public DefaultXmlPluginLoader(EmbeddedPlatforms embeddedPlatforms, ConfigurationStorage storage) {
        this.embeddedPlatforms = embeddedPlatforms;
        this.configStorage = storage;
    }

    @Override
    public void loadPlugins(List<Path> sourceDirs) throws Exception {
        this.sourceDirs = sourceDirs;
        reload();
    }

    @Override
    public void reload() {
        synchronized (allPlugins) {
            allPlugins.clear();
        }

        loadErrrors.clear();
        try {
            for (var path : sourceDirs) {
                logger.log(INFO, "Traversing " + path + " for plugins");
                for (var dir : Files.list(path).filter(f -> Files.isDirectory(f)).collect(Collectors.toList())) {
                    if (Files.exists((dir.resolve("tcmenu-plugin.xml")))) {
                        logger.log(System.Logger.Level.INFO, "Plugin xml found in " + dir);
                        var loadedPlugin = loadPluginLib(dir);
                        if (loadedPlugin != null) {
                            synchronized (allPlugins) {
                                allPlugins.add(loadedPlugin);
                            }
                        } else {
                            logger.log(ERROR, "Plugin didn't load" + dir);
                            loadErrrors.add(dir + " did not contain valid plugin");
                        }
                    } else {
                        loadErrrors.add(dir + " was not a plugin, no tcmenu-plugin.xml");
                    }
                }
            }
            logger.log(INFO, "Plugins are now fully loaded");
        } catch (Exception ex) {
            logger.log(ERROR, "Plugins not loaded!", ex);
            loadErrrors.add("Exception processing plugins, see log");
        }
    }

    public List<String> getLoadErrrors() {
        return loadErrrors;
    }

    @Override
    public List<CodePluginConfig> getLoadedPlugins() {
        synchronized (allPlugins) {
            return List.copyOf(allPlugins);
        }
    }

    @Override
    public Optional<Image> getImageForName(CodePluginItem item, String imageName) {
        try {
            Image img = new Image(new FileInputStream(item.getConfig().getPath().resolve("Images").resolve(item.getImageFileName()).toFile()));
            return Optional.of(img);
        } catch (Exception e) {
            logger.log(ERROR, "Image load failed for " + item.getId(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<CodePluginItem> getPluginsThatMatch(EmbeddedPlatform platform, SubSystem subSystem) {
        synchronized (allPlugins) {
            return allPlugins.stream()
                    .flatMap(module -> module.getPlugins().stream())
                    .filter(item -> item.getSupportedPlatforms().contains(platform) && item.getSubsystem() == subSystem)
                    .collect(Collectors.toList());
        }
    }

    public CodePluginConfig loadPluginLib(Path directoryPath) {
        logger.log(System.Logger.Level.INFO, "Loading plugins in directory " + directoryPath);

        try {
            var pluginConfigFile = directoryPath.resolve("tcmenu-plugin.xml");
            byte[] dataToLoad = Files.readAllBytes(pluginConfigFile);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            dBuilder = factory.newDocumentBuilder();
            ByteArrayInputStream in = new ByteArrayInputStream(dataToLoad);
            Document doc = dBuilder.parse(in);
            var root = doc.getDocumentElement();

            var shortName = root.getAttribute("shortName");
            var detailElem = elementWithName(root, "GeneralDetails");
            var pluginsElem = elementWithName(root, "Plugins");
            if (detailElem == null || pluginsElem == null) {
                logger.log(ERROR, "GeneralDetails or Plugins elements missing, not loading: " + directoryPath);
                return null;
            }

            List<CodePluginItem> items = new ArrayList<>();

            CodePluginConfig config = new CodePluginConfig();
            config.setPath(directoryPath);
            config.setPlugins(transformElements(root, "Plugins", "Plugin", (ele) -> {
                try {
                    var pluginName = ele.getTextContent().trim();
                    logger.log(System.Logger.Level.INFO, "Loading plugin item " + pluginName);
                    var path = directoryPath.resolve(pluginName);
                    String strPlugin = null;
                    strPlugin = new String(Files.readAllBytes(path));
                    var created = loadPlugin(strPlugin);
                    if (created != null) {
                        logger.log(System.Logger.Level.INFO, "Loaded item " + pluginName);
                        created.setConfig(config);
                        return created;
                    } else {
                        logger.log(WARNING, "Failed loading " + pluginName);
                        return null;
                    }
                } catch (IOException e) {
                    logger.log(ERROR, "Exception while loading plugin", e);
                    return null;
                }
            }));

            config.setModuleName(shortName);
            config.setName(textOfElementByName(detailElem, "Name"));

            var licenseField = elementWithName(detailElem, "License");
            if (licenseField != null) {
                config.setLicense(licenseField.getAttribute("name"));
                config.setLicenseUrl(licenseField.getAttribute("url"));
            }

            var vendorField = elementWithName(detailElem, "Author");
            if (vendorField != null) {
                config.setVendor(vendorField.getAttribute("name"));
                config.setVendorUrl(vendorField.getAttribute("url"));
            }
            config.setVersion(textOfElementByName(detailElem, "Version"));

            return config;
        } catch (Exception e) {
            logger.log(ERROR, "Did not fully load library because of error in " + directoryPath, e);
            return null;
        }
    }

    public CodePluginItem loadPlugin(String dataToLoad) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = factory.newDocumentBuilder();
            ByteArrayInputStream in = new ByteArrayInputStream(dataToLoad.getBytes());
            Document doc = dBuilder.parse(in);
            var root = doc.getDocumentElement();

            var requiresVersion = root.getAttribute("requiresDesigner");
            if(!StringHelper.isStringEmptyOrNull(requiresVersion)) {
                var requiredVersion = new VersionInfo(requiresVersion);
                // get the app version and remove any maven artefacts such as -SNAPSHOT
                var appVersion = configStorage.getVersion();
                if(appVersion.indexOf('-') != -1) {
                    appVersion = appVersion.substring(0, appVersion.indexOf('-'));
                }
                var currentVersion = new VersionInfo(appVersion);
                if(!currentVersion.isSameOrNewerThan(requiredVersion)) {
                    logger.log(ERROR, "Cannot load plugin as it needs a newer designer version");
                    return null;
                }
            }

            CodePluginItem item = new CodePluginItem();

            generateDescriptionFromXml(root, item);
            generateProperties(root, item);
            var applicabilityByKey = generateApplicabilityMappings(root, item);

            item.setIncludeFiles(transformElements(root, "IncludeFiles", "Header", (ele) ->
                    new HeaderDefinition(
                            ele.getAttribute("name"),
                            toHeaderType(getAttributeOrDefault(ele, "inSource", false)),
                            toPriority(ele.getAttribute("priority")),
                            toApplicability(ele, applicabilityByKey)
                    )
            ));

            item.setVariables(transformElements(root, "GlobalVariables", "Variable", (ele) ->
                    new CodeVariable(
                            ele.getAttribute("name"),
                            getAttributeOrDefault(ele, "object", ele.getAttribute("type")),
                            toDefinitionMode(ele.getAttribute("export")),
                            Boolean.parseBoolean(getAttributeOrDefault(ele, "progmem", "false")),
                            toCodeParameters(ele, new HashMap<String, LambdaDefinition>()),
                            toApplicability(ele, applicabilityByKey)
                    )
            ));

            item.setFunctions(generateFunctions(elementWithName(root, "SetupFunctions"), new HashMap<>(), applicabilityByKey));

            List<CodeReplacement> replacements = transformElements(root, "SourceFiles", "Replacement", ele ->
                    new CodeReplacement(ele.getAttribute("find"), ele.getAttribute("replace"), toApplicability(ele, applicabilityByKey))
            );
            item.setRequiredSourceFiles(transformElements(root, "SourceFiles", "SourceFile", (ele) ->
                    new RequiredSourceFile(getAttributeOrDefault(ele, "name", ""), replacements,
                            toApplicability(ele, applicabilityByKey),
                            Boolean.parseBoolean(getAttributeOrDefault(ele,"overwrite", "true"))
                    )
            ));

            return item;
        } catch (Exception ex) {
            logger.log(ERROR, "Unable to generate plugin " + dataToLoad, ex);
            return null;
        }
    }

    private HeaderDefinition.HeaderType toHeaderType(String headerType) {
        if(headerType == null) return HeaderDefinition.HeaderType.SOURCE;
        if(headerType.equals("true")) return HeaderDefinition.HeaderType.SOURCE;
        if(headerType.equals("cpp")) return HeaderDefinition.HeaderType.CPP_FILE;
        if(headerType.equals("cppSrc")) return HeaderDefinition.HeaderType.CPP_SRC_FILE;
        if(headerType.equals("font")) return HeaderDefinition.HeaderType.FONT;
        return HeaderDefinition.HeaderType.GLOBAL;
    }

    private List<FunctionDefinition> generateFunctions(Element fnElements, Map<String, LambdaDefinition> lambdas,
                                                       Map<String, CodeApplicability> applicByKey) {
        var functionList = new ArrayList<FunctionDefinition>();
        if (fnElements == null) return functionList;

        for (var childElem : listOf(fnElements)) {
            var name = childElem.getAttribute("name");

            if (childElem.getNodeName().equals("Lambda")) {
                var fnParams = toCodeParameters(childElem, lambdas);
                var fnList = generateFunctions(childElem, lambdas, applicByKey);
                var defn = new LambdaDefinition(name, fnParams, fnList, toApplicability(childElem, applicByKey));
                lambdas.put(name, defn);
            } else if (childElem.getNodeName().equals("Function")) {
                var obj = childElem.getAttribute("object");
                var applicability = toApplicability(childElem, applicByKey);
                var isPtr = getAttrOrNull(childElem, "pointer") != null;
                functionList.add(new FunctionDefinition(name, obj, isPtr, toCodeParameters(childElem, lambdas), applicability));
            }
        }
        return functionList;
    }

    private List<CodeParameter> toCodeParameters(Element ele, Map<String, LambdaDefinition> lambdaMap) {
        if (ele == null) return List.of();

        return transformElements(ele, null, "Param", (param) -> {
            var classType = param.getAttribute("type");
            var used = Boolean.parseBoolean(getAttributeOrDefault(param, "used", "true"));
            var refType = getAttrOrNull(param, "ref");
            var fontType = getAttrOrNull(param, "font");
            var lambdaType = getAttrOrNull(param, "lambda");
            var defVal = getAttrOrNull(param, "default");

            if (refType != null) {
                return new ReferenceCodeParameter(refType, defVal, used);
            } else if (lambdaType != null) {
                var lambda = lambdaMap.get(lambdaType);
                return new LambdaCodeParameter(lambda);
            } else if(fontType != null) {
                return new FontCodeParameter(fontType, defVal, used);
            } else {
                var valueType = getAttributeOrDefault(param, "value", param.getAttribute("name"));
                return new CodeParameter(classType, used, valueType, defVal);
            }
        });
    }

    private int toPriority(String priority) {
        if (priority == null) return HeaderDefinition.PRIORITY_NORMAL;
        switch (priority) {
            case "high":
                return HeaderDefinition.PRIORITY_MAX;
            case "low":
                return HeaderDefinition.PRIORITY_MIN;
            default:
                return HeaderDefinition.PRIORITY_NORMAL;
        }
    }

    private Map<String, CodeApplicability> generateApplicabilityMappings(Element root, CodePluginItem item) {
        Element applicDefs = elementWithName(root, "ApplicabilityDefs");
        if (applicDefs == null) return Map.of();

        var applicabilityMap = new HashMap<String, CodeApplicability>();

        for (var applicability : listOf(applicDefs)) {
            var key = applicability.getAttribute("key");
            if (key == null) throw new IllegalArgumentException("Applicability key not set");
            var val = toApplicability(applicability, applicabilityMap);
            applicabilityMap.put(key, val);
        }
        return applicabilityMap;
    }

    private void generateProperties(Element root, CodePluginItem item) {
        item.setProperties(transformElements(root, "Properties", "Property", (elem) -> {
            String initial = elem.getAttribute("initial");
            return new CreatorProperty(
                    elem.getAttribute("id"),
                    elem.getAttribute("name"),
                    initial,
                    item.getSubsystem(),
                    validatorFor(elem, initial)
            );
        }));
    }

    private PropertyValidationRules validatorFor(Element elem, String initialValue) {
        boolean req = Boolean.parseBoolean(elem.getAttribute("required"));

        switch (elem.getAttribute("type")) {
            case "header":
            case "variable":
                return CannedPropertyValidators.variableValidator();
            case "int":
                int min = Integer.parseInt(getAttributeOrDefault(elem, "min", 0));
                int max = Integer.parseInt(getAttributeOrDefault(elem, "max", 65535));
                return new IntegerPropertyValidationRules(min, max);
            case "boolean":
                return CannedPropertyValidators.boolValidator();
            case "font":
                return CannedPropertyValidators.fontValidator();
            case "choice":
                return makeChoices(elem, initialValue);
            case "pin":
                return CannedPropertyValidators.pinValidator();

            case "MenuItem": return CannedPropertyValidators.menuItemValidatorForAllItems();
            case "BooleanMenuItem": return CannedPropertyValidators.menuItemValidatorForSpecifcType(BooleanMenuItem.class);
            case "TextMenuItem": return CannedPropertyValidators.menuItemValidatorForSpecifcType(EditableTextMenuItem.class);
            case "AnalogMenuItem": return CannedPropertyValidators.menuItemValidatorForSpecifcType(AnalogMenuItem.class);
            case "SubMenuItem": return CannedPropertyValidators.menuItemValidatorForSpecifcType(SubMenuItem.class);
            case "EnumMenuItem": return CannedPropertyValidators.menuItemValidatorForSpecifcType(EnumMenuItem.class);
            case "FloatMenuItem": return CannedPropertyValidators.menuItemValidatorForSpecifcType(FloatMenuItem.class);
            case "RuntimeMenuItem": return CannedPropertyValidators.menuItemValidatorForSpecifcType(RuntimeListMenuItem.class);
            case "LargeNumberMenuItem": return CannedPropertyValidators.menuItemValidatorForSpecifcType(EditableLargeNumberMenuItem.class);

            case "text":
            default:
                return CannedPropertyValidators.textValidator();
        }
    }

    private PropertyValidationRules makeChoices(Element elem, String initialValue) {
        return CannedPropertyValidators.choicesValidator(transformElements(elem, "Choices", "Choice", chElem ->
            new ChoiceDescription(chElem.getTextContent(), getAttributeOrDefault(chElem, "desc", chElem.getTextContent()))
        ), initialValue);
    }

    private VariableDefinitionMode toDefinitionMode(String mode) {
        switch (mode) {
            case "true":
                return VariableDefinitionMode.VARIABLE_AND_EXPORT;
            case "only":
                return VariableDefinitionMode.EXPORT_ONLY;
            default:
                return VariableDefinitionMode.VARIABLE_ONLY;
        }
    }

    private CodeApplicability toApplicability(Element varElement, Map<String, CodeApplicability> applicabilityByKey) {
        if (varElement.getNodeName().equals("ApplicabilityDef")) {
            var list = new ArrayList<CodeApplicability>();
            for (var childApplicability : listOf(varElement)) {
                if (childApplicability.getNodeName().equals("ApplicabilityDef")) {
                    list.add(toApplicability(childApplicability, applicabilityByKey));
                } else {
                    list.add(toSingleApplicability(childApplicability));
                }
            }
            var mode = NestedApplicability.NestingMode.valueOf(getAttributeOrDefault(varElement, "mode", "AND").toUpperCase());
            return new NestedApplicability(mode, list);
        } else if (!StringHelper.isStringEmptyOrNull(varElement.getAttribute("applicabilityRef"))) {
            var key = varElement.getAttribute("applicabilityRef");
            if (!applicabilityByKey.containsKey(key))
                throw new IllegalArgumentException("Missing applicability key " + key);
            return applicabilityByKey.get(key);
        } else return toSingleApplicability(varElement);
    }

    private List<Element> listOf(Element varElement) {
        NodeList children = varElement.getChildNodes();
        if (children == null || children.getLength() == 0) return List.of();

        var list = new ArrayList<Element>();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                list.add((Element) children.item(i));
            }
        }
        return list;
    }

    private CodeApplicability toSingleApplicability(Element varElement) {
        var whenProperty = getAttrOrNull(varElement, "whenProperty");
        if (whenProperty != null) {
            var equalProp = getAttrOrNull(varElement, "isValue");
            if (equalProp != null) {
                return new EqualityApplicability(whenProperty, equalProp, false);
            }

            var notEqualProp = getAttrOrNull(varElement, "isNotValue");
            if (notEqualProp != null) {
                return new EqualityApplicability(whenProperty, notEqualProp, true);
            }
            var matchesProp = getAttrOrNull(varElement, "matches");
            if(matchesProp != null) {
                return new MatchesApplicability(whenProperty, matchesProp);
            }

            throw new IllegalArgumentException("Unsupported option to whenProperty on " + whenProperty);
        } else {
            return new AlwaysApplicable();
        }
    }

    private String getAttrOrNull(Element ele, String attr) {
        var node = ele.getAttributes().getNamedItem(attr);
        if (node == null) return null;
        return node.getTextContent();
    }

    private String getAttributeOrDefault(Element elem, String val, Object def) {
        String att = elem.getAttribute(val);
        if (att == null || att.length() == 0) {
            return def.toString();
        }
        return att;
    }

    private void generateDescriptionFromXml(Element root, CodePluginItem config) {
        config.setSubsystem(SubSystem.valueOf(root.getAttribute("subsystem")));
        config.setThemeNeeded(Boolean.parseBoolean(getAttributeOrDefault(root, "needsTheme", "false")));
        config.setId(root.getAttribute("id"));
        config.setDescription(root.getAttribute("name"));
        config.setExtendedDescription(textOfElementByName(root, "Description"));
        config.setImageFileName(textOfElementByName(root, "ImageFile"));
        config.setSupportedPlatforms(transformElements(root, "SupportedPlatforms", "Platform",
                (ele) -> embeddedPlatforms.getEmbeddedPlatformFromId(ele.getTextContent())));
        config.setRequiredLibraries(transformElements(root, "RequiredLibraries", "Library", Node::getTextContent));
        var docs = elementWithName(root, "Documentation");
        if (docs != null) config.setDocsLink(docs.getAttribute("link"));
    }

    private List<Element> getChildElementsWithName(Element ele, String name) {
        if (ele == null) return List.of();

        var childNodes = ele.getChildNodes();
        var list = new ArrayList<Element>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            var ch = childNodes.item(i);
            if (ch instanceof Element && ch.getNodeName().equals(name)) {
                list.add((Element) ch);
            }
        }
        return list;
    }

    private <T> List<T> transformElements(Element root, String eleName, String childName, Function<Element, T> transform) {
        var ret = new ArrayList<T>();
        Element ele = root;
        if (eleName != null) {
            ele = elementWithName(root, eleName);
            if (ele == null) return Collections.emptyList();
        }

        var childList = getChildElementsWithName(ele, childName);
        for (var ch : childList) {
            var created = transform.apply(ch);
            if (created != null) ret.add(created);
        }
        return ret;
    }

    private String textOfElementByName(Element elem, String child) {
        var ch = getChildElementsWithName(elem, child);
        if (ch == null || ch.size() == 0) return "";
        return ch.get(0).getTextContent();
    }

    private Element elementWithName(Element elem, String child) {
        var ch = getChildElementsWithName(elem, child);
        if (ch == null || ch.size() == 0) return null;
        return (Element) ch.get(0);
    }
}
