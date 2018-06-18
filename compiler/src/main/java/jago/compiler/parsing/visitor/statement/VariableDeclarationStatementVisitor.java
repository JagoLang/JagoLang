package jago.compiler.parsing.visitor.statement;

import jago.antlr.JagoBaseVisitor;
import jago.antlr.JagoParser;
import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.node.expression.LocalVariable;
import jago.compiler.domain.node.expression.call.Call;
import jago.compiler.domain.node.statement.VariableDeclarationStatement;
import jago.compiler.domain.scope.LocalScope;
import jago.compiler.domain.type.NullType;
import jago.compiler.domain.type.NullableType;
import jago.compiler.domain.type.Type;
import jago.compiler.domain.type.generic.BindableType;
import jago.compiler.domain.type.generic.GenericParameterType;
import jago.compiler.domain.type.generic.GenericType;
import jago.compiler.exception.TypeMismatchException;
import jago.compiler.exception.VariableRedeclarationException;
import jago.compiler.parsing.visitor.expression.ExpressionVisitor;
import jago.compiler.util.TypeResolver;

public class VariableDeclarationStatementVisitor extends JagoBaseVisitor<VariableDeclarationStatement> {


    private final ExpressionVisitor expressionVisitor;
    private final LocalScope scope;

    public VariableDeclarationStatementVisitor(ExpressionVisitor expressionVisitor,
                                               LocalScope scope) {
        this.expressionVisitor = expressionVisitor;
        this.scope = scope;
    }

    @Override
    public VariableDeclarationStatement visitVariableDeclaration(JagoParser.VariableDeclarationContext ctx) {

        String varName = ctx.id().getText();
        String explicitTypeString = ctx.type() == null ? null : ctx.type().getText();

        Expression expression = ctx.expression()
                .accept(expressionVisitor)
                .used();

        boolean isMutable = ctx.variable_keyword().VARIABLE_IMMUTABLE() == null;

        Type expressionType = expression.getType();
        if (explicitTypeString == null) {
            if (expressionType == NullType.INSTANCE) {
                throw new TypeMismatchException();
            }
            if (expressionType instanceof BindableType && ((BindableType) expressionType).isUnbound()) {
                throw new TypeMismatchException();
            }
            return saveAddLocalVariable(new LocalVariable(varName, expressionType, isMutable),expression);
        }

        // explicit type handling
        Type explicitType = TypeResolver.getFromTypeContext(ctx.type(), scope);

        // TODO bypass the null craze if we have an immutable numeric variable
        if (expressionType.equals(explicitType) || NullableType.isNullableOf(explicitType, expressionType)) {
            return saveAddLocalVariable(new LocalVariable(varName, explicitType, isMutable), expression);
        }
        if (expressionType instanceof BindableType
                && ((BindableType) expressionType).isUnbound()
                && expression instanceof Call) {
            Call call = (Call) expression;
            if (expressionType instanceof GenericParameterType) {
                call.lateBindType(explicitType);
                return saveAddLocalVariable(new LocalVariable(varName, explicitType, isMutable), expression);
            }
            if (expressionType instanceof GenericType && explicitType instanceof GenericType) {
                GenericType explicitTypeG = (GenericType) explicitType;
                GenericType expressionTypeG = (GenericType) expressionType;
                if (explicitTypeG.getType().equals(expressionTypeG.getType())) {
                    GenericType bind = expressionTypeG.bind(explicitTypeG.getGenericArguments());
                    call.lateBindType(bind);
                    return saveAddLocalVariable(new LocalVariable(varName, explicitType, isMutable), expression);
                }
            }
        }

        //TODO coalesce the types
        throw new TypeMismatchException();

    }

    private VariableDeclarationStatement saveAddLocalVariable(LocalVariable localVariable, Expression expression) {
        if (scope.addLocalVariable(localVariable)) {
            return new VariableDeclarationStatement(localVariable.getName(), expression);
        }
        throw new VariableRedeclarationException(localVariable.getName());
    }


}
