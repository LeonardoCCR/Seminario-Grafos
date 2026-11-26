package grafos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BellmanFord<T> implements AlgoritmoBellmanFord<T> {

    private final HashMap<Node<T>, Node<T>> nodePredecessor = new HashMap<>();
    private final HashMap<Node<T>, Double> minimumCostForEachNode = new HashMap<>();

    @Override
    public HashMap<Node<T>, Double> minimumCostForEachNode(AdjMatrixGraph<T> graph, String source) {
        this.initializeCostMatrix(graph, source);
        this.initializeNodePredecessor(graph);

        this.relaxEdge(graph, "runtimeCycle");
        this.relaxEdge(graph, "checkForANegativeCycle");

        return minimumCostForEachNode;
    }

    private void relaxEdge(AdjMatrixGraph<T> graph, String execution) {
        List<Edge<T>> listOfEdges = this.createEdgeLists(graph);
        Node<T> sourceNode;
        Node<T> destinationNode;
        Edge<T> edge;
        double sumOfCurrentDistance;
        boolean relaxation;

        int iterations = execution.equals("checkForANegativeCycle") ? 1 : Math.max(0, graph.getOrder() - 1);

        for (int i = 0; i < iterations; i++) {
            relaxation = false;
            for (int j = 0; j < listOfEdges.size(); j++) {
                edge = listOfEdges.get(j);
                sourceNode = edge.getSourceNode();
                destinationNode = edge.getDestinationNode();
                sumOfCurrentDistance = minimumCostForEachNode.get(sourceNode) + edge.getWeight();
                if (sumOfCurrentDistance < minimumCostForEachNode.get(destinationNode)) {
                    minimumCostForEachNode.put(destinationNode, sumOfCurrentDistance);
                    nodePredecessor.put(destinationNode, sourceNode);
                    relaxation = true;

                    if (execution.equals("checkForANegativeCycle")) {
                        throw new IllegalArgumentException("Operação finalizada com erro. O algoritmo detectou um ciclo negativo no grafo.");
                    }
                }
            }
            if (!relaxation) {
                break;
            }
        }
    }

    @Override
    public List<Node<T>> getShortestPath(AdjMatrixGraph<T> graph, String source, String destination) {
        Node<T> sourceNode = graph.getNode(source);
        Node<T> destinationNode = graph.getNode(destination);
        if (sourceNode == null || destinationNode == null) {
            return new ArrayList<>();
        }

        List<Node<T>> shortestPath = new ArrayList<>();
        Node<T> currentPredecessor = destinationNode;

        while (currentPredecessor != null) {
            shortestPath.add(currentPredecessor);
            currentPredecessor = nodePredecessor.get(currentPredecessor);
        }

        List<Node<T>> reverseList = new ArrayList<>(shortestPath);
        Collections.reverse(reverseList);

        if (reverseList.isEmpty() || !reverseList.get(0).equals(sourceNode)) {
            return new ArrayList<>();
        }

        return reverseList;
    }

    private void initializeCostMatrix(AdjMatrixGraph<T> graph, String source) {
        Node<T> currentNode;
        for (int i = 0; i < graph.getOrder(); i++) {
            currentNode = graph.getNodes().get(i);
            if (currentNode.equals(graph.getNode(source))) {
                minimumCostForEachNode.put(currentNode, 0.0);
                continue;
            }
            minimumCostForEachNode.put(currentNode, Double.POSITIVE_INFINITY);
        }
    }

    private void initializeNodePredecessor(AdjMatrixGraph<T> graph) {
        for (int i = 0; i < graph.getOrder(); i++) {
            nodePredecessor.put(graph.getNode(i), null);
        }
    }

    private List<Edge<T>> createEdgeLists(AdjMatrixGraph<T> graph) {
        Double[][] distanceMatrix = graph.createDistanceMatrix(graph);
        Edge<T> edge;
        List<Edge<T>> listOfEdges = new ArrayList<>();


        for (int i = 0; i < graph.getOrder(); i++) {
            for (int j = 0; j < graph.getOrder(); j++) {
                if (!distanceMatrix[i][j].equals(Double.POSITIVE_INFINITY)) {
                    edge = new Edge<>(graph.getNode(i), graph.getNode(j), distanceMatrix[i][j]);
                    listOfEdges.add(edge);
                }
            }
        }

        return listOfEdges;
    }
}
