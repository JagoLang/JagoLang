package jago.util;

import jago.JagoParser;
import jago.bytecodegeneration.intristics.JVMNullableNumericEquivalent;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.imports.Import;
import jago.domain.scope.CompilationUnitScope;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import jago.exception.IllegalReferenceException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TypeResolver {

    public static Type getFromTypeContext(JagoParser.TypeContext typeContext, List<Import> imports) {

        if (typeContext == null) return null;

        Type type = getFromTypeNameOrThrow(typeContext.start.getText(), imports);
        if (typeContext.nullable != null) type = NullableType.of(type);
        return type;
    }

    public static Optional<Type> getFromTypeName(String typeName, LocalScope scope) {
        return getFromTypeName(typeName, scope.getCompilationUnitScope().getImports());
    }

    public static Type getFromTypeNameOrThrow(String typeName, LocalScope scope) {
        return getFromTypeNameOrThrow(typeName, scope.getCompilationUnitScope().getImports());
    }

    public static Optional<Type> getFromTypeNameBypassImportCheck(String typeName, LocalScope scope) {
        // TODO check for the names of compilation units that don't contains a top level class (when we are going to implement classes)
        try {
            if (Class.forName(typeName) != null) {
                return Optional.of(new ClassType(typeName));
            }
        } catch (ClassNotFoundException ignored) {
        }
        return Optional.empty();
    }

    public static Type getFromTypeNameOrThrow(String typeName, List<Import> imports) {
        return getFromTypeName(typeName, imports).orElseThrow(() -> new IllegalReferenceException("type " + typeName + " not found"));
    }

    //TODO: this method should be primary
    public static Optional<Type> getFromTypeName(String typeName, List<Import> imports) {
        Optional<Type> numericType = NumericType.getNumericType(typeName);
        if (numericType.isPresent()) return numericType;
        if (NumericType.ARRAY_NAMES.contains(typeName)) {
            return Optional.of(new PrimitiveArrayType(typeName));
        }
        //TODO remove soon, once arrays are added
        Optional<Type> builtInType = getBuiltInType(typeName);
        if (builtInType.isPresent()) return builtInType;

        for (Import i : imports) {
            // regular class import
            if (!i.isPackageImport() && typeName.equals(i.getImportedClass())) {
                String fullName = i.getFromPackage() + "." + typeName;
                boolean valid = checkClassNameForValidity(fullName);
                if (valid) return Optional.of(new ClassType(fullName));
            } else {
                // package import
                String tryingToImport = i.getFromPackage() + "." + typeName;
                if (checkClassNameForValidity(tryingToImport)) {
                    return Optional.of(new ClassType(tryingToImport));
                }
            }

        }
        if (checkClassNameForValidity(typeName)) {
            return Optional.of(new ClassType(typeName));
        }
        return Optional.empty();
    }

    public static Type getFromClass(Class<?> clazz) {
        if (clazz == void.class) {
            return UnitType.INSTANCE;
        }
        if (clazz.isPrimitive()) {
            return NumericType.valueOf(clazz.getComponentType().getName().toUpperCase());
        }
        Optional<NumericType> numericType = JVMNullableNumericEquivalent.fromInternalName(clazz.getName().replace('.', '/'));
        if (numericType.isPresent()) {
            return NullableType.of(numericType.get());
        }
        if (clazz.isArray()) {
            if (clazz.getComponentType().isPrimitive()) {
                return new PrimitiveArrayType(NumericType.valueOf(clazz.getComponentType().getName().toUpperCase()));
            }
            return new ArrayType(getFromClass(clazz.getComponentType()));
        }
        if (clazz == String.class) {
            return StringType.INSTANCE;
        }
        if (clazz == Object.class) {
            return AnyType.INSTANCE;
        }
        return new ClassType(clazz.getName());
    }

    private static boolean checkClassNameForValidity(String fullName) {
        String internal = fullName.replace('.', '/');
        CompilationUnitScope compilationUnitScope = CompilationMetadataStorage.compilationUnitScopes
                .entrySet()
                .stream()
                .filter(e -> e.getKey().endsWith(internal))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);

        if (compilationUnitScope != null) {
            return true;
        }
        try {
            if (Class.forName(fullName) != null)
                return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }


    public static Object getValueFromString(String stringValue, Type type) {
        if (type == NumericType.INT) {
            return Integer.valueOf(stringValue);
        }
        if (type == NumericType.LONG) {
            return Long.valueOf(stringValue);
        }
        if (type == NumericType.DOUBLE) {
            return Double.valueOf(stringValue);
        }
        if (type == NumericType.FLOAT) {
            return Float.valueOf(stringValue);
        }
        if (type == NumericType.BOOLEAN) {
            return Boolean.valueOf(stringValue);
        }
        if (type == NumericType.CHAR) {
            return stringValue.charAt(1);
        }
        if (type == StringType.INSTANCE) {
            stringValue = StringUtils.removeStart(stringValue, "\"");
            stringValue = StringUtils.removeEnd(stringValue, "\"");
            return stringValue;
        }
        if (type == NullType.INSTANCE) {
            return "null";
        }
        throw new NotImplementedException("Objects not yet implemented!");
    }

    @Deprecated
    private static Optional<Type> getBuiltInType(String typeName) {
        return Arrays.stream(BuiltInType.values())
                .filter(type -> type.getName().equals(typeName))
                .findFirst().map(builtInType -> builtInType);
    }
}
