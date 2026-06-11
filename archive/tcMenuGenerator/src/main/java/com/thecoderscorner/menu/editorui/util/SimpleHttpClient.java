/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SimpleHttpClient implements IHttpClient {
    private static final int HTTP_SUCCESS = 200;
    private final HttpClient client = HttpClient.newBuilder().build();

    @Override
    public byte[] postRequestForBinaryData(String url, String parameter,
                                    HttpDataType reqDataType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", toContentType(reqDataType))
                .POST(HttpRequest.BodyPublishers.ofString(parameter))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if(response.statusCode() != HTTP_SUCCESS) throw new IOException("Call returned bad status " + response.statusCode());
        return response.body();

    }

    private String toContentType(HttpDataType datatype) {
        switch (datatype) {
            case XML_DATA:
                return "application/xml";
            case FORM:
                return "application/x-www-form-urlencoded";
            case JSON_DATA:
            default:
                return "application/json";
        }
    }

    @Override
    public String getRequestForString(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != HTTP_SUCCESS) throw new IOException("Call returned bad status " + response.statusCode());
        return response.body();
    }

    @Override
    public String postRequestForString(String url, String parameter,
                                           HttpDataType reqDataType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", toContentType(reqDataType))
                .POST(HttpRequest.BodyPublishers.ofString(parameter))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != HTTP_SUCCESS) throw new IOException("Call returned bad status " + response.statusCode());
        return response.body();
    }
}
