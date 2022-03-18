package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.applicability.*;
import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.StringPropertyValidationRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodeParameterTest {

    private final List<CreatorProperty> props = List.of(
            new CreatorProperty("VAR1", "Var 1", "Var 1 desc", "12345", SubSystem.DISPLAY, CreatorProperty.PropType.USE_IN_DEFINE, CannedPropertyValidators.textValidator(), new AlwaysApplicable()),
            new CreatorProperty("VAR2", "Var 2", "Var 2 desc", "54321", SubSystem.DISPLAY, CreatorProperty.PropType.USE_IN_DEFINE, CannedPropertyValidators.textValidator(), new AlwaysApplicable()),
            new CreatorProperty("VAR3", "Var 3", "Var 3 desc", "ada:font,1", SubSystem.DISPLAY, CreatorProperty.PropType.USE_IN_DEFINE, CannedPropertyValidators.textValidator(), new AlwaysApplicable()));
    private CodeConversionContext context;
    private final UUID APPUUID = UUID.randomUUID();

    @BeforeEach
    public void InitTest() {
        CodeGeneratorOptions opts = mock(CodeGeneratorOptions.class);
        when(opts.getApplicationName()).thenReturn("MyAppName");
        when(opts.getApplicationUUID()).thenReturn(APPUUID);
        when(opts.getPackageNamespace()).thenReturn("my.namespace");
        context = new CodeConversionContext(EmbeddedPlatform.ARDUINO_AVR, "volumeMenuItem", opts, props);
    }

    @Test
    public void testDefaultVariables() {
        var param = new CodeParameter("MyType", "name", false, "unquoted");
        assertEquals("MyAppName", param.expandExpression(context, "${APP_NAME}"));
        assertEquals(APPUUID.toString(), param.expandExpression(context, "${APP_UUID}"));
        assertEquals("volumeMenuItem", param.expandExpression(context, "${ROOT}"));
        assertEquals("ARDUINO_AVR", param.expandExpression(context, "${TARGET}"));
        assertEquals("my.namespace", param.expandExpression(context, "${NAMESPACE}"));
    }

    @Test
    public void TestVariableSubstitution() {
        var param = new CodeParameter("MyType", "name", false, "unquoted");
        assertEquals("unquotedText", param.expandExpression(context, "unquotedText"));
        assertEquals("part 12345 app", param.expandExpression(context, "part ${VAR1} app"));
        assertEquals("54321", param.expandExpression(context, "${VAR2}"));
        assertEquals("54321", param.expandExpression(context, "${VAR2")); // allow lax parsing.
        assertEquals("improperlyDefinedVariable", param.expandExpression(context, "$improperlyDefinedVariable")); // no braces is not defined
        assertEquals("$60", param.expandExpression(context, "\\$60")); // escape the dollar symbol
        assertEquals("\\", param.expandExpression(context, "\\")); // invalid escape is verbatim.
        assertEquals("hello \\ world", param.expandExpression(context, "hello \\\\ world")); // escape the escape char.
        assertEquals("", param.expandExpression(context, "${UNKNOWN}")); // unknown variable is blank
        assertEquals("font", param.expandExpression(context, "${VAR3/.*:(.*),1/}")); // reg on variable is actioned
        assertEquals("font ada", param.expandExpression(context, "${VAR3/.*:(.*),1/} ${VAR3/(.*):.*/}")); // reg on variable is actioned

        assertEquals("Var 1", props.get(0).getDescription());
        assertEquals("Var 1 desc", props.get(0).getExtendedDescription());
        assertEquals("VAR1", props.get(0).getName());

        var textValidator = (StringPropertyValidationRules) props.get(0).getValidationRules();
        assertNotNull(textValidator);
        assertTrue(textValidator.isValueValid("1234567890123456789"));
        assertFalse(textValidator.isValueValid("1234567890123456789012345678901234567890"));
    }

    @Test
    public void TestNestedApplicability() {
        List<CodeApplicability> applicabilityList = List.of(
                new EqualityApplicability("VAR1", "12345", false),
                new EqualityApplicability("VAR2", "none", false)
        );

        var nestedAnd = new NestedApplicability(NestedApplicability.NestingMode.AND, applicabilityList);
        var nestedOr = new NestedApplicability(NestedApplicability.NestingMode.OR, applicabilityList);

        // match on VAR1
        assertFalse(nestedAnd.isApplicable(props));
        assertTrue(nestedOr.isApplicable(props));

        // match on both VAR1 and VAR2
        props.get(1).setLatestValue("none");
        assertTrue(nestedAnd.isApplicable(props));
        assertTrue(nestedOr.isApplicable(props));

        // match on neither
        props.get(0).setLatestValue("edfds");
        props.get(1).setLatestValue("ghjfgh");
        assertFalse(nestedAnd.isApplicable(props));
        assertFalse(nestedOr.isApplicable(props));
    }

    @Test
    public void TestApplicability() {
        var alwaysApplicable = new AlwaysApplicable();
        assertTrue(alwaysApplicable.isApplicable(props));

        var whenProperty = new EqualityApplicability("VAR1", "12345", false);
        assertTrue(whenProperty.isApplicable(props));

        var whenPropertyFalse = new EqualityApplicability("jksef", "343", false);
        assertFalse(whenPropertyFalse.isApplicable(props));

        var whenPropertyInverted = new EqualityApplicability("VAR2", "none", true);
        assertTrue(whenPropertyInverted.isApplicable(props));

        var whenPropertyInvert2 = new EqualityApplicability("VAR2", "54321", true);
        assertFalse(whenPropertyInvert2.isApplicable(props));

        var matchesExpression = new MatchesApplicability("VAR2", "\\d*");
        assertTrue(matchesExpression.isApplicable(props));

        var matchesExpression2 = new MatchesApplicability("VAR2", "true");
        assertFalse(matchesExpression2.isApplicable(props));
    }
}
