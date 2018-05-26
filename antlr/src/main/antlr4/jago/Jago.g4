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

field : type name;
callable : callableDeclaration callableBody;
callableBody:  (block| '=>' expression);
callableDeclaration : {getCurrentToken().getText().equals("util")}? ID  callableName '('? parametersList? ')'? (':' type)?;
parametersList:  parameter (',' parameter)*
          |  parameter (',' parameterWithDefaultValue)*
          |  parameterWithDefaultValue (',' parameterWithDefaultValue)* ;
callableName : id ;
parameter : type ID ;
parameterWithDefaultValue : type ID '=' defaultValue=expression ;
type : primitiveType
     | classType ;

primitiveType :  'boolean' ('[' ']')*
                |   'string' ('[' ']')*
                |   'char' ('[' ']')*
                |   'byte' ('[' ']')*
                |   'short' ('[' ']')*
                |   'int' ('[' ']')*
                |   'long' ('[' ']')*
                |   'float' ('[' ']')*
                |  'double' ('[' ']')*
                | 'void' ('[' ']')* ;

classType : qualifiedName ('[' ']')* ;

block : '{' statement* '}' ;

statement : (block
           | variableDeclaration
           | assignment
           | logStatement
           | forStatement
           | returnStatement
           | ifStatement
           | expression) ';'?;

variableDeclaration : variable_keyword name (':' qualifiedName nullable='?'?)? EQUALS expression;
assignment : name EQUALS expression;
logStatement : LOG expression ;
returnStatement : 'return' expression #ReturnWithValue
                | 'return' #ReturnVoid ;
ifStatement :  'if'  ('(')? expression (')')? trueStatement=statement ('else' falseStatement=statement)?;
forStatement : 'for' ('(')? forConditions (')')? statement ;
forConditions : iterator=variableReference  'from' startExpr=expression range='to' endExpr=expression ;
name : ID ;

argumentList : argument? (',' a=argument)* #UnnamedArgumentsList
             | namedArgument? (',' namedArgument)* #NamedArgumentsList ;
argument : expression ;
namedArgument : name '->' expression ;
expression:
             '(' expression ')' #ParenExpr
           |<assoc=right>  owner=expression '.' callableName '(' argumentList ')' #MethodCall
           |<assoc=right>  (qualifiedName '.')? callableName '(' argumentList ')' #MethodCall
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
           ;


variableReference : id;
qualifiedName : id ('.'id)*;

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