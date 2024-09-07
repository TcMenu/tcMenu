package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import javafx.application.Application;
import javafx.application.Platform;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.locateProjectFile;
import static picocli.CommandLine.Command;

@Command(name="gui")
public class StartUICommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-f", "--emf-file"}, description = "emf file name")
    private File projectFile;

    @CommandLine.Option(names = {"-c", "--create"}, description = "create emf if needed")
    private boolean createIfNeeded;

    @CommandLine.Option(names = {"-p", "--preview"}, description = "start preview window for form editing etc")
    private boolean previewAtStart;

    public static AtomicReference<String> userSelectedProject = new AtomicReference<>(null);

    public static boolean didUserSelectProject() {
        return userSelectedProject.get() != null;
    }

    public static String userSelectedProject() {
        return userSelectedProject.get();
    }

    public static void userDidSelectProject(File projectFile) {
        userSelectedProject.set(projectFile.getAbsolutePath());
        System.out.println("Designer is starting with project " + userSelectedProject.get());

    }

    @Override
    public Integer call() {
        try {
            projectFile = locateProjectFile(projectFile, createIfNeeded);
            userDidSelectProject(projectFile);
            if(previewAtStart) {
                Executors.newSingleThreadScheduledExecutor().schedule(() ->
                    Platform.runLater(() -> MenuEditorApp.getContext().previewOnProject(projectFile.toPath()))
                    ,2, TimeUnit.SECONDS);
            }
            Application.launch(MenuEditorApp.class, "");

        }
        catch (Exception ex) {
            System.out.format("There does not seem to be a valid EMF project: %s - %s\n",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
        return 0;
    }
}
