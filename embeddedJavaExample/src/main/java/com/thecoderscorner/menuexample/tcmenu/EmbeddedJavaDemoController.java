package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.menu.mgr.*;
import com.thecoderscorner.menu.domain.*;

public class EmbeddedJavaDemoController implements MenuManagerListener {
    private final EmbeddedJavaDemoMenu  menuDef;
    
    public EmbeddedJavaDemoController(EmbeddedJavaDemoMenu menuDef) {
        this.menuDef = menuDef;
    }

    @MenuCallback(id=1)
    public void led1BrightnessHasChanged(AnalogMenuItem item, boolean remoteAction) {
        // TODO - implement your menu behaviour here for LED1  brightness
    }

    @MenuCallback(id=2)
    public void led2BrightnessHasChanged(AnalogMenuItem item, boolean remoteAction) {
        // TODO - implement your menu behaviour here for LED2 brightness
    }

    @MenuCallback(id=3)
    public void inputControlHasChanged(EnumMenuItem item, boolean remoteAction) {
        // TODO - implement your menu behaviour here for Input Control
    }

    public void menuItemHasChanged(MenuItem item, boolean remoteAction) {
        // Called every time any menu item changes
    }

    @Override
    public void managerWillStart() {
        // This is called just before the menu manager starts up, you can initialise your system here.
    }

    @Override
    public void managerWillStop() {
        // This is called just before the menu manager stops, you can do any shutdown tasks here.
    }

}
