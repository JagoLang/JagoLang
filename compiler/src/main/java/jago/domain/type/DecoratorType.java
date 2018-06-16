package jago.domain.type;

public interface DecoratorType extends Type {

    Type getInnerType();
}
