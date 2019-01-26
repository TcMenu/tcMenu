package com.thecoderscorner.menu.remote;

import java.util.concurrent.ThreadFactory;

public class NamedDaemonThreadFactory implements ThreadFactory {
    private final String name;

    public NamedDaemonThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread th = new Thread(r);
        th.setDaemon(true);
        th.setName(name);
        return th;
    }
}
