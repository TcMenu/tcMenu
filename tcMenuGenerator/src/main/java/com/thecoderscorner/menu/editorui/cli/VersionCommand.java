package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.controller.PrefsConfigurationStorage;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name="version")
public class VersionCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        var storage = new PrefsConfigurationStorage();
        System.out.println("TcMenu Designer V" + storage.getVersion() + " - " + storage.getReleaseType());
        System.out.println("Built on " + storage.getBuildTimestamp());
        return 0;
    }
}
