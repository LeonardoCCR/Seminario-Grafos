package grafos.model;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;

public class BellmanFord implements AlgoritmoBellmanFord{

    private HashMap<Integer, Node> nodePredecessor;
    private List<Double> minimumCostForEachNode;

    @Override
    public Double minimumCost(AdjMatrixGraph<String> graph, String source, String destination) {
        return 0.0;
    }

    @Override
    public List<Node> getShortestPath(AdjMatrixGraph<String> graph, String source, String destination) {
        return List.of();
    }
}
