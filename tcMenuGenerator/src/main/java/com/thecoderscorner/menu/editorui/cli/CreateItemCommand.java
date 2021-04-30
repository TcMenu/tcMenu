package com.thecoderscorner.menu.editorui.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name="create-item")
public class CreateItemCommand implements Callable<Integer> {
    @Option(names = {"-p", "--parent"}, required = true, description = "parent id or name")
    String parent;

    @Option(names = {"-e", "--eeprom"}, description = "An EEPROM value, NONE, or AUTO", defaultValue = "NONE")
    String eeprom;

    @Option(names = {"-t", "--type"}, description = "One of analog, enum, boolean, submenu, float, action", required = true)
    String menuType;

    @Option(names = {"-n", "--name"}, description = "Name of the item (19 chars max)", required = true)
    String itemName;

    @Option(names = {"-c", "--callback"}, description = "The callback function or NONE, default NONE")
    String callback;

    @Option(names = {"-v", "--variable"}, description = "The variable name to be appended after menu")
    String varName;

    @Option(names = {"-r", "--readonly"}, description = "Mark the item as readonly")
    boolean readonly;

    @Option(names = {"-l", "--localonly"}, description = "Mark the item as local only")
    boolean localOnly;

    @Option(names = {"-x", "--hide"}, description = "Hide the item from view")
    boolean hide;

    @Override
    public Integer call() throws Exception {
        System.out.println("CreateItem not implemented");
        return -1;
    }
}
