package jago.domain.type;

public interface CompositeType extends Type {
     Type getComponentType();
}
