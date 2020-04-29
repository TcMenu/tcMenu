/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.util;

import java.io.IOException;

public interface IHttpClient {
    enum HttpDataType { XML_DATA, JSON_DATA }
    byte[] postRequestForBinaryData(String url, String parameter, HttpDataType reqDataType) throws IOException, InterruptedException;
    String postRequestForString(String url, String parameter, HttpDataType reqDataType) throws IOException, InterruptedException;
}
