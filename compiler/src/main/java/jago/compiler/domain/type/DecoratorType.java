package jago.compiler.domain.type;

public interface DecoratorType extends Type {

    Type getInnerType();
}
