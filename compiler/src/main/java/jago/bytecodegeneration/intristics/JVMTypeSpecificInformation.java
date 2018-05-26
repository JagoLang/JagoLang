package jago.bytecodegeneration.intristics;

import jago.domain.type.NumericType;
import jago.domain.type.Type;

import static org.objectweb.asm.Opcodes.*;


public enum JVMTypeSpecificInformation {

    INT(ILOAD, ISTORE, IRETURN, IADD, ISUB, IMUL, IDIV, IREM),
    LONG(LLOAD, LSTORE, LRETURN, LADD, LSUB, LMUL, LDIV, LREM, 2),
    FLOAT(FLOAD, FSTORE, FRETURN, FADD, FSUB, FMUL, FDIV, FREM),
    DOUBLE(DLOAD, DSTORE, DRETURN, DADD, DSUB, DMUL, DDIV, DREM, 2),
    VOID(ALOAD, ASTORE, RETURN, 0, 0, 0, 0, 0),
    OBJECT(ALOAD, ASTORE, ARETURN, 0, 0, 0, 0, 0);

    private final int load;
    private final int store;
    private final int ret;
    private final int add;
    private final int sub;
    private final int mul;
    private final int div;
    private final int rem;
    private final int stackSize;


    JVMTypeSpecificInformation(int load, int store, int ret, int add, int sub, int mul, int div, int rem) {
        this.load = load;
        this.store = store;
        this.ret = ret;
        this.add = add;
        this.sub = sub;
        this.mul = mul;
        this.div = div;
        this.rem = rem;
        stackSize = 1;
    }
    JVMTypeSpecificInformation(int load, int store, int ret, int add, int sub, int mul, int div, int rem, int stackSize) {
        this.load = load;
        this.store = store;
        this.ret = ret;
        this.add = add;
        this.sub = sub;
        this.mul = mul;
        this.div = div;
        this.rem = rem;
        this.stackSize = stackSize;
    }
    public static JVMTypeSpecificInformation of(Type type) {
        if (type instanceof NumericType) {
            switch ((NumericType) type) {
                case LONG:
                    return LONG;
                case FLOAT:
                    return FLOAT;
                case DOUBLE:
                    return DOUBLE;
                default:
                    return INT;
            }
        }
        return OBJECT;
    }

    public int getLoad() {
        return load;
    }

    public int getStore() {
        return store;
    }

    public int getReturn() {
        return ret;
    }

    public int getAdd() {
        return add;
    }

    public int getSubstract() {
        return sub;
    }

    public int getMultiply() {
        return mul;
    }

    public int getDivide() {
        return div;
    }

    public int getRem() {
        return rem;
    }

    public int getStackSize() {
        return stackSize;
    }
}
