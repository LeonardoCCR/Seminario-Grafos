package grafos.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
                new Node<>("A", 10),
                new Node<>("B", 20),
                new Node<>("C", 30)
        ), directed, weighted);
    }

    @Test
    void construtorPadraoDeveCriarGrafoNaoOrientadoENaoPonderado() {
        AdjMatrixGraph<Integer> defaultGraph = new AdjMatrixGraph<>(
                Arrays.asList(new Node<>("A", 10))
        );

        assertFalse(defaultGraph.isDirected());
        assertFalse(defaultGraph.isWeighted());
        assertEquals(1, defaultGraph.getOrder());
        assertEquals(0, defaultGraph.getNumEdges());
    }

    @Test
    void construtorComOrientacaoDeveCriarGrafoNaoPonderado() {
        AdjMatrixGraph<Integer> directedGraph = new AdjMatrixGraph<>(
                Arrays.asList(new Node<>("A", 10)), true
        );

        assertTrue(directedGraph.isDirected());
        assertFalse(directedGraph.isWeighted());
    }

    @Test
    void deveRespeitarTodasAsCombinacoesDeTipos() {
        for (boolean directed : new boolean[]{false, true}) {
            for (boolean weighted : new boolean[]{false, true}) {
                AdjMatrixGraph<Integer> current = createGraph(directed, weighted);

                assertEquals(directed, current.isDirected());
                assertEquals(weighted, current.isWeighted());
                assertEquals(3, current.getOrder());
                assertEquals(0, current.getNumEdges());
                assertFalse(current.hasEdge("A", "B"));
            }
        }
    }

    @Test
    void devePermitirGrafoVazioEAdicionarPrimeiroVertice() {
        AdjMatrixGraph<Integer> emptyGraph =
                new AdjMatrixGraph<>(new ArrayList<Node<Integer>>());

        assertEquals(0, emptyGraph.getOrder());
        assertEquals(0, emptyGraph.getNumEdges());
        assertTrue(emptyGraph.getNodes().isEmpty());

        emptyGraph.addNode(new Node<>("A", 10));

        assertEquals(1, emptyGraph.getOrder());
        assertEquals(0, emptyGraph.getDegree("A"));
        assertFalse(emptyGraph.hasEdge("A", "A"));
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
    void alterarListasRetornadasNaoDeveAlterarGrafo() {
        graph.addEdge("A", "B");

        graph.getNeighbors("A").clear();
        graph.getPredecessors("A").clear();

        assertTrue(graph.hasEdge("A", "B"));
        assertEquals(1, graph.countNeighbors("A"));
        assertEquals(Arrays.asList(graph.getNode("B")), graph.getPredecessors("A"));
        assertEquals(1, graph.getNumEdges());
    }

    @Test
    void deveRetornarGrauZeroEListasVaziasParaVerticeIsolado() {
        assertEquals(0, graph.getDegree("C"));
        assertEquals(0, graph.getInDegree("C"));
        assertEquals(0, graph.getOutDegree("C"));
        assertEquals(0, graph.countNeighbors("C"));
        assertTrue(graph.getNeighbors("C").isEmpty());
        assertTrue(graph.getPredecessors("C").isEmpty());
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
    void deveRejeitarConsultasERemocaoComVerticeInexistente() {
        assertThrows(IllegalArgumentException.class, () -> graph.getNode("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getNeighbors("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getPredecessors("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.countNeighbors("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getDegree("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getInDegree("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.getOutDegree("X"));
        assertThrows(IllegalArgumentException.class, () -> graph.hasEdge("A", "X"));
        assertThrows(IllegalArgumentException.class, () -> graph.hasEdge("X", "A"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeEdge("X", "A"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeEdge("A", "X"));

        assertEquals(0, graph.getNumEdges());
    }

    @Nested
    class NaoOrientadoENaoPonderado {

        @Test
        void deveAdicionarArestaNosDoisSentidos() {
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
            assertThrows(IllegalArgumentException.class, () -> graph.addEdge("X", "A"));
            assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "X"));

            assertEquals(0, graph.getNumEdges());
            assertEquals(0, graph.getDegree("A"));
        }

        @Test
        void deveRejeitarInsercaoComPesoEConsultaDePeso() {
            assertThrows(IllegalArgumentException.class,
                    () -> graph.addEdge("A", "B", 5.0));

            assertFalse(graph.hasEdge("A", "B"));
            assertEquals(0, graph.getNumEdges());

            graph.addEdge("A", "B");

            assertThrows(IllegalArgumentException.class,
                    () -> graph.getWeight("A", "B"));

            assertTrue(graph.hasEdge("A", "B"));
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
        void deveRetornarVizinhosEPredecessoresIguais() {
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");

            assertEquals(Arrays.asList(
                    graph.getNode("B"),
                    graph.getNode("C")
            ), graph.getNeighbors("A"));

            assertEquals(graph.getNeighbors("A"), graph.getPredecessors("A"));
            assertEquals(2, graph.countNeighbors("A"));
            assertEquals(1, graph.countNeighbors("B"));
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
    }

    @Nested
    class OrientadoENaoPonderado {

        @BeforeEach
        void setUpDirectedGraph() {
            graph = createGraph(true, false);
        }

        @Test
        void deveAdicionarArestaSomenteNoSentidoInformado() {
            graph.addEdge("A", "B");
            graph.addEdge("A", "B");

            assertTrue(graph.hasEdge("A", "B"));
            assertFalse(graph.hasEdge("B", "A"));
            assertEquals(1, graph.getNumEdges());
        }

        @Test
        void deveTratarArestasOpostasComoIndependentes() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "A");

            assertEquals(2, graph.getNumEdges());

            graph.removeEdge("A", "B");

            assertFalse(graph.hasEdge("A", "B"));
            assertTrue(graph.hasEdge("B", "A"));
            assertEquals(1, graph.getNumEdges());

            graph.removeEdge("A", "B");

            assertEquals(1, graph.getNumEdges());
        }

        @Test
        void deveDistinguirSucessoresPredecessoresEGraus() {
            graph.addEdge("A", "B");
            graph.addEdge("C", "B");
            graph.addEdge("B", "C");

            assertEquals(Arrays.asList(graph.getNode("C")), graph.getNeighbors("B"));
            assertEquals(Arrays.asList(
                    graph.getNode("A"),
                    graph.getNode("C")
            ), graph.getPredecessors("B"));

            assertEquals(1, graph.countNeighbors("B"));
            assertEquals(2, graph.getInDegree("B"));
            assertEquals(1, graph.getOutDegree("B"));
            assertEquals(3, graph.getDegree("B"));
        }

        @Test
        void lacoDeveContribuirUmaVezNaEntradaEUmaNaSaida() {
            graph.addEdge("A", "A");
            graph.addEdge("A", "A");

            assertEquals(1, graph.getNumEdges());
            assertEquals(1, graph.getInDegree("A"));
            assertEquals(1, graph.getOutDegree("A"));
            assertEquals(2, graph.getDegree("A"));
            assertEquals(1, graph.countNeighbors("A"));

            graph.removeEdge("A", "A");

            assertEquals(0, graph.getNumEdges());
            assertEquals(0, graph.getDegree("A"));
        }

        @Test
        void somaDasEntradasEDasSaidasDeveCorresponderAoNumeroDeArestas() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "A");
            graph.addEdge("C", "B");
            graph.addEdge("A", "A");

            int entries = 0;
            int exits = 0;

            for (Node<Integer> node : graph.getNodes()) {
                entries += graph.getInDegree(node.getId());
                exits += graph.getOutDegree(node.getId());
            }

            assertEquals(graph.getNumEdges(), entries);
            assertEquals(graph.getNumEdges(), exits);
        }
    }

    @Nested
    class Ponderado {

        @BeforeEach
        void setUpWeightedGraph() {
            graph = createGraph(false, true);
        }

        @Test
        void deveArmazenarPesoNosDoisSentidosQuandoNaoOrientado() {
            graph.addEdge("A", "B", 5.5);

            assertTrue(graph.hasEdge("A", "B"));
            assertTrue(graph.hasEdge("B", "A"));
            assertEquals(5.5, graph.getWeight("A", "B"), 0.000001);
            assertEquals(5.5, graph.getWeight("B", "A"), 0.000001);
            assertEquals(1, graph.getNumEdges());
        }

        @Test
        void deveDistinguirPesoZeroDeArestaAusente() {
            graph.addEdge("A", "B", 0.0);

            assertTrue(graph.hasEdge("A", "B"));
            assertFalse(graph.hasEdge("A", "C"));
            assertEquals(0.0, graph.getWeight("A", "B"), 0.000001);
            assertEquals(1, graph.getNumEdges());
            assertEquals(1, graph.getDegree("A"));
            assertEquals(Arrays.asList(graph.getNode("B")), graph.getNeighbors("A"));
            assertThrows(IllegalArgumentException.class, () -> graph.getWeight("A", "C"));
        }

        @Test
        void deveAceitarPesoNegativoSemAlterarCalculoDeGrau() {
            graph.addEdge("A", "B", -3.5);
            graph.addEdge("A", "C", 100.0);

            assertEquals(-3.5, graph.getWeight("A", "B"), 0.000001);
            assertEquals(2, graph.getDegree("A"));
            assertEquals(2, graph.countNeighbors("A"));
            assertEquals(2, graph.getNumEdges());
        }

        @Test
        void deveExigirPesoNaInsercao() {
            assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B"));

            assertFalse(graph.hasEdge("A", "B"));
            assertEquals(0, graph.getNumEdges());
        }

        @Test
        void deveRejeitarPesosNaoFinitos() {
            assertThrows(IllegalArgumentException.class,
                    () -> graph.addEdge("A", "B", Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> graph.addEdge("A", "B", Double.POSITIVE_INFINITY));
            assertThrows(IllegalArgumentException.class,
                    () -> graph.addEdge("A", "B", Double.NEGATIVE_INFINITY));

            assertFalse(graph.hasEdge("A", "B"));
            assertFalse(graph.hasEdge("B", "A"));
            assertEquals(0, graph.getNumEdges());
        }

        @Test
        void deveRejeitarVerticesInexistentesNaInsercaoEConsultaDePeso() {
            assertThrows(IllegalArgumentException.class,
                    () -> graph.addEdge("X", "A", 2.0));
            assertThrows(IllegalArgumentException.class,
                    () -> graph.addEdge("A", "X", 2.0));
            assertThrows(IllegalArgumentException.class,
                    () -> graph.getWeight("X", "A"));
            assertThrows(IllegalArgumentException.class,
                    () -> graph.getWeight("A", "X"));

            assertEquals(0, graph.getNumEdges());
        }

        @Test
        void inserirArestaExistenteNaoDeveSubstituirPesoNemDuplicarContagem() {
            graph.addEdge("A", "B", 5.5);
            graph.addEdge("A", "B", 10.0);
            graph.addEdge("B", "A", 20.0);

            assertEquals(1, graph.getNumEdges());
            assertEquals(5.5, graph.getWeight("A", "B"), 0.000001);
            assertEquals(5.5, graph.getWeight("B", "A"), 0.000001);
        }

        @Test
        void deveRemoverArestaPonderadaEPermitirReinsercaoComOutroPeso() {
            graph.addEdge("A", "B", 5.5);
            graph.removeEdge("B", "A");

            assertFalse(graph.hasEdge("A", "B"));
            assertFalse(graph.hasEdge("B", "A"));
            assertEquals(0, graph.getNumEdges());
            assertThrows(IllegalArgumentException.class, () -> graph.getWeight("A", "B"));
            assertThrows(IllegalArgumentException.class, () -> graph.getWeight("B", "A"));

            graph.addEdge("A", "B", 8.0);

            assertEquals(1, graph.getNumEdges());
            assertEquals(8.0, graph.getWeight("A", "B"), 0.000001);
            assertEquals(8.0, graph.getWeight("B", "A"), 0.000001);
        }

        @Test
        void devePreservarPesosAoAdicionarVerticeNosDoisTiposDeOrientacao() {
            for (boolean directed : new boolean[]{false, true}) {
                AdjMatrixGraph<Integer> current = createGraph(directed, true);
                current.addEdge("A", "B", 0.0);
                current.addEdge("B", "C", -2.5);

                current.addNode(new Node<>("D", 40));

                assertEquals(4, current.getOrder());
                assertEquals(2, current.getNumEdges());
                assertTrue(current.hasEdge("A", "B"));
                assertEquals(0.0, current.getWeight("A", "B"), 0.000001);
                assertEquals(-2.5, current.getWeight("B", "C"), 0.000001);
                assertEquals(!directed, current.hasEdge("B", "A"));
                assertEquals(0, current.getDegree("D"));
                assertFalse(current.hasEdge("D", "A"));
                assertFalse(current.hasEdge("A", "D"));

                current.addEdge("D", "A", 7.5);

                assertEquals(3, current.getNumEdges());
                assertEquals(7.5, current.getWeight("D", "A"), 0.000001);
                assertEquals(!directed, current.hasEdge("A", "D"));

                if (!directed) {
                    assertEquals(7.5, current.getWeight("A", "D"), 0.000001);
                }
            }
        }

        @Test
        void devePermitirLacoPonderadoComPesoZero() {
            graph.addEdge("A", "A", 0.0);

            assertTrue(graph.hasEdge("A", "A"));
            assertEquals(0.0, graph.getWeight("A", "A"), 0.000001);
            assertEquals(1, graph.getNumEdges());
            assertEquals(2, graph.getDegree("A"));

            graph.removeEdge("A", "A");

            assertFalse(graph.hasEdge("A", "A"));
            assertEquals(0, graph.getNumEdges());
            assertEquals(0, graph.getDegree("A"));
        }

        @Test
        void deveManterPesosIndependentesEmArestasOrientadasOpostas() {
            graph = createGraph(true, true);

            graph.addEdge("A", "B", 2.5);

            assertFalse(graph.hasEdge("B", "A"));
            assertThrows(IllegalArgumentException.class, () -> graph.getWeight("B", "A"));

            graph.addEdge("B", "A", 9.0);
            graph.addEdge("A", "B", 100.0);

            assertEquals(2, graph.getNumEdges());
            assertEquals(2.5, graph.getWeight("A", "B"), 0.000001);
            assertEquals(9.0, graph.getWeight("B", "A"), 0.000001);

            graph.removeEdge("A", "B");

            assertFalse(graph.hasEdge("A", "B"));
            assertTrue(graph.hasEdge("B", "A"));
            assertEquals(9.0, graph.getWeight("B", "A"), 0.000001);
            assertEquals(1, graph.getNumEdges());
            assertEquals(1, graph.getInDegree("A"));
            assertEquals(0, graph.getOutDegree("A"));
        }

        @Test
        void deveCalcularVizinhosEGrausOrientadosIndependentementeDosPesos() {
            graph = createGraph(true, true);

            graph.addEdge("A", "B", 0.0);
            graph.addEdge("C", "B", -4.0);
            graph.addEdge("B", "B", 8.0);

            assertEquals(Arrays.asList(graph.getNode("B")), graph.getNeighbors("B"));
            assertEquals(Arrays.asList(
                    graph.getNode("A"),
                    graph.getNode("B"),
                    graph.getNode("C")
            ), graph.getPredecessors("B"));

            assertEquals(3, graph.getInDegree("B"));
            assertEquals(1, graph.getOutDegree("B"));
            assertEquals(4, graph.getDegree("B"));
            assertEquals(1, graph.countNeighbors("B"));
            assertEquals(3, graph.getNumEdges());
        }
    }
}