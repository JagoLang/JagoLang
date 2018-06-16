package jago.util;

import jago.JagoParser;
import jago.bytecodegeneration.intristics.JvmNumericEquivalent;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.generic.GenericParameter;
import jago.domain.imports.Import;
import jago.domain.scope.CompilationUnitScope;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import jago.domain.type.generic.GenericParameterType;
import jago.domain.type.generic.GenericType;
import jago.exception.IllegalReferenceException;
import jago.exception.TypeMismatchException;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public final class TypeResolver {

    public static Type getFromTypeContext(JagoParser.TypeContext typeContext,
                                          List<Import> imports,
                                          List<GenericParameter> genericParametersCtx) {
        if (typeContext == null) return null;

        for (GenericParameter genericParameter : genericParametersCtx) {
            if (genericParameter.getName().equals(typeContext.qualifiedName().getText())) {
                return new GenericParameterType(genericParameter);
            }
        }
        Type type = getFromTypeNameOrThrow(typeContext.qualifiedName().getText(), imports);
        if (typeContext.genericArguments() != null) {

            List<Type> genericArguments = ParserUtils.parseGenericArguments(typeContext.genericArguments(), imports, genericParametersCtx);
            if (type instanceof ArrayType) {
                if (genericArguments.size() != 1) {
                    throw new TypeMismatchException();
                }
                return new ArrayType(genericArguments.get(0));
            }

            Class<?> clazz = SignatureResolver.getClassFromType(type);

            TypeVariable<? extends Class<?>>[] typeParameters = Objects.requireNonNull(clazz).getTypeParameters();
            if (genericArguments.size() != typeParameters.length) {
                throw new TypeMismatchException();
            }
            //TODO Check constrains

            List<GenericParameter> genericParameters = Arrays.stream(clazz.getTypeParameters()).map(tp -> new GenericParameter(tp.getName(), 0, NullableType.of(AnyType.INSTANCE))).collect(toList());
            final GenericType genericType = new GenericType(type, genericArguments, genericParameters);
            genericParameters.forEach(gp -> gp.setOwner(genericType));
            type = genericType;
        }
        return typeContext.nullable != null ? NullableType.of(type) : type;
    }

    public static Type getFromTypeContext(JagoParser.TypeContext typeContext, List<Import> imports) {
        return getFromTypeContext(typeContext, imports, Collections.emptyList());
    }

    public static Type getFromTypeContext(JagoParser.TypeContext typeContext, LocalScope scope) {
        return getFromTypeContext(typeContext, scope.getImports(), scope.getAllGenericParameters());
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
        if (typeName.equals("Array")) {
            return Optional.of(ArrayType.UNBOUND);
        }
        if (NumericType.ARRAY_NAMES.contains(typeName)) {
            return Optional.of(new PrimitiveArrayType(typeName));
        }

        Optional<Type> t = fromImport(typeName, imports);
        if (t.isPresent()) return t;

        return validateClassName(typeName);
    }

    private static Optional<Type> fromImport(String typeName, List<Import> imports) {
        for (Import i : imports) {
            // regular class import
            if (!i.isPackageImport() && typeName.equals(i.getImportedClass())) {
                String fullName = i.getFromPackage() + "." + typeName;
                Optional<Type> type = validateClassName(fullName);
                if (type.isPresent()) {
                    return type;
                }
            } else {
                // package import
                String tryingToImport = i.getFromPackage() + "." + typeName;
                Optional<Type> type = validateClassName(tryingToImport);
                if (type.isPresent()) {
                    return type;
                }
            }
        }
        return Optional.empty();
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
        if (fromJavaType instanceof GenericParameterType) {
            return fromJavaType;
        }
        //TODO: it might not be null tolerable
        return NullTolerableType.of(fromJavaType);
    }

    private static Optional<Type> validateClassName(String fullName) {
        String internal = fullName.replace('.', '/');
        CompilationUnitScope compilationUnitScope = CompilationMetadataStorage.compilationUnitScopes
                .entrySet()
                .stream()
                .filter(e -> e.getKey().endsWith(internal))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);

        if (compilationUnitScope != null) {
            // what kind of scope is this?
            return Optional.of(new NonInstantiatableType(fullName));
        }
        try {
            ClassUtils.getClass(fullName, false);
            return Optional.of(new ClassType(fullName));
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
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
}
