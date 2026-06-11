package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.locateProjectFile;
import static com.thecoderscorner.menu.editorui.cli.CreateProjectCommand.enableI18nSupport;

@CommandLine.Command(name = "i18n-enable")
public class ConfigureI18nCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-f", "--emf-file"}, description = "emf file name, will be defaulted")
    private File projectFile;
    @CommandLine.Option(names = {"-l", "--locales"}, description = "A comma separated list of locales", required = true)
    private String locales;
    @CommandLine.Option(names = {"-v", "--verbose"}, description = "verbose logging")
    private boolean verbose;

    void testAccess(Path projectFile, String locales) {
        this.projectFile = projectFile.toFile();
        this.locales = locales;
    }

    @Override
    public Integer call() throws Exception {
        try {
            if(!StringHelper.isStringEmptyOrNull(locales)) {
                var emf = locateProjectFile(projectFile, false);
                var localeList = Arrays.stream(locales.split("\\s*,\\s*")).map(Locale::of).toList();
                enableI18nSupport(emf.getParentFile().toPath(), localeList, System.out::println, Optional.of(projectFile.toPath()));
            }
            return 0;
        } catch(Exception ex) {
            if(verbose)
                ex.printStackTrace();
            else
                System.out.format("Error while creating project %s, %s", ex.getClass().getSimpleName(), ex.getMessage());
            return -1;

        }
    }
}
