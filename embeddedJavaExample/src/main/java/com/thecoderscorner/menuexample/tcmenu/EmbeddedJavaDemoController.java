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
import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.state.ListResponse;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.MenuCallback;
import com.thecoderscorner.menu.mgr.MenuManagerListener;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.INFO;

public class EmbeddedJavaDemoController implements MenuManagerListener {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
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

    @MenuCallback(id=15, listResult=true)
    public void listHasChanged(Object sender, RuntimeListMenuItem item, ListResponse listResponse) {
        logger.log(INFO, String.format("List %s has changed: %s", item, listResponse));
        navigationManager.getDialogManager().withTitle("List Change on " + item.getName(), false)
                .withMessage(String.format("Action was %s on row %d", listResponse.getResponseType(), listResponse.getRow()), false)
                .showDialogWithButtons(MenuButtonType.NONE, MenuButtonType.CLOSE);
    }

    @MenuCallback(id=1)
    public void led1BrightnessHasChanged(Object sender, AnalogMenuItem item) {
        // TODO - implement your menu behaviour here for LED1  brightness
    }

    @MenuCallback(id=2)
    public void led2BrightnessHasChanged(Object sender, AnalogMenuItem item) {
        // TODO - implement your menu behaviour here for LED2 brightness
    }

    @MenuCallback(id=3)
    public void inputControlHasChanged(Object sender, EnumMenuItem item) {
        // TODO - implement your menu behaviour here for Input Control
    }

    // Auto generated menu callbacks end here. Please do not remove this line or change code after it.

    public void menuItemHasChanged(Object sender, MenuItem item) {
        // Called every time any menu item changes
    }

    @Override
    public void managerWillStart() {
        Platform.runLater(() -> {
            if(globalSettings.isSetupLayoutModeEnabled()) {
                navigationManager.setItemEditorPresenter(new JfxPanelLayoutEditorPresenter(layoutPersistence, menuDef.getMenuTree(), navigationManager, globalSettings));
            }

            TitleWidget<Image> wifiWidget = JfxNavigationHeader.standardWifiWidget();
            navigationManager.addTitleWidget(wifiWidget);
            executorService.scheduleAtFixedRate(() -> wifiWidget.setCurrentState((int) (Math.random() * 5)), 1000, 100, TimeUnit.MILLISECONDS);

            TitleWidget<Image> layoutWidget = JfxNavigationHeader.standardLayoutWidget();
            layoutWidget.setCurrentState(globalSettings.isSetupLayoutModeEnabled() ? 1 : 0);
            navigationManager.addTitleWidget(layoutWidget);

            TitleWidget<Image> settingsWidget = JfxNavigationHeader.standardSettingsWidget();
            navigationManager.addTitleWidget(settingsWidget);

            navigationManager.addWidgetClickedListener((actionEvent, widget) -> {
                    if(widget == settingsWidget) {
                        navigationManager.pushNavigation(new ColorSettingsPresentable(globalSettings, navigationManager, layoutPersistence, menuDef.getMenuTree()));
                    }
                    else if(widget == layoutWidget) {
                        globalSettings.setSetupLayoutModeEnabled(!globalSettings.isSetupLayoutModeEnabled());
                        globalSettings.save();
                        layoutWidget.setCurrentState(globalSettings.isSetupLayoutModeEnabled() ? 1 : 0);
                    }
            });

            MenuItemHelper.setMenuState(menuDef.getStatusMyListItem(), List.of("Item 1", "Item 2", "Item 3"), menuDef.getMenuTree());
        });
    }

    @Override
    public void managerWillStop() {
        // This is called just before the menu manager stops, you can do any shutdown tasks here.
    }

}
