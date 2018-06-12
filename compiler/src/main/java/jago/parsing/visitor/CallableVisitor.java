package jago.parsing.visitor;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.imports.Import;
import jago.domain.Parameter;
import jago.domain.scope.CallableSignature;
import jago.domain.type.NoninstantiatableType;
import jago.domain.type.Type;
import jago.util.ParserUtils;
import jago.util.TypeResolver;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class CallableVisitor extends JagoBaseVisitor<Pair<CallableSignature, JagoParser.CallableBodyContext>> {

    private final List<Import> imports;
    private final Type owner;

    public CallableVisitor(String owner, List<Import> imports) {
        this.imports = imports;
        this.owner = new NoninstantiatableType(owner);
    }

    @Override
    public Pair<CallableSignature, JagoParser.CallableBodyContext> visitCallable(JagoParser.CallableContext ctx) {
        String name = ctx.callableDeclaration().callableName().getText();
        List<Parameter> parameters;

        if (ctx.callableDeclaration().parametersList() == null) {
            parameters = Collections.emptyList();
        } else {
            parameters = ParserUtils.parseParameters(ctx.callableDeclaration(), imports);
        }

        Type returnType = TypeResolver.getFromTypeContext(ctx.callableDeclaration().type(), imports);

        CallableSignature signature = new CallableSignature(owner, name, parameters, returnType);

        return Pair.of(signature, ctx.callableBody());
    }

}
