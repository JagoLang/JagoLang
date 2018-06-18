package jago.compiler.util.graphs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableSet;

public class ConcurrentDirectionalGraph<T> implements DirectionalGraph<T> {

    private final Map<T, Set<T>> graph = new HashMap<>();

    /**
     * Adds a new node to the graph.
     * If the node already exists, this
     * function is a no-op.
     *
     * @param node The node to add.
     * @return Whether or not the node was added.
     */
    @Override
    public boolean addNode(T node) {
        // If the node already exists, don't do anything.
        if (graph.containsKey(node))
            return false;

        // Otherwise, add the node with an empty set of outgoing edges.
        graph.put(node, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        return true;
    }

    /**
     * <p>
     * Given a start node, and a destination, adds a directed edge from the start node
     * to the destination.  If an arc already exists, this operation is a
     * no-op.
     * </p>
     * If either endpoint does not exist in the graph, throws a
     * NoSuchElementException.
     *
     * @param start The start node.
     * @param dest  The destination node.
     * @throws NoSuchElementException If either the start or destination nodes
     *                                do not exist.
     */
    @Override
    public void addEdge(T start, T dest) {
        if (!graph.containsKey(start) || !graph.containsKey(dest))
            throw new NoSuchElementException("Both nodes must be in the graph.");
        graph.get(start).add(dest);
    }

    /**
     * Removes the edge from start to dest from the graph.  If the edge does
     * not exist, this operation is a no-op.  If either endpoint does not
     * exist, this throws a NoSuchElementException.
     *
     * @param start The start node.
     * @param dest  The destination node.
     * @throws NoSuchElementException If either node is not in the graph.
     */
    @Override
    public void removeEdge(T start, T dest) {
        // Confirm both endpoints exist.
        if (!graph.containsKey(start) || !graph.containsKey(dest))
            throw new NoSuchElementException("Both nodes must be in the graph.");

        graph.get(start).remove(dest);
    }

    /**
     * Given two nodes in the graph, returns whether there is an edge from the
     * first node to the second node.  If either node does not exist in the
     * graph, throws a NoSuchElementException.
     *
     * @param start The start node.
     * @param end   The destination node.
     * @return Whether there is an edge from start to end.
     * @throws NoSuchElementException If either endpoint does not exist.
     */
    @Override
    public boolean edgeExists(T start, T end) {
        /* Confirm both endpoints exist. */
        if (!graph.containsKey(start) || !graph.containsKey(end))
            throw new NoSuchElementException("Both nodes must be in the graph.");

        return graph.get(start).contains(end);
    }

    /**
     * Given a node in the graph, returns an immutable view of the edges
     * leaving that node as a set of endpoints.
     *
     * @param node The node whose edges should be queried.
     * @return An immutable view of the edges leaving that node.
     * @throws NoSuchElementException If the node does not exist.
     */
    @Override
    public Set<T> edgesFrom(T node) {
        /* Check that the node exists. */
        Set<T> arcs = graph.get(node);
        if (arcs == null)
            throw new NoSuchElementException("Source node does not exist.");

        return unmodifiableSet(arcs);
    }

    /**
     * Returns an iterator that can traverse the nodes in the graph.
     *
     * @return An iterator that traverses the nodes in the graph.
     */
    @Override
    public Iterator<T> iterator() {
        return graph.keySet().iterator();
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return The number of nodes in the graph.
     */
    @Override
    public int size() {
        return graph.size();
    }

    /**
     * Returns whether the graph is empty.
     *
     * @return Whether the graph is empty.
     */
    @Override
    public boolean isEmpty() {
        return graph.isEmpty();
    }

    @Override
    public void clear() {
        graph.clear();
    }
}
