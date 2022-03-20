package com.thecoderscorner.menu.persist;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class XMLDOMHelperTest {
    private final String xmlToLoad = "<Animals><IntEle>4</IntEle><Dogs><Dog type=\"Beagle\"/><Dog type=\"Poodle\"/></Dogs><Elephant>Huge animal</Elephant></Animals>";

    @Test
    void testXMLReading() throws Exception {
        Document doc = XMLDOMHelper.loadDocumentFromData(xmlToLoad);
        var dogs = XMLDOMHelper.elementWithName(doc.getDocumentElement(), "Dogs");
        var allDogElements = XMLDOMHelper.getChildElementsWithName(dogs, "Dog");

        assertEquals(2, allDogElements.size());
        assertEquals("Beagle", XMLDOMHelper.getAttrOrNull(allDogElements.get(0), "type"));
        assertEquals("Poodle", XMLDOMHelper.getAttributeOrDefault(allDogElements.get(1), "type", "??"));
        assertEquals("??", XMLDOMHelper.getAttributeOrDefault(allDogElements.get(1), "unknown", "??"));
        assertEquals(4, XMLDOMHelper.integerOfElementByName(doc.getDocumentElement(), "IntEle", 99));
        assertEquals(99, XMLDOMHelper.integerOfElementByName(doc.getDocumentElement(), "IntNone", 99));

        assertEquals("Huge animal", XMLDOMHelper.textOfElementByName(doc.getDocumentElement(), "Elephant"));
    }

    @Test
    void testXMLTransformation() throws Exception {
        Document doc = XMLDOMHelper.loadDocumentFromData(xmlToLoad);

        var out = XMLDOMHelper.transformElements(doc.getDocumentElement(), "Dogs", "Dog",
                element -> XMLDOMHelper.getAttrOrNull(element, "type"));

        assertThat(out).containsExactly("Beagle", "Poodle");

        var dogs = XMLDOMHelper.elementWithName(doc.getDocumentElement(), "Dogs");
        out = XMLDOMHelper.transformElements(dogs, "Dog",
                element -> XMLDOMHelper.getAttrOrNull(element, "type"));

        assertThat(out).containsExactly("Beagle", "Poodle");
    }
}