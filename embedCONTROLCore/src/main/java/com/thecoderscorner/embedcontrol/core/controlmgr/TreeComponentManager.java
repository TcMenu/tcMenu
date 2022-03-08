package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.PrefsConditionalColoring;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.scene.Node;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

public class TreeComponentManager {
    protected final Map<Integer, EditorComponent> editorComponents = new HashMap<>();
    protected final ScheduledExecutorService executor;
    protected final ThreadMarshaller marshaller;
    protected final GlobalSettings appSettings;
    protected final ScreenManager screenManager;
    protected final DialogViewer dialogViewer;
    protected final MenuComponentControl controller;
    protected final int _cols;
    protected final ScheduledFuture<?> remoteTickTask;
    protected final PrefsConditionalColoring prefsColoring;

    protected int _currRow = 0;
    protected int _currCol = 0;

    public TreeComponentManager(ScreenManager screenManager, GlobalSettings appSettings, DialogViewer dialogViewer,
                                ScheduledExecutorService executor, ThreadMarshaller marshaller, MenuComponentControl controller) {
        this.appSettings = appSettings;
        this.executor = executor;
        this.marshaller = marshaller;
        this.screenManager = screenManager;
        this.dialogViewer = dialogViewer;
        this.controller = controller;
        _cols = 2;

        prefsColoring = new PrefsConditionalColoring(appSettings);
        remoteTickTask = executor.scheduleAtFixedRate(this::timerTick, 100, 100, TimeUnit.MILLISECONDS);
    }

    protected void connectionChanged(AuthStatus status) {
    }

    public void renderMenuRecursive(SubMenuItem sub, ScreenManager screenManager, GlobalSettings appSettings, boolean recurse) {
        var tree = controller.getMenuTree();

        var menuName = sub == MenuTree.ROOT ? controller.getConnectionName() : sub.getName();
        screenManager.addStaticLabel(menuName, new ComponentSettings(prefsColoring,
                screenManager.getDefaultFontSize(),
                PortableAlignment.LEFT, nextRowCol(true), RedrawingMode.SHOW_VALUE, false), true);

        screenManager.startNesting();
        for (var item : tree.getMenuItems(sub)) {
            if (!item.isVisible()) continue;
            if (item instanceof SubMenuItem && recurse) {
                renderMenuRecursive((SubMenuItem) item, screenManager, appSettings, recurse);
            }
            else {
                if(item instanceof RuntimeListMenuItem) {
                    screenManager.addStaticLabel(item.getName(), new ComponentSettings(prefsColoring, screenManager.getDefaultFontSize(),
                            PortableAlignment.LEFT, nextRowCol(true), RedrawingMode.SHOW_NAME_VALUE, false), false);
                }

                var editorComponent = getComponentEditorItem(item);
                if(editorComponent!=null) {
                    editorComponents.put(item.getId(), editorComponent);
                }
            }

            if (editorComponents.containsKey(item.getId()) && tree.getMenuState(item) != null) {
                editorComponents.get(item.getId()).onItemUpdated(tree.getMenuState(item));
            }
        }

        screenManager.endNesting();
    }

    public EditorComponent getComponentEditorItem(MenuItem item) {
        if (item instanceof BooleanMenuItem boolItem) {
            return screenManager.addBooleanButton(boolItem, new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.CENTER, nextRowCol(false),
                            RedrawingMode.SHOW_NAME_VALUE, false));
        } else if (item instanceof ActionMenuItem actionItem) {
            return screenManager.addBooleanButton(actionItem, new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.CENTER, nextRowCol(false),
                            RedrawingMode.SHOW_NAME, false));
        } else if (item instanceof AnalogMenuItem analogItem) {
            return screenManager.addHorizontalSlider(analogItem,  new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.CENTER, nextRowCol(true),
                            RedrawingMode.SHOW_NAME_VALUE, false));
        } else if (item instanceof Rgb32MenuItem rgb) {
            return screenManager.addRgbColorControl(rgb,  new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.CENTER, nextRowCol(false),
                            RedrawingMode.SHOW_LABEL_NAME_VALUE, false));
        } else if (item instanceof EnumMenuItem enumItem) {
            return screenManager.addUpDownInteger(enumItem,  new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.CENTER, nextRowCol(true),
                            RedrawingMode.SHOW_NAME_VALUE, false));
        } else if (item instanceof ScrollChoiceMenuItem scrollItem) {
            return screenManager.addUpDownScroll(scrollItem, new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.CENTER, nextRowCol(true),
                            RedrawingMode.SHOW_NAME_VALUE, false));
        } else if (item instanceof FloatMenuItem floatItem) {
            return screenManager.addTextEditor(floatItem, new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.CENTER, nextRowCol(true),
                            RedrawingMode.SHOW_NAME_VALUE, false), 0.0F);
        } else if (item instanceof RuntimeListMenuItem listItem) {
            return screenManager.addListEditor(listItem, new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.LEFT, nextRowCol(true), RedrawingMode.SHOW_NAME_VALUE, false));
        } else if (item instanceof EditableTextMenuItem textItem) {
            if (textItem.getItemType() == EditItemType.GREGORIAN_DATE) {
                return screenManager.addDateEditorComponent(textItem,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(true),
                                RedrawingMode.SHOW_LABEL_NAME_VALUE, false));
            } else if (textItem.getItemType() == EditItemType.TIME_24_HUNDREDS ||
                    textItem.getItemType() == EditItemType.TIME_12H ||
                    textItem.getItemType() == EditItemType.TIME_24H) {
                return screenManager.addTimeEditorComponent(textItem, new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(true),
                                RedrawingMode.SHOW_LABEL_NAME_VALUE, false));
            } else {
                return screenManager.addTextEditor(textItem, new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(true),
                                RedrawingMode.SHOW_LABEL_NAME_VALUE, false), "");
            }
        } else if (item instanceof EditableLargeNumberMenuItem largeNum) {
            return screenManager.addTextEditor(largeNum, new ComponentSettings(prefsColoring,
                            screenManager.getDefaultFontSize(),
                            PortableAlignment.CENTER, nextRowCol(true),
                            RedrawingMode.SHOW_LABEL_NAME_VALUE, false), BigDecimal.ZERO);
        }
        return null;
    }

    private ComponentPositioning nextRowCol(boolean startNewRow) {
        if (startNewRow && _currCol != 0) {
            _currRow++;
            _currCol = 0;
        }

        var pos = new ComponentPositioning(_currRow, _currCol, 1, startNewRow ? _cols : 1);

        if (++_currCol >= _cols || startNewRow) {
            _currRow++;
            _currCol = 0;
        }

        return pos;
    }

    public void reset() {
        _currCol = _currRow = 0;
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