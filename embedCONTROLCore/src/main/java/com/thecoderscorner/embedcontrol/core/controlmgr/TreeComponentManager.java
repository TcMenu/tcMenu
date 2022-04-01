package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.AuthStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Tree component manager takes menu structures and turns them into an auto UI using the `ScreenManager` object which
 * creates components of type `EditorComponent`. Each editor component has a UI element associated with it and can be
 * presented onto a UI. For an automatic UI, we normally create a suitable ScreenManager object, then create an
 * extension of TreeComponentManager to render it.
 * @param <T> is the base UI class for this item.
 */
public class TreeComponentManager<T> {
    protected final Map<Integer, EditorComponent<T>> editorComponents = new HashMap<>();
    protected final ScheduledExecutorService executor;
    protected final ThreadMarshaller marshaller;
    protected final GlobalSettings appSettings;
    protected final MenuComponentControl controller;
    protected final ScheduledFuture<?> remoteTickTask;
    private final ScreenLayoutPersistence layoutPersistence;

    public TreeComponentManager(GlobalSettings appSettings, ScheduledExecutorService executor,
                                ThreadMarshaller marshaller, MenuComponentControl controller, ScreenLayoutPersistence layoutPersistence) {
        this.appSettings = appSettings;
        this.executor = executor;
        this.marshaller = marshaller;
        this.controller = controller;
        this.layoutPersistence = layoutPersistence;

        remoteTickTask = executor.scheduleAtFixedRate(this::timerTick, 100, 100, TimeUnit.MILLISECONDS);
    }

    protected void connectionChanged(AuthStatus status) {
    }

    public void renderMenuRecursive(MenuControlGrid<T> layoutControlGrid, SubMenuItem sub, boolean recurse) {
        renderMenuRecursive(layoutControlGrid, sub, recurse, 0);
    }

    public void renderMenuRecursive(MenuControlGrid<T> layoutControlGrid, SubMenuItem sub, boolean recurse, int level) {
        var tree = controller.getMenuTree();

        if(level != 0) {
            layoutControlGrid.addStaticLabel(sub.getName(), layoutPersistence.getSettingsForStaticItem(sub, 1, true), true);
        }

        layoutControlGrid.startNesting();
        for (var item : tree.getMenuItems(sub)) {
            if (!item.isVisible()) continue;
            if (item instanceof SubMenuItem && recurse) {
                renderMenuRecursive(layoutControlGrid, (SubMenuItem) item, recurse, level + 1);
            }
            else {
                if(item instanceof RuntimeListMenuItem) {
                    layoutControlGrid.addStaticLabel(item.getName(), layoutPersistence.getSettingsForStaticItem(sub, 2, true), false);
                }

                getComponentEditorItem(layoutControlGrid, item).ifPresent(comp -> editorComponents.put(item.getId(), comp));
            }

            if (editorComponents.containsKey(item.getId()) && tree.getMenuState(item) != null) {
                editorComponents.get(item.getId()).onItemUpdated(tree.getMenuState(item));
            }
        }

        layoutControlGrid.endNesting();
    }

    public Optional<EditorComponent<T>> getComponentEditorItem(MenuControlGrid<T> layoutControlGrid, MenuItem item) {
        var componentSettings = layoutPersistence.getSettingsForMenuItem(item, true);

        if(componentSettings.getDrawMode() == RedrawingMode.HIDDEN) return Optional.empty();

        if(item instanceof SubMenuItem sub) {
            return Optional.of(layoutControlGrid.addButtonWithAction(sub, sub.getName(), componentSettings,
                    subMenuItem -> controller.getNavigationManager().pushMenuNavigation(subMenuItem)));
        }

        return Optional.ofNullable(switch(componentSettings.getControlType()) {
            case HORIZONTAL_SLIDER -> layoutControlGrid.addHorizontalSlider(item, componentSettings);
            case UP_DOWN_CONTROL -> layoutControlGrid.addUpDownControl(item, componentSettings);
            case TEXT_CONTROL -> layoutControlGrid.addTextEditor(item, componentSettings, MenuItemHelper.getDefaultFor(item));
            case BUTTON_CONTROL -> layoutControlGrid.addBooleanButton(item, componentSettings);
            case VU_METER -> throw new UnsupportedOperationException("VU TODO");
            case DATE_CONTROL -> layoutControlGrid.addDateEditorComponent(item, componentSettings);
            case TIME_CONTROL -> layoutControlGrid.addTimeEditorComponent(item, componentSettings);
            case RGB_CONTROL -> layoutControlGrid.addRgbColorControl(item, componentSettings);
            case LIST_CONTROL -> layoutControlGrid.addListEditor(item, componentSettings);
            case CANT_RENDER -> null;
        });
    }

    public void timerTick() {
        marshaller.runOnUiThread(() -> {
            for (var component : editorComponents.values()) {
                component.tick();
            }
        });
    }

    public void dispose() {
        remoteTickTask.cancel(false);
    }
}