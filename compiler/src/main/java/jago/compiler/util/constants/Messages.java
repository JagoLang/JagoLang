package jago.compiler.util.constants;

public interface Messages {
    String PRIMITIVE_RETURN_ERROR = "Type mismatch in return statement with primitives expected %s, got %s";
    String OBJECT_RETURN_ERROR = "Type mismatch in return statement: expected %s or derived, got: %s";
    String VARIABLE_NOT_DECLARED = "Var %s not declared";
    String VARIABLE_REDECLARATION = "Variable with name = %s exists";
    String VARIABLE_IS_NOT_MUTABLE = "Variable %s is not mutable";
    String ASSIGNMENT_TYPE_MISMATCH = "Type mismatch";
    String METHOD_DONT_EXIST = "Method %s with arguments %s does not exists";
    String CALL_ARGUMENTS_MISMATCH = "Arguments does not match parameters for method call %s, REPORT IMMEDIATELY";
    String ILLEGAL_PARAMATER = "Parameter %s does not exists, the only legal parameters are %s";
    String SELF_METHOD_CALL = "Method cant be reference since it is instance method %s";
    String CLASS_DOES_NOT_EXIST = "Class for name = %s doesn't exist";
    String RETURN_TYPE_INFERENCE_FAILED = "Return type inference failed for the function %s";
    String RECURSIVE_RETURN_TYPE_INFERENCE = "%s is recursively defined, unable to resolve return type";
}
