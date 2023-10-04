package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.customization.*;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.customization.FontInformation.SizeMeasurement.PERCENT;
import static com.thecoderscorner.embedcontrol.customization.formbuilder.CustomDrawingConfiguration.NO_CUSTOM_DRAWING;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;

public abstract class MenuGridComponent<T> {
    private final MenuItemStore menuItemStore;
    private final JfxNavigationManager navMgr;
    private final MenuTree tree;
    protected final Map<Integer, EditorComponent<T>> editorComponents = new HashMap<>();
    private final ScheduledExecutorService executor;
    private final ScheduledFuture<?> remoteTickTask;
    private ThreadMarshaller marshaller;
    private int row;

    public MenuGridComponent(MenuItemStore store, JfxNavigationManager navMgr, ScheduledExecutorService executor,
                             ThreadMarshaller marshaller) {
        this.menuItemStore = store;
        this.tree = store.getTree();
        this.navMgr = navMgr;
        this.executor = executor;
        this.marshaller = marshaller;

        remoteTickTask = executor.scheduleAtFixedRate(this::timerTick, 100, 100, TimeUnit.MILLISECONDS);
    }



    public void renderMenuRecursive(MenuEditorFactory<T> editorFactory, SubMenuItem sub, boolean recurse, int level) {
        Consumer<MenuItem> subRenderer = subMenuItem -> navMgr.pushMenuNavigation(asSubMenu(subMenuItem), menuItemStore);
        if (menuItemStore.hasSubConfiguration(sub.getId())) {
            menuItemStore.changeSubStore(sub.getId());
            for (var entry : menuItemStore.allRowEntries()) {
                if (entry instanceof MenuItemFormItem mfi) {
                    ComponentSettings settings = new ComponentSettings(
                            new ScreenLayoutBasedConditionalColor(menuItemStore, entry.getPositioning()),
                            entry.getFontInfo(), mfi.getAlignment(), entry.getPositioning(),
                            mfi.getRedrawingMode(), mfi.getControlType(), mfi.getCustomDrawing(),  true);
                    var comp = editorFactory.getComponentEditorItem(mfi.getItem(), settings, subRenderer);
                    comp.ifPresent(tEditorComponent -> addToGrid(entry.getPositioning(), tEditorComponent));
                } else if (entry instanceof SpaceFormItem sfi) {
                    addSpaceToGrid(sfi.getPositioning(), sfi.getVerticalSpace());
                } else if (entry instanceof TextFormItem tfi) {
                    ComponentSettings settings = new ComponentSettings(
                            new ScreenLayoutBasedConditionalColor(menuItemStore, entry.getPositioning()),
                            entry.getFontInfo(), tfi.getAlignment(), entry.getPositioning(),
                            RedrawingMode.SHOW_NAME, ControlType.TEXT_CONTROL, NO_CUSTOM_DRAWING, true);
                    addTextToGrid(settings, tfi.getText());
                }
            }
        } else {
            if (level != 0) {
                var position = defaultSpaceForItem(Optional.of(sub));
                var settings = new ComponentSettings(new ScreenLayoutBasedConditionalColor(menuItemStore, position),
                        new FontInformation(150, PERCENT), EditorComponent.PortableAlignment.LEFT,
                        position, RedrawingMode.SHOW_NAME, ControlType.TEXT_CONTROL, NO_CUSTOM_DRAWING, false);

                editorFactory.getComponentEditorItem(sub, settings, subRenderer).ifPresent(comp -> {
                    addToGrid(settings.getPosition(), comp);
                    editorComponents.put(sub.getId(), comp);
                    MenuItemHelper.getValueFor(sub, tree);
                    comp.onItemUpdated(sub, tree.getMenuState(sub));
                });

            }
            for (var item : tree.getMenuItems(sub)) {
                if (!item.isVisible()) continue;
                if (item instanceof SubMenuItem && recurse) {
                    renderMenuRecursive(editorFactory, (SubMenuItem) item, recurse, level + 1);
                } else {
                    var settings = getComponentForMenuItem(item);
                    editorFactory.getComponentEditorItem(item, settings, subRenderer).ifPresent(comp -> {
                        addToGrid(settings.getPosition(), comp);
                        editorComponents.put(item.getId(), comp);
                        MenuItemHelper.getValueFor(item, tree, MenuItemHelper.getDefaultFor(item));
                        comp.onItemUpdated(item, tree.getMenuState(item));
                    });
                }
            }
        }
    }

    public void itemHasUpdated(MenuItem item) {
        if(editorComponents.containsKey(item.getId())) {
            editorComponents.get(item.getId()).onItemUpdated(item, tree.getMenuState(item));
        }
    }

    public void tickAll() {
        for(var component : editorComponents.values()) {
            component.tick();
        }
    }

