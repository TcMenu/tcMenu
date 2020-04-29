/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;

import java.util.Map;

public interface LibraryVersionDetector {
    public Map<String, VersionInfo> acquireVersions(OnlineLibraryVersionDetector.ReleaseType relType);
}
