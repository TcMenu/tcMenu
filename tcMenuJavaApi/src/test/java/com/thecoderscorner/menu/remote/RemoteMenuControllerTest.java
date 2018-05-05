/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.rs232;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;

public class RemoteMenuControllerTest {
    @Test
    public void testRemoteController() {
        MenuTree menuTree = new MenuTree();
        RemoteConnector connector = Mockito.mock(RemoteConnector.class);
        ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        Clock clock = Mockito.mock(Clock.class);

        RemoteMenuController controller = new RemoteMenuController(connector, menuTree, executor, clock);
        controller.start();
    }
}