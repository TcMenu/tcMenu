package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import javafx.application.Application;
import picocli.CommandLine;

import static picocli.CommandLine.*;

@Command(name = "tcMenuDesigner", versionProvider = CliVersionProvider.class, subcommands = {
        CodeGeneratorCommand.class,
        CodeVerificationCommand.class,
        CreateProjectCommand.class,
        CreateItemCommand.class,
        DeleteItemCommand.class,
        VersionCommand.class,
        StartUICommand.class,
        GetConfigCommand.class,
        SetConfigCommand.class,
        ListPlatformsCommand.class,
        WrapWebServerFilesCommand.class
})
public class TcMenuDesignerCmd {

    public static void main(String[] args) {
        if(args.length == 0) {
            Application.launch(MenuEditorApp.class, args);
        }
        else {
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
