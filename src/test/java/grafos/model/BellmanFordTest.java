package grafos.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BellmanFordTest {

    private AdjMatrixGraph<String> createRotasDescontoGraph() {
        AdjMatrixGraph<String> graph = new AdjMatrixGraph<>(Arrays.asList(
                new Node<>("A", "Centro A"),
                new Node<>("B", "Hub B"),
                new Node<>("C", "Hub C"),
                new Node<>("D", "Destino D")
        ), true, true);

        graph.addEdge("A", "B", 2.0);
        graph.addEdge("A", "C", 5.0);
        graph.addEdge("C", "B", -4.0);
        graph.addEdge("B", "D", 3.0);

        return graph;
    }

    private List<String> ids(List<Node<String>> nodes) {
        return nodes.stream().map(Node::getId).collect(Collectors.toList());
    }

    @Test
    void deveCalcularMenorCaminhoComPesosNegativos() {
        AdjMatrixGraph<String> graph = createRotasDescontoGraph();
        BellmanFord<String> bellmanFord = new BellmanFord<>();

        HashMap<Node<String>, Double> costs = bellmanFord.minimumCostForEachNode(graph, "A");

        assertEquals(0.0, costs.get(graph.getNode("A")));
        assertEquals(1.0, costs.get(graph.getNode("B")));
        assertEquals(5.0, costs.get(graph.getNode("C")));
        assertEquals(4.0, costs.get(graph.getNode("D")));

        List<Node<String>> path = bellmanFord.getShortestPath(graph, "A", "D");
        assertEquals(Arrays.asList("A", "C", "B", "D"), ids(path));
    }

    @Test
    void deveDetectarCicloNegativo() {
        AdjMatrixGraph<String> graph = createRotasDescontoGraph();
        graph.addEdge("D", "C", -2.0);

        BellmanFord<String> bellmanFord = new BellmanFord<>();
        OutputBellmanFord<String> output = new OutputBellmanFord<>();

        assertThrows(IllegalArgumentException.class, () -> bellmanFord.minimumCostForEachNode(graph, "A"));
        assertThrows(IllegalArgumentException.class, () -> output.minimumCost(graph, "A"));
    }

    @Test
    void deveTratarDestinoInalcancavel() {
        AdjMatrixGraph<String> graph = createRotasDescontoGraph();
        graph.addNode(new Node<>("E", "Isolado E"));

        OutputBellmanFord<String> output = new OutputBellmanFord<>();
        String costsOutput = output.minimumCost(graph, "A");
        String pathOutput = output.getShortestPath(graph, "A", "E");

        assertTrue(costsOutput.contains("A -> E = Infinity"));
        assertTrue(pathOutput.contains("Não existe caminho de A para E."));
    }

    @Test
    void deveRejeitarVerticeInexistente() {
        AdjMatrixGraph<String> graph = createRotasDescontoGraph();
        OutputBellmanFord<String> output = new OutputBellmanFord<>();

        assertThrows(IllegalArgumentException.class, () -> output.minimumCost(graph, "X"));
        assertThrows(IllegalArgumentException.class, () -> output.getShortestPath(graph, "A", "X"));
    }

    @Test
    void deveRejeitarGrafoInvalido() {
        OutputBellmanFord<String> output = new OutputBellmanFord<>();

        assertThrows(IllegalArgumentException.class, () -> output.minimumCost(null, "A"));

        AdjMatrixGraph<String> unweighted = new AdjMatrixGraph<>(Arrays.asList(
                new Node<>("A", "A"), new Node<>("B", "B")
        ), true, false);
        assertThrows(IllegalArgumentException.class, () -> output.minimumCost(unweighted, "A"));

        AdjMatrixGraph<String> undirected = new AdjMatrixGraph<>(Arrays.asList(
                new Node<>("A", "A"), new Node<>("B", "B")
        ), false, true);
        assertThrows(IllegalArgumentException.class, () -> output.minimumCost(undirected, "A"));
    }
}
