package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ColorSettingsController;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.GeneralSettingsPresentable;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.mgr.MenuCallback;
import com.thecoderscorner.menu.mgr.MenuManagerListener;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmbeddedJavaDemoController implements MenuManagerListener {
    private final EmbeddedJavaDemoMenu  menuDef;
    private final JfxNavigationManager navigationManager;
    private final ScheduledExecutorService executorService;
    private final GlobalSettings globalSettings;

    public EmbeddedJavaDemoController(EmbeddedJavaDemoMenu menuDef, JfxNavigationManager navigationManager,
                                      ScheduledExecutorService executorService, GlobalSettings settings) {
        this.menuDef = menuDef;
        this.navigationManager = navigationManager;
        this.executorService = executorService;
        this.globalSettings = settings;
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
        Platform.runLater(() -> {
            var wifiWidget = JfxNavigationHeader.widgetFromImages(
                    JfxNavigationHeader.class.getResource("/img/con-fail.png"),
                    JfxNavigationHeader.class.getResource("/img/wifi-poor.png"),
                    JfxNavigationHeader.class.getResource("/img/wifi-low.png"),
                    JfxNavigationHeader.class.getResource("/img/wifi-fair.png"),
                    JfxNavigationHeader.class.getResource("/img/wifi-full.png")
            );
            navigationManager.addTitleWidget(wifiWidget);
            executorService.scheduleAtFixedRate(() -> wifiWidget.setCurrentState((int) (Math.random() * 5)), 1000, 100, TimeUnit.MILLISECONDS);

            var configurationWidget = JfxNavigationHeader.widgetFromImages(
                    JfxNavigationHeader.class.getResource("/img/settings-cog.png")
            );
            navigationManager.addTitleWidget(configurationWidget);
            navigationManager.addWidgetClickedListener((actionEvent, widget) -> {
                    if(widget == configurationWidget) {
                        showGlobalColorConfiguration();
                    }
            });
        });
    }

    @Override
    public void managerWillStop() {
        // This is called just before the menu manager stops, you can do any shutdown tasks here.
    }

    private void showGlobalColorConfiguration() {
        var colorRanges = new HashMap<String, ColorCustomizable>();
        colorRanges.put(ColorSettingsController.DEFAULT_COLOR_NAME, new GlobalColorCustomizable(globalSettings));
        navigationManager.pushNavigation(new GeneralSettingsPresentable(globalSettings, navigationManager, colorRanges));
    }
}
