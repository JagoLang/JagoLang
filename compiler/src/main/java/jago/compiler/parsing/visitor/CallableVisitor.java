package jago.compiler.parsing.visitor;

import jago.antlr.JagoBaseVisitor;
import jago.antlr.JagoParser;
import jago.compiler.domain.Parameter;
import jago.compiler.domain.generic.GenericParameter;
import jago.compiler.domain.imports.Import;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.scope.GenericCallableSignature;
import jago.compiler.domain.type.AnyType;
import jago.compiler.domain.type.NonInstantiatableType;
import jago.compiler.domain.type.NullableType;
import jago.compiler.domain.type.Type;
import jago.compiler.domain.type.generic.GenericParameterType;
import jago.compiler.domain.type.generic.GenericType;
import jago.compiler.exception.VariableRedeclarationException;
import jago.compiler.util.ParserUtils;
import jago.compiler.util.TypeResolver;
import jago.compiler.util.constants.Messages;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CallableVisitor extends JagoBaseVisitor<Pair<CallableSignature, JagoParser.CallableBodyContext>> {

    private final List<Import> imports;
    private final Type owner;

    public CallableVisitor(String owner, List<Import> imports) {
        this.imports = imports;
        this.owner = new NonInstantiatableType(owner);
    }

    @Override
    public Pair<CallableSignature, JagoParser.CallableBodyContext> visitCallable(JagoParser.CallableContext ctx) {
        JagoParser.CallableDeclarationContext callableDeclaration = ctx.callableDeclaration();
        String name = callableDeclaration.callableName().getText();


        List<GenericParameter> genericParameters = Collections.emptyList();
        JagoParser.GenericParametersContext genericParametersCtx = callableDeclaration.genericParameters();
        if (genericParametersCtx != null) {
            List<JagoParser.GenericParameterContext> genericParameterCtxs = genericParametersCtx.genericParameter();
            for (int i = 0; i < genericParameterCtxs.size(); i++) {
                for (int j = 1; j < genericParameterCtxs.size(); j++) {
                    String t1 = genericParameterCtxs.get(i).getText();
                    if (t1.equals(genericParameterCtxs.get(j).getText())) {
                        throw new VariableRedeclarationException(t1);
                    }
                }
            }
            // consider params in local functions
            List<GenericParameter> genericParametersOwner = owner instanceof GenericType ? ((GenericType) owner).getBounds() : Collections.emptyList();
            for (JagoParser.GenericParameterContext gpCtx : genericParametersCtx.genericParameter()) {
                if (genericParametersOwner.stream().anyMatch(gpO -> gpO.getName().equals(gpCtx.id().getText()))) {
                    throw new VariableRedeclarationException(Messages.VARIABLE_REDECLARATION);
                }
            }

            genericParameters = genericParametersCtx.genericParameter().stream()
                    .map(gpCtx -> {
                        int variance = 0;
                        if (gpCtx.OUT_KEYWORD() != null) {
                            variance = GenericParameter.CO_VARIANT;
                        }
                        if (gpCtx.IN_KEYWORD() != null) {
                            variance = GenericParameter.CONTR_VARIANT;
                        }
                        Type constraint;
                        constraint = gpCtx.type() == null
                                ? NullableType.of(AnyType.INSTANCE)
                                : TypeResolver.getFromTypeContext(gpCtx.type(), imports);
                        return new GenericParameter(gpCtx.id().getText(), variance, constraint);
                    }).collect(Collectors.toList());
        }

        List<Parameter> parameters = callableDeclaration.parametersList() == null
                ? Collections.emptyList()
                : ParserUtils.parseParameters(callableDeclaration, imports, genericParameters);
        Type returnType = TypeResolver.getFromTypeContext(callableDeclaration.type(), imports);

        if (genericParameters == null) {
            CallableSignature signature = new CallableSignature(owner, name, parameters, returnType);
            return Pair.of(signature, ctx.callableBody());
        }

        List<Type> genericParameterTypes = genericParameters.stream()
                .map(GenericParameterType::new)
                .collect(Collectors.toList());

        GenericCallableSignature signature = new GenericCallableSignature(owner,
                name,
                parameters,
                returnType,
                genericParameterTypes,
                genericParameters);
        genericParameters.forEach(gp -> gp.setOwner(signature));
        return Pair.of(signature, ctx.callableBody());
    }

}
