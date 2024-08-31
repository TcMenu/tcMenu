/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import static com.thecoderscorner.menu.editorui.generator.GitHubAppVersionChecker.TcMenuRelease;

public interface AppVersionDetector {
    TcMenuRelease acquireVersion();
}
