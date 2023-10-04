package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;

public class EditCustomDrawablesController {
    private GlobalSettings settings;
    private FormMenuComponent component;
    private JfxNavigationManager navMgr;
    private MenuItemStore store;

    public void initialise(GlobalSettings settings, FormMenuComponent component, JfxNavigationManager navMgr) {
        this.settings = settings;
        this.component = component;
        this.navMgr = navMgr;
        this.store = component.getStore();
    }

    public void closePressed() {

    }
}
