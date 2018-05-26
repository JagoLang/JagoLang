package jago.util;

import jago.JagoParser;
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

public class TypeResolver {

    public static Type getFromTypeContext(JagoParser.TypeContext typeContext, List<Import> imports) {
        if (typeContext == null) return null;
        return getFromTypeNameOrThrow(typeContext.getText(), imports);
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
        return getFromTypeName(typeName, imports).orElseThrow(() -> new IllegalReferenceException(typeName + "not found"));
    }
    //TODO: this method should be primary
    public static Optional<Type> getFromTypeName(String typeName, List<Import> imports) {
        if (typeName.equals("java.lang.String")) return Optional.of(ClassType.STRING);
        Optional<Type> numericType = NumericType.getNumericType(typeName);
        if (numericType.isPresent()) return numericType;
        //TODO remove once arrays are added
        Optional<Type> builtInType = getBuiltInType(typeName);
        if (builtInType.isPresent()) return builtInType;

        for (Import i : imports) {
            if (!i.isPackageImport() && typeName.equals(i.getImportedClass())) {
                String fullName = i.getFromPackage() + "." + typeName;
                boolean valid = checkClassNameForValidity(fullName);
                if (valid) return Optional.of(new ClassType(fullName));
            } else {
                //TODO package import
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

    private static Optional<Type> getBuiltInType(String typeName) {
        return Arrays.stream(BuiltInType.values())
                .filter(type -> type.getName().equals(typeName))
                .findFirst().map(builtInType -> builtInType);
    }
}
