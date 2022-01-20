/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VersionInfoTest {


    @Test
    public void testVersionInfoNoPatch() {
        checkVersionInfoNewerSame("1.2", "1.2", true);
        checkVersionInfoNewerSame("1.1", "1.2", true);
        checkVersionInfoNewerSame("1.2", "1.1", false);
    }

    @Test
    public void testVersionInfoSame() {
        checkVersionInfoNewerSame("1.2.3", "1.2.3", true);
    }

    @Test
    public void testVersionInfoNewer() {
        checkVersionInfoNewerSame("1.2.3", "1.3.3", true);
        checkVersionInfoNewerSame("1.2.3", "1.2.4", true);
        checkVersionInfoNewerSame("1.2.2", "10.2.2", true);
    }

    @Test
    public void testVersionInfoOlder() {
        checkVersionInfoNewerSame("1.2.3", "1.2.2", false);
        checkVersionInfoNewerSame("1.2.3", "1.1.3", false);
        checkVersionInfoNewerSame("10.2.2", "1.2.2", false);
    }

    private void checkVersionInfoNewerSame(String verSrc, String verDst, boolean shouldBeSame) {
        VersionInfo infoSrc = new VersionInfo(verSrc);
        VersionInfo infoDst = new VersionInfo(verDst);

        assertEquals(shouldBeSame, infoDst.isSameOrNewerThan(infoSrc));
    }
}