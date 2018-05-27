package jago.util.graphs;

import java.util.Set;

public interface DirectionalGraph<T> extends Iterable<T> {

    boolean addNode(T node);

    void addEdge(T start, T dest);

    void removeEdge(T start, T dest);

    boolean edgeExists(T start, T end);

    Set<T> edgesFrom(T node);

    int size();

    boolean isEmpty();

    void clear();
}
