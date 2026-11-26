package grafos.model;

import java.util.HashMap;
import java.util.List;

public class OutputBellmanFord<T> {

    BellmanFord<T> bellmanFord = new BellmanFord<>();

    public OutputBellmanFord() {
        System.out.println("\nProblema resolvido com o uso do algoritmo de Bellman Ford (caminho mínimo com pesos negativos)\n");
    }

    public String minimumCost(AdjMatrixGraph<T> graph, String source) {

        this.validateGraph(graph);
        Node<T> node = this.validateNode(graph, source, "origem");
        HashMap<Node<T>, Double> minimumCost = bellmanFord.minimumCostForEachNode(graph, source);
        StringBuilder msg = new StringBuilder();

        msg.append("Imprimindo o custo mínimo do vértice de origem de ID ").append(node.getId()).append(" para todos os outros vértices: \n");

        for (int i = 0; i < graph.getOrder(); i++) {
            msg.append(node.getId()).append(" -> ").append(graph.getNode(i).getId()).append(" = ").append(minimumCost.get(graph.getNode(i))).append("\n");
        }
        return msg.toString();
    }

    public String getShortestPath(AdjMatrixGraph<T> graph, String source, String destination) {

        this.validateGraph(graph);
        Node<T> sourceNode = this.validateNode(graph, source, "origem");
        Node<T> destinationNode = this.validateNode(graph, destination, "destino");
        StringBuilder msg = new StringBuilder();
        List<Node<T>> shortestPath = bellmanFord.getShortestPath(graph, source, destination);

        if (shortestPath.isEmpty()) {
            return "Não existe caminho de " + sourceNode.getId() + " para " + destinationNode.getId() + ".\n";
        }

        msg.append("Caminho mais curto do ").append(sourceNode).append(" para o ").append(destinationNode).append(": \n");

        for (Node<T> node : shortestPath) {
            msg.append(node.getId()).append(" -> ");
        }
        msg.replace(msg.length() - 4, msg.length(), "");

        return msg.toString();
    }

    private Node<T> validateNode(AdjMatrixGraph<T> graph, String id, String role) {
        Node<T> node = graph.getNode(id);
        if (node == null) {
            throw new IllegalArgumentException("Vértice de " + role + " não encontrado: " + id);
        }
        return node;
    }

    private void validateGraph(AdjMatrixGraph<T> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("O grafo não pode ser nulo.");
        }

        if (!graph.isWeighted()) {
            throw new IllegalArgumentException("O algoritmo de Bellman Ford exige um grafo ponderado.");
        }

        if (!graph.isDirected()) {
            throw new IllegalArgumentException("O algoritmo de Bellman Ford exige um grafo dirigido (com orientação)");
        }
    }
}
