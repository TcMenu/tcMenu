package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.LayoutBasedItemColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.LayoutEditorSettingsPresenter;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ItemSettingsPresentable;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;

import java.util.Optional;


public record JfxPanelLayoutEditorPresenter(
        ScreenLayoutPersistence layoutPersistence,
        MenuTree menuTree,
        JfxNavigationManager navigationManager,
        GlobalSettings globalSettings) implements LayoutEditorSettingsPresenter {

    @Override
    public void layoutEditorRequired(MenuItem item, ComponentSettings settings) {
        var customizer = layoutPersistence.getColorCustomizerFor(item, Optional.ofNullable(settings), true);
        if (customizer instanceof LayoutBasedItemColorCustomizable itemCustomizer) {
            var panel = new ItemSettingsPresentable(item, navigationManager, globalSettings, itemCustomizer);
            navigationManager.pushNavigation(panel);
        }
    }
}
