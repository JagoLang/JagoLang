package jago.bytecodegeneration.expression;

import jago.bytecodegeneration.intristics.*;
import jago.domain.node.expression.EmptyExpression;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.ValueExpression;
import jago.domain.node.expression.VariableReference;
import jago.domain.node.expression.call.ConstructorCall;
import jago.domain.node.expression.call.InstanceCall;
import jago.domain.node.expression.call.StaticCall;
import jago.domain.node.expression.initializer.ArrayInitializer;
import jago.domain.scope.LocalScope;
import jago.domain.type.CompositeType;
import jago.domain.type.NullType;
import jago.domain.type.PrimitiveArrayType;
import jago.domain.type.Type;
import jago.util.GeneratorResolver;
import jago.util.TypeResolver;
import org.apache.commons.lang3.NotImplementedException;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static jago.bytecodegeneration.intristics.ArrayIntrinsics.getNewArrayTypeCode;
import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("unused")
public class ExpressionGenerator {

    private static final Map<Class<?>, Method> CLASS_METHOD_MAP = GeneratorResolver
            .getAllGenerationMethods(ExpressionGenerator.class, Expression.class);

    private final MethodCallGenerator methodCallGenerator;
    private final MethodVisitor mv;
    private final LocalScope scope;


    public ExpressionGenerator(MethodVisitor methodVisitor, LocalScope scope) {
        this.scope = scope;
        this.mv = methodVisitor;
        methodCallGenerator = new MethodCallGenerator(mv, scope, this);
    }

    public void generate(Expression expression) {
        Method method = CLASS_METHOD_MAP.get(expression.getClass());
        if (method == null) {
            throw new NotImplementedException("generator not present for " + expression.getClass().getName());
        }
        try {
            method.invoke(this, expression);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        pop(expression);
    }

    public void pop(Expression expression) {
        if (!expression.isUsed()) {
            mv.visitInsn(JvmTypeSpecificInformation.of(expression.getType())
                    .getStackSize() == 2
                    ? POP2
                    : POP);
        }
    }

    public void generateConstructorCall(ConstructorCall call) {
        methodCallGenerator.generateMethodCall(call);
    }

    public void generateInstanceCall(InstanceCall call) {
        methodCallGenerator.generateMethodCall(call);
    }

    public void generateStaticCall(StaticCall call) {
        methodCallGenerator.generateMethodCall(call);
    }

    public void generateEmptyExpression(EmptyExpression emptyExpression) {
    }

    public void generateVariableReference(VariableReference variableReference) {
        mv.visitVarInsn(
                TypeOpcodesIntrinsics.getLoadOpcode(variableReference.getType()),
                LocalVariableIntrinsics.getVariableIndex(variableReference.getName(), scope)
        );
    }

    public void generateArrayInitializer(ArrayInitializer arrayInitializer) {
        CompositeType type = arrayInitializer.getType();
        visitIntLdc(arrayInitializer.getExpressionList().size());
        boolean isPrimitive = type instanceof PrimitiveArrayType;
        if (isPrimitive) {
            mv.visitIntInsn(NEWARRAY, getNewArrayTypeCode(((PrimitiveArrayType) type).getComponentType()));
        } else {
            mv.visitTypeInsn(ANEWARRAY, JvmNamingIntrinsics.getJVMInternalName(type));
        }
        List<Expression> expressionList = arrayInitializer.getExpressionList();
        for (int i = 0; i < expressionList.size(); i++) {
            Expression expression = expressionList.get(i);
            mv.visitInsn(DUP);
            visitIntLdc(i);
            generate(expression);
          int aStoreTypeCode = ArrayIntrinsics.getAStoreTypeCode(expression.getType());
            mv.visitInsn(aStoreTypeCode);
        }

    }


    public void generateValueExpression(ValueExpression valueExpression) {
        Type type = valueExpression.getType();
        String stringValue = valueExpression.getValue();
        Object value = TypeResolver.getValueFromString(stringValue, type);
        if (value instanceof Integer) {
            visitIntLdc(((Integer) value));
            return;
        }
        if (value instanceof Boolean) {
            mv.visitInsn(((Boolean) value) ? ICONST_1 : ICONST_0);
            return;
        }
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == 0.0) {
                mv.visitInsn(DCONST_0);
                return;
            }
            if (d == 1.0) {
                mv.visitInsn(DCONST_1);
                return;
            }
        } else if (type == NullType.INSTANCE) {
            mv.visitInsn(ACONST_NULL);
            return;
        }
        mv.visitLdcInsn(value);
    }

    private void visitIntLdc(int i) {
        switch (i) {
            case 0:
                mv.visitInsn(ICONST_0);
                return;
            case 1:
                mv.visitInsn(ICONST_1);
                return;
            case 2:
                mv.visitInsn(ICONST_2);
                return;
            case 3:
                mv.visitInsn(ICONST_3);
                return;
            case 4:
                mv.visitInsn(ICONST_4);
                return;
            case 5:
                mv.visitInsn(ICONST_5);
                return;
            case -1:
                mv.visitInsn(ICONST_M1);
                return;
        }
        if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, i);
            return;
        }
        if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, i);
            return;
        }
        mv.visitLdcInsn(i);
    }
}
