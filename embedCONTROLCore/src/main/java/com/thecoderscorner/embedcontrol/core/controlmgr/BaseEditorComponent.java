package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.Set;

public abstract class BaseEditorComponent implements EditorComponent {
    public static final int MAX_CORRELATION_WAIT = 5000;

    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    protected final RemoteMenuController remoteController;
    private final ComponentSettings drawingSettings;
    protected final ThreadMarshaller threadMarshaller;
    protected final MenuItem item;
    private final Object tickLock = new Object();
    private CorrelationId correlation;
    private long lastCorrelation = System.currentTimeMillis();
    private long lastUpdate = System.currentTimeMillis();
    protected volatile RenderingStatus status = RenderingStatus.NORMAL;
    private boolean locallyReadOnly;

    protected BaseEditorComponent(RemoteMenuController controller, ComponentSettings settings,
                                  MenuItem item, ThreadMarshaller threadMarshaller) {
        this.remoteController = controller;
        this.item = item;
        this.drawingSettings = settings;
        this.threadMarshaller = threadMarshaller;
    }

    public void setLocallyReadOnly(boolean locallyReadOnly) {
        this.locallyReadOnly = locallyReadOnly;
    }

    public abstract void changeControlSettings(RenderingStatus status, String text);

    public abstract String getControlText();

    public void updateEditor() {
        threadMarshaller.runOnUiThread(() ->
        {
            logger.log(System.Logger.Level.INFO, "Updating editor for " + item + ", status is " + status);
            String str = getControlText();
            changeControlSettings(status, str);
        });
    }

    public boolean controlTextIncludesName() {
        return getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME || getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME_VALUE;
    }

    public boolean controlTextIncludesValue() {
        return getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_VALUE ||
                getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME_VALUE ||
                getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_LABEL_NAME_VALUE;
    }

    public void editStarted(CorrelationId correlation) {
        status = RenderingStatus.EDIT_IN_PROGRESS;
        synchronized (tickLock) {
            lastCorrelation = System.currentTimeMillis();
            this.correlation = correlation;
        }
        updateEditor();
    }

    public void markRecentlyUpdated(RenderingStatus status) {
        synchronized (tickLock) {
            this.status = status;
            lastUpdate = System.currentTimeMillis();
        }
        updateEditor();
    }

    @Override
    public void tick() {
        if (status == RenderingStatus.RECENT_UPDATE || status == RenderingStatus.CORRELATION_ERROR) {
            synchronized (tickLock) {
                var span = System.currentTimeMillis() - lastUpdate;
                if (span > 1000) {
                    status = RenderingStatus.NORMAL;
                    updateEditor();
                }
            }
        }

        var updateErr = false;
        var corId = CorrelationId.EMPTY_CORRELATION;
        synchronized (tickLock) {
            var span = System.currentTimeMillis() - lastCorrelation;
            if (correlation != null && span > MAX_CORRELATION_WAIT) {
                corId = correlation;
                correlation = null;
                updateErr = true;
            }
        }

        if (updateErr) {
            logger.log(System.Logger.Level.ERROR, "No correlation update received for " + corId);
            markRecentlyUpdated(RenderingStatus.CORRELATION_ERROR);
        }
    }

    @Override
    public void onCorrelation(CorrelationId correlationId, AckStatus status) {
        var ourUpdate = false;
        synchronized (tickLock) {
            if (correlation != null && correlation.equals(correlationId)) {
                correlation = null;
                ourUpdate = true;
            }
        }

        if (ourUpdate) {
            logger.log(System.Logger.Level.INFO, "Correlation update received for " + correlationId + ", status = " + status);
            if (status != AckStatus.SUCCESS) {
                markRecentlyUpdated(RenderingStatus.CORRELATION_ERROR);
            } else {
                markRecentlyUpdated(RenderingStatus.RECENT_UPDATE);
            }
        }
    }

    private final Set<Class<?>> userEditableMenuTypes = Set.of(
            EditableTextMenuItem.class, Rgb32MenuItem.class,
            ScrollChoiceMenuItem.class, EditableLargeNumberMenuItem.class,
            AnalogMenuItem.class, EnumMenuItem.class, BooleanMenuItem.class
    );

    public ComponentSettings getDrawingSettings() {
        return drawingSettings;
    }

    public boolean isItemEditable(MenuItem item) {
        return userEditableMenuTypes.contains(item.getClass()) && !item.isReadOnly() && !locallyReadOnly;
    }

    @Override
    public void load(String data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String save() {
        throw new UnsupportedOperationException();
    }
}