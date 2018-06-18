package jago.compiler.domain.type;

public interface CompositeType extends Type {
     Type getComponentType();
}
