package jago.util;

import jago.JagoParser;
import jago.bytecodegeneration.intristics.JvmNumericEquivalent;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.generic.GenericParameter;
import jago.domain.imports.Import;
import jago.domain.scope.CompilationUnitScope;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import jago.domain.type.generic.GenericType;
import jago.exception.IllegalReferenceException;
import jago.exception.TypeMismatchException;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.reflect.TypeVariable;
import java.util.*;

import static java.util.stream.Collectors.toList;

public final class TypeResolver {

    public static Type getFromTypeContext(JagoParser.TypeContext typeContext, List<Import> imports) {

        if (typeContext == null) return null;

        Type type = getFromTypeNameOrThrow(typeContext.start.getText(), imports);
        if (typeContext.genericArguments() != null) {
            List<Type> genericArguments = ParserUtils.parseGenericArguments(typeContext.genericArguments(), imports);
            if (type instanceof ArrayType) {
                if (genericArguments.size() != 1) {
                    throw new TypeMismatchException();
                }
                return new ArrayType(genericArguments.get(0));
            }
            //TODO perform a local generic check

            Class<?> clazz = SignatureResolver.getClassFromType(type);
            TypeVariable<? extends Class<?>>[] typeParameters = Objects.requireNonNull(clazz).getTypeParameters();

            if (genericArguments.size() != typeParameters.length) {
                throw new TypeMismatchException();
            }

            //TODO Check constrains
            List<GenericParameter> genericParameters = Arrays.stream(clazz.getTypeParameters()).map(tp -> new GenericParameter(tp.getName(), 0, AnyType.INSTANCE)).collect(toList());
            type = new GenericType(type, genericArguments, genericParameters);
        }
        if (typeContext.nullable != null) type = NullableType.of(type);
        return type;
    }

    public static Optional<Type> getFromTypeName(String typeName, LocalScope scope) {
        return getFromTypeName(typeName, scope.getCompilationUnitScope().getImports());
    }

    public static Type getFromTypeNameOrThrow(String typeName, LocalScope scope) {
        return getFromTypeNameOrThrow(typeName, scope.getCompilationUnitScope().getImports());
    }

    public static Type getFromTypeNameOrThrow(String typeName, List<Import> imports) {
        return getFromTypeName(typeName, imports).orElseThrow(() -> new IllegalReferenceException("type " + typeName + " not found"));
    }

    //TODO: this method should be primary
    public static Optional<Type> getFromTypeName(String typeName, List<Import> imports) {
        Optional<Type> numericType = NumericType.getNumericType(typeName);
        if (numericType.isPresent()) return numericType;
        if (typeName.equals("String")) {
            return Optional.of(StringType.INSTANCE);
        }
        if (typeName.equals("Any")) {
            return Optional.of(AnyType.INSTANCE);
        }
        if (typeName.equals("Unit")) {
            return Optional.of(UnitType.INSTANCE);
        }
        if(typeName.equals("Array")) {
            return Optional.of(ArrayType.UNBOUND);
        }
        if (NumericType.ARRAY_NAMES.contains(typeName)) {
            return Optional.of(new PrimitiveArrayType(typeName));
        }

        //TODO remove soon, once arrays are added
        Optional<Type> builtInType = getBuiltInType(typeName);
        if (builtInType.isPresent()) return builtInType;

        Type t = fromImport(typeName, imports);
        if (t != null) return Optional.of(t);

        if (checkClassNameForValidity(typeName)) {
            return Optional.of(new ClassType(typeName));
        }
        return Optional.empty();
    }

    private static Type fromImport(String typeName, List<Import> imports) {
        for (Import i : imports) {
            // regular class import
            if (!i.isPackageImport() && typeName.equals(i.getImportedClass())) {
                String fullName = i.getFromPackage() + "." + typeName;
                if (checkClassNameForValidity(fullName)) return new ClassType(fullName);
            } else {
                // package import
                String tryingToImport = i.getFromPackage() + "." + typeName;
                if (checkClassNameForValidity(tryingToImport)) {
                    return new ClassType(tryingToImport);
                }
            }
        }
        return null;
    }

    public static Type getFromClass(Class<?> clazz) {
        if (clazz == void.class) {
            return UnitType.INSTANCE;
        }
        if (clazz.isPrimitive()) {
            return NumericType.valueOf(clazz.getName().toUpperCase());
        }
        Optional<NumericType> numericType = JvmNumericEquivalent.fromInternalName(clazz.getName().replace('.', '/'));
        if (numericType.isPresent()) {
            return NullableType.of(numericType.get());
        }
        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            if (componentType.isPrimitive()) {
                return new PrimitiveArrayType(NumericType.valueOf(componentType.getName().toUpperCase()));
            }
            return new ArrayType(nullify(getFromClass(componentType)));
        }
        if (clazz == String.class) {
            return StringType.INSTANCE;
        }
        if (clazz == Object.class) {
            return AnyType.INSTANCE;
        }
        return new ClassType(clazz.getName());
    }
    public static Type nullify(Type fromJavaType) {
        if (fromJavaType instanceof NumericType) return fromJavaType;
        if (fromJavaType instanceof DecoratorType
                && ((DecoratorType) fromJavaType).getInnerType() instanceof NumericType) {
            return fromJavaType;
        }
        //TODO: it might not be null tolerable
        return NullTolerableType.of(fromJavaType);
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
            ClassUtils.getClass(fullName, false);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }

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
            return StringEscapeUtils.unescapeJava(stringValue.substring(1, stringValue.length() - 1)).charAt(0);
        }
        if (type == StringType.INSTANCE) {
            return StringEscapeUtils.unescapeJava(stringValue.substring(1, stringValue.length() - 1));
        }
        if (type == NullType.INSTANCE) {
            return null;
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
