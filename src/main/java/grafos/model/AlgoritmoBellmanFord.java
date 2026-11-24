package grafos.model;

import java.util.HashMap;
import java.util.List;

public interface AlgoritmoBellmanFord<T> {

    List<Node<T>> getShortestPath(AdjMatrixGraph<T> graph, String source, String destination);

    HashMap<Node<T>, Double> minimumCostForEachNode(AdjMatrixGraph<T> graph, String source);
}
