/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.NestedApplicability;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.LambdaCodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.LambdaDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.ReferenceCodeParameter;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.pluginapi.validation.IntegerPropertyValidationRules;
import com.thecoderscorner.menu.pluginapi.validation.PropertyValidationRules;
import javafx.scene.image.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class DefaultXmlPluginLoader implements CodePluginManager {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final EmbeddedPlatforms embeddedPlatforms;
    private final List<CodePluginConfig> allPlugins = new ArrayList<>();
    private List<String> loadErrrors = new CopyOnWriteArrayList<>();

    public DefaultXmlPluginLoader(EmbeddedPlatforms embeddedPlatforms) {
        this.embeddedPlatforms = embeddedPlatforms;
    }

    @Override
    public void loadPlugins(List<Path> sourceDirs) throws Exception {
        synchronized (allPlugins) {
            allPlugins.clear();
        }

        loadErrrors.clear();
        try
        {
            for(var path : sourceDirs) {
                logger.log(INFO, "Traversing " + path + " for plugins");
                for(var dir : Files.list(path).filter(f -> Files.isDirectory(f)).collect(Collectors.toList())) {
                    if(Files.exists((dir.resolve("tcmenu-plugin.xml")))) {
                        logger.log(System.Logger.Level.INFO, "Plugin xml found in " + dir);
                        var loadedPlugin = loadPluginLib(dir);
                        if(loadedPlugin != null) {
                            synchronized (allPlugins) {
                                allPlugins.add(loadedPlugin);
                            }
                        }
                        else {
                            logger.log(ERROR, "Plugin didn't load" + dir);
                            loadErrrors.add(dir + " did not contain valid plugin");
                        }
                    }
                    else {
                        loadErrrors.add(dir + " was not a plugin, no tcmenu-plugin.xml");
                    }
                }
            }
            logger.log(INFO, "Plugins are now fully loaded");
        }
        catch (Exception ex)
        {
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
        return Optional.empty();
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

    CodePluginConfig loadPluginLib(Path directoryPath) {
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
                        logger.log(ERROR, "Failed loading " + pluginName);
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

    CodePluginItem loadPlugin(String dataToLoad) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = factory.newDocumentBuilder();
            ByteArrayInputStream in = new ByteArrayInputStream(dataToLoad.getBytes());
            Document doc = dBuilder.parse(in);
            var root = doc.getDocumentElement();

            CodePluginItem item = new CodePluginItem();

            generateDescriptionFromXml(root, item);
            generateProperties(root, item);
            var applicabilityByKey = generateApplicabilityMappings(root, item);

            item.setIncludeFiles(transformElements(root, "IncludeFiles", "Header", (ele) ->
                    new HeaderDefinition(
                            ele.getAttribute("name"),
                            Boolean.parseBoolean(getAttributeOrDefault(ele, "inSource", false)),
                            toPriority(ele.getAttribute("priority"))
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

            item.setFunctions(generateFunctions(root, new HashMap<>(), applicabilityByKey));

            List<CodeReplacement> replacements = transformElements(root, "SourceFiles", "Replacement", ele ->
                    new CodeReplacement(ele.getAttribute("find"), ele.getAttribute("replace"), toApplicability(ele, applicabilityByKey))
            );
            item.setRequiredSourceFiles(transformElements(root, "SourceFiles", "SourceFile", (ele) ->
                    new RequiredSourceFile(getAttributeOrDefault(ele, "name", ""), replacements)
            ));

            return item;
        } catch (Exception ex) {
            logger.log(ERROR, "Unable to generate plugin " + dataToLoad, ex);
            return null;
        }
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
                var isPtr = !StringHelper.isStringEmptyOrNull(childElem.getAttribute("pointer"));
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
            var refType = param.getAttribute("ref");
            var lambdaType = param.getAttribute("lambda");

            if (!StringHelper.isStringEmptyOrNull(refType)) {
                return new ReferenceCodeParameter(refType, used);
            } else if (!StringHelper.isStringEmptyOrNull(lambdaType)) {
                var lambda = lambdaMap.get(lambdaType);
                return new LambdaCodeParameter(lambda);
            } else {
                var valueType = getAttributeOrDefault(param, "value", param.getAttribute("name"));
                var defVal = param.getAttribute("default");
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
            return new CreatorProperty(
                    elem.getAttribute("id"),
                    elem.getAttribute("name"),
                    elem.getAttribute("initial"),
                    item.getSubsystem(),
                    validatorFor(elem)
            );
        }));
    }

    private PropertyValidationRules validatorFor(Element elem) {
        boolean req = Boolean.parseBoolean(elem.getAttribute("required"));
        switch (elem.getAttribute("type")) {
            case "header":
            case "variable":
                return CannedPropertyValidators.variableValidator();
            case "int":
                int min = Integer.parseInt(getAttributeOrDefault(elem, "min", 0));
                int max = Integer.parseInt(getAttributeOrDefault(elem, "max", 65355));
                return new IntegerPropertyValidationRules(min, max);
            case "boolean":
                return CannedPropertyValidators.boolValidator();
            case "choice":
                return CannedPropertyValidators.choicesValidator(transformElements(elem, "Choices", "Choice", Node::getTextContent));
            case "pin":
                return CannedPropertyValidators.pinValidator();
            case "text":
            default:
                return CannedPropertyValidators.textValidator();
        }
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
        var whenProperty = varElement.getAttribute("whenProperty");
        if (!StringHelper.isStringEmptyOrNull(whenProperty)) {
            var equalProp = varElement.getAttribute("isValue");
            if (!StringHelper.isStringEmptyOrNull(equalProp)) {
                return new EqualityApplicability(whenProperty, equalProp, false);
            }

            var notEqualProp = varElement.getAttribute("isNotValue");
            if (!StringHelper.isStringEmptyOrNull(notEqualProp)) {
                return new EqualityApplicability(whenProperty, notEqualProp, true);
            }

            throw new IllegalArgumentException("Unsupported option to whenProperty");
        } else {
            return new AlwaysApplicable();
        }
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

    private <T> List<T> transformElements(Element root, String eleName, String childName, Function<Element, T> transform) {
        var ret = new ArrayList<T>();
        Element ele = root;
        if (eleName != null) {
            ele = elementWithName(root, eleName);
            if (ele == null) return Collections.emptyList();
        }

        NodeList childList = ele.getElementsByTagName(childName);
        for (int i = 0; i < childList.getLength(); i++) {
            var created = transform.apply((Element) childList.item(i));
            if (created != null) ret.add(created);
        }
        return ret;
    }

    private String textOfElementByName(Element elem, String child) {
        var ch = elem.getElementsByTagName(child);
        if (ch == null || ch.getLength() == 0) return "";
        return ch.item(0).getTextContent();
    }

    private Element elementWithName(Element elem, String child) {
        var ch = elem.getElementsByTagName(child);
        if (ch == null || ch.getLength() == 0) return null;
        return (Element) ch.item(0);
    }

}
