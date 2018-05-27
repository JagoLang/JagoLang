package jago.parsing.visitor.statement;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.statement.VariableDeclarationStatement;
import jago.domain.scope.LocalScope;
import jago.domain.type.NullType;
import jago.domain.type.NullableType;
import jago.domain.type.Type;
import jago.exception.TypeMismatchException;
import jago.exception.VariableRedeclarationException;
import jago.parsing.visitor.expression.ExpressionVisitor;
import jago.util.TypeResolver;

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
        String explicitTypeString = ctx.type() == null? null: ctx.type().getText();

        Expression expression = ctx.expression()
                .accept(expressionVisitor)
                .used();

        boolean isMutable = ctx.variable_keyword().VARIABLE_IMMUTABLE() == null;

        if (explicitTypeString == null) {
            boolean added = scope.addLocalVariable(new LocalVariable(varName, expression.getType(), isMutable));

            if (added) {
                return new VariableDeclarationStatement(varName, expression);
            }
            throw new VariableRedeclarationException(varName);
        }

        Type explicitType = TypeResolver.getFromTypeContext(ctx.type(), scope.getImports());
        // TODO bypass the null craze if we have an immutable numeric variable
        if (expression.getType().equals(explicitType) || NullableType.isNullableOf(explicitType, expression.getType())) {
            boolean added = scope.addLocalVariable(new LocalVariable(varName, explicitType, isMutable));
            if (added) {
                return new VariableDeclarationStatement(varName, expression);
            }
            throw new VariableRedeclarationException(varName);
        }

        //TODO coalesce the types if we are going to add this feature
        throw new TypeMismatchException();

    }


}
