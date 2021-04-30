package com.thecoderscorner.menu.editorui.cli;

import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name="delete-item")
public class DeleteItemCommand implements Callable<Integer> {
    @Option(names = { "-i", "--id"}, required = true)
    private int id;

    @Option(names = { "--force"}, defaultValue = "false")
    private boolean force;

    @Option(names = { "-r", "--recurse"}, defaultValue = "false")
    private boolean recurse;

    @CommandLine.Option(names = {"-f", "--emf-file"}, description = "emf file name")
    private File projectFile;

    @Override
    public Integer call() throws Exception {
        System.out.println("DeleteItem not implemented");
        return -1;
    }
}
