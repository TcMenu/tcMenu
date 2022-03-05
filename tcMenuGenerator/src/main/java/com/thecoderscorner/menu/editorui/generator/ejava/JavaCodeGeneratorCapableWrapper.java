package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.parameters.CodeGeneratorCapable;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;

import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.METHOD_IF_MISSING;

public class JavaCodeGeneratorCapableWrapper {

    public void addAppFields(CodeGeneratorCapable capable, JavaClassBuilder classBuilder) {
        makeSureNotEepromForJava(capable);

    }

    public void addAppMethods(CodeGeneratorCapable capable, JavaClassBuilder classBuilder) {
        makeSureNotEepromForJava(capable);

    }

    public void addToContext(CodeGeneratorCapable capable, JavaClassBuilder classBuilder) {
        makeSureNotEepromForJava(capable);
        if(capable instanceof NoAuthenticatorDefinition) {
            classBuilder.addPackageImport("com.thecoderscorner.menu.auth");
            classBuilder.addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuAuthenticator", "menuAuthenticator")
                    .withStatement("return new PreDefinedAuthenticator(true);").withAnnotation("Bean"));
        }
        else if(capable instanceof ReadOnlyAuthenticatorDefinition readAuth) {
            var authTokens = readAuth.getRemoteIds().stream()
                    .map(remId -> "new PreDefinedAuthenticator.AuthenticationToken(\"" + remId.name() +"\", \"" + remId.uuid() +"\")").toList();
            classBuilder.addPackageImport("com.thecoderscorner.menu.auth");
            classBuilder.addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuAuthenticator", "menuAuthenticator")
                    .withStatement("var remoteTokens = List.of(" + String.join(", ", authTokens) + ");")
                    .withStatement("return new PreDefinedAuthenticator(\"" + readAuth.getPin() + "\", remoteTokens);")
                    .withAnnotation("Bean"));
        }
        else if(capable instanceof EepromAuthenticatorDefinition) {
            classBuilder.addPackageImport("com.thecoderscorner.menu.auth");
            classBuilder.addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "MenuAuthenticator", "menuAuthenticator")
                    .withParameter("@Value(\"${file.auth.storage}\") String propsPath")
                    .withStatement("return new PropertiesAuthenticator(propsPath);"));
        }
    }

    private void makeSureNotEepromForJava(CodeGeneratorCapable capable) {
        if(capable instanceof EepromDefinition) {
            if(!(capable instanceof NoEepromDefinition)) {
                throw new UnsupportedOperationException("Embedded Java does not presently support EEPROM");
            }
        }
    }
}
