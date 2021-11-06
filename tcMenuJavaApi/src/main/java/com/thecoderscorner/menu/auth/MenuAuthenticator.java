package com.thecoderscorner.menu.auth;

import java.util.UUID;

public interface MenuAuthenticator {
    boolean authenticate(String user, UUID uuid);
}
