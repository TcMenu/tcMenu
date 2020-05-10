/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.util.IHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector.*;
import static com.thecoderscorner.menu.editorui.util.IHttpClient.HttpDataType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class OnlineLibraryVersionDetectorTest {
    private final String xmlData= "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
            "<LibraryVersions app=\"tcMenuDesigner\">\n" +
            "    <Libraries stream=\"STABLE\">\n" +
            "        <Library name=\"tcMenu\" version=\"1.4.1\"/>\n" +
            "        <Library name=\"IoAbstraction\" version=\"1.4.11\"/>\n" +
            "        <Library name=\"LiquidCrystalIO\" version=\"1.2.0\"/>\n" +
            "    </Libraries>\n" +
            "    <Plugins stream=\"STABLE\">\n" +
            "        <Plugin name=\"core-display\" version=\"1.4.3\"/>\n" +
            "        <Plugin name=\"core-remote\" version=\"1.4.4\"/>\n" +
            "    </Plugins>\n" +
            "    <Libraries stream=\"BETA\">\n" +
            "        <Library name=\"tcMenu\" version=\"10.4.1\"/>\n" +
            "        <Library name=\"IoAbstraction\" version=\"10.4.11\"/>\n" +
            "        <Library name=\"LiquidCrystalIO\" version=\"10.2.0\"/>\n" +
            "    </Libraries>\n" +
            "    <Plugins stream=\"BETA\">\n" +
            "        <Plugin name=\"core-display\" version=\"10.4.3\"/>\n" +
            "        <Plugin name=\"core-remote\" version=\"10.4.4\"/>\n" +
            "    </Plugins>\n" +
            "</LibraryVersions>";

    @Test
    public void testReadingXmlOverMockHttp() throws IOException, InterruptedException {
        var mockHttp = Mockito.mock(IHttpClient.class);
        when(mockHttp.postRequestForString(LIBRARY_VERSIONING_URL, "", HttpDataType.JSON_DATA)).thenReturn(xmlData);

        var verDet = new OnlineLibraryVersionDetector(mockHttp, ReleaseType.STABLE);
        var versions = verDet.acquireVersions();

        assertEquals("1.4.1", versions.get("tcMenu/Library").toString());
        assertEquals("1.4.11", versions.get("IoAbstraction/Library").toString());
        assertEquals("1.2.0", versions.get("LiquidCrystalIO/Library").toString());
        assertEquals("1.4.3", versions.get("core-display/Plugin").toString());
        assertEquals("1.4.4", versions.get("core-remote/Plugin").toString());
    }

    @Test
    public void testReadingXmlOverMockHttpForBeta() throws IOException, InterruptedException {
        var mockHttp = Mockito.mock(IHttpClient.class);
        when(mockHttp.postRequestForString(LIBRARY_VERSIONING_URL, "", HttpDataType.JSON_DATA)).thenReturn(xmlData);

        var verDet = new OnlineLibraryVersionDetector(mockHttp, ReleaseType.BETA);
        var versions = verDet.acquireVersions();

        assertEquals("10.4.1", versions.get("tcMenu/Library").toString());
        assertEquals("10.4.11", versions.get("IoAbstraction/Library").toString());
        assertEquals("10.2.0", versions.get("LiquidCrystalIO/Library").toString());
        assertEquals("10.4.3", versions.get("core-display/Plugin").toString());
        assertEquals("10.4.4", versions.get("core-remote/Plugin").toString());
    }

}