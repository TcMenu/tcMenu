package com.thecoderscorner.menu.domain.state;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class CurrentScrollPositionTest {
    @Test
    void checkScrollPosition() {
        CurrentScrollPosition pos1 = new CurrentScrollPosition(10, "ABC");
        CurrentScrollPosition pos2 = new CurrentScrollPosition("20-Another Super-Duper");
        CurrentScrollPosition pos3 = new CurrentScrollPosition("ABC"); // not valid but shouldnt fail
        CurrentScrollPosition pos4 = new CurrentScrollPosition("a-ABC"); // not valid but shouldnt fail
        CurrentScrollPosition pos5 = new CurrentScrollPosition("252-");

        Assert.assertEquals(pos1.getPosition(), 10);
        Assert.assertEquals(pos1.getValue(), "ABC");

        Assert.assertEquals(pos2.getPosition(), 20);
        Assert.assertEquals(pos2.getValue(), "Another Super-Duper");

        Assert.assertEquals(pos5.getPosition(), 252);
        Assert.assertEquals(pos5.getValue(), "");

        Assert.assertEquals(pos2, new CurrentScrollPosition("20-Another Super-Duper"));
        Assert.assertNotEquals(pos2, new CurrentScrollPosition("10-Another Super-Duper"));
        Assert.assertNotEquals(pos2, new CurrentScrollPosition("20-Another Duper"));

        Assert.assertEquals("10-ABC", pos1.toString());
        Assert.assertEquals("20-Another Super-Duper", pos2.toString());
        Assert.assertEquals("0-Unknown", pos3.toString());
        Assert.assertEquals("0-ABC", pos4.toString());
        Assert.assertEquals("252-", pos5.toString());

    }
}
