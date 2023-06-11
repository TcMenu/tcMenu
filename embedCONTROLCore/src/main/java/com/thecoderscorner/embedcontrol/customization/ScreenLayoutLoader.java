package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.MenuTree;

import java.nio.file.Path;
import java.util.*;

public class ScreenLayoutLoader {
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    protected final GlobalSettings globalSettings;
    protected UUID appUuid;
    protected MenuItemStore store;
    protected MenuTree tree;
    private Optional<Path> layoutPath;

    public ScreenLayoutLoader(MenuTree tree, GlobalSettings settings, UUID appUuid, Path path, FontInformation defFont,
                              Optional<Path> layoutPath) {
        this.globalSettings = settings;
        this.tree = tree;
        this.appUuid = appUuid;
        this.layoutPath = layoutPath;
        store = new MenuItemStore(settings, tree, "Untitled",  1, 4, true);
        store.setGlobalFontInfo(defFont);
    }

    public void loadApplicationData() {
        try {
            if(layoutPath.isEmpty()) return;
            store.loadLayout(String.valueOf(layoutPath), appUuid);
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Layout restore failed for " + layoutPath, e);
        }
    }

    public void remoteApplicationDidLoad(UUID appUuid, MenuTree tree) {
        this.appUuid = appUuid;
        this.tree = tree;
    }
}
