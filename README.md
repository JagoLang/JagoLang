# This is JagoLang main repository
## This currently includes just the compiler

### _Current features_
* Multi-source compilation
* Maven Plugin 
* Return type inference (<small>please report if this fails in some odd way </small>)
* Array Initializer syntax
* Basic generics with type checking and proper erasure
* Non-nullable by default
* Instance method calls
* Static method calls
* function declarations
* Expression body functions
* Constructors
* Simple arithmetic
* Varags (<small>might be slightly bugged still</small>)
* kotlin styled named arguments
* Return statements
* Type inference
* Local variable(<small>mutable and immutable</small>)
* Imports

### _Next on the list_
* Casts
* If else statements/expressions
* Data flow and control flow analysis
* Loops
* Classes
* mvn package (someone is just too lazy figure out how to work with the filesystem and mvn subpackages)
* Inheritance
* Interfaces
* stdlib (basic runtime and compilation support)
* Annotations
* Operator override
* Metadata
* Closures
* Array constructors
* Discriminated types, Monad, railway pattern

### Installation guide:
First clone the repository
___
Then maven install from IDEA toolbar tab or using `mvn install` in root folder.
___
Compile by running `mvn compile` on JagoProject module.
___
You can play around and change source code in the JagoProject yourself. 
___
Compiled .class files will be in `target/classes` folder in same folder. 


## Developer list:
* Denis - https://github.com/hunter04d
* Oleg - https://github.com/0lejk4