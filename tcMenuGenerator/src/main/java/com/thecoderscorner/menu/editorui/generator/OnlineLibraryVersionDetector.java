/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.plugin.LibraryUpgradeException;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.util.IHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.System.Logger.Level.*;

public class OnlineLibraryVersionDetector implements LibraryVersionDetector {
    public enum ReleaseType { STABLE, BETA, PREVIOUS }

    public final static String LIBRARY_VERSIONING_URL = "http://thecoderscorner.com/tcc/app/getLibraryVersions";
    private static final long REFRESH_TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(2);
    private static final String PLUGIN_DOWNLOAD_URL = "http://thecoderscorner.com/tcc/app/downloadPlugin";
    private static final int PLUGIN_API_VERSION = 2;

    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final IHttpClient client;

    private final Object cacheLock = new Object();
    private long lastAccess;
    private Map<String, VersionInfo> versionCache;
    private ReleaseType cachedReleaseType;

    public OnlineLibraryVersionDetector(IHttpClient client, ReleaseType initialReleaseType) {
        this.client = client;
        changeReleaseType(initialReleaseType);
    }

    public void changeReleaseType(ReleaseType relType) {
        synchronized (cacheLock) {
            lastAccess = 0;
            versionCache = Map.of();
            cachedReleaseType = relType;
        }
    }

    public ReleaseType getReleaseType() {
        synchronized (cacheLock) {
            return cachedReleaseType;
        }
    }


    public Map<String, VersionInfo> acquireVersions() {
        ReleaseType relType;
        synchronized (cacheLock) {
            if (!versionCache.isEmpty() && (System.currentTimeMillis() - lastAccess) < REFRESH_TIMEOUT_MILLIS) {
                return versionCache;
            }
            relType = cachedReleaseType;
        }

        try {
            var libDict = new HashMap<String, VersionInfo>();

            var verData = client.postRequestForString(LIBRARY_VERSIONING_URL, "pluginVer=" + PLUGIN_API_VERSION, IHttpClient.HttpDataType.FORM);
            var inStream = new ByteArrayInputStream(verData.getBytes());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = factory.newDocumentBuilder();
            Document doc = dBuilder.parse(inStream);
            var root = doc.getDocumentElement();

            addVersionsToMap(root.getElementsByTagName("Libraries"), "Library", relType, libDict);
            addVersionsToMap(root.getElementsByTagName("Plugins"), "Plugin", relType, libDict);
            synchronized (cacheLock) {
                lastAccess = System.currentTimeMillis();
                versionCache = libDict;
            }
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
        logger.log(System.Logger.Level.INFO, "Successfully got version list from core site for " + cachedReleaseType);
    }

    public void upgradePlugin(String name, VersionInfo requestedVersion) throws LibraryUpgradeException {
        var pluginsFolder = Paths.get(System.getProperty("user.home"), ".tcmenu", "plugins", name);
        if (Files.exists(pluginsFolder.resolve(".git"))) throw new LibraryUpgradeException("Not overwriting git repo " + name);
        performUpgradeFromWeb(name, requestedVersion, pluginsFolder);
    }

    private void performUpgradeFromWeb(String name, VersionInfo requestedVersion, Path outDir) throws LibraryUpgradeException {
        try
        {
            if(!Files.exists(outDir)) Files.createDirectories(outDir);

            logger.log(INFO, "Upgrade in progress for " + name + " to " + requestedVersion);

            var json = "{\"name\": \"" + name + "\", \"version\": \"" + requestedVersion + "\"}";
            byte[] data = client.postRequestForBinaryData(PLUGIN_DOWNLOAD_URL, json, IHttpClient.HttpDataType.JSON_DATA);
            var inStream = new ByteArrayInputStream(data);

            try(var zipStream =  new ZipInputStream(inStream)) {
                ZipEntry entry;
                while((entry = zipStream.getNextEntry())!=null) {
                    Path filePath = outDir.resolve(entry.getName());
                    String fileInfo = String.format("Entry: [%s] len %d to %s", entry.getName(), entry.getSize(), filePath);
                    logger.log(DEBUG, fileInfo);
                    if(entry.isDirectory()) {
                        Files.createDirectories(filePath);
                    }
                    else {
                        Files.write(filePath, zipStream.readAllBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.log(ERROR, "Could not update " + name,ex);
            throw new LibraryUpgradeException(ex.getMessage());
        }
    }
}
