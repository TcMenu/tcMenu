package com.thecoderscorner.embedcontrol.core.controlmgr;

public interface ThreadMarshaller {
    void runOnUiThread(Runnable toRun);
}
