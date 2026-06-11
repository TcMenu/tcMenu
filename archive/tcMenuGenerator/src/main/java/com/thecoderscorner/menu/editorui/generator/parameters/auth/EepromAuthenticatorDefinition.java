package com.thecoderscorner.menu.editorui.generator.parameters.auth;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;

public class EepromAuthenticatorDefinition implements AuthenticatorDefinition {
    private final int offset;
    private final int numRemotes;

    public EepromAuthenticatorDefinition(int offset, int numRemotes) {
        this.offset = offset;
        this.numRemotes = numRemotes;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.of("    authManager.initialise(menuMgr.getEepromAbstraction(), " + offset + ");" + LINE_BREAK +
                "    menuMgr.setAuthenticator(&authManager);");
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of("EepromAuthenticatorManager authManager(" + numRemotes + ");");
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("RemoteAuthentication.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }

    @Override
    public String writeToProject() {
        return "rom:" + offset + ":" + numRemotes;
    }

    @Override
    public String toString() {
        return "EEPROM Authenticator, offset=" + offset;
    }
}
