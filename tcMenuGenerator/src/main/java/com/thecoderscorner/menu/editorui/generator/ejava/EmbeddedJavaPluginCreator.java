package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.LambdaCodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.ReferenceCodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.CodeVariable;
import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredSourceFile;
import com.thecoderscorner.menu.editorui.generator.plugin.VariableDefinitionMode;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.METHOD_IF_MISSING;

public class EmbeddedJavaPluginCreator {
    private final CodeConversionContext context;
    private final CodeParameter expando = new CodeParameter("", "", true, "");

    public EmbeddedJavaPluginCreator(CodeConversionContext context) {
        this.context = context;
    }

    public void mapImports(List<HeaderDefinition> headers, JavaClassBuilder builder) {
        var applicableHeaders = headers.stream()
                .filter(h -> h.getApplicability().isApplicable(context.getProperties()) && h.getHeaderType() == HeaderDefinition.HeaderType.SOURCE)
                .toList();
        for(var h : applicableHeaders) {
            builder.addPackageImport(expando.expandExpression(context, h.getHeaderName()));
        }
    }

    public void mapMethodCalls(List<FunctionDefinition> functions, GeneratedJavaMethod method, List<String> extras) {
        var l = functions.stream()
                .filter(fn -> fn.getApplicability().isApplicable(context.getProperties()))
                .filter(fn -> !fn.isInfiniteLoopFn())
                .toList();
        for(var fn : l) {
            processFunctionDefToMethod(method, fn);
        }

        for(var extra : extras) {
            method.withStatement(extra);
        }

        functions.stream()
                .filter(fn -> fn.getApplicability().isApplicable(context.getProperties()))
                .filter(FunctionDefinition::isInfiniteLoopFn)
                .findFirst().ifPresent(fn -> processFunctionDefToMethod(method, fn));
    }

    private void processFunctionDefToMethod(GeneratedJavaMethod method, FunctionDefinition fn) {
        String objName = expando.expandExpression(context, fn.getObjectName());
        var obj = fn.isObjectPointer() ? "context.getBean(" + objName + ")" : objName;
        obj = obj + "." + expando.expandExpression(context, fn.getFunctionName()) + "(";
        obj += fn.getParameters().stream().map(this::transformParam).collect(Collectors.joining(", "));
        obj += ");";
        method.withStatement(obj);
    }

    public void mapVariables(List<CodeVariable> variables, JavaClassBuilder cb) {
        var applicableVariables = getApplicableVariablesForAppField(variables);
        for(var v : applicableVariables) {
            cb.addStatement(new GeneratedJavaField(v.getObjectName(), v.getVariableName()));
        }
    }

    public void mapConstructorStatements(List<CodeVariable> variables, GeneratedJavaMethod cb) {
        var applicableVariables = getApplicableVariablesForAppField(variables);

        for(var v : applicableVariables) {
            String objName = expando.expandExpression(context, v.getObjectName());
            String value = expando.expandExpression(context, v.getVariableName());
            if(v.isInContext()) {
                cb.withStatement(v.getVariableName() + " = context.getBean(" + objName + ".class);");
            } else {
                cb.withStatement(value + " = new " + objName + "(" +
                        v.getParameterList().stream().map(this::transformParam).collect(Collectors.joining(", ")) +
                        ");");
            }
        }
    }

    private List<CodeVariable> getApplicableVariablesForAppField(List<CodeVariable> variables) {
        return variables.stream()
                .filter(CodeVariable::isVariableDefNeeded)
                .filter(cv -> cv.getApplicability().isApplicable(context.getProperties()) &&
                        (!cv.isInContext() || cv.getDefinitionMode() == VariableDefinitionMode.VARIABLE_AND_EXPORT))
                .toList();
    }

    public void mapContext(List<CodeVariable> variables, JavaClassBuilder cb) {
        var l = variables.stream()
                .filter(cv -> cv.getApplicability().isApplicable(context.getProperties()) && cv.isInContext())
                .toList();
        for(var v : l) {
            String objName = expando.expandExpression(context, v.getObjectName());
            String value = expando.expandExpression(context, v.getVariableName());
            var method = new GeneratedJavaMethod(METHOD_IF_MISSING, objName, value).withAnnotation("Bean");
            StringBuilder statement = new StringBuilder("return new " + objName + "(");
            boolean firstStatement = true;
            for(var param : v.getParameterList()) {
                if(!firstStatement) statement.append(", ");
                firstStatement = false;
                String typeName = expando.expandExpression(context, param.getType());
                String paramName = expando.expandExpression(context, param.getName());
                if(param instanceof ReferenceCodeParameter) {
                    statement.append(paramName);
                    method.withParameter(typeName + " " + paramName);
                }
                else {
                    statement.append(expandValueForParameter(param));
                }
            }
            statement.append(");");
            method.withStatement(statement.toString());
            cb.addStatement(method);
        }

    }

    private String transformParam(CodeParameter p) {
        String paramVal;
        if (p instanceof LambdaCodeParameter) {
            throw new UnsupportedOperationException("Lambda not supported in Java, use a method reference instead");
        }
        else {
            paramVal = expandValueForParameter(p);
            if(p instanceof ReferenceCodeParameter && !paramVal.equals("NULL")) {
                paramVal = "context.getBean(" + paramVal + ")";
            }
        }
        return paramVal;
    }

    private String expandValueForParameter(CodeParameter p) {
        String paramVal;
        paramVal = p.expandExpression(context, p.getValue());
        if (StringHelper.isStringEmptyOrNull(paramVal) && !StringHelper.isStringEmptyOrNull(p.getDefaultValue())) {
            paramVal = p.expandExpression(context, p.getDefaultValue());
        }
        return paramVal;
    }
}
