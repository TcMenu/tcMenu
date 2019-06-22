/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.junit.jupiter.api.Test;

class TextTreeItemRendererTest {

    @Test
    void testRenderingOfComplexTree() {
        MenuTree tree = TestUtils.buildCompleteTree();
        TextTreeItemRenderer renderer = new TextTreeItemRenderer(tree);
        String actual = renderer.getTreeAsText();

        TestUtils.assertEqualsIgnoringCRLF(
                " Extra                    test\n" +
                        " test                    100dB\n" +
                        " sub                       >>>\n" +
                        "   test                  100dB\n" +
                        " BoolTest                   ON\n" +
                        " TextTest           AAAAAAAAAA\n" +
                        " FloatTest         -12345.1235\n" +
                        " RemoteTest            No Link\n" +
                        " ActionTest                  \n" +
                        " Subnet Mask         127.0.0.1\n" +
                        " List                      >>>\n",
                actual);
    }
}