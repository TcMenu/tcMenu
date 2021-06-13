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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.System.Logger.Level.*;

public class OnlineLibraryVersionDetector implements LibraryVersionDetector {
    private static final System.Logger logger = System.getLogger(OnlineLibraryVersionDetector.class.getSimpleName());

    public enum ReleaseType { STABLE, BETA, PREVIOUS }

    public final static String LIBRARY_VERSIONING_URL_APPEND = "/app/getLibraryVersions";
    private static final String PLUGIN_DOWNLOAD_URL_APPEND = "/app/downloadPlugin";
    private static final long REFRESH_TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(2);
    private static final int PLUGIN_API_VERSION = 2;

    private final String urlBase;
    private final IHttpClient client;

    private final Object cacheLock = new Object();
    private long lastAccess;
    private Map<String, VersionInfo> versionCache;
    private Map<String, List<VersionInfo>> allVersions;
    private ReleaseType cachedReleaseType;

    public OnlineLibraryVersionDetector(String urlBase, IHttpClient client, ReleaseType initialReleaseType) {
        this.client = client;
        this.urlBase = urlBase;
        changeReleaseType(initialReleaseType);
    }

    public void changeReleaseType(ReleaseType relType) {
        synchronized (cacheLock) {
            lastAccess = 0;
            versionCache = Map.of();
            allVersions = Map.of();
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
            logger.log(INFO, "Starting to acquire version, cache not present or timed out");
            var libDict = new HashMap<String, VersionInfo>();

            var verData = client.postRequestForString(urlBase + LIBRARY_VERSIONING_URL_APPEND, "pluginVer=" + PLUGIN_API_VERSION, IHttpClient.HttpDataType.FORM);
            var inStream = new ByteArrayInputStream(verData.getBytes());

            logger.log(INFO, "Data acquisition from server completed");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = factory.newDocumentBuilder();
            Document doc = dBuilder.parse(inStream);
            var root = doc.getDocumentElement();

            logger.log(INFO, "Document created");

            addVersionsToMap(root.getElementsByTagName("Libraries"), "Library", relType, libDict);
            addVersionsToMap(root.getElementsByTagName("Plugins"), "Plugin", relType, libDict);
            addVersionsToMap(root.getElementsByTagName("Apps"), "App", relType, libDict);


            var allVer = handleAllVersionBlockForPlugins(root.getElementsByTagName("AllVersions"));

            logger.log(INFO, "All done, saving out new versions.");

            synchronized (cacheLock) {
                lastAccess = System.currentTimeMillis();
                versionCache = libDict;
                allVersions = allVer;
            }
            return libDict;
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Unable to get versions from main site", e);
        }
        return versionCache;
    }

    private Map<String, List<VersionInfo>> handleAllVersionBlockForPlugins(NodeList allVersions) {
        if(allVersions == null || allVersions.getLength() == 0) return Map.of();
        var allVerMap = new HashMap<String, List<VersionInfo>>();
        logger.log(System.Logger.Level.INFO, "Starting to acquire version list from core site");
        for(int i=0; i< allVersions.getLength(); i++) {
            var item = allVersions.item(i);
            var name = item.getAttributes().getNamedItem("name").getNodeValue();
            var children = ((Element)item).getElementsByTagName("Version");
            var list = new ArrayList<VersionInfo>();
            for(int j=0; j<children.getLength(); j++) {
                try {
                    var versionData = children.item(j);
                    list.add(new VersionInfo(versionData.getAttributes().getNamedItem("ver").getTextContent()));
                }
                catch (Exception e) {
                    logger.log(WARNING, "Parse error on " + name);
                }
            }
            allVerMap.put(name, list);
        }
        return allVerMap;
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
        var pluginsFolder = Paths.get(System.getProperty("user.home"), ".tcmenu", "plugins");
        if (Files.exists(pluginsFolder.resolve(".git")) || Files.exists(pluginsFolder.resolve(".development"))) {
            throw new LibraryUpgradeException("Found .development or .git, not overwriting  " + name);
        }
        performUpgradeFromWeb(name, requestedVersion, pluginsFolder.resolve(name));
    }

    @Override
    public boolean availableVersionsAreValid(boolean doRefresh) {
        synchronized (cacheLock) {
            if (versionCache.isEmpty() || (System.currentTimeMillis() - lastAccess) > (REFRESH_TIMEOUT_MILLIS - 1000) && doRefresh) {
                acquireVersions();
            }
            return (!versionCache.isEmpty()) && ((System.currentTimeMillis() - lastAccess) < REFRESH_TIMEOUT_MILLIS);
        }
    }

    @Override
    public Optional<List<VersionInfo>> acquireAllVersionsFor(String pluginName) {
        if(availableVersionsAreValid(true)) {
            var allVer = allVersions.get(pluginName);
            return Optional.ofNullable(allVer);
        }
        return Optional.empty();
    }

    private void performUpgradeFromWeb(String name, VersionInfo requestedVersion, Path outDir) throws LibraryUpgradeException {
        try
        {
            if(!Files.exists(outDir)) Files.createDirectories(outDir);

            logger.log(INFO, "Upgrade in progress for " + name + " to " + requestedVersion);

            var json = "{\"name\": \"" + name + "\", \"version\": \"" + requestedVersion + "\"}";
            byte[] data = client.postRequestForBinaryData(urlBase + PLUGIN_DOWNLOAD_URL_APPEND, json, IHttpClient.HttpDataType.JSON_DATA);
            var inStream = new ByteArrayInputStream(data);

            extractFilesFromZip(outDir, inStream);
        }
        catch (Exception ex)
        {
            logger.log(ERROR, "Could not update " + name,ex);
            throw new LibraryUpgradeException(ex.getMessage());
        }
    }

    public static void extractFilesFromZip(Path outDir, InputStream inStream) throws IOException {
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
}
