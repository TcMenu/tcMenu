package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.remote.AuthStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
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

                var editorComponent = getComponentEditorItem(layoutControlGrid, item);
                if(editorComponent!=null) {
                    editorComponents.put(item.getId(), editorComponent);
                }
            }

            if (editorComponents.containsKey(item.getId()) && tree.getMenuState(item) != null) {
                editorComponents.get(item.getId()).onItemUpdated(tree.getMenuState(item));
            }
        }

        layoutControlGrid.endNesting();
    }

    public EditorComponent<T> getComponentEditorItem(MenuControlGrid<T> layoutControlGrid, MenuItem item) {
        if(item instanceof SubMenuItem sub) {
            var componentSettings = layoutPersistence.getSettingsForMenuItem(sub, true);
            return layoutControlGrid.addButtonWithAction(sub, sub.getName(), componentSettings,
                    subMenuItem -> controller.getNavigationManager().pushMenuNavigation(subMenuItem));
        }
        else if (item instanceof BooleanMenuItem boolItem) {
            return layoutControlGrid.addBooleanButton(boolItem, layoutPersistence.getSettingsForMenuItem(boolItem, false));
        } else if (item instanceof ActionMenuItem actionItem) {
            return layoutControlGrid.addBooleanButton(actionItem, layoutPersistence.getSettingsForMenuItem(actionItem, false));
        } else if (item instanceof AnalogMenuItem analogItem) {
            return layoutControlGrid.addHorizontalSlider(analogItem, layoutPersistence.getSettingsForMenuItem(analogItem, false));
        } else if (item instanceof Rgb32MenuItem rgb) {
            return layoutControlGrid.addRgbColorControl(rgb,  layoutPersistence.getSettingsForMenuItem(rgb, false));
        } else if (item instanceof EnumMenuItem enumItem) {
            return layoutControlGrid.addUpDownInteger(enumItem,  layoutPersistence.getSettingsForMenuItem(enumItem, true));
        } else if (item instanceof ScrollChoiceMenuItem scrollItem) {
            return layoutControlGrid.addUpDownScroll(scrollItem, layoutPersistence.getSettingsForMenuItem(scrollItem, true));
        } else if (item instanceof FloatMenuItem floatItem) {
            return layoutControlGrid.addTextEditor(floatItem, layoutPersistence.getSettingsForMenuItem(floatItem, true), 0.0F);
        } else if (item instanceof RuntimeListMenuItem listItem) {
            return layoutControlGrid.addListEditor(listItem, layoutPersistence.getSettingsForMenuItem(listItem, true));
        } else if (item instanceof EditableTextMenuItem textItem) {
            if (textItem.getItemType() == EditItemType.GREGORIAN_DATE) {
                return layoutControlGrid.addDateEditorComponent(textItem, layoutPersistence.getSettingsForMenuItem(textItem, true));
            } else if (textItem.getItemType() == EditItemType.TIME_24_HUNDREDS ||
                    textItem.getItemType() == EditItemType.TIME_12H ||
                    textItem.getItemType() == EditItemType.TIME_24H) {
                return layoutControlGrid.addTimeEditorComponent(textItem, layoutPersistence.getSettingsForMenuItem(textItem, true));
            } else {
                return layoutControlGrid.addTextEditor(textItem, layoutPersistence.getSettingsForMenuItem(textItem, true), "");
            }
        } else if (item instanceof EditableLargeNumberMenuItem largeNum) {
            return layoutControlGrid.addTextEditor(largeNum, layoutPersistence.getSettingsForMenuItem(largeNum, true), BigDecimal.ZERO);
        }
        return null;
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