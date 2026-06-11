package com.thecoderscorner.embedcontrol.core.controlmgr;

/**
 * When runOnUiThread is called with a Runnable, the run method should be called on the UI thread. For example
 * the JavaFX implementation of this is simply Platform::runLater because that ensures that the work is moved onto
 * the UI thread.
 */
@FunctionalInterface
public interface ThreadMarshaller {
    void runOnUiThread(Runnable toRun);
}
