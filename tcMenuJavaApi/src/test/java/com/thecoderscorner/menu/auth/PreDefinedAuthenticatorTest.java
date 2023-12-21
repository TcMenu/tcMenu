package com.thecoderscorner.menu.auth;

import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PreDefinedAuthenticatorTest {

    private final UUID davesUuid = UUID.randomUUID();
    private final UUID petesUuid = UUID.randomUUID();
    private final UUID danielsUuid = UUID.randomUUID();
    private final UUID mariannasUuid = UUID.randomUUID();

    @Test
    void testEmptyAlwaysAllow() {
        var auth = new PreDefinedAuthenticator(true);
        assertTrue(auth.authenticate("anyone", UUID.randomUUID()));
    }

    @Test
    void testEmptyAlwaysDisallow() {
        var auth = new PreDefinedAuthenticator(false);
        assertFalse(auth.authenticate("anyone", UUID.randomUUID()));
    }

    @Test
    void testContainsItemsPass() throws Exception {
        var auth = new PreDefinedAuthenticator("4321", List.of(
                new PreDefinedAuthenticator.AuthenticationToken("dave", davesUuid.toString()),
                new PreDefinedAuthenticator.AuthenticationToken("marianna", mariannasUuid.toString()),
                new PreDefinedAuthenticator.AuthenticationToken("daniel", danielsUuid.toString()))
        );
        assertFalse(auth.addAuthentication("pete", UUID.fromString(petesUuid.toString()), true).get());

        assertTrue(auth.authenticate("dave", davesUuid));
        assertFalse(auth.authenticate("dave", petesUuid));
        assertFalse(auth.authenticate("daniel", davesUuid));
        assertFalse(auth.authenticate("pete", petesUuid));
        assertTrue(auth.doesPasscodeMatch("4321"));
        assertFalse(auth.doesPasscodeMatch("1234"));
    }

    @Test
    void testPropertiesAuthenticator() throws Exception {
        var tempFile = Files.createTempFile("tcmenuProps", ".properties");

        try {
            Files.writeString(tempFile, "dave=" + davesUuid + "\nmarianna=" + mariannasUuid + "\npete=" + petesUuid + "\n");
            UnitDialogManager dialogManager = new UnitDialogManager();
            var auth = new PropertiesAuthenticator(tempFile.toString(), dialogManager);
            assertTrue(auth.authenticate("dave", davesUuid));
            assertFalse(auth.authenticate("dave", petesUuid));
            assertFalse(auth.authenticate("daniel", danielsUuid));
            assertTrue(auth.doesPasscodeMatch("1234"));
            assertFalse(auth.doesPasscodeMatch("4321"));

            dialogManager.setPressedButton(MenuButtonType.ACCEPT);
            var future = auth.addAuthentication("daniel", danielsUuid, true);
            assertTrue(future.get());
            assertTrue(auth.authenticate("daniel", danielsUuid));

            dialogManager.setPressedButton(MenuButtonType.CANCEL);
            UUID another = UUID.randomUUID();
            future = auth.addAuthentication("jjsdlf", another, true);
            assertFalse(future.get());
            assertFalse(auth.authenticate("jjsdlf", another));

            var props = new Properties();
            props.load(Files.newBufferedReader(tempFile));
            assertEquals(props.getProperty("daniel"), danielsUuid.toString());
            assertEquals(props.getProperty("dave"), davesUuid.toString());
            assertEquals(props.getProperty("pete"), petesUuid.toString());

        }
        finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * The test is responsible for verifying that the removeAuthentication method
     * correctly removes a user's authentication from the stored authenticationItems list if it exists
     */
    @Test
    void testRemoveExistingAuthentication() {
        List<PreDefinedAuthenticator.AuthenticationToken> tokens = new ArrayList<>();
        tokens.add(new PreDefinedAuthenticator.AuthenticationToken("user1", UUID.randomUUID().toString()));
        tokens.add(new PreDefinedAuthenticator.AuthenticationToken("user2", UUID.randomUUID().toString()));
        PreDefinedAuthenticator authenticator = new PreDefinedAuthenticator("pass1234", tokens);

        authenticator.removeAuthentication("user1");

        List<String> names = authenticator.getAllNames();
        assertFalse(names.contains("user1"), "User1 should be removed");
    }

    /**
     * The test verifies that the removeAuthentication method does not remove
     * a user's authentication from the stored authenticationItems list if it does not exist
     */
    @Test
    void testRemoveNonexistentAuthentication() {
        List<PreDefinedAuthenticator.AuthenticationToken> tokens = new ArrayList<>();
        tokens.add(new PreDefinedAuthenticator.AuthenticationToken("user1", UUID.randomUUID().toString()));
        tokens.add(new PreDefinedAuthenticator.AuthenticationToken("user2", UUID.randomUUID().toString()));
        PreDefinedAuthenticator authenticator = new PreDefinedAuthenticator("pass1234", tokens);

        authenticator.removeAuthentication("user3");

        List<String> names = authenticator.getAllNames();
        assertTrue(names.contains("user1"), "User1 should still be present");
        assertTrue(names.contains("user2"), "User2 should still be present");
    }

    private class UnitDialogManager extends DialogManager {
        private MenuButtonType pressedButton;

        void assertContents(String title, String text) {
            assertEquals(title, this.title);
            assertEquals(message, this.message);
        }

        private void setPressedButton(MenuButtonType btn) {
            synchronized (lock) {
                pressedButton = btn;
            }
        }

        @Override
        protected void dialogDidChange() {
            synchronized (lock) {
                if (mode == DialogMode.SHOW) {
                    delegate.apply(pressedButton);
                }
            }
        }

        @Override
        protected void buttonWasPressed(MenuButtonType btn) {
        }
    }
}