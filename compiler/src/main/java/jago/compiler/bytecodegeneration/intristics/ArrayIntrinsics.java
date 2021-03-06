package jago.compiler.bytecodegeneration.intristics;

import jago.compiler.domain.type.ArrayType;
import jago.compiler.domain.type.CompositeType;
import jago.compiler.domain.type.NumericType;
import jago.compiler.domain.type.Type;

import java.util.EnumMap;

import static jago.compiler.domain.type.NumericType.*;
import static jago.compiler.domain.type.NumericType.DOUBLE;
import static jago.compiler.domain.type.NumericType.FLOAT;
import static jago.compiler.domain.type.NumericType.LONG;
import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("SuspiciousMethodCalls")
public class ArrayIntrinsics {

    private static final EnumMap<NumericType, Integer> NUMERIC_TYPE_NEW_ARRAY_MAP;
    private static final EnumMap<NumericType, Integer> NUMERIC_TYPE_ASTORE_MAP;
    private static final EnumMap<NumericType, Integer> NUMERIC_TYPE_ALOAD_MAP;

    static {
        NUMERIC_TYPE_NEW_ARRAY_MAP = new EnumMap<>(NumericType.class);
        NUMERIC_TYPE_NEW_ARRAY_MAP.put(BOOLEAN, T_BOOLEAN);
        NUMERIC_TYPE_NEW_ARRAY_MAP.put(BYTE, T_BYTE);
        NUMERIC_TYPE_NEW_ARRAY_MAP.put(CHAR, T_CHAR);
        NUMERIC_TYPE_NEW_ARRAY_MAP.put(DOUBLE, T_DOUBLE);
        NUMERIC_TYPE_NEW_ARRAY_MAP.put(FLOAT, T_FLOAT);
        NUMERIC_TYPE_NEW_ARRAY_MAP.put(INT, T_INT);
        NUMERIC_TYPE_NEW_ARRAY_MAP.put(LONG, T_LONG);
        NUMERIC_TYPE_NEW_ARRAY_MAP.put(SHORT, T_SHORT);
        NUMERIC_TYPE_ALOAD_MAP = new EnumMap<>(NumericType.class);
        NUMERIC_TYPE_ALOAD_MAP.put(BOOLEAN, BALOAD);
        NUMERIC_TYPE_ALOAD_MAP.put(BYTE, BALOAD);
        NUMERIC_TYPE_ALOAD_MAP.put(CHAR, CALOAD);
        NUMERIC_TYPE_ALOAD_MAP.put(DOUBLE, DALOAD);
        NUMERIC_TYPE_ALOAD_MAP.put(FLOAT, FALOAD);
        NUMERIC_TYPE_ALOAD_MAP.put(INT, IALOAD);
        NUMERIC_TYPE_ALOAD_MAP.put(LONG, LALOAD);
        NUMERIC_TYPE_ALOAD_MAP.put(SHORT, SALOAD);
        NUMERIC_TYPE_ASTORE_MAP = new EnumMap<>(NumericType.class);
        NUMERIC_TYPE_ASTORE_MAP.put(BOOLEAN, BASTORE);
        NUMERIC_TYPE_ASTORE_MAP.put(BYTE, BASTORE);
        NUMERIC_TYPE_ASTORE_MAP.put(CHAR, CASTORE);
        NUMERIC_TYPE_ASTORE_MAP.put(DOUBLE, DASTORE);
        NUMERIC_TYPE_ASTORE_MAP.put(FLOAT, FASTORE);
        NUMERIC_TYPE_ASTORE_MAP.put(INT, IASTORE);
        NUMERIC_TYPE_ASTORE_MAP.put(LONG, LASTORE);
        NUMERIC_TYPE_ASTORE_MAP.put(SHORT, SASTORE);
    }

    public static int getNewArrayTypeCode(NumericType numericType) {
        return NUMERIC_TYPE_NEW_ARRAY_MAP.get(numericType);
    }

    public static int getAStoreTypeCode(CompositeType type) {
        if (type instanceof ArrayType) {
            return AASTORE;
        }
        return NUMERIC_TYPE_ASTORE_MAP.get(type.getComponentType());
    }

    public static int getALoadTypeCode(CompositeType type) {
        if (type instanceof ArrayType) {
            return AASTORE;
        }
        return NUMERIC_TYPE_ALOAD_MAP.get(type.getComponentType());
    }
}
