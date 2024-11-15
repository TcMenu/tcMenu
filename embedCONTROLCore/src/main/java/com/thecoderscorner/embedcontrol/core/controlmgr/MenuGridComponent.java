package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.customization.*;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.customization.FontInformation.SizeMeasurement.PERCENT;
import static com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration.NO_CUSTOM_DRAWING;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;

/// MenuGridComponent is the auto UI, it is responsible for taking part of a menu tree and rendering it onto the display.
/// It is used by both the Local Embedded UI, the Preview function and also all forms of Embed Control Fx. It is provided
/// with a starting point and if the render is recursive, and then renders either all items in the current menu, or
/// everything from that point down.
///
/// It is an abstract class, in that the absolute methods by which the controls are put into the grid are implemented
/// differently in each case. Normally you will not need to create these yourself as they are created automatically
/// when no custom panel exists.
/// @param <T> The type of UI component, normally Node
public abstract class MenuGridComponent<T> {
    private final MenuItemStore menuItemStore;
    private final JfxNavigationManager navMgr;
    private final MenuTree tree;
    protected final Map<Integer, EditorComponent<T>> editorComponents = new HashMap<>();
    private final ScheduledExecutorService executor;
    private ThreadMarshaller marshaller;
    private int row;

    public MenuGridComponent(MenuItemStore store, JfxNavigationManager navMgr, ScheduledExecutorService executor,
                             ThreadMarshaller marshaller) {
        this.menuItemStore = store;
        this.tree = store.getTree();
        this.navMgr = navMgr;
        this.executor = executor;
        this.marshaller = marshaller;
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
                    var editorComponent = editorFactory.getComponentEditorItem(mfi.getItem(), settings, subRenderer);
                    editorComponent.ifPresent(comp -> {
                        addToGrid(entry.getPositioning(), comp, entry.getFontInfo());
                        editorComponents.put(mfi.getItem().getId(), comp);
                        MenuItemHelper.getValueFor(mfi.getItem(), tree, MenuItemHelper.getDefaultFor(mfi.getItem()));
                        comp.onItemUpdated(mfi.getItem(), tree.getMenuState(mfi.getItem()));

                    });
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
                    addToGrid(settings.getPosition(), comp, settings.getFontInfo());
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
                        addToGrid(settings.getPosition(), comp, settings.getFontInfo());
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

    private ComponentSettings getComponentForMenuItem(MenuItem item) {
        var position = defaultSpaceForItem(Optional.of(item));
        return ComponentSettingsBuilder.forMenuItem(item, new ScreenLayoutBasedConditionalColor(menuItemStore, position))
                .withPosition(position)
                .build();
    }

    protected ComponentPositioning defaultSpaceForItem(Optional<MenuItem> item) {
        var pos = new ComponentPositioning(row, 0, 1, menuItemStore.getGridSize());
        row++;
        return pos;
    }


    protected abstract void addToGrid(ComponentPositioning where, EditorComponent<T> item, FontInformation fontInfo);

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

    public void tickAll() {
        for(var comp : editorComponents.values()) {
            comp.tick();
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

        @Override
        public ControlColor colorFor(EditorComponent.RenderingStatus status, ColorComponentType ty) {
            return getControlColor(status, ty);
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
}
