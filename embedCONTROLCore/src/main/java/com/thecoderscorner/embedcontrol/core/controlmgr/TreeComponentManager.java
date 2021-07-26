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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

public class TreeComponentManager {
    private final Map<Integer, EditorComponent> editorComponents = new HashMap<>();
    private final RemoteMenuController controller;
    private final GlobalSettings appSettings;
    private final ScheduledExecutorService executor;
    private final ThreadMarshaller marshaller;
    private final ScreenManager screenManager;
    private final DialogViewer dialogViewer;
    private final int _cols;
    private final ScheduledFuture<?> remoteTickTask;
    private RemoteControllerListener remoteListener;
    int _currRow = 0;
    int _currCol = 0;

    public TreeComponentManager(ScreenManager screenManager, RemoteMenuController controller,
                                GlobalSettings appSettings, DialogViewer dialogViewer,
                                ScheduledExecutorService executor, ThreadMarshaller marshaller) {
        this.appSettings = appSettings;
        this.executor = executor;
        this.marshaller = marshaller;
        this.screenManager = screenManager;
        this.dialogViewer = dialogViewer;
        this.controller = controller;
        _cols = 2;

        remoteTickTask = executor.scheduleAtFixedRate(this::timerTick, 100, 100, TimeUnit.MILLISECONDS);

        remoteListener = new RemoteControllerListener() {
            @Override
            public void menuItemChanged(MenuItem item, boolean valueOnly) {
                if (editorComponents.containsKey(item.getId())) {
                    editorComponents.get(item.getId()).onItemUpdated(controller.getManagedMenu().getMenuState(item));
                }
            }

            @Override
            public void treeFullyPopulated() {
                marshaller.runOnUiThread(() -> {
                    _currRow = 0;
                    _currCol = 0;
                    screenManager.clear();
                    editorComponents.clear();
                    renderMenuRecursive(MenuTree.ROOT, screenManager, appSettings);
                });
            }

            @Override
            public void connectionState(RemoteInformation remoteInformation, AuthStatus connected) {
                connectionChanged(connected);
            }

            @Override
            public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
                for (var uiItem : editorComponents.values()) {
                    uiItem.onCorrelation(key, status);
                }
            }

            @Override
            public void dialogUpdate(DialogMode mode, String header, String buffer, MenuButtonType b1, MenuButtonType b2) {
                marshaller.runOnUiThread(() -> {
                    dialogViewer.show(mode == DialogMode.SHOW);
                    if (mode == DialogMode.SHOW) {
                        dialogViewer.setButton1(b1);
                        dialogViewer.setButton2(b2);
                        dialogViewer.setText(header, buffer);
                    }
                });
            }
        };
        controller.addListener(remoteListener);

