package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DialogManagerTest {

    private MenuButtonType buttonPressed;
    private boolean shouldClose;
    private boolean didChange;
    private MenuButtonType regularButtonPress = MenuButtonType.NONE;

    @Test
    void testDialogPresentedLocally() {
        UnitTestDlgManager dlgManager = new UnitTestDlgManager();
        shouldClose = false;
        didChange = false;
        dlgManager.withMessage("hello", false)
                .withTitle("world", false)
                .withDelegate(DialogShowMode.LOCAL_DELEGATE_LOCKED, menuButtonType -> {
                    buttonPressed = menuButtonType;
                    return shouldClose;
                })
                .showDialogWithButtons(MenuButtonType.OK, MenuButtonType.CANCEL);
        assertTrue(dlgManager.isDialogVisible());
        assertEquals("hello", dlgManager.getMessage());
        assertEquals("world", dlgManager.getTitle());
        assertEquals(MenuButtonType.OK, dlgManager.getButtonType(1));
        assertEquals(MenuButtonType.CANCEL, dlgManager.getButtonType(2));
        assertTrue(didChange);

        // try and remotely update, should not be possible as in local locked mode.
        dlgManager.updateStateFromCommand(new MenuDialogCommand(DialogMode.SHOW, "New123", "New234",
                MenuButtonType.ACCEPT, MenuButtonType.CLOSE, CorrelationId.EMPTY_CORRELATION));
        assertEquals("hello", dlgManager.getMessage());
        assertEquals("world", dlgManager.getTitle());

        // try and remotely close, should not be possible as in locked local mode.
        dlgManager.updateStateFromCommand(new MenuDialogCommand(DialogMode.ACTION, "New123", "New234",
                MenuButtonType.ACCEPT, MenuButtonType.CLOSE, CorrelationId.EMPTY_CORRELATION));
        assertTrue(dlgManager.isDialogVisible());

        // now simulate closing when our button delegate handler returns false, should not close.
        dlgManager.buttonWasPressed(MenuButtonType.OK);
        assertTrue(dlgManager.isDialogVisible());

        // now actually allow the close and ensure it closes
        shouldClose = true;
        dlgManager.buttonWasPressed(MenuButtonType.OK);
        assertFalse(dlgManager.isDialogVisible());
        assertEquals(MenuButtonType.OK, buttonPressed);
        assertEquals(MenuButtonType.NONE, regularButtonPress);
    }

    @Test
    void testDialogPresentedRemotely() {
        didChange = false;
        UnitTestDlgManager dlgManager = new UnitTestDlgManager();
        dlgManager.updateStateFromCommand(new MenuDialogCommand(DialogMode.SHOW, "New123", "New234",
                MenuButtonType.ACCEPT, MenuButtonType.CLOSE, CorrelationId.EMPTY_CORRELATION));
        assertEquals("New234", dlgManager.getMessage());
        assertEquals("New123", dlgManager.getTitle());
        assertEquals(MenuButtonType.ACCEPT, dlgManager.getButtonType(1));
        assertEquals(MenuButtonType.CLOSE, dlgManager.getButtonType(2));
        assertTrue(didChange);

        dlgManager.updateStateFromCommand(new MenuDialogCommand(DialogMode.ACTION, "New123", "New234",
                MenuButtonType.ACCEPT, MenuButtonType.NONE, CorrelationId.EMPTY_CORRELATION));

        assertEquals(MenuButtonType.ACCEPT, regularButtonPress);
    }

    @Test
    void textAllPrintableTextCases() {
        UnitTestDlgManager dlgManager = new UnitTestDlgManager();
        assertEquals("OK", dlgManager.getPrintableText(MenuButtonType.OK));
        assertEquals("Close", dlgManager.getPrintableText(MenuButtonType.CLOSE));
        assertEquals("Accept", dlgManager.getPrintableText(MenuButtonType.ACCEPT));
        assertEquals("Cancel", dlgManager.getPrintableText(MenuButtonType.CANCEL));
    }

    class UnitTestDlgManager extends DialogManager {

        @Override
        protected void dialogDidChange() {
            didChange = true;
        }

        @Override
        protected void buttonWasPressed(MenuButtonType btn) {
            if(getDialogShowMode() == DialogShowMode.REGULAR) {
                regularButtonPress = btn;
            }
            super.buttonWasPressed(btn);
        }

        public String getMessage() {
            return message;
        }

        public String getTitle() {
            return title;
        }

        public String getPrintableText(MenuButtonType type) {
            return toPrintableText(type);
        }
    }
}