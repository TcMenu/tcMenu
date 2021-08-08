package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticatorDefinitionTest {
    @Test
    public void testNoAuthenticator() {
        var definition = AuthenticatorDefinition.readFromProject("");
        assertThat(definition).isInstanceOf(NoAuthenticatorDefinition.class);
        assertEquals("", definition.writeToProject());

        assertThat(definition.generateHeader()).isEmpty();
        assertThat(definition.generateGlobal()).isEmpty();
        assertThat(definition.generateCode()).isEmpty();
        assertEquals("No Authenticator", definition.toString());
    }

    @Test
    public void testEEPROMBasedAuthenticator() {
        var definition = AuthenticatorDefinition.readFromProject("rom:512:3");
        assertThat(definition).isInstanceOf(EepromAuthenticatorDefinition.class);
        assertEquals("rom:512:3", definition.writeToProject());

        assertThat(definition.generateHeader()).contains(new HeaderDefinition("RemoteAuthentication.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
        assertThat(definition.generateGlobal()).contains("EepromAuthenticatorManager authManager(3);");
        assertThat(definition.generateCode()).contains("    authManager.initialise(menuMgr.getEepromAbstraction(), 512);" + LINE_BREAK +
                        "    menuMgr.setAuthenticator(&authManager);");
        assertEquals("EEPROM Authenticator, offset=512", definition.toString());
    }

    @Test
    public void testFlashBasedAuthenticator() {
        var definition = AuthenticatorDefinition.readFromProject("flash:1234:super123:94242-234234-23423-2342:super2:342342-234234-234-234-234");
        assertThat(definition).isInstanceOf(ReadOnlyAuthenticatorDefinition.class);
        assertEquals("flash:1234:super123:94242-234234-23423-2342:super2:342342-234234-234-234-234", definition.writeToProject());

        assertThat(definition.generateHeader()).contains(new HeaderDefinition("RemoteAuthentication.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
        assertThat(definition.generateGlobal()).contains("const AuthBlock authMgrAllowedRemotes[] PROGMEM = {" + LINE_BREAK +
                "    { \"94242-234234-23423-2342\", \"super123\" }," + LINE_BREAK +
                "    { \"342342-234234-234-234-234\", \"super2\" }" + LINE_BREAK +
                "};" + LINE_BREAK +
                "const char pgmAuthMgrPassword[] PROGMEM = \"1234\";" + LINE_BREAK +
                "ReadOnlyAuthenticationManager authManager(authMgrAllowedRemotes, 2, pgmAuthMgrPassword);");
        assertThat(definition.generateCode()).contains("    menuMgr.setAuthenticator(&authManager);");
        assertEquals("FLASH Authenticator, remotes=2", definition.toString());
    }
}
