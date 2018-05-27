package jago.util;

import jago.util.graphs.ConcurrentDirectionalGraph;
import jago.util.graphs.DirectionalGraph;

import java.util.HashSet;
import java.util.Set;

public final class GraphUtil {

    public static <T> void findCycle(DirectionalGraph<T> g, T node) {
        Set<T> visited = new HashSet<>();
        Set<T> expanded = new HashSet<>();
        exploreDFS(node, g,  visited, expanded);
    }


    /**
     * Recursively performs a DFS from the specified node, marking all nodes
     * encountered by the search.
     *
     * @param node     The node to begin the search from.
     * @param g        The graph in which to perform the search.
     * @param visited  A set of nodes that have already been visited.
     * @param expanded A set of nodes that have been fully expanded.
     */
    private static <T> void exploreDFS(T node, DirectionalGraph<T> g,
                                       Set<T> visited,
                                       Set<T> expanded) {
        /* Check whether we've been here before. If so, we should stop the
         * search.
         */
        if (visited.contains(node)) {
            /* There are two cases to consider.  First, if this node has
             * already been expanded, then it's already been assigned a
             * position in the final topological sort and we don't need to
             * explore it again.  However, if it hasn't been expanded, it means
             * that we've just found a node that is currently being explored,
             * and therefore is part of a cycle.  In that case, we should
             * report an error.
             */
            if (expanded.contains(node)) return;
            throw new IllegalArgumentException("Graph contains a cycle.");
        }

        /* Mark that we've been here */
        visited.add(node);

        /* Recursively explore all of the node's predecessors. */
        for (T predecessor : g.edgesFrom(node))
            exploreDFS(predecessor, g, visited, expanded);

        /* Similarly, mark that this node is done being expanded. */
        expanded.add(node);
    }

    /**
     * Returns the reverse of the input graph.
     *
     * @param g A graph to reverse.
     * @return The reverse of that graph.
     */
    private static <T> DirectionalGraph<T> reverseGraph(DirectionalGraph<T> g) {
        DirectionalGraph<T> result = new ConcurrentDirectionalGraph<>();
        for (T node : g)
            result.addNode(node);
        for (T node : g)
            for (T endpoint : g.edgesFrom(node))
                result.addEdge(endpoint, node);
        return result;
    }
}
