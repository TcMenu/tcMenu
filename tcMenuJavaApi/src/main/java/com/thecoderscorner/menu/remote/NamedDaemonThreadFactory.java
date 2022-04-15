/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import java.util.concurrent.ThreadFactory;

/**
 * this thread factory provides better naming for threads
 */
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
