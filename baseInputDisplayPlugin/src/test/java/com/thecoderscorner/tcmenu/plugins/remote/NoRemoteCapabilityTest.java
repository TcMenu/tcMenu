/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoRemoteCapabilityTest {

    @Test
    public void testNoRemoteCapabilities() {
        NoRemoteCapability creator = new NoRemoteCapability();
        assertEquals(0, creator.properties().size());
        var extractor = TestUtil.extractorFor(creator);

        creator.initCreator("root");

        assertThat(extractor.mapVariables(creator.getVariables())).isEmpty();

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isBlank();

        assertThat(extractor.mapExports(creator.getVariables())).isEmpty();

        assertThat(includeToString(creator.getIncludes())).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(extractor.mapDefines()).isEmpty();
    }
}