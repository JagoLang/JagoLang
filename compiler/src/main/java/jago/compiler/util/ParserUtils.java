package jago.compiler.util;

import jago.antlr.JagoParser;
import jago.compiler.domain.Parameter;
import jago.compiler.domain.VarargParameter;
import jago.compiler.domain.generic.GenericParameter;
import jago.compiler.domain.imports.Import;
import jago.compiler.domain.scope.LocalScope;
import jago.compiler.domain.type.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static List<Parameter> parseParameters(JagoParser.CallableDeclarationContext ctx,
                                                  List<Import> imports,
                                                  List<GenericParameter> genericParameters) {
        if (ctx.parametersList() == null) {
            return Collections.emptyList();
        }
        int hasVararg = ctx.parametersList().varargParameter != null ? 1 : 0;
        List<Parameter> parameters = new ArrayList<>();
        List<JagoParser.ParameterContext> parameterCtxs = ctx.parametersList().parameter();
        for (int i = 0; i < parameterCtxs.size() - hasVararg; i++) {
            JagoParser.ParameterContext parameterContext = parameterCtxs.get(i);
            Type type = TypeResolver.getFromTypeContext(parameterContext.type(), imports, genericParameters);
            parameters.add(new Parameter(parameterContext.id().getText(), type, parameterContext.expression()));
        }
        if (hasVararg != 0) {
            JagoParser.ParameterContext varargParam = parameterCtxs.get(parameterCtxs.size() - 1);
            Type componentType = TypeResolver.getFromTypeContext(varargParam.type(), imports, genericParameters);
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

    public static List<Type> parseGenericArguments(JagoParser.GenericArgumentsContext ctx,
                                                   List<Import> imports,
                                                   List<GenericParameter> genericParameters) {
        List<Type> list = new ArrayList<>();
        for (JagoParser.TypeContext typeCtx : ctx.type()) {
            list.add(TypeResolver.getFromTypeContext(typeCtx, imports, genericParameters));
        }
        return list;
    }

    public static List<Type> parseGenericArguments(JagoParser.GenericArgumentsContext ctx,
                                                   LocalScope scope) {
        return parseGenericArguments(ctx, scope.getImports(), scope.getAllGenericParameters());
    }

}
