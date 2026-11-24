package grafos.model;

import org.w3c.dom.Node;

import java.util.List;

public interface AlgoritmoBellmanFord {

     Double minimumCost(AdjMatrixGraph<String> graph, String source, String destination);

    List<Node> getShortestPath(AdjMatrixGraph<String> graph, String source, String destination);
}
