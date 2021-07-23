/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.plugin.LibraryUpgradeException;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector.*;

public interface LibraryVersionDetector {
    void changeReleaseType(ReleaseType releaseType);
    ReleaseType getReleaseType();

    Map<String, VersionInfo> acquireVersions();
    void upgradePlugin(String name, VersionInfo requestedVersion) throws LibraryUpgradeException;

    boolean availableVersionsAreValid(boolean refresh);

    Optional<List<VersionInfo>> acquireAllVersionsFor(String pluginName);
}
