package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.controller.PrefsConfigurationStorage;
import picocli.CommandLine;

/**
 * Returns the version in the usual format x.x.x for the CLI to report during help commands.
 */
public class CliVersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        var storage = new PrefsConfigurationStorage();
        return new String[]{storage.getVersion()};
    }
}
