package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseJavaPluginItem implements JavaPluginItem {

    protected String findPropOrFail(String s) {
        return getRequiredProperties().stream().filter(p -> p.getName().equals(s))
                .findFirst().orElseThrow()
                .getLatestValue();
    }

    protected CodeVariable basicGraphicsDeviceVariable(int sizeBuffer) {
        return new CodeVariable("renderer", "GraphicsDeviceRenderer", VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, List.of(
                CodeParameter.unNamedValue(sizeBuffer),
                CodeParameter.unNamedValue("applicationInfo.name"),
                CodeParameter.unNamedValue("&drawable")
        ), ALWAYS_APPLICABLE);
    }

    protected FunctionDefinition basicUpdatesPerSecond() {
        return new FunctionDefinition("setUpdatesPerSecond", "renderer", false, false, List.of(
                CodeParameter.unNamedValue("${UPDATES_PER_SEC")
        ), ALWAYS_APPLICABLE);
    }

    protected FunctionDefinition basicSetRotation() {
        return new FunctionDefinition("setRotation", "display", false, false, List.of(
                CodeParameter.unNamedValue("${DISPLAY_ROTATION}")
        ), ALWAYS_APPLICABLE);
    }

    protected String expandToNull(String s) {
        return StringHelper.isStringEmptyOrNull(s) ? "nullptr" : s;
    }

    @Override
    public void beforeGenerationStarts(CodeConversionContext context) {
        updatePropertiesFromContext(context, getPlugin().getSubsystem());
    }

    private void updatePropertiesFromContext(CodeConversionContext context, SubSystem subsystem) {
        var propsForThisPlugin = context.getProperties().stream()
                .filter(p -> p.getSubsystem() == subsystem)
                .collect(Collectors.toMap(CreatorProperty::getName, p -> p));

        for(var prop : getRequiredProperties()) {
            if(propsForThisPlugin.containsKey(prop.getName())) {
                prop.setLatestValue( propsForThisPlugin.get(prop.getName()).getLatestValue() );
            }
        }
    }
}
