package com.thecoderscorner.menu.auth;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    void testContainsItemsPass() {
        var auth = new PreDefinedAuthenticator(List.of(
                new PreDefinedAuthenticator.AuthenticationToken("dave", davesUuid.toString()),
                new PreDefinedAuthenticator.AuthenticationToken("marianna", mariannasUuid.toString()),
                new PreDefinedAuthenticator.AuthenticationToken("daniel", danielsUuid.toString()))
        );
        auth.addAuthentication("pete", UUID.fromString(petesUuid.toString()));

        assertTrue(auth.authenticate("dave", davesUuid));
        assertFalse(auth.authenticate("dave", petesUuid));
        assertFalse(auth.authenticate("daniel", petesUuid));
        assertTrue(auth.authenticate("pete", petesUuid));
    }

    @Test
    void testPropertiesAuthenticator() throws Exception {
        var tempFile = Files.createTempFile("tcmenuProps", ".properties");

        try {
            Files.writeString(tempFile, "dave=" + davesUuid + "\nmarianna=" + mariannasUuid + "\npete=" + petesUuid + "\n");
            var auth = new PropertiesAuthenticator(tempFile.toString());
            assertTrue(auth.authenticate("dave", davesUuid));
            assertFalse(auth.authenticate("dave", petesUuid));
            assertFalse(auth.authenticate("daniel", danielsUuid));

            auth.addAuthentication("daniel", danielsUuid);
            assertTrue(auth.authenticate("daniel", danielsUuid));

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
}