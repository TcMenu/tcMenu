package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name="version")
public class VersionCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        var storage = new PrefsConfigurationStorage();
        System.out.println("TcMenu Designer V" + storage.getVersion() + " - " + storage.getReleaseType());
        System.out.println("Built on " + storage.getBuildTimestamp());

        // For beta releases make people aware they should not use it in production!
        if(storage.getReleaseType() == ConfigurationStorage.TcMenuReleaseType.BETA) {
            System.out.println("This is a BETA release and should only be used for evaluation.");
        }
        return 0;
    }
}
