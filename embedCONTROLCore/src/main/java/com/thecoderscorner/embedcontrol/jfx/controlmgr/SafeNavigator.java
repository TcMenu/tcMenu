package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Safe navigator provides a way to navigate to a web page that works on all platforms. You just provide the web address
 * to the safeNavigateTo method.
 */
public class SafeNavigator {
    private final static System.Logger logger = System.getLogger(SafeNavigator.class.getSimpleName());

    /**
     * Navigate to a given URL in a way that works on all platforms.
     * @param url the address to navigate to.
     */
    public static void safeNavigateTo(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        if(Desktop.isDesktopSupported() && !os.contains("inux")){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                logger.log(System.Logger.Level.WARNING, "Didn't browse to URL ", e);
            }
        } else if(os.contains("inux")) {

            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                logger.log(System.Logger.Level.WARNING, "Did not xdg open", e);
            }
        }
    }
}
