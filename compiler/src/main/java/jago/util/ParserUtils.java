package jago.util;

import jago.JagoParser;
import jago.domain.imports.Import;
import jago.domain.node.expression.Parameter;

import java.util.ArrayList;
import java.util.List;

public final class ParserUtils {
    public static List<Import> parseImports(JagoParser.ImportsContext ctx) {
        List<Import> imports = new ArrayList<>(ctx.importStatement().size());
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

    public static List<Parameter> parseParameters(JagoParser.CallableContext ctx, List<Import> imports) {
        List<Parameter> parameters = new ArrayList<>();
        for (JagoParser.ParameterContext parameterContext : ctx.callableDeclaration().parametersList().parameter()) {
            parameters.add(new Parameter(parameterContext.ID().getText(), TypeResolver.getFromTypeContext(parameterContext.type(), imports)));
        }
        return parameters;
    }

}
