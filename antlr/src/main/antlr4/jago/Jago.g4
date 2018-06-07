grammar Jago;

compilationUnit :
 ('package' packageName)?
 imports
 (callable|classDeclaration)*
 EOF ;

imports: (importStatement)*;
importStatement : 'import' importName ;
importName: (qualifiedName ('.' fromClass=(ID | '*'))) | fromClass=ID;
packageName : qualifiedName;
classDeclaration : {getCurrentToken().getText().equals("data")}? ID  className '{' classBody '}';
className :  id ;
classBody :  field*;

field : type id;
callable : callableDeclaration callableBody;
callableBody:  (block| '=>' expression);
callableDeclaration : {getCurrentToken().getText().equals("util")}? ID  callableName parametersList? (':' type)?;
parametersList:  '(' parameter? (',' parameter)* ('vararg' varargParameter=parameter)?')';
callableName : id ;

parameter : id ':' type (EQUALS expression)?;

type : (primitiveType | classType) nullable='?'? ;

primitiveType :  'boolean' ('[' ']')*
                | 'string' ('[' ']')*
                | 'char' ('[' ']')*
                | 'byte' ('[' ']')*
                | 'short' ('[' ']')*
                | 'int' ('[' ']')*
                | 'long' ('[' ']')*
                | 'float' ('[' ']')*
                | 'double' ('[' ']')*
                | 'void' ('[' ']')* ;

classType : qualifiedName;

block : '{' statement* '}' ;

statement : (block
           | variableDeclaration
           | assignment
           | indexerAssignment
           | logStatement
           | forStatement
           | returnStatement
           | ifStatement
           | expression) ';'?;

variableDeclaration : variable_keyword id (':' type)? EQUALS expression;
assignment : (qualifiedName '.')? id EQUALS expression;
indexerAssignment: (qualifiedName '.')? id '['  argument (',' argument)* ']' EQUALS expression;
logStatement : LOG expression ;
returnStatement : 'return' expression #ReturnWithValue
                | 'return' #ReturnVoid ;
ifStatement :  'if'  ('(')? expression (')')? trueStatement=statement ('else' falseStatement=statement)?;
forStatement : 'for' ('(')? forConditions (')')? statement ;
forConditions : iterator=variableReference  'from' startExpr=expression range='to' endExpr=expression ;

argumentList : argument? (',' argument)*
             | argument?  (',' argument)* (',' namedArgument)*
             | namedArgument? (',' namedArgument)*;
argument : expression ;
namedArgument : id EQUALS expression ;
expression:
             '(' expression ')' #ParenExpr
           | expression '[' argument? (',' argument)* ']' #IndexerCall
           |<assoc=right>  owner=expression '.' callableName genericArguments? '(' argumentList ')' #MethodCall
           |<assoc=right>  (qualifiedName '.')? callableName genericArguments? '(' argumentList ')' #MethodCall
           |<assoc=right>  qualifiedName '.' id #FieldReference
           | superCall='super' '('argumentList ')' #Supercall
           | variableReference #VarReference
           | value        #ValueExpr
           | ('-' expression) #Negation
           |<assoc=right> expression '**' expression #Power
           | expression op=('*'|'/'|'%') expression  #Multiplicative
           | expression op=('+'|'-') expression #Additive
           | expression cmp='>' expression #ConditionalExpression
           | expression cmp='<' expression #ConditionalExpression
           | expression cmp='==' expression #ConditionalExpression
           | expression cmp='!=' expression #ConditionalExpression
           | expression cmp='>=' expression #ConditionalExpression
           | expression cmp='<=' expression #ConditionalExpression
           | '|' expression (',' expression)* '|' #ArrayInitializer
           ;

// Unused right now, when  proper generics are added this will be useful
genericParameters: '<' (id (',' id)* )'>';
genericArguments: '<' genericArgument  (',' genericArgument?)* '>';
genericArgument: type genericArguments?;
variableReference : id;
qualifiedName : id ('.' id)*;

value : number
      | BOOL
      | STRING
      | CHAR
      | NULL;

variable_keyword: (VARIABLE_MUTABLE|VARIABLE_IMMUTABLE);
number: '-'? NUMBER  (NUMBER_SUFFIX)?;
id: ID|VARIABLE_MUTABLE|VARIABLE_IMMUTABLE;
NUMBER: DIGIT_FRAGMET+ ('.' DIGIT_FRAGMET+)?;
//TOKENS

LOG : 'log' ;
EQUALS : '=' ;
BOOL : 'true' | 'false' ;
NULL: 'null';
CHAR: '\'' ~('\r' | '\n' | '"')? '\'';
STRING : '"' ~('\r' | '\n' | '"')* '"' ;
VARIABLE_MUTABLE : 'mutable';
VARIABLE_IMMUTABLE: 'let';
ID : ID_FRAGMENT;
NUMBER_SUFFIX : ('L'|'l'|'f'|'F');
fragment DIGIT_FRAGMET : [0-9] ;
fragment ID_FRAGMENT: [a-zA-Z_] [a-zA-Z0-9_]*;
WS: [ \t\n\r]+ -> skip ;
ErrorToken: . ;