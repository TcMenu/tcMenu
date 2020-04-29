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
import com.thecoderscorner.menu.editorui.generator.parameters.LambdaDefinition;
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
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultXmlPluginLoader implements CodePluginManager {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final EmbeddedPlatforms embeddedPlatforms;
    private final List<CodePluginConfig> allPlugins = new ArrayList<>();

    public DefaultXmlPluginLoader(EmbeddedPlatforms embeddedPlatforms) {
        this.embeddedPlatforms = embeddedPlatforms;
    }

    @Override
    public void loadPlugins(List<Path> sourceDirs) throws Exception {
        allPlugins.clear();

    }

    @Override
    public List<CodePluginConfig> getLoadedPlugins() {
        return allPlugins;
    }

    @Override
    public Optional<Image> getImageForName(CodePluginItem item, String imageName) {
        return Optional.empty();
    }

    @Override
    public synchronized List<CodePluginItem> getPluginsThatMatch(EmbeddedPlatform platform, SubSystem subSystem) {
        return allPlugins.stream()
                .flatMap(module -> module.getPlugins().stream())
                .filter(item -> item.getSupportedPlatforms().contains(platform) && item.getSubsystem() == subSystem)
                .collect(Collectors.toList());
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
                        Boolean.parseBoolean(getAttributeOrDefault(ele,"inSource", false)),
                        toPriority(ele.getAttribute("priority"))
                )
            ));

            item.setVariables(transformElements(root, "GlobalVariables", "Variable", (ele) ->
                    new CodeVariable(
                            ele.getAttribute("name"),
                            getAttributeOrDefault(ele, "object", ele.getAttribute("type")),
                            toDefinitionMode(ele.getAttribute("export")),
                            Boolean.parseBoolean(getAttributeOrDefault(ele,"progmem", "false")),
                            toCodeParameters(ele, new HashMap<String, LambdaDefinition>()),
                            toApplicability(ele, applicabilityByKey)
                    )
            ));

            item.setFunctions(List.of());

            item.setRequiredSourceFiles(List.of());

            return item;
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Unable to generate plugin " + dataToLoad);
            return null;
        }
    }

    private List<CodeParameter> toCodeParameters(Element ele, HashMap<String, LambdaDefinition> lambdaMap) {
        return List.of();
    }

    private int toPriority(String priority) {
        if (priority == null) return  HeaderDefinition.PRIORITY_NORMAL;
        switch(priority)
        {
            case "high": return HeaderDefinition.PRIORITY_MAX;
            case "low": return HeaderDefinition.PRIORITY_MIN;
            default: return HeaderDefinition.PRIORITY_NORMAL;
        }
    }

    private Map<String, CodeApplicability> generateApplicabilityMappings(Element root, CodePluginItem item) {
        Element applicDefs = elementWithName(root, "ApplicabilityDefs");
        if(applicDefs == null) return Map.of();

        var applicabilityMap = new HashMap<String, CodeApplicability>();

        for(var applicability : listOf(applicDefs)) {
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
                return CannedPropertyValidators.choicesValidator(transformElements(elem, "Choices", "Choice", Node::getNodeValue));
            case "pin":
                return CannedPropertyValidators.pinValidator();
            case "text":
            default:
                return CannedPropertyValidators.textValidator();
        }
    }

    private VariableDefinitionMode toDefinitionMode(String mode) {
        switch(mode) {
            case "true": return VariableDefinitionMode.VARIABLE_AND_EXPORT;
            case "only": return VariableDefinitionMode.EXPORT_ONLY;
            default: return VariableDefinitionMode.VARIABLE_ONLY;
        }
    }

    private CodeApplicability toApplicability(Element varElement, Map<String, CodeApplicability> applicabilityByKey)
    {
        if (varElement.getNodeName().equals("ApplicabilityDef"))
        {
            var list = new ArrayList<CodeApplicability>();
            for (var childApplicability : listOf(varElement))
            {
                if (childApplicability.getNodeName().equals("ApplicabilityDef"))
                {
                    list.add(toApplicability(childApplicability, applicabilityByKey));
                }
                else
                {
                    list.add(toSingleApplicability(childApplicability));
                }
            }
            var mode = NestedApplicability.NestingMode.valueOf(getAttributeOrDefault(varElement, "mode", "AND"));
            return new NestedApplicability(mode, list);
        }
        else if(varElement.getAttribute("applicabilityRef") != null)
        {
            var key = varElement.getAttribute("applicabilityRef");
            if (!applicabilityByKey.containsKey(key)) throw new IllegalArgumentException("Missing applicability key " + key);
            return applicabilityByKey.get(key);
        }
        else return toSingleApplicability(varElement);
    }

    private List<Element> listOf(Element varElement) {
        NodeList children = varElement.getChildNodes();
        if(children == null || children.getLength() == 0) return List.of();

        var list = new ArrayList<Element>();
        for(int i=0;i<children.getLength();i++) {
            list.add((Element) children.item(i));
        }
        return list;
    }

    private CodeApplicability toSingleApplicability(Element varElement) {
        var whenProperty = varElement.getAttribute("whenProperty");
        if (whenProperty != null)
        {
            var equalProp = varElement.getAttribute("isValue");
            if (equalProp != null)
            {
                return new EqualityApplicability(whenProperty, equalProp, false);
            }

            var notEqualProp = varElement.getAttribute("isNotValue");
            if (notEqualProp != null)
            {
                return new EqualityApplicability(whenProperty, notEqualProp, true);
            }

            throw new IllegalArgumentException("Unsupported option to whenProperty");
        }
        else
        {
            return new AlwaysApplicable();
        }
    }

    private String getAttributeOrDefault(Element elem, String val, Object def) {
        String att = elem.getAttribute(val);
        if(att == null || att.length() == 0) {
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
        config.setSupportedPlatforms(transformElements(root, "SupportedPlatforms", "SupportedPlatform",
                (ele) -> embeddedPlatforms.getEmbeddedPlatformFromId(ele.getNodeValue())));
        config.setRequiredLibraries(transformElements(root, "RequiredLibraries", "Library", Node::getNodeValue));
        var docs = elementWithName(root, "Documentation");
        if (docs != null) config.setDocsLink(docs.getAttribute("link"));
    }

    private <T> List<T> transformElements(Element root, String eleName, String childName, Function<Element, T> transform) {
        var ret = new ArrayList<T>();
        Element ele = elementWithName(root, eleName);
        if (ele == null) return Collections.emptyList();

        NodeList childList = ele.getElementsByTagName(childName);
        for (int i = 0; i < childList.getLength(); i++) {
            ret.add(transform.apply((Element) childList.item(i)));
        }
        return ret;
    }

    private String textOfElementByName(Element elem, String child) {
        var ch = elem.getElementsByTagName(child);
        if (ch == null || ch.getLength() == 0) return "";
        return ch.item(0).getNodeValue();
    }

    private Element elementWithName(Element elem, String child) {
        var ch = elem.getElementsByTagName(child);
        if (ch == null || ch.getLength() == 0) return null;
        return (Element) ch.item(0);
    }

}
