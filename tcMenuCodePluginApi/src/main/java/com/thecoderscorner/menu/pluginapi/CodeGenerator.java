/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import com.thecoderscorner.menu.domain.state.MenuTree;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface CodeGenerator {
    boolean startConversion(Path directory, List<EmbeddedCodeCreator> generators, MenuTree menuTree);
    void setLoggerFunction(Consumer<String> logLine);
}
