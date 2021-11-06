package com.thecoderscorner.menu.auth;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class PreDefinedAuthenticator implements MenuAuthenticator {
    private final List<AuthenticationToken> authenticationItems = new CopyOnWriteArrayList<>();
    private final boolean alwaysAllow;

    public PreDefinedAuthenticator(boolean alwaysAllow) {
        this.alwaysAllow = alwaysAllow;
    }

    public PreDefinedAuthenticator(List<AuthenticationToken> upfrontTokens) {
        this.alwaysAllow = false;
        authenticationItems.addAll(upfrontTokens);
    }

    public void addAuthenticationToken(String name, String uuid) {
        authenticationItems.add(new AuthenticationToken(name, uuid));
    }

    @Override
    public boolean authenticate(String user, UUID uuid) {
        if(authenticationItems.isEmpty()) return alwaysAllow;

        for(var auth: authenticationItems) {
            if(auth.doesMatch(user, uuid)) return true;
        }

        return false;
    }

    public static class AuthenticationToken {
        private final String name;
        private final String uuid;

        public AuthenticationToken(String name, String uuid) {
            this.name = name;
            this.uuid = uuid;
        }

        public boolean doesMatch(String name, UUID uuid) {
            if(name == null || uuid == null) return false;
            return this.name.equals(name) && this.uuid.equalsIgnoreCase(uuid.toString());
        }
    }
}
