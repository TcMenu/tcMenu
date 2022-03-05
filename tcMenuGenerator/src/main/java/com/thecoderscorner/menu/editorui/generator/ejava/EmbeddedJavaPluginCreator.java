package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.LambdaCodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.ReferenceCodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.List;
import java.util.stream.Collectors;

public class EmbeddedJavaPluginCreator {
    private final CodeConversionContext context;
    private final CodeParameter expando = new CodeParameter("", true, "");

    public EmbeddedJavaPluginCreator(CodeConversionContext context) {
        this.context = context;
    }

    public void mapMethodCalls(List<FunctionDefinition> functions, GeneratedJavaMethod methodBuilder, String prefix) {
        var functionsForPlatform = functions.stream()
                .filter(fn -> fn.getApplicability().isApplicable(context.getProperties()))
                .toList();

        for(var func : functionsForPlatform) {
            var classAccessor = (func.isObjectPointer()) ? "" : ".";

            String fn = prefix;
            if(!StringHelper.isStringEmptyOrNull(func.getObjectName())) {
                fn += expando.expandExpression(context, func.getObjectName()) + '.';
            }
            fn += func.getFunctionName() + "(";
            var parameters = func.getParameters().stream()
                    .map(this::transformParam)
                    .collect(Collectors.joining(", "));
            fn += parameters;
            fn += ");";

        }

    }

    private String transformParam(CodeParameter p) {
        String paramVal;
        if (p instanceof LambdaCodeParameter lcp) {
            paramVal = transformLambda(lcp);
        }
        else {
            paramVal = p.expandExpression(context, p.getValue());
            if (StringHelper.isStringEmptyOrNull(paramVal) && !StringHelper.isStringEmptyOrNull(p.getDefaultValue())) {
                paramVal = p.expandExpression(context, p.getDefaultValue());
            }
            if(p instanceof ReferenceCodeParameter && !paramVal.equals("NULL")) {
                paramVal = "context.getBean(" + paramVal + ")";
            }
        }
        return paramVal;
    }

    private String transformLambda(LambdaCodeParameter lambda) {
        return "";
    }
}
