package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import javafx.application.Application;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static picocli.CommandLine.Command;

@Command(name = "tcMenuDesigner", versionProvider = CliVersionProvider.class, subcommands = {
        ApiTestCommand.class,
        CodeGeneratorCommand.class,
        CodeVerificationCommand.class,
        CreateProjectCommand.class,
        CreateItemCommand.class,
        DeleteItemCommand.class,
        VersionCommand.class,
        OnlineHelpCommand.class,
        StartUICommand.class,
        GetConfigCommand.class,
        SetConfigCommand.class,
        ListPlatformsCommand.class,
        WrapWebServerFilesCommand.class,
        ConfigureI18nCommand.class,
        ConvertExtAsciiUtf8Command.class
})
public class TcMenuDesignerCmd {

    public static void main(String[] args) {
        if(args.length == 0) {
            // no args, launch UI
            Application.launch(MenuEditorApp.class, args);
        } else if(args.length == 1 && args[0].toLowerCase().endsWith(".emf") && Files.exists(Path.of(args[0]))) {
            // if the only arg ends with ".emf" and it exists then it is most likely user double-clicked on a file
            StartUICommand.userDidSelectProject(new File(args[0]));
            Application.launch(MenuEditorApp.class, args);
        } else {
            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n");
            CommandLine commandLine = new CommandLine(new TcMenuDesignerCmd());
            String[] cliArgs;
            if(args[0].equals("help")) {
                cliArgs = new String[0];
            }
            else {
                cliArgs = args;
            }
            var res = commandLine.execute(cliArgs);
            if (res != 0) {
                System.exit(res);
            }
        }
    }
}
