/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.google.gson.JsonParser;
import com.thecoderscorner.menu.editorui.util.IHttpClient;
import com.thecoderscorner.menu.persist.VersionInfo;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

public class GitHubAppVersionChecker implements AppVersionDetector {
    private static final System.Logger logger = System.getLogger(GitHubAppVersionChecker.class.getSimpleName());


    public final static String GITHUB_VERSIONING_URL = "https://api.github.com/repos/TcMenu/tcMenu/releases/latest";
    private static final long REFRESH_TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(4);

    private final String urlBase;
    private final IHttpClient client;

    private final Object cacheLock = new Object();
    private long lastAccess;
    private TcMenuRelease versionCache = new TcMenuRelease("", VersionInfo.ERROR_VERSION, LocalDateTime.MIN);

    public GitHubAppVersionChecker(String urlBase, IHttpClient client) {
        this.urlBase = urlBase;
        this.client = client;
    }

    public TcMenuRelease acquireVersion() {
        synchronized (cacheLock) {
            if ((System.currentTimeMillis() - lastAccess) < REFRESH_TIMEOUT_MILLIS) {
                return versionCache;
            }
        }

        try {
            logger.log(INFO, "Starting to acquire version, cache not present or timed out");
            var verData = client.getRequestForString(urlBase);
            logger.log(DEBUG, "Data acquisition from GitHub completed, reading response");

            var releaseData = JsonParser.parseReader(new StringReader(verData)).getAsJsonObject();
            var url = releaseData.get("html_url").getAsString();
            var when = DateTimeFormatter.ISO_DATE_TIME.parse(releaseData.get("published_at").getAsString());
            var version = VersionInfo.fromString(releaseData.get("tag_name").getAsString());
            var ver = new TcMenuRelease(url, version, LocalDateTime.from(when));

            synchronized (cacheLock) {
                lastAccess = System.currentTimeMillis();
                versionCache = ver;
            }
            logger.log(INFO, "Version acquired from GitHub successfully");
            return ver;
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Unable to get versions from main site", e);
        }
        return versionCache;
    }

    public record TcMenuRelease(String htmlUrl, VersionInfo version, LocalDateTime when) {
    }
}
