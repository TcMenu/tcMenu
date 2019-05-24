/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

public class Esp32WifiRemoteCreator extends Esp8266WifiRemoteCreator {
    @Override
    protected String getWifiInclude() {
        return "WiFi.h";
    }
}
