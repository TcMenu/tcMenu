package com.thecoderscorner.embedcontrol.core.controlmgr;

@FunctionalInterface
public interface ThreadMarshaller {
    void runOnUiThread(Runnable toRun);
}
