package com.thecoderscorner.embedcontrol.core.util;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.AnalogMenuItemBuilder;
import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItemBuilder;
import com.thecoderscorner.menu.persist.VersionInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseMenuConfigTest {
    /**
     * Test case for `resolveProperties` method when global properties and environment-specific properties are both present.
     */
    @Test
    public void testResolveProperties_GlobalAndEnvPropsPresent() throws Exception {
        TestMenuConfig config = new TestMenuConfig();
        assertEquals("bool name", config.getBean(BooleanMenuItem.class).getName());
        assertEquals(ControlColor.BLACK, config.getBean(ControlColor.class).getFg());
        assertEquals(ControlColor.WHITE, config.getBean(ControlColor.class).getBg());
        assertEquals("4.2.0", config.getBean(MenuAppVersion.class).getVersionInfo().toString());
        assertEquals("bool name", config.getBean(DependentItem.class).dependee().getName());
        assertEquals("12345", config.getBean(DependentItem.class).dependee2().getName());
        assertEquals("12345", config.getBean(AnalogMenuItem.class).getName());
    }

    class TestMenuConfig extends BaseMenuConfig {
        public TestMenuConfig() {
            super("unittest_app", null);
            asBean(new ControlColor(ControlColor.BLACK, ControlColor.WHITE));
            asBean(new MenuAppVersion(VersionInfo.fromString("4.2.0"), "TS", "Group", "abc"));
            scanForComponents();
        }

        @TcComponent
        DependentItem dependentItem(BooleanMenuItem dependee, AnalogMenuItem dependee2) {
            return new DependentItem(dependee, dependee2);
        }

        @TcComponent
        AnalogMenuItem analogItem() {
            return new AnalogMenuItemBuilder().withName(mandatoryStringProp("dev.only.config")).withId(102)
                    .menuItem();
        }

        @TcComponent
        BooleanMenuItem booleanItem() {
            return BooleanMenuItemBuilder.aBooleanMenuItemBuilder().withId(100)
                    .withName(mandatoryStringProp("bool.item.name")).menuItem();
        }
    }

    public record DependentItem(BooleanMenuItem dependee, AnalogMenuItem dependee2) {
    }
}