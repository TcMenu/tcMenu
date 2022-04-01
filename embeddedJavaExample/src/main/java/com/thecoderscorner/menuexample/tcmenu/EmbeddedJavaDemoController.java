package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxPanelLayoutEditorPresenter;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.TitleWidget;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ColorSettingsPresentable;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.mgr.MenuCallback;
import com.thecoderscorner.menu.mgr.MenuManagerListener;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmbeddedJavaDemoController implements MenuManagerListener {
    private final EmbeddedJavaDemoMenu  menuDef;
    private final JfxNavigationManager navigationManager;
    private final ScheduledExecutorService executorService;
    private final GlobalSettings globalSettings;
    private final ScreenLayoutPersistence layoutPersistence;

    public EmbeddedJavaDemoController(EmbeddedJavaDemoMenu menuDef, JfxNavigationManager navigationManager,
                                      ScheduledExecutorService executorService, GlobalSettings settings,
                                      ScreenLayoutPersistence layoutPersistence) {
        this.menuDef = menuDef;
        this.navigationManager = navigationManager;
        this.executorService = executorService;
        this.globalSettings = settings;
        this.layoutPersistence = layoutPersistence;
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
            TitleWidget<Image> wifiWidget = JfxNavigationHeader.standardWifiWidget();
            navigationManager.setItemEditorPresenter(new JfxPanelLayoutEditorPresenter(layoutPersistence, menuDef.getMenuTree(), navigationManager, globalSettings));
            navigationManager.addTitleWidget(wifiWidget);
            executorService.scheduleAtFixedRate(() -> wifiWidget.setCurrentState((int) (Math.random() * 5)), 1000, 100, TimeUnit.MILLISECONDS);

            TitleWidget<Image> settingsWidget = JfxNavigationHeader.standardSettingsWidget();
            navigationManager.addTitleWidget(settingsWidget);
            navigationManager.addWidgetClickedListener((actionEvent, widget) -> {
                    if(widget == settingsWidget) {
                        navigationManager.pushNavigation(new ColorSettingsPresentable(globalSettings, navigationManager, layoutPersistence, menuDef.getMenuTree()));
                    }
            });
        });
    }

    @Override
    public void managerWillStop() {
        // This is called just before the menu manager stops, you can do any shutdown tasks here.
    }

}
