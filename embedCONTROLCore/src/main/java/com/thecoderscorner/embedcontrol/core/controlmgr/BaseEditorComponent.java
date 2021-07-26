package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public abstract class BaseEditorComponent implements EditorComponent {
    public static final int MAX_CORRELATION_WAIT = 5;

    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    protected final RemoteMenuController remoteController;
    private final ComponentSettings drawingSettings;
    protected final ThreadMarshaller threadMarshaller;
    protected final MenuItem item;
    private final Object tickLock = new Object();
    private CorrelationId correlation;
    private Instant lastCorrelation = Instant.now();
    private Instant lastUpdate = Instant.now();
    protected volatile RenderingStatus status = RenderingStatus.NORMAL;

    protected BaseEditorComponent(RemoteMenuController controller, ComponentSettings settings,
                                  MenuItem item, ThreadMarshaller threadMarshaller) {
        this.remoteController = controller;
        this.item = item;
        this.drawingSettings = settings;
        this.threadMarshaller = threadMarshaller;
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
            lastCorrelation = Instant.now();
            this.correlation = correlation;
        }
        updateEditor();
    }

    public void markRecentlyUpdated(RenderingStatus status) {
        synchronized (tickLock) {
            this.status = status;
            lastUpdate = Instant.now();
        }
        updateEditor();
    }

    @Override
    public void tick() {
        if (status == RenderingStatus.RECENT_UPDATE || status == RenderingStatus.CORRELATION_ERROR) {
            synchronized (tickLock) {
                var span = Duration.between(Instant.now(), lastUpdate);
                if (span.toSeconds() > 1) {
                    status = RenderingStatus.NORMAL;
                    updateEditor();
                }
            }
        }

        var updateErr = false;
        var corId = CorrelationId.EMPTY_CORRELATION;
        synchronized (tickLock) {
            var span = Duration.between(Instant.now(), lastCorrelation);
            if (correlation != null && span.toSeconds() > MAX_CORRELATION_WAIT) {
                corId = correlation;
                correlation = null;
                updateErr = true;
            }
        }

        if (updateErr) {
            logger.log(System.Logger.Level.ERROR, "No correlation update recieved for " + corId);
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
            logger.log(System.Logger.Level.INFO, "Correlation update recieved for " + correlationId + ", status = " + status);
            if (status != AckStatus.SUCCESS) {
                markRecentlyUpdated(RenderingStatus.CORRELATION_ERROR);
            } else {
                markRecentlyUpdated(RenderingStatus.NORMAL);
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
        return userEditableMenuTypes.contains(item.getClass()) && !item.isReadOnly();
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