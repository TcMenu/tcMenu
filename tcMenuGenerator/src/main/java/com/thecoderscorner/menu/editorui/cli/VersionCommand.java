package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.MenuEditorConfig;
import com.thecoderscorner.menu.persist.ReleaseType;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;

@Command(name="version")
public class VersionCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        var appContext = new AnnotationConfigApplicationContext(MenuEditorConfig.class);
        var storage = appContext.getBean(ConfigurationStorage.class);
        System.out.println("TcMenu Designer V" + storage.getVersion() + " - " + storage.getReleaseType());
        System.out.println("Built on " + storage.getBuildTimestamp());

        // For beta releases make people aware they should not use it in production!
        if(storage.getReleaseType() == ReleaseType.BETA) {
            System.out.println("This is a BETA release and should only be used for evaluation.");
        }
        return 0;
    }
}
