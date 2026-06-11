package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.uimodel.UrlsForDocumentation;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "online-help")
public class OnlineHelpCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("TcMenu CLI online help locations");
        System.out.println("================================");
        System.out.println("");
        System.out.println("To get help with command syntax just type 'tcmenu help' or 'tcmenu command --help'");
        System.out.println("");
        System.out.println("Menu Items docs:   " + UrlsForDocumentation.MENU_MENU_ITEM_URL);
        System.out.println("CLI Documentation: " + UrlsForDocumentation.CLI_DOCUMENTATION_URL);
        System.out.println("Main TcMenu page:  " + UrlsForDocumentation.MAIN_TCMENU_URL);
        return 0;
    }
}