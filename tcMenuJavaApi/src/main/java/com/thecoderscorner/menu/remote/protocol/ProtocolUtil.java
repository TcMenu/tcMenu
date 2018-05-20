/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import com.google.common.collect.ImmutableMap;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A few general helper method to get the version and platform information to and from messages.
 */
public class ProtocolUtil {
    private static AtomicReference<String> version = new AtomicReference<>();
    private static AtomicReference<Map<Integer, ApiPlatform>> keyToPlatform = new AtomicReference<>();
    private static Pattern versionPattern = Pattern.compile(".*(\\d+)\\.(\\d+).*");

    /**
     * gets and caches the current version from the version properties file
     * @return the current version as major * 100 + minor
     */
    public static int getVersionFromProperties() {
        String ver = version.get();
        if(ver == null) {
            try {
                InputStream resourceAsStream = ProtocolUtil.class.getResourceAsStream("/japi-version.properties");
                Properties props = new Properties();
                props.load( resourceAsStream );

                ver = props.getProperty("build.version");
                Matcher verMatch = versionPattern.matcher(ver);
                if(verMatch.matches() && verMatch.groupCount() == 2) {
                    int major = Integer.parseInt(verMatch.group(1));
                    int minor = Integer.parseInt(verMatch.group(2));
                    return (major * 100) + minor;
                }

            } catch (Exception e) {
                LoggerFactory.getLogger(ProtocolUtil.class).info("Did not successfully obtain version", e);
            }
        }
        return 0;
    }

    /**
     * get the api platform given it's integer key value.
     * @param key the platform key
     * @return platform enum entry.
     */
    public static ApiPlatform fromKeyToApiPlatform(int key) {
        if(keyToPlatform.get() == null) {
            ImmutableMap.Builder<Integer, ApiPlatform> builder = ImmutableMap.builder();
            for (ApiPlatform apiPlatform : ApiPlatform.values()) {
                builder.put(apiPlatform.getKey(), apiPlatform);
            }
            keyToPlatform.set(builder.build());
        }
        return keyToPlatform.get().get(key);
    }
}
