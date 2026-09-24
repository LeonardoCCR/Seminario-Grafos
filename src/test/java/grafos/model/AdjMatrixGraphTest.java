package grafos.model;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdjMatrixGraphTest {
    private AdjMatrixGraph<Integer> graph;

    @BeforeEach
    void setUp() {
        //pensando em colocar um tipo integer já no início, então o grafo só vai aceitar integers no teste
        graph = createGraph(false, false);
    }

    private AdjMatrixGraph<Integer> createGraph(boolean directed, boolean weighted) {
        return new AdjMatrixGraph<>(Arrays.asList(
                new Node<>("A", 10), new Node<>("B", 20), new Node<>("C", 30)
        ), directed, weighted);
    }

    private List<String> ids(List<Node<Integer>> nodes) {
        return nodes.stream().map(Node::getId).collect(Collectors.toList());
    }

    @Test
    void deveRespeitarConstrutoresETipos() {
        AdjMatrixGraph<Integer> empty = new AdjMatrixGraph<>(new ArrayList<>());
        assertEquals(0, empty.getOrder());
        assertFalse(empty.isDirected());
        assertFalse(empty.isWeighted());
        AdjMatrixGraph<Integer> directed = new AdjMatrixGraph<>(graph.getNodes(), true);
        assertTrue(directed.isDirected());
        assertFalse(directed.isWeighted());
        for (boolean d : new boolean[]{false, true}) {
            for (boolean w : new boolean[]{false, true}) {
                AdjMatrixGraph<Integer> current = createGraph(d, w);
                assertEquals(d, current.isDirected());
                assertEquals(w, current.isWeighted());
                assertEquals(3, current.getNumNodes());
                assertEquals(0, current.getNumEdges());
            }
        }
    }

    @Test
    void deveRejeitarVerticesInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new AdjMatrixGraph<Integer>(null));
        assertThrows(IllegalArgumentException.class, () -> new AdjMatrixGraph<>(Arrays.asList(new Node<>("A", 1), null)));
        assertThrows(IllegalArgumentException.class, () -> new AdjMatrixGraph<>(Arrays.asList(new Node<>("A", 1), new Node<>("A", 2))));
        assertThrows(IllegalArgumentException.class, () -> graph.addNode(null));
        assertThrows(IllegalArgumentException.class, () -> graph.addNode(new Node<>("A", 99)));
        assertEquals(3, graph.getOrder());
        assertEquals(Integer.valueOf(10), graph.getNode("A").getValue());
    }

    @Test
    void deveProtegerListaDeVertices() {
        assertEquals(Arrays.asList("A", "B", "C"), ids(graph.getNodes()));
        assertThrows(UnsupportedOperationException.class, () -> graph.getNodes().clear());
        assertEquals(3, graph.getNumNodes());
    }

    @Test
    void deveAdicionarVerticePreservandoPesos() {
        graph = createGraph(false, true);
        graph.addEdge("A", "B", 5.5);
        Node<Integer> node = new Node<>("D", 40);
        graph.addNode(node);
        assertSame(node, graph.getNode("D"));
        assertEquals(4, graph.getOrder());
        assertEquals(5.5, graph.getWeight("A", "B"));
        assertEquals(5.5, graph.getWeight("B", "A"));
        assertEquals(0, graph.getDegree("D"));
        assertFalse(graph.hasEdge("D", "A"));
        graph.addEdge("C", "D", -2);
        assertEquals(-2.0, graph.getWeight("D", "C"));
        assertEquals(2, graph.getNumEdges());
    }

    @Test
    void deveRejeitarMultiarestasSemAlterarPeso() {
        graph = createGraph(false, true);
        graph.addEdge("A", "B", 5.5);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> graph.addEdge("A", "B", 10));
        assertEquals("Representação por Matriz de Adjacências não permite multiarestas / multigrafo", error.getMessage());
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("B", "A", 20));
        assertEquals(1, graph.getNumEdges());
        assertEquals(5.5, graph.getWeight("A", "B"));
        assertEquals(5.5, graph.getWeight("B", "A"));
    }

    @Test
    void deveRespeitarDirecaoDasArestas() {
        graph = createGraph(true, true);
        graph.addEdge("A", "B", 2);
        assertFalse(graph.hasEdge("B", "A"));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B", 9));
        graph.addEdge("B", "A", 3);
        assertEquals(2, graph.getNumEdges());
        graph.removeEdge("A", "B");
        assertFalse(graph.hasEdge("A", "B"));
        assertEquals(3.0, graph.getWeight("B", "A"));
        graph.removeEdge("A", "B");
        assertEquals(1, graph.getNumEdges());
    }

    @Test
    void deveRemoverArestaNosDoisSentidos() {
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.removeEdge("B", "A");
        assertFalse(graph.hasEdge("A", "B"));
        assertFalse(graph.hasEdge("B", "A"));
        assertTrue(graph.hasEdge("B", "C"));
        graph.removeEdge("A", "B");
        assertEquals(1, graph.getNumEdges());
        assertEquals(0, graph.getDegree("A"));
    }

    @Test
    void deveRemoverVerticeOrientadoComLaco() {
        graph = createGraph(true, true);
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "A", 2);
        graph.addEdge("B", "B", 3);
        graph.addEdge("B", "C", 4);
        graph.addEdge("A", "C", 5);
        graph.removeNode("B");
        assertEquals(Arrays.asList("A", "C"), ids(graph.getNodes()));
        assertEquals(1, graph.getNumEdges());
        assertEquals(5.0, graph.getWeight("A", "C"));
        assertThrows(IllegalArgumentException.class, () -> graph.getNode("B"));
        graph.addNode(new Node<>("D", 40));
        graph.addEdge("C", "D", 6);
        assertEquals(6.0, graph.getWeight("C", "D"));
        assertEquals(2, graph.getNumEdges());
    }

    @Test
    void deveRemoverVerticeNaoOrientadoComLaco() {
        graph.addEdge("A", "B");
        graph.addEdge("B", "B");
        graph.addEdge("B", "C");
        graph.addEdge("A", "C");
        graph.removeNode("B");
        assertEquals(2, graph.getOrder());
        assertEquals(1, graph.getNumEdges());
        assertTrue(graph.hasEdge("A", "C"));
        assertTrue(graph.hasEdge("C", "A"));
        assertEquals(1, graph.getDegree("C"));
    }

    @Test
    void devePermitirEsvaziarEReutilizarGrafo() {
        graph.removeNode("A");
        graph.removeNode("C");
        graph.addEdge("B", "B");
        graph.removeNode("B");
        assertEquals(0, graph.getOrder());
        assertEquals(0, graph.getNumEdges());
        graph.addNode(new Node<>("D", 40));
        assertEquals(1, graph.getOrder());
        assertFalse(graph.hasEdge("D", "D"));
    }

    @Test
    void deveValidarPesos() {
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B", 1));
        assertThrows(IllegalArgumentException.class, () -> graph.getWeight("A", "B"));
        graph = createGraph(false, true);
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B"));
        for (double weight : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B", weight));
        }
        assertEquals(0, graph.getNumEdges());
        graph.addEdge("A", "B", 0);
        graph.addEdge("B", "C", -3.5);
        assertTrue(graph.hasEdge("A", "B"));
        assertEquals(0.0, graph.getWeight("A", "B"));
        assertEquals(-3.5, graph.getWeight("B", "C"));
        assertEquals(2, graph.getDegree("B"));
        assertThrows(IllegalArgumentException.class, () -> graph.getWeight("A", "C"));
    }

    @Test
    void deveRejeitarIdsInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> graph.getNode("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("X", "A"));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "X"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeEdge("A", "X"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeNode("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.hasEdge("X", "A"));
        assertThrows(IllegalArgumentException.class, () -> graph.getNeighbors("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getPredecessors("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getDegree("X"));
        assertEquals(3, graph.getOrder());
        assertEquals(0, graph.getNumEdges());
    }

    @Test
    void deveContarLacosCorretamente() {
        graph.addEdge("A", "A");
        assertEquals(1, graph.getNumEdges());
        assertEquals(2, graph.getDegree("A"));
        assertEquals(1, graph.countNeighbors("A"));
        assertEquals(Arrays.asList("A"), ids(graph.getLoopNodes()));
        assertFalse(graph.isSimple());
        assertFalse(graph.isComplete());
        assertFalse(graph.isBipartite());
        graph.removeEdge("A", "A");
        assertFalse(graph.hasLoops());
        assertEquals(0, graph.getDegree("A"));
        graph = createGraph(true, false);
        graph.addEdge("A", "A");
        assertEquals(1, graph.getInDegree("A"));
        assertEquals(1, graph.getOutDegree("A"));
        assertEquals(2, graph.getDegree("A"));
        assertEquals(Arrays.asList("A"), ids(graph.getDirectTransitiveClosure("A")));
    }

    @Test
    void deveIdentificarFolhasIsoladosEVizinhos() {
        graph.addNode(new Node<>("D", 40));
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        assertEquals(Arrays.asList("A", "C"), ids(graph.getLeafNodes()));
        assertEquals(Arrays.asList("D"), ids(graph.getIsolatedNodes()));
        assertEquals(Arrays.asList("A", "C"), ids(graph.getNeighbors("B")));
        assertEquals(Arrays.asList("A", "C"), ids(graph.getPredecessors("B")));
        graph.getNeighbors("B").clear();
        assertEquals(2, graph.countNeighbors("B"));
        assertEquals(0, graph.getDegree("D"));
        assertTrue(graph.getNeighbors("D").isEmpty());
        assertFalse(graph.isRegular());
        assertFalse(graph.isComplete());
        assertTrue(graph.isBipartite());
    }

    @Test
    void deveClassificarTrianguloECicloPar() {
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");
        assertTrue(graph.isSimple());
        assertTrue(graph.isRegular());
        assertTrue(graph.isComplete());
        assertFalse(graph.isBipartite());
        graph.removeEdge("C", "A");
        graph.addNode(new Node<>("D", 40));
        graph.addEdge("C", "D");
        graph.addEdge("D", "A");
        assertTrue(graph.isRegular());
        assertTrue(graph.isBipartite());
        assertFalse(graph.isComplete());
    }

    @Test
    void deveVerificarBiparticaoEmTodosOsComponentes() {
        graph.addNode(new Node<>("D", 40));
        graph.addNode(new Node<>("E", 50));
        graph.addEdge("A", "B");
        graph.addEdge("C", "D");
        graph.addEdge("D", "E");
        assertTrue(graph.isBipartite());
        graph.addEdge("E", "C");
        assertFalse(graph.isBipartite());
    }

    @Test
    void deveIdentificarFontesESumidouros() {
        graph = createGraph(true, false);
        graph.addNode(new Node<>("D", 40));
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        assertEquals(Arrays.asList("A"), ids(graph.getSources()));
        assertEquals(Arrays.asList("C"), ids(graph.getSinks()));
        assertEquals(Arrays.asList("A"), ids(graph.getPredecessors("B")));
        assertEquals(Arrays.asList("C"), ids(graph.getNeighbors("B")));
        assertEquals(1, graph.getInDegree("B"));
        assertEquals(1, graph.getOutDegree("B"));
        assertFalse(graph.isSource("D"));
        assertFalse(graph.isSink("D"));
        assertFalse(graph.isRegular());
    }

    @Test
    void deveClassificarDigrafoCompletoERegular() {
        graph = createGraph(true, false);
        for (String src : Arrays.asList("A", "B", "C")) {
            for (String dest : Arrays.asList("A", "B", "C")) {
                if (!src.equals(dest)) graph.addEdge(src, dest);
            }
        }
        assertEquals(6, graph.getNumEdges());
        assertTrue(graph.isSimple());
        assertTrue(graph.isRegular());
        assertTrue(graph.isComplete());
        assertTrue(graph.getSources().isEmpty());
        assertTrue(graph.getSinks().isEmpty());
        for (Node<Integer> node : graph.getNodes()) {
            assertEquals(2, graph.getInDegree(node.getId()));
            assertEquals(2, graph.getOutDegree(node.getId()));
        }
        graph.removeEdge("A", "B");
        assertFalse(graph.isComplete());
        assertFalse(graph.isRegular());
    }

    @Test
    void deveCalcularFechosComESemCiclo() {
        graph = createGraph(true, false);
        graph.addNode(new Node<>("D", 40));
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");
        assertEquals(Arrays.asList("C", "D"), ids(graph.getDirectTransitiveClosure("B")));
        assertEquals(Arrays.asList("A"), ids(graph.getInverseTransitiveClosure("B")));
        assertTrue(graph.getDirectTransitiveClosure("D").isEmpty());
        assertTrue(graph.getInverseTransitiveClosure("A").isEmpty());
        graph.addEdge("C", "B");
        assertEquals(Arrays.asList("B", "C", "D"), ids(graph.getDirectTransitiveClosure("B")));
        assertEquals(Arrays.asList("A", "B", "C"), ids(graph.getInverseTransitiveClosure("B")));
        assertThrows(IllegalArgumentException.class, () -> graph.getDirectTransitiveClosure("X"));
    }

    @Test
    void deveRejeitarOperacoesIncompativeisComTipo() {
        assertThrows(IllegalArgumentException.class, () -> graph.getSources());
        assertThrows(IllegalArgumentException.class, () -> graph.getSinks());
        assertThrows(IllegalArgumentException.class, () -> graph.getDirectTransitiveClosure("A"));
        assertThrows(IllegalArgumentException.class, () -> graph.getInverseTransitiveClosure("A"));
        graph = createGraph(true, false);
        assertThrows(IllegalArgumentException.class, () -> graph.getLeafNodes());
        assertThrows(IllegalArgumentException.class, () -> graph.isBipartite());
    }

    @Test
    void deveGerarInformacoesNaoOrientadas() {
        graph.addEdge("A", "A");
        graph.addEdge("B", "B");
        graph.addEdge("A", "B");
        String info = graph.getGraphInformation();
        assertTrue(info.contains("1.6.1 — Ordem: 3"));
        assertTrue(info.contains("Tamanho: 3 arestas"));
        assertTrue(info.contains("Não orientado e não ponderado"));
        assertTrue(info.contains("Vértice A: grau 3"));
        assertTrue(info.contains("Laços: A → A, B → B"));
        assertTrue(info.contains("Vértices isolados: [C]"));
        assertTrue(info.contains("1.6.3 — Simples - False"));
        assertTrue(info.contains("1.6.6 — Bipartido - False"));
        assertTrue(info.contains("Não se aplica a grafos não orientados."));
        assertFalse(info.contains("Escolha uma opção"));
    }

    @Test
    void deveGerarInformacoesOrientadasComFechos() {
        graph = createGraph(true, true);
        graph.addEdge("A", "B", 0);
        graph.addEdge("B", "C", 2);
        String info = graph.getGraphInformation("B");
        assertTrue(info.contains("Tamanho: 2 arcos"));
        assertTrue(info.contains("Orientado e ponderado"));
        assertTrue(info.contains("Vértice B: grau de entrada 1, grau de saída 1"));
        assertTrue(info.contains("Sucessores: [C]; Antecessores: [A]"));
        assertTrue(info.contains("Fontes: [A]"));
        assertTrue(info.contains("Sumidouros: [C]"));
        assertTrue(info.contains("1.6.3 — Simples - True"));
        assertTrue(info.contains("1.6.4 — Regular - False"));
        assertTrue(info.contains("1.6.5 — Completo - False"));
        assertTrue(info.contains("1.6.6 — Bipartido - Não se aplica nesta entrega"));
        assertTrue(info.contains("Direto: [C]"));
        assertTrue(info.contains("Inverso: [A]"));
        assertTrue(graph.getGraphInformation().contains("Informe um vértice"));
        assertThrows(IllegalArgumentException.class, () -> graph.getGraphInformation("X"));
    }

    @Test
    void deveInformarGrafoVazio() {
        graph = new AdjMatrixGraph<>(new ArrayList<>());
        assertTrue(graph.isSimple());
        assertTrue(graph.isRegular());
        assertTrue(graph.isComplete());
        assertTrue(graph.isBipartite());
        assertTrue(graph.getDegreesInformation().contains("O grafo não possui vértices."));
        graph = new AdjMatrixGraph<>(new ArrayList<>(), true, false);
        assertTrue(graph.getGraphInformation().contains("1.6.8 — Fecho Transitivo Direto e Inverso:\nO grafo não possui vértices."));
    }

    @Test
    void deveImprimirMatrizDistinguindoZeroEAusencia() {
        PrintStream original = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(bytes)) {
            System.setOut(capture);
            graph.addEdge("A", "B");
            graph.printGraph();
            assertArrayEquals(new String[]{"A", "B", "C", "A", "0", "1", "0", "B", "1", "0", "0", "C", "0", "0", "0"},
                    bytes.toString().trim().split("\\s+"));
            bytes.reset();
            graph = createGraph(true, true);
            graph.addEdge("A", "B", 0);
            graph.printGraph();
            assertArrayEquals(new String[]{"A", "B", "C", "A", "-", "0.0", "-", "B", "-", "-", "-", "C", "-", "-", "-"},
                    bytes.toString().trim().split("\\s+"));
        } finally {
            System.setOut(original);
        }
    }
}
