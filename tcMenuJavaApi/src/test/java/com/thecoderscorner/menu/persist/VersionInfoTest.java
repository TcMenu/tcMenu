package com.thecoderscorner.menu.persist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VersionInfoTest {

    @Test
    public void testCreatingObjects() {
        var test1 = VersionInfo.fromString("1.0");
        assertEquals("1.0.0", test1.toString());
        var test1a = VersionInfo.fromString("1.0.0");
        assertEquals("1.0.0", test1.toString());
        var test2 = new VersionInfo("1.2.3");
        assertEquals("1.2.3", test2.toString());
        var test3 = new VersionInfo("1.2.3-SNAPSHOT");
        assertEquals("1.2.3-BETA", test3.toString());
        var test4 = new VersionInfo("1.2.3-BETA");
        assertEquals("1.2.3-BETA", test3.toString());
        var test5 = new VersionInfo("100.203.293-RC");
        assertEquals("100.203.293-BETA", test5.toString());
        var test6 = new VersionInfo("182.32222.22-PREVIOUS");
        assertEquals("182.32222.22-PREVIOUS", test6.toString());

        assertNotEquals(test1, test2);
        assertEquals(test1, test1);
        assertEquals(test1, test1a);
        assertEquals(test3, test4);
        assertNotEquals(test2, test3);
    }

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