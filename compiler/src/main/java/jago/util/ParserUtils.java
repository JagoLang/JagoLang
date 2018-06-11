package jago.util;

import jago.JagoParser;
import jago.domain.VarargParameter;
import jago.domain.imports.Import;
import jago.domain.Parameter;
import jago.domain.node.expression.call.Argument;
import jago.domain.type.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ParserUtils {

    //TODO this method is stupid
    public static List<Import> parseImports(JagoParser.ImportsContext ctx) {
        List<Import> imports = new ArrayList<>(ctx.importStatement().size() + 1);
        imports.add(new Import("jago", null, true));
        for (JagoParser.ImportStatementContext importStatementContext : ctx.importStatement()) {
            JagoParser.ImportNameContext importNameContext = importStatementContext.importName();
            String fromPackage;
            if (importNameContext.qualifiedName() == null) {
                fromPackage = "";
            } else {
                fromPackage = importNameContext.qualifiedName().getText();
            }
            String clazz = importNameContext.fromClass.getText();
            if (clazz.equals("*")) {
                imports.add(new Import(fromPackage, null, true));
            } else {
                imports.add(new Import(fromPackage, clazz, false));
            }
        }
        return imports;
    }

    public static List<Parameter> parseParameters(JagoParser.CallableDeclarationContext ctx, List<Import> imports) {
        if (ctx.parametersList() == null) {
            return Collections.emptyList();
        }
        int hasVararg = ctx.parametersList().varargParameter != null ? 1 : 0;
        List<Parameter> parameters = new ArrayList<>();
        List<JagoParser.ParameterContext> parameterCtxs = ctx.parametersList().parameter();
        for (int i = 0; i < parameterCtxs.size() - hasVararg; i++) {
            JagoParser.ParameterContext parameterContext = parameterCtxs.get(i);
            Type type = TypeResolver.getFromTypeContext(parameterContext.type(), imports);
            parameters.add(new Parameter(parameterContext.id().getText(), type, parameterContext.expression()));
        }
        if (hasVararg != 0) {
            JagoParser.ParameterContext varargParam = parameterCtxs.get(parameterCtxs.size() - 1);
            Type componentType = TypeResolver.getFromTypeContext(varargParam.type(), imports);
            CompositeType type = componentType instanceof NumericType
                    ? new PrimitiveArrayType((NumericType) componentType)
                    : new ArrayType(componentType);
            parameters.add(new VarargParameter(varargParam.id().getText(),
                    type,
                    varargParam.expression()));
        }
        if (parameters.stream().map(Parameter::getName).distinct().count() != parameters.size()) {
            throw new NotImplementedException("2 params withs the same name");
        }
        return parameters;
    }

    public static List<Type> parseGenericArguments(JagoParser.GenericArgumentsContext ctx, List<Import> imports) {
        List<Type> list = new ArrayList<>();
        for (JagoParser.TypeContext typeCtx : ctx.type()) {
            list.add( TypeResolver.getFromTypeContext(typeCtx, imports));
        }
        return list;
    }
}
