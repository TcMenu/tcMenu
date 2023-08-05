/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.IHttpClient;
import com.thecoderscorner.menu.persist.ReleaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector.LIBRARY_VERSIONING_URL_APPEND;
import static com.thecoderscorner.menu.editorui.util.IHttpClient.HttpDataType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OnlineLibraryVersionDetectorTest {
    private final String xmlData= """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <LibraryVersions apiVersion="3" app="tcMenuDesigner" timestamp="2021-06-10T14:41:23">
                <Libraries stream="STABLE">
                    <Library id="1" name="tcMenu" version="4.1.3"/>
                    <Library id="2" name="IoAbstraction" version="4.0.3"/>
                    <Library id="3" name="LiquidCrystalIO" version="4.4.1"/>
                    <Library id="7" name="TaskManagerIO" version="4.2.3"/>
                </Libraries>
                <Libraries stream="PREVIOUS">
                    <Library id="1" name="tcMenu" version="3.7.1"/>
                    <Library id="2" name="IoAbstraction" version="3.1.3"/>
                    <Library id="3" name="LiquidCrystalIO" version="3.4.1"/>
                    <Library id="7" name="TaskManagerIO" version="3.2.3"/>
                </Libraries>
                <Libraries stream="BETA">
                    <Library id="1" name="tcMenu" version="5.1.3"/>
                    <Library id="2" name="IoAbstraction" version="5.0.3"/>
                    <Library id="3" name="LiquidCrystalIO" version="5.4.1"/>
                    <Library id="7" name="TaskManagerIO" version="5.2.3"/>
                </Libraries>
                <Plugins stream="STABLE">
                    <Plugin id="4" name="core-display" version="2.1.0"/>
                    <Plugin id="5" name="core-remote" version="2.1.1"/>
                    <Plugin id="10" name="core-themes" version="2.1.2"/>
                </Plugins>
                <Plugins stream="PREVIOUS">
                    <Plugin id="4" name="core-display" version="1.7.2"/>
                    <Plugin id="5" name="core-remote" version="1.7.2"/>
                    <Plugin id="10" name="core-themes" version=""/>
                </Plugins>
                <Plugins stream="BETA">
                    <Plugin id="4" name="core-display" version="2.3.4"/>
                    <Plugin id="5" name="core-remote" version="2.3.5"/>
                    <Plugin id="10" name="core-themes" version="2.3.6"/>
                </Plugins>
                <Apps stream="STABLE">
                    <App id="6" name="java-app" version="2.1.1"/>
                    <App id="8" name="uwp-app" version="2.0.7"/>
                    <App id="9" name="mac-app" version="1.7.7"/>
                </Apps>
                <Apps stream="PREVIOUS">
                    <App id="6" name="java-app" version="1.7.0"/>
                    <App id="8" name="uwp-app" version="1.7.13"/>
                    <App id="9" name="mac-app" version="1.7.7"/>
                </Apps>
                <Apps stream="BETA">
                    <App id="6" name="java-app" version="2.1.3"/>
                    <App id="8" name="uwp-app" version="2.0.7"/>
                    <App id="9" name="mac-app" version="1.7.7"/>
                </Apps>
                <AllVersions name="core-display">
                    <Version ver="1.7.0"/>
                    <Version ver="1.7.1"/>
                    <Version ver="1.7.2"/>
                </AllVersions>
                <AllVersions name="core-remote">
                    <Version ver="1.4.1"/>
                    <Version ver="1.4.2"/>
                    <Version ver="1.4.3"/>
                </AllVersions>
            </LibraryVersions>""";

    private OnlineLibraryVersionDetector verDet;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        var mockHttp = Mockito.mock(IHttpClient.class);
        when(mockHttp.postRequestForString("https://mockAddr" + LIBRARY_VERSIONING_URL_APPEND, "", HttpDataType.FORM)).thenReturn(xmlData);
        var store = mock(ConfigurationStorage.class);
        when(store.getReleaseStream()).thenReturn(ReleaseType.STABLE);
        verDet = new OnlineLibraryVersionDetector("https://mockAddr", mockHttp, store);
    }

    @Test
    public void testReadingXmlOverMockHttp() {
        var versions = verDet.acquireVersions();

        assertEquals("4.1.3", versions.get("tcMenu/Library").toString());
        assertEquals("4.0.3", versions.get("IoAbstraction/Library").toString());
        assertEquals("4.4.1", versions.get("LiquidCrystalIO/Library").toString());
        assertEquals("4.2.3", versions.get("TaskManagerIO/Library").toString());
        assertEquals("2.1.0", versions.get("core-display/Plugin").toString());
        assertEquals("2.1.1", versions.get("core-remote/Plugin").toString());
        assertEquals("2.1.2", versions.get("core-themes/Plugin").toString());
        assertEquals("2.1.1", versions.get("java-app/App").toString());
    }

    @Test
    public void testReadingXmlOverMockHttpOtherStreams() {
        verDet.changeReleaseType(ReleaseType.BETA);
        var versions = verDet.acquireVersions();

        assertEquals("5.1.3", versions.get("tcMenu/Library").toString());
        assertEquals("5.0.3", versions.get("IoAbstraction/Library").toString());
        assertEquals("5.4.1", versions.get("LiquidCrystalIO/Library").toString());
        assertEquals("5.2.3", versions.get("TaskManagerIO/Library").toString());
        assertEquals("2.3.4", versions.get("core-display/Plugin").toString());
        assertEquals("2.3.5", versions.get("core-remote/Plugin").toString());
        assertEquals("2.1.3", versions.get("java-app/App").toString());

        verDet.changeReleaseType(ReleaseType.PREVIOUS);
        versions = verDet.acquireVersions();

        assertEquals("3.7.1", versions.get("tcMenu/Library").toString());
        assertEquals("3.1.3", versions.get("IoAbstraction/Library").toString());
        assertEquals("1.7.2", versions.get("core-display/Plugin").toString());
        assertEquals("1.7.2", versions.get("core-remote/Plugin").toString());
        assertEquals("1.7.0", versions.get("java-app/App").toString());
    }
}