package com.thecoderscorner.menu.auth;

import org.junit.jupiter.api.Test;

import java.util.List;
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
        auth.addAuthenticationToken("pete", petesUuid.toString());

        assertTrue(auth.authenticate("dave", davesUuid));
        assertFalse(auth.authenticate("dave", petesUuid));
        assertFalse(auth.authenticate("daniel", petesUuid));
        assertTrue(auth.authenticate("pete", petesUuid));
    }
}