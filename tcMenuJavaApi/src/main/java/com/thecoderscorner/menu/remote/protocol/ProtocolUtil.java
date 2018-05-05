/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class ProtocolUtil {
    private static AtomicReference<String> version = new AtomicReference<>();

    public static String getVersionFromProperties() {
        String ver = version.get();
        if(ver == null) {
            try {
                InputStream resourceAsStream = ProtocolUtil.class.getResourceAsStream("/japi-version.properties");
                Properties props = new Properties();
                props.load( resourceAsStream );

                ver = "api" + props.getProperty("api.name") + "_" + props.getProperty("build.version");

            } catch (IOException e) {
                ver = "ERROR";
            }
        }
        return ver;
    }
}
