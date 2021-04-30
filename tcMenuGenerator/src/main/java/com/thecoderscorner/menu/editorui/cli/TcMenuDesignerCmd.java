package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import javafx.application.Application;
import picocli.CommandLine;

import static picocli.CommandLine.*;

@Command(name = "tcMenuDesigner", versionProvider = CliVersionProvider.class, subcommands = {
        CodeGeneratorCommand.class,
        CodeVerificationCommand.class,
        CreateProjectCommand.class
})
public class TcMenuDesignerCmd {

    public static void main(String[] args) {
        if(args.length == 0) {
            Application.launch(MenuEditorApp.class, args);
        }
        else {
            CommandLine commandLine = new CommandLine(new TcMenuDesignerCmd());
            var res = commandLine.execute(args);
            if (res != 0) {
                System.exit(res);
            }
        }
    }
}
