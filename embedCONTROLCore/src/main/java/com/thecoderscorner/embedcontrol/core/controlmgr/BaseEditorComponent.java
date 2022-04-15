package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.Set;

/**
 * The absolute base class of editor components, this class contains many helper functions needed to be able to render
 * a menu item onto the display, regardless of the type of item, or display technology. This includes working out
 * what text needs to be displayed, handling updates including the momentary change in color on value change. Error
 * handling and correlation is also dealt with here.
 *
 * @param <W> The window component type
 */
public abstract class BaseEditorComponent<W> implements EditorComponent<W> {
    public static final int MAX_CORRELATION_WAIT = 5000;

    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    protected final MenuComponentControl componentControl;
    private final ComponentSettings drawingSettings;
    protected final ThreadMarshaller threadMarshaller;
    protected final MenuItem item;
    private final Object tickLock = new Object();
    private CorrelationId correlation;
    private long lastCorrelation = System.currentTimeMillis();
    private long lastUpdate = System.currentTimeMillis();
    protected volatile RenderingStatus status = RenderingStatus.NORMAL;
    private boolean locallyReadOnly;

    protected BaseEditorComponent(MenuComponentControl controller, ComponentSettings settings,
                                  MenuItem item, ThreadMarshaller threadMarshaller) {
        this.componentControl = controller;
        this.item = item;
        this.drawingSettings = settings;
        this.threadMarshaller = threadMarshaller;
    }

    /**
     * Tell the component that it is read only regardless of item state.
     * @param locallyReadOnly true if read only, otherwise false, or do not set.
     */
    public void setLocallyReadOnly(boolean locallyReadOnly) {
        this.locallyReadOnly = locallyReadOnly;
    }

    /**
     * Abstract method that actually makes the change to the component, needs to be implemented. This is called whenever
     * a change in component is needed.
     * @param status the latest status
     * @param text the text to present
     */
    public abstract void changeControlSettings(RenderingStatus status, String text);

    /**
     * @return the text to display
     */
    public abstract String getControlText();

    /**
     * Actually requests that the control be updated.
     */
    public void updateEditor() {
        threadMarshaller.runOnUiThread(() ->
        {
            logger.log(System.Logger.Level.INFO, "Updating editor for " + item + ", status is " + status);
            String str = getControlText();
            changeControlSettings(status, str);
        });
    }

    /**
     * Checks if the control text includes the name of the item
     * @return true if the name is needed
     */
    public boolean controlTextIncludesName() {
        return getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME || getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME_VALUE;
    }

    /**
     * Checks if the control text includes the value for the item
     * @return true if the value is needed
     */
    public boolean controlTextIncludesValue() {
        return getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_VALUE ||
                getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME_VALUE;
    }

    /**
     * Whenever an edit starts on an item, this should be called providing the correlation, if the update is entirely
     * local use CorrelationId.EMPTY_CORRELATION
     * @param correlation the correlation id or CorrelationId.EMPTY_CORRELATION
     */
    public void editStarted(CorrelationId correlation) {
        if(correlation == null || correlation.equals(CorrelationId.EMPTY_CORRELATION)) return;
        status = RenderingStatus.EDIT_IN_PROGRESS;
        synchronized (tickLock) {
            lastCorrelation = System.currentTimeMillis();
            this.correlation = correlation;
        }
        updateEditor();
    }

    /**
     * Indicates the item has been recently update into a new state and needs to be updated
     * @param status the new status
     */
    public void markRecentlyUpdated(RenderingStatus status) {
        synchronized (tickLock) {
            this.status = status;
            lastUpdate = System.currentTimeMillis();
        }
        updateEditor();
    }

    /**
     * Called frequently by TreeComponentManager to allow the control to determine if any momentary state such as
     * recently updated, error, or pending needs to be cleared.
     * @see TreeComponentManager
     */
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

    /**
     * Called when a correlation arrives, this clears any pending status from the control.
     * @param correlationId the correlation ID that arrived
     * @param status the status
     */
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

    /**
     * a list of types that we can edit
     */
    private final Set<Class<?>> userEditableMenuTypes = Set.of(
            EditableTextMenuItem.class, Rgb32MenuItem.class,
            ScrollChoiceMenuItem.class, EditableLargeNumberMenuItem.class,
            AnalogMenuItem.class, EnumMenuItem.class, BooleanMenuItem.class
    );

    /**
     * @return the drawing settings for this component
     */
    public ComponentSettings getDrawingSettings() {
        return drawingSettings;
    }

    /**
     * checks if an item is editable, if it is an editable type of item, and is not read only
     */
    public boolean isItemEditable(MenuItem item) {
        return userEditableMenuTypes.contains(item.getClass()) && !item.isReadOnly() && !locallyReadOnly;
    }
}