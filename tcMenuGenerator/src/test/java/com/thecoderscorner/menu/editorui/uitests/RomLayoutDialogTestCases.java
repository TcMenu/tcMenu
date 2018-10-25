package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.dialog.RomLayoutDialog;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.scene.control.Label;
import javafx.stage.Stage;
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
        romDialog = new RomLayoutDialog(stage, TestUtils.buildSimpleTree());
        romDialog.show();

        this.stage = stage;
    }

    @Test
    void testCheckingDialog(FxRobot robot) {

        // get all the heading labels and check them.
        List<String> idTitle = getStringsFromNodeQuery(robot, "#idContainer > .label-bright");
        assertThat(idTitle).containsExactly("ID Ranges");

        List<String> eepromTitle = getStringsFromNodeQuery(robot, "#eepromContainer > .label-bright");
        assertThat(eepromTitle).containsExactly("EEPROM Ranges");

        List<String> idStrings = getStringsFromNodeQuery(robot, "#idContainer > .idRomEntry");
        assertThat(idStrings).containsExactly(
                "1 - test",
                "2 - test",
                "20 - Extra",
                "100 - sub"
        );

        List<String> brokenEeproms = getStringsFromNodeQuery(robot, "#eepromContainer > .brokenEeprom");
        assertThat(brokenEeproms).containsExactly("4-5: test", "4-5: Extra");

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