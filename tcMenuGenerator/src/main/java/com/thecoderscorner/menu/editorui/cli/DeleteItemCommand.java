package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.project.MenuTreeWithCodeOptions;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.persistProject;
import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.projectFileOrNull;
import static picocli.CommandLine.*;

@Command(name="delete-item")
public class DeleteItemCommand implements Callable<Integer> {
    @Option(names = { "-i", "--id"}, required = true)
    private int id;

    @Option(names = { "-r", "--recurse"}, defaultValue = "false")
    private boolean recurse;

    @CommandLine.Option(names = {"-f", "--emf-file"}, description = "emf file name")
    private File projectFile;

    @Override
    public Integer call() throws Exception {
        try {
            var project = projectFileOrNull(projectFile, new PrefsConfigurationStorage());

            if(id <= 0) throw new IllegalArgumentException("Cannot remove ROOT item!!");

            var item = project.getMenuTree().getMenuById(id);
            if(item.isEmpty()) throw new IllegalArgumentException("ID does not exist " + id);
            if(item.get() instanceof SubMenuItem && !recurse) throw new IllegalArgumentException("To delete submenu use --recurse option");

            project.getMenuTree().removeMenuItem(item.get());

            persistProject(project.getMenuTree(), project.getOptions());

            return 0;
        }
        catch(Exception ex) {
            System.out.println("Error during delete item " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            return -1;
        }
    }
}
