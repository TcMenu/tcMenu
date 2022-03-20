package com.thecoderscorner.menu.persist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A series of static helper method that make dealing with XML easier, this is because many areas of tcMenu deal with XML
 * documents, and these functions are generally useful.
 */
public class XMLDOMHelper {
    /**
     * Load an XML document from a file system path
     *
     * @param filePath the path to load from
     * @return an XML document if successful, otherwise throws
     * @throws IOException                  for any IO problems
     * @throws ParserConfigurationException if the document does not parse
     * @throws SAXException                 if the document does not parse
     */
    public static Document loadDocumentFromPath(Path filePath) throws IOException, ParserConfigurationException, SAXException {
        var dataToLoad = Files.readAllBytes(filePath);
        var in = new ByteArrayInputStream(dataToLoad);
        return loadDocumentStream(in);
    }

    /**
     * Load an XML document from a string of data
     *
     * @param data the data to load from
     * @return an XML document if successful, otherwise throws
     * @throws IOException                  for any IO problems
     * @throws ParserConfigurationException if the document does not parse
     * @throws SAXException                 if the document does not parse
     */
    public static Document loadDocumentFromData(String data) throws IOException, ParserConfigurationException, SAXException {
        var in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        return loadDocumentStream(in);
    }

    /**
     * Load an XML document from a stream
     *
     * @param stream the steam to load from
     * @return an XML document if successful, otherwise throws
     * @throws IOException                  for any IO problems
     * @throws ParserConfigurationException if the document does not parse
     * @throws SAXException                 if the document does not parse
     */
    public static Document loadDocumentStream(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = factory.newDocumentBuilder();
        return dBuilder.parse(stream);
    }

    /**
     * Find all child elements with a given name
     *
     * @param ele  the parent element
     * @param name the name to search for
     * @return a list of elements directly under ele that are named `name`
     */
    public static List<Element> getChildElementsWithName(Element ele, String name) {
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

    /**
     * Get the text of an element with the name child. Saves looking up the element and then getting text.
     *
     * @param elem  the parent element
     * @param child the name of the child to get the text of
     * @return the text content or blank.
     */
    public static String textOfElementByName(Element elem, String child) {
        var ch = getChildElementsWithName(elem, child);
        if (ch == null || ch.size() == 0) return "";
        return ch.get(0).getTextContent();
    }

    /**
     * Gets the int value associated with the named child, or the default if it does not exist or can't be parsed.
     * @param element the parent element
     * @param child the name of the child element
     * @param def the default value in case of problem
     */
    public static int integerOfElementByName(Element element, String child, int def) {
        var txt = textOfElementByName(element, child);
        if(txt.isEmpty()) return def;
        try {
            return Integer.parseInt(txt);
        }
        catch (Exception e) {
            return def;
        }
    }

    /**
     * Gets an element with a given name or null
     *
     * @param elem  the parent element
     * @param child the child to locate
     * @return the element or null
     */
    public static Element elementWithName(Element elem, String child) {
        var ch = getChildElementsWithName(elem, child);
        if (ch == null || ch.size() == 0) return null;
        return ch.get(0);
    }

    /**
     * Gets the text of attribute if it is available, or null
     *
     * @param ele  the element
     * @param attr the attribute name
     * @return the text of the attribute or null
     */
    public static String getAttrOrNull(Element ele, String attr) {
        var node = ele.getAttributes().getNamedItem(attr);
        if (node == null) return null;
        return node.getTextContent();
    }

    /**
     * Gets the text of an attribute or a default value.
     *
     * @param elem the element
     * @param val  the attribute name
     * @param def  the default value
     * @return the attribute text or otherwise the default
     */
    public static String getAttributeOrDefault(Element elem, String val, Object def) {
        String att = elem.getAttribute(val);
        if (att == null || att.length() == 0) {
            return def.toString();
        }
        return att;
    }

    /**
     * Transforms all elements that match `childName` under either `root` if eleName is null, or under root/eleName if
     * eleName is provided. You provide the transformation function that takes an Element and returns the desired result
     *
     * @param root      the root element to start at
     * @param eleName   optionally, another level of indirection (can be null) EG root/eleName
     * @param childName the items to filter for
     * @param transform the transformation to apply - returns T, takes an element
     * @param <T>       can be any type you wish to return in your transformation
     * @return the transformed list or an empty list.
     */
    public static <T> List<T> transformElements(Element root, String eleName, String childName, Function<Element, T> transform) {
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

    /**
     * Transforms all elements that match `childName` under `root`, there is another overload that provides indirection.
     * You provide the transformation function that takes an Element and returns the desired result.
     *
     * @param root      the root element to start at
     * @param childName the items to filter for
     * @param transform the transformation to apply - returns T, takes an element
     * @param <T>       can be any type you wish to return in your transformation
     * @return the transformed list or an empty list.
     */
    public static <T> List<T> transformElements(Element root, String childName, Function<Element, T> transform) {
        return transformElements(root, null, childName, transform);
    }

    /**
     * Writes an XML document to a stream
     * @param doc the document
     * @param output the stream
     * @throws TransformerException if it cannot be written
     */
    public static void writeXml(Document doc, OutputStream output) throws TransformerException {
        var transformerFactory = TransformerFactory.newInstance();
        var transformer = transformerFactory.newTransformer();
        var source = new DOMSource(doc);
        var result = new StreamResult(output);
        transformer.transform(source, result);

    }
}
