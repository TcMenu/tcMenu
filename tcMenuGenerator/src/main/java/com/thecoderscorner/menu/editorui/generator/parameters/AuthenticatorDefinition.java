package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.AVREepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.ArrayList;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition.*;

public interface AuthenticatorDefinition extends CodeGeneratorCapable {
    String writeToProject();

    static AuthenticatorDefinition readFromProject(String encoding) {
        if(StringHelper.isStringEmptyOrNull(encoding)) return new NoAuthenticatorDefinition();

        try {
            if (encoding.startsWith("rom:")) {
                var entries = encoding.split(":");
                if(entries.length != 3) return new NoAuthenticatorDefinition();
                int offset = Integer.parseInt(entries[1]);
                int numRemotes = Integer.parseInt(entries[2]);
                return new EepromAuthenticatorDefinition(offset, numRemotes);
            }
            else if(encoding.startsWith("flash:")) {
                var remoteIds = encoding.split(":");
                int current = 2;
                var remoteList = new ArrayList<FlashRemoteId>();
                while((current + 1) < remoteIds.length) {
                    remoteList.add(new FlashRemoteId(remoteIds[current], remoteIds[current + 1]));
                    current += 2;
                }
                return new ReadOnlyAuthenticatorDefinition(remoteIds[1], remoteList);
            }
            else return new NoAuthenticatorDefinition();
        }
        catch (Exception ex) {
            return new NoAuthenticatorDefinition();
        }
    }

    @Override
    default Optional<String> generateExport() {
        return Optional.empty();
    }
}
