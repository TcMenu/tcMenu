/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.util.IHttpClient;
import com.thecoderscorner.menu.persist.VersionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class OnlineLibraryVersionDetectorTest {
    private GitHubAppVersionChecker verDet;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        var mockHttp = Mockito.mock(IHttpClient.class);
        when(mockHttp.getRequestForString("https://mockAddr")).thenReturn(returnFromUrl);
        verDet = new GitHubAppVersionChecker("https://mockAddr", mockHttp);
    }

    @Test
    public void testReadingXmlOverMockHttp() {
        var r = verDet.acquireVersion();

        assertEquals(VersionInfo.fromString("4.3.0"), r.version());
        assertEquals("https://github.com/TcMenu/tcMenu/releases/tag/4.3.0", r.htmlUrl());
        assertEquals(LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse("2024-08-04T07:47:05Z")), r.when());
    }

    private String returnFromUrl = """
            {
              "url": "https://api.github.com/repos/TcMenu/tcMenu/releases/168594723",
              "assets_url": "https://api.github.com/repos/TcMenu/tcMenu/releases/168594723/assets",
              "upload_url": "https://uploads.github.com/repos/TcMenu/tcMenu/releases/168594723/assets{?name,label}",
              "html_url": "https://github.com/TcMenu/tcMenu/releases/tag/4.3.0",
              "id": 168594723,
              "author": {
                "login": "davetcc",
                "id": 12195465,
                "node_id": "MDQ6VXNlcjEyMTk1NDY1",
                "avatar_url": "https://avatars.githubusercontent.com/u/12195465?v=4",
                "gravatar_id": "",
                "url": "https://api.github.com/users/davetcc",
                "html_url": "https://github.com/davetcc",
                "followers_url": "https://api.github.com/users/davetcc/followers",
                "following_url": "https://api.github.com/users/davetcc/following{/other_user}",
                "gists_url": "https://api.github.com/users/davetcc/gists{/gist_id}",
                "starred_url": "https://api.github.com/users/davetcc/starred{/owner}{/repo}",
                "subscriptions_url": "https://api.github.com/users/davetcc/subscriptions",
                "organizations_url": "https://api.github.com/users/davetcc/orgs",
                "repos_url": "https://api.github.com/users/davetcc/repos",
                "events_url": "https://api.github.com/users/davetcc/events{/privacy}",
                "received_events_url": "https://api.github.com/users/davetcc/received_events",
                "type": "User",
                "site_admin": false
              },
              "node_id": "RE_kwDOB8a7WM4KDI0j",
              "tag_name": "4.3.0",
              "target_commitish": "main",
              "name": "4.3.0 - Major Improvements and fixes",
              "draft": false,
              "prerelease": false,
              "created_at": "2024-08-03T20:39:45Z",
              "published_at": "2024-08-04T07:47:05Z"
            }
            """;
}