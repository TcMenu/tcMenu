/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.util.IHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class OnlineLibraryVersionDetector implements LibraryVersionDetector {
    private static final long THIRTY_MINUTES = TimeUnit.MINUTES.toMillis(30);

    public enum ReleaseType { STABLE }

    public final static String LIBRARY_VERSIONING_URL = "http://thecoderscorner.com/tcc/app/getLibraryVersions";
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final IHttpClient client;
    private final AtomicLong lastAccess = new AtomicLong();
    private volatile Map<String, VersionInfo> versionCache = Map.of();

    public OnlineLibraryVersionDetector(IHttpClient client) {
        this.client = client;
    }

    public Map<String, VersionInfo> acquireVersions(ReleaseType relType) {
        if(!versionCache.isEmpty() && (System.currentTimeMillis() - lastAccess.get()) < THIRTY_MINUTES)
        {
            return versionCache;
        }

        try {
            var libDict = new HashMap<String, VersionInfo>();

            var verData = client.postRequestForString(LIBRARY_VERSIONING_URL, "", IHttpClient.HttpDataType.JSON_DATA);
            var inStream = new ByteArrayInputStream(verData.getBytes());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = factory.newDocumentBuilder();
            Document doc = dBuilder.parse(inStream);
            var root = doc.getDocumentElement();

            addVersionsToMap(root.getElementsByTagName("Libraries"), "Library", relType, libDict);
            addVersionsToMap(root.getElementsByTagName("Plugins"), "Plugin", relType, libDict);
            lastAccess.set(System.currentTimeMillis());
            versionCache = libDict;
            return libDict;
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Unable to get versions from main site", e);
        }
        return versionCache;
    }

    private void addVersionsToMap(NodeList topLevelElem, String type, ReleaseType relType, HashMap<String, VersionInfo> libDict) {
        logger.log(System.Logger.Level.INFO, "Starting to acquire version list from core site");
        for(int i=0; i< topLevelElem.getLength(); i++) {
            var item = topLevelElem.item(i);
            if (item.getAttributes().getNamedItem("stream").getNodeValue().equals(relType.toString())) {
                var children = ((Element)item).getElementsByTagName(type);
                for(int j=0; j<children.getLength(); j++) {
                    var versionData = children.item(j);
                    var ver = new VersionInfo(versionData.getAttributes().getNamedItem("version").getNodeValue());
                    libDict.put(versionData.getAttributes().getNamedItem("name").getNodeValue() + "/" + type, ver);
                }
            }
        }
        logger.log(System.Logger.Level.INFO, "Successfully got version list from core site for " + relType);

    }


}
