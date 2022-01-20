/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.dialog.RomLayoutDialog;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.NodeQuery;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class RomLayoutDialogTestCases {

    RomLayoutDialog romDialog;
    private Stage stage;

    @Start
    public void onStart(Stage stage) {
        romDialog = new RomLayoutDialog(stage, TestUtils.buildSimpleTree(), false);

        this.stage = stage;
    }

    @AfterEach
    public void tearDown() {
        Platform.runLater(()-> stage.close());
    }

    @Test
    void testCheckingDialog(FxRobot robot) {

        List<String> idStrings = getStringsFromNodeQuery(robot, "#idContainer > .idRomEntry");
        assertThat(idStrings).containsExactly(
                "1 - test",
                "2 - test",
                "20 - Extra",
                "100 - sub"
        );

        List<String> brokenEeproms = getStringsFromNodeQuery(robot, "#eepromContainer > .brokenEeprom");
        assertThat(brokenEeproms).containsExactly("4-5: test", "5-6: Extra");

        List<String> goodEeproms = getStringsFromNodeQuery(robot, "#eepromContainer > .eepromEntry");
        assertThat(goodEeproms).containsExactly("2-3: test");

        robot.clickOn("#closeButton");
    }

    private List<String> getStringsFromNodeQuery(FxRobot robot, String q) {
        NodeQuery query = robot.lookup(q);

        // filter down all the ID label text fields into a string list
        return query.queryAll().stream()
                .map(lbl-> ((Label)lbl).getText())
                .collect(Collectors.toList());
    }

}