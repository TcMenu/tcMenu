package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "list-platforms")
public class ListPlatformsCommand implements Callable<Integer> {
    PluginEmbeddedPlatformsImpl platforms = new PluginEmbeddedPlatformsImpl();

    @Override
    public Integer call() throws Exception {
        System.out.println("PlatformID       Description                        Progmem   Platform");
        System.out.println("----------------------------------------------------------------------");
        for(var pl : platforms.getEmbeddedPlatforms()) {
            System.out.format("%-17s%-35s%-10s%s",pl.getBoardId(), pl, pl.isUsesProgmem(), getBoardInfo(pl));
            System.out.println();
        }
        return 0;
    }

    private String getBoardInfo(EmbeddedPlatform pl) {
        if(platforms.isNativeCpp(pl)) return "mbedOS";
        else if(platforms.isArduino(pl)) return "Arduino";
        else if(platforms.isJava(pl)) return "Java";
        else return "Unknown";
    }
}

