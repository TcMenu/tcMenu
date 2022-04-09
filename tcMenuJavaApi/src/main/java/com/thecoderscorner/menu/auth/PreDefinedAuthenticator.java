package com.thecoderscorner.menu.auth;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Implements the authentication interface using a pre-defined upfront set of name and UUID pairs that must be provided
 * upfront. This implementation will never save authentication blocks to storage.
 */
public class PreDefinedAuthenticator implements MenuAuthenticator {
    private final List<AuthenticationToken> authenticationItems = new CopyOnWriteArrayList<>();
    private final boolean alwaysAllow;
    private final String securePasscode;

    public PreDefinedAuthenticator(boolean alwaysAllow) {
        this.alwaysAllow = alwaysAllow;
        this.securePasscode = null;
    }

    public PreDefinedAuthenticator(String securePasscode, List<AuthenticationToken> upfrontTokens) {
        this.alwaysAllow = false;
        this.securePasscode = securePasscode;
        authenticationItems.addAll(upfrontTokens);
    }

    @Override
    public CompletableFuture<Boolean> addAuthentication(String name, UUID uuid) {
        // pre defined authenticator cannot add items at runtime.
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public void removeAuthentication(String user) {
        var maybeAuth = authenticationItems.stream().filter(auth -> auth.name.equals(user)).findFirst();
        maybeAuth.ifPresent(authenticationItems::remove);
    }

    @Override
    public boolean authenticate(String user, UUID uuid) {
        if(authenticationItems.isEmpty()) return alwaysAllow;

        for(var auth: authenticationItems) {
            if(auth.doesMatch(user, uuid)) return true;
        }

        return false;
    }

    public boolean doesPasscodeMatch(String passcode) {
        if(securePasscode == null) return alwaysAllow;
        return securePasscode.equals(passcode);
    }

    @Override
    public ManagementCapabilities managementCapabilities() {
        return alwaysAllow ? ManagementCapabilities.NOT_EDITABLE : ManagementCapabilities.CAN_REMOVE_ADD;
    }

    @Override
    public List<String> getAllNames() {
        return authenticationItems.stream().map(auth -> auth.name).collect(Collectors.toList());
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