        // handle the case where it's already connected really quick!
        if (controller.getConnector().getAuthenticationStatus() == AuthStatus.CONNECTION_READY) {
            connectionChanged(AuthStatus.CONNECTION_READY);
        }
    }

    private void connectionChanged(AuthStatus status) {
    }

    private void renderMenuRecursive(SubMenuItem sub, ScreenManager screenManager, GlobalSettings appSettings) {
        var tree = controller.getManagedMenu();
        var prefsColoring = new PrefsConditionalColoring(appSettings);

        var menuName = sub == MenuTree.ROOT ? controller.getConnector().getConnectionName() : sub.getName();
        screenManager.addStaticLabel(menuName, new ComponentSettings(prefsColoring,
                screenManager.getDefaultFontSize(),
                PortableAlignment.LEFT, nextRowCol(true), RedrawingMode.SHOW_VALUE, false), true);

        screenManager.startNesting();
        for (var item : tree.getMenuItems(sub)) {
            if (!item.isVisible()) continue;
            if (item instanceof SubMenuItem) {
                renderMenuRecursive((SubMenuItem) item, screenManager, appSettings);
            } else if (item instanceof BooleanMenuItem) {
                var boolItem = (BooleanMenuItem) item;
                editorComponents.put(boolItem.getId(), screenManager.addBooleanButton(boolItem,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(false),
                                RedrawingMode.SHOW_NAME_VALUE, false)));
            } else if (item instanceof ActionMenuItem) {
                var actionItem = (ActionMenuItem) item;
                editorComponents.put(actionItem.getId(), screenManager.addBooleanButton(actionItem,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(false),
                                RedrawingMode.SHOW_NAME, false)));
            } else if (item instanceof AnalogMenuItem) {
                var analogItem = (AnalogMenuItem) item;
                editorComponents.put(analogItem.getId(), screenManager.addHorizontalSlider(analogItem,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(false),
                                RedrawingMode.SHOW_NAME_VALUE, false)));
            } else if (item instanceof Rgb32MenuItem) {
                var rgb = (Rgb32MenuItem) item;
                editorComponents.put(rgb.getId(), screenManager.addRgbColorControl(rgb,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(false),
                                RedrawingMode.SHOW_LABEL_NAME_VALUE, false)));
            } else if (item instanceof EnumMenuItem) {
                var enumItem = (EnumMenuItem) item;
                editorComponents.put(enumItem.getId(), screenManager.addUpDownInteger(enumItem,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(false),
                                RedrawingMode.SHOW_NAME_VALUE, false)));
            } else if (item instanceof ScrollChoiceMenuItem) {
                var scrollItem = (ScrollChoiceMenuItem) item;
                editorComponents.put(scrollItem.getId(), screenManager.addUpDownScroll(scrollItem,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(false),
                                RedrawingMode.SHOW_NAME_VALUE, false)));
            } else if (item instanceof FloatMenuItem) {
                var floatItem = (FloatMenuItem) item;
                editorComponents.put(floatItem.getId(), screenManager.addTextEditor(floatItem,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(false),
                                RedrawingMode.SHOW_NAME_VALUE, false), 0.0F));
            } else if (item instanceof RuntimeListMenuItem) {
                var listItem = (RuntimeListMenuItem) item;
                screenManager.addStaticLabel(item.getName(), new ComponentSettings(prefsColoring,
                        screenManager.getDefaultFontSize(),
                        PortableAlignment.LEFT, nextRowCol(true), RedrawingMode.SHOW_NAME_VALUE, false), false);
                editorComponents.put(listItem.getId(), screenManager.addListEditor(listItem,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.LEFT, nextRowCol(true), RedrawingMode.SHOW_NAME_VALUE, false)));
            } else if (item instanceof EditableTextMenuItem) {
                var textItem = (EditableTextMenuItem) item;
                if (textItem.getItemType() == EditItemType.GREGORIAN_DATE) {
                    editorComponents.put(textItem.getId(), screenManager.addDateEditorComponent(textItem,
                            new ComponentSettings(prefsColoring,
                                    screenManager.getDefaultFontSize(),
                                    PortableAlignment.CENTER, nextRowCol(false),
                                    RedrawingMode.SHOW_LABEL_NAME_VALUE, false)));
                } else if (textItem.getItemType() == EditItemType.TIME_24_HUNDREDS ||
                        textItem.getItemType() == EditItemType.TIME_12H ||
                        textItem.getItemType() == EditItemType.TIME_24H) {
                    editorComponents.put(textItem.getId(), screenManager.addTimeEditorComponent(textItem,
                            new ComponentSettings(prefsColoring,
                                    screenManager.getDefaultFontSize(),
                                    PortableAlignment.CENTER, nextRowCol(false),
                                    RedrawingMode.SHOW_LABEL_NAME_VALUE, false)));
                } else {
                    editorComponents.put(textItem.getId(), screenManager.addTextEditor(textItem,
                            new ComponentSettings(prefsColoring,
                                    screenManager.getDefaultFontSize(),
                                    PortableAlignment.CENTER, nextRowCol(false),
                                    RedrawingMode.SHOW_LABEL_NAME_VALUE, false), ""));
                }
            } else if (item instanceof EditableLargeNumberMenuItem) {
                var largeNum = (EditableLargeNumberMenuItem) item;
                editorComponents.put(largeNum.getId(), screenManager.addTextEditor(largeNum,
                        new ComponentSettings(prefsColoring,
                                screenManager.getDefaultFontSize(),
                                PortableAlignment.CENTER, nextRowCol(false),
                                RedrawingMode.SHOW_LABEL_NAME_VALUE, false), BigDecimal.ZERO));
            }

            if (editorComponents.containsKey(item.getId()) && tree.getMenuState(item) != null) {
                editorComponents.get(item.getId()).onItemUpdated(tree.getMenuState(item));
            }
        }

        screenManager.endNesting();
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

    public void timerTick() {
        marshaller.runOnUiThread(() -> {
            for (var component : editorComponents.values()) {
                component.tick();
            }
        });
    }

    public void dispose() {
        controller.removeListener(remoteListener);
        remoteTickTask.cancel(false);
    }
}