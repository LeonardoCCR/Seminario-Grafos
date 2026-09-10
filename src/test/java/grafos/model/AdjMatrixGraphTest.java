package grafos.model.model;

import java.util.ArrayList;
import java.util.Arrays;

import grafos.model.AdjMatrixGraph;
import grafos.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdjMatrixGraphTest {
    private AdjMatrixGraph<Integer> graph;

    @BeforeEach
    void setUp() {
        //pensando em colocar um tipo integer já no início, então o grafo só vai aceitar integers no teste
        graph = new AdjMatrixGraph<>(Arrays.asList(
                new Node<>("A", 10),
                new Node<>("B", 20),
                new Node<>("C", 30)
        ));
    }

    @Test
    void deveCriarGrafoSemArestas() {
        assertEquals(3, graph.getNumNodes());
        assertEquals(0, graph.getNumEdges());
        assertFalse(graph.hasEdge("A", "B"));
    }

    @Test
    void devePermitirGrafoVazio() {
        AdjMatrixGraph<Integer> emptyGraph =
                new AdjMatrixGraph<>(new ArrayList<Node<Integer>>());

        assertEquals(0, emptyGraph.getOrder());
        assertEquals(0, emptyGraph.getNumEdges());
        assertTrue(emptyGraph.getNodes().isEmpty());
    }

    @Test
    void deveRejeitarListaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new AdjMatrixGraph<Integer>(null));
    }

    @Test
    void deveRejeitarVerticeNuloNoConstrutor() {
        assertThrows(IllegalArgumentException.class,
                () -> new AdjMatrixGraph<Integer>(Arrays.asList(
                        new Node<>("A", 10),
                        null
                )));
    }

    @Test
    void deveRejeitarIdsRepetidosNoConstrutor() {
        assertThrows(IllegalArgumentException.class,
                () -> new AdjMatrixGraph<>(Arrays.asList(
                        new Node<>("A", 10),
                        new Node<>("A", 20)
                )));
    }

    @Test
    void devePermitirValoresIguaisComIdsDiferentes() {
        AdjMatrixGraph<Integer> sameValuesGraph =
                new AdjMatrixGraph<>(Arrays.asList(
                        new Node<>("A", 10),
                        new Node<>("B", 10)
                ));

        assertEquals(2, sameValuesGraph.getOrder());
        assertEquals(Integer.valueOf(10), sameValuesGraph.getNode("A").getValue());
        assertEquals(Integer.valueOf(10), sameValuesGraph.getNode("B").getValue());
    }

    @Test
    void deveAdicionarVerticePreservandoArestasExistentes() {
        graph.addEdge("A", "B");
        Node<Integer> node = new Node<>("D", 40);

        graph.addNode(node);

        assertEquals(4, graph.getOrder());
        assertEquals(4, graph.getNumNodes());
        assertSame(node, graph.getNode("D"));
        assertEquals(1, graph.getNumEdges());
        assertTrue(graph.hasEdge("A", "B"));
        assertTrue(graph.hasEdge("B", "A"));
        assertEquals(0, graph.getDegree("D"));

        graph.addEdge("C", "D");

        assertTrue(graph.hasEdge("C", "D"));
        assertTrue(graph.hasEdge("D", "C"));
        assertEquals(2, graph.getNumEdges());
    }

    @Test
    void deveRejeitarCadastroDeVerticeNulo() {
        assertThrows(IllegalArgumentException.class, () -> graph.addNode(null));

        assertEquals(3, graph.getOrder());
    }

    @Test
    void deveRejeitarCadastroDeIdRepetido() {
        assertThrows(IllegalArgumentException.class,
                () -> graph.addNode(new Node<>("A", 99)));

        assertEquals(3, graph.getOrder());
        assertEquals(Integer.valueOf(10), graph.getNode("A").getValue());
    }

    @Test
    void deveAdicionarArestaNaoDirecionada() {
        graph.addEdge("A", "B");

        assertTrue(graph.hasEdge("A", "B"));
        assertTrue(graph.hasEdge("B", "A"));
        assertEquals(1, graph.getNumEdges());
    }

    @Test
    void naoDeveDuplicarArestaMesmoNaOrdemInversa() {
        graph.addEdge("A", "B");
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        assertEquals(1, graph.getNumEdges());
        assertEquals(1, graph.getDegree("A"));
        assertEquals(1, graph.getDegree("B"));
    }

    @Test
    void deveRejeitarArestaComVerticeInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> graph.addEdge("X", "A"));

        assertThrows(IllegalArgumentException.class,
                () -> graph.addEdge("A", "X"));

        assertEquals(0, graph.getNumEdges());
        assertEquals(0, graph.getDegree("A"));
    }

    @Test
    void deveRemoverArestaNosDoisSentidos() {
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        graph.removeEdge("B", "A");

        assertFalse(graph.hasEdge("A", "B"));
        assertFalse(graph.hasEdge("B", "A"));
        assertTrue(graph.hasEdge("B", "C"));
        assertEquals(1, graph.getNumEdges());
        assertEquals(0, graph.getDegree("A"));
        assertEquals(1, graph.getDegree("B"));
    }

    @Test
    void removerArestaAusenteNaoDeveAlterarContagem() {
        graph.addEdge("A", "B");

        graph.removeEdge("A", "C");

        assertEquals(1, graph.getNumEdges());
        assertTrue(graph.hasEdge("A", "B"));

        graph.removeEdge("A", "B");
        graph.removeEdge("A", "B");

        assertEquals(0, graph.getNumEdges());
    }

    @Test
    void deveRetornarVerticePeloId() {
        Node<Integer> node = graph.getNode("B");

        assertEquals("B", node.getId());
        assertEquals(Integer.valueOf(20), node.getValue());
    }

    @Test
    void deveRetornarTodosOsVerticesNaOrdemDeCadastro() {
        assertEquals(Arrays.asList(
                new Node<>("A", 10),
                new Node<>("B", 20),
                new Node<>("C", 30)
        ), graph.getNodes());
    }

    @Test
    void naoDevePermitirAlterarListaDeVerticesExternamente() {
        assertThrows(UnsupportedOperationException.class,
                () -> graph.getNodes().add(new Node<>("D", 40)));

        assertThrows(UnsupportedOperationException.class,
                () -> graph.getNodes().remove(0));

        assertEquals(3, graph.getOrder());
    }

    @Test
    void deveRetornarVizinhosDoVertice() {
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");

        assertEquals(Arrays.asList(
                graph.getNode("B"),
                graph.getNode("C")
        ), graph.getNeighbors("A"));

        assertEquals(2, graph.countNeighbors("A"));
        assertEquals(1, graph.countNeighbors("B"));
    }

    @Test
    void alterarListaRetornadaDeVizinhosNaoDeveAlterarGrafo() {
        graph.addEdge("A", "B");

        graph.getNeighbors("A").clear();

        assertTrue(graph.hasEdge("A", "B"));
        assertEquals(1, graph.countNeighbors("A"));
        assertEquals(1, graph.getNumEdges());
    }

    @Test
    void deveRetornarGrauZeroEVizinhosVaziosParaVerticeIsolado() {
        assertEquals(0, graph.getDegree("C"));
        assertEquals(0, graph.countNeighbors("C"));
        assertTrue(graph.getNeighbors("C").isEmpty());
    }

    @Test
    void ordemDeveDependerApenasDaQuantidadeDeVertices() {
        assertEquals(3, graph.getOrder());

        graph.addEdge("A", "B");

        assertEquals(3, graph.getOrder());

        graph.removeEdge("A", "B");

        assertEquals(3, graph.getOrder());

        graph.addNode(new Node<>("D", 40));

        assertEquals(4, graph.getOrder());
    }

    @Test
    void deveCalcularGrauDeCadaVertice() {
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");

        assertEquals(2, graph.getDegree("A"));
        assertEquals(1, graph.getDegree("B"));
        assertEquals(1, graph.getDegree("C"));
    }

    @Test
    void lacoDeveContarComoUmaArestaEDoisNoGrau() {
        graph.addEdge("A", "A");
        graph.addEdge("A", "A");

        assertTrue(graph.hasEdge("A", "A"));
        assertEquals(1, graph.getNumEdges());
        assertEquals(2, graph.getDegree("A"));
        assertEquals(1, graph.countNeighbors("A"));
        assertEquals(Arrays.asList(graph.getNode("A")), graph.getNeighbors("A"));
    }

    @Test
    void deveRemoverLacoAtualizandoGrauEContagem() {
        graph.addEdge("A", "A");
        graph.addEdge("A", "B");

        assertEquals(3, graph.getDegree("A"));

        graph.removeEdge("A", "A");

        assertFalse(graph.hasEdge("A", "A"));
        assertTrue(graph.hasEdge("A", "B"));
        assertEquals(1, graph.getNumEdges());
        assertEquals(1, graph.getDegree("A"));
    }

    @Test
    void somaDosGrausDeveSerDobroDaQuantidadeDeArestas() {
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("A", "A");

        int sum = 0;

        for (Node<Integer> node : graph.getNodes()) {
            sum += graph.getDegree(node.getId());
        }

        assertEquals(2 * graph.getNumEdges(), sum);
    }

    @Test
    void deveRejeitarConsultasEremocaoComVerticeInexistente() {
        assertThrows(IllegalArgumentException.class, () -> graph.getNode("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getNeighbors("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.countNeighbors("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getDegree("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.hasEdge("A", "X"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeEdge("X", "A"));
    }
}