    private ComponentSettings getComponentForMenuItem(MenuItem item) {
        var position = defaultSpaceForItem(Optional.of(item));

        return new ComponentSettings(new ScreenLayoutBasedConditionalColor(menuItemStore, position),
                MenuFormItem.FONT_100_PERCENT, defaultJustificationForItem(Optional.of(item)),
                position, defaultRedrawModeForItem(Optional.of(item)), defaultControlForType(item),
                NO_CUSTOM_DRAWING, false);
    }

    protected RedrawingMode defaultRedrawModeForItem(Optional<MenuItem> item) {
        return RedrawingMode.SHOW_NAME_VALUE;
    }

    protected EditorComponent.PortableAlignment defaultJustificationForItem(Optional<MenuItem> item) {
        return EditorComponent.PortableAlignment.CENTER;
    }

    protected ComponentPositioning defaultSpaceForItem(Optional<MenuItem> item) {
        var pos = new ComponentPositioning(row, 0, 1, menuItemStore.getGridSize());
        row++;
        return pos;
    }


    protected abstract void addToGrid(ComponentPositioning where, EditorComponent<T> item);

    protected abstract void addTextToGrid(ComponentSettings settings, String item);

    protected abstract void addSpaceToGrid(ComponentPositioning where, int amount);

    public void clearGrid() {
        row = 0;
    }

    public void acknowledgementReceived(CorrelationId key, AckStatus status) {
        for(var comp : editorComponents.values()) {
            comp.onCorrelation(key, status);
        }
    }

    public record ScreenLayoutBasedConditionalColor(MenuItemStore store, ComponentPositioning where) implements ConditionalColoring {
        @Override
        public PortableColor foregroundFor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            return getControlColor(status, compType).getFg();
        }

        @Override
        public PortableColor backgroundFor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            return getControlColor(status, compType).getBg();
        }

        private ColorCustomizable findColorSet() {
            var entryOpt = store.getFormItemIfPresent(where.getRow(), where.getCol());
            if (entryOpt.isEmpty()) return store.getTopLevelColorSet();
            else return entryOpt.get().getSettings();
        }

        private ControlColor getControlColor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            if (status == EditorComponent.RenderingStatus.RECENT_UPDATE) compType = ColorComponentType.CUSTOM;
            else if (status == EditorComponent.RenderingStatus.EDIT_IN_PROGRESS) compType = ColorComponentType.PENDING;
            else if (status == EditorComponent.RenderingStatus.CORRELATION_ERROR) compType = ColorComponentType.ERROR;

            var csSelected = findColorSet();
            if(csSelected.getColorStatus(compType) == ColorCustomizable.ColorStatus.AVAILABLE) {
                return csSelected.getColorFor(compType);
            } else if(store.getTopLevelColorSet().getColorStatus(compType) == ColorCustomizable.ColorStatus.AVAILABLE) {
                return store.getTopLevelColorSet().getColorFor(compType);
            } else {
                return store.getColorSet(GlobalColorCustomizable.KEY_NAME).getColorFor(compType);
            }
        }
    }

    /**
     * Called frequently by the executor to update the UI components.
     */
    public void timerTick() {
        marshaller.runOnUiThread(this::tickAll);
    }

    /**
     * Call this to stop the associated tasks.
     */
    public void dispose() {
        remoteTickTask.cancel(false);
    }

    public static ControlType defaultControlForType(MenuItem item) {
        if (item instanceof SubMenuItem || item instanceof BooleanMenuItem || item instanceof ActionMenuItem) {
            return ControlType.BUTTON_CONTROL;
        } else if (item instanceof AnalogMenuItem) {
            return ControlType.HORIZONTAL_SLIDER;
        } else if (item instanceof Rgb32MenuItem) {
            return ControlType.RGB_CONTROL;
        } else if (item instanceof EnumMenuItem || item instanceof ScrollChoiceMenuItem) {
            return ControlType.UP_DOWN_CONTROL;
        } else if (item instanceof RuntimeListMenuItem) {
            return ControlType.LIST_CONTROL;
        } else if (item instanceof CustomBuilderMenuItem) {
            return ControlType.AUTH_IOT_CONTROL;
        } else if (item instanceof EditableTextMenuItem textItem) {
            if (textItem.getItemType() == EditItemType.GREGORIAN_DATE) {
                return ControlType.DATE_CONTROL;
            } else if (textItem.getItemType() == EditItemType.TIME_24_HUNDREDS ||
                    textItem.getItemType() == EditItemType.TIME_12H ||
                    textItem.getItemType() == EditItemType.TIME_24H) {
                return ControlType.TIME_CONTROL;
            } else {
                return ControlType.TEXT_CONTROL;
            }
        } else {
            return ControlType.TEXT_CONTROL;
        }
    }

}
