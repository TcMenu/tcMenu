package com.thecoderscorner.menu.editorui.generator.parameters.auth;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

public class ReadOnlyAuthenticatorDefinition implements AuthenticatorDefinition {
    private String pin;
    private List<FlashRemoteId> remoteIds;

    public ReadOnlyAuthenticatorDefinition(String pin, List<FlashRemoteId> remoteIds) {
        this.pin = pin;
        this.remoteIds = remoteIds;
    }

    public String getPin() {
        return pin;
    }

    public List<FlashRemoteId> getRemoteIds() {
        return remoteIds;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.of("    menuMgr.setAuthenticator(&authManager);");
    }

    @Override
    public Optional<String> generateGlobal() {
        StringBuilder sb = new StringBuilder(255);
        sb.append("const AuthBlock authMgrAllowedRemotes[] PROGMEM = {").append(LINE_BREAK);
        sb.append(remoteIds.stream()
                .map(rem -> "    { \"" + rem.name() + "\", \"" + rem.uuid() + "\" }")
                .collect(Collectors.joining("," + LINE_BREAK)));
        sb.append(LINE_BREAK);
        sb.append("};");
        sb.append(LINE_BREAK);
        sb.append("const char pgmAuthMgrPassword[] PROGMEM = \"").append(pin).append("\";").append(LINE_BREAK);
        sb.append("ReadOnlyAuthenticationManager authManager(authMgrAllowedRemotes, ");
        sb.append(remoteIds.size());
        sb.append(", pgmAuthMgrPassword);");
        return Optional.of(sb.toString());
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("RemoteAuthentication.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }

    @Override
    public String writeToProject() {
        return "flash:" + pin + ':' +
                remoteIds.stream()
                        .map(rem -> rem.name() + ":" + rem.uuid())
                        .collect(Collectors.joining(":"));
    }

    @Override
    public String toString() {
        return "FLASH Authenticator, remotes=" + remoteIds.size();
    }

    public record FlashRemoteId(String name, String uuid) {
        @Override
        public String toString() {
            return name + " ID(" + uuid + ")";
        }
    }
}
