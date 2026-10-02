import grafos.model.AdjMatrixGraph;
import grafos.model.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphTestRoutine {

    public static void main(String[] args) {
        testCompleteUndirectedGraph();
        testLeavesAndIsolatedNode();
        testLoopAndDuplicateEdge();
        testBipartiteGraph();
        testDirectedGraph();
        testCompleteDirectedGraph();
        testRemoval();

        System.out.println("\nTodos os testes passaram.");
    }

    private static AdjMatrixGraph<Double> createGraph(boolean directed, boolean weighted, String... ids) {
        AdjMatrixGraph<Double> graph = new AdjMatrixGraph<>(new ArrayList<>(), directed, weighted);

        for (int i = 0; i < ids.length; i++) {
            graph.addNode(new Node<>(ids[i], (double) (i + 1)));
        }

        return graph;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void show(String title, String expected, AdjMatrixGraph<Double> graph, String vertexId) {
        System.out.println("\n==================================================");
        System.out.println(title);
        System.out.println("Esperado: " + expected);
        System.out.println(graph.getGraphInformation(vertexId));
    }

    private static List<String> getIds(List<Node<Double>> nodes) {
        List<String> ids = new ArrayList<>();

        for (Node<Double> node : nodes) {
            ids.add(node.getId());
        }

        return ids;
    }

    private static void testCompleteUndirectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, false, "A", "B", "C");

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        check(Double.valueOf(1.0).equals(graph.getNode("A").getValue()), "O valor do vértice A deve ser 1.0.");
        check(Double.valueOf(2.0).equals(graph.getNode("B").getValue()), "O valor do vértice B deve ser 2.0.");
        check(Double.valueOf(3.0).equals(graph.getNode("C").getValue()), "O valor do vértice C deve ser 3.0.");
        check(graph.getOrder() == 3, "O triângulo deve possuir três vértices.");
        check(graph.getNumEdges() == 3, "O triângulo deve possuir três arestas.");
        check(graph.isSimple(), "O triângulo deve ser simples.");
        check(graph.isRegular(), "O triângulo deve ser regular.");
        check(graph.isComplete(), "O triângulo deve ser completo.");
        check(!graph.isBipartite(), "O triângulo não deve ser bipartido.");

        for (Node<Double> node : graph.getNodes()) {
            check(graph.getDegree(node.getId()) == 2, "Cada vértice do triângulo deve ter grau dois.");
        }

        show("TESTE 1 - Triângulo não orientado", "Simples = Sim; Regular = Sim; Completo = Sim; Bipartido = Não.", graph, null);
    }

    private static void testLeavesAndIsolatedNode() {
        AdjMatrixGraph<Double> graph = createGraph(false, false, "A", "B", "C", "D");

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        check(graph.getDegree("A") == 1, "A deve possuir grau um.");
        check(graph.getDegree("B") == 2, "B deve possuir grau dois.");
        check(graph.getDegree("C") == 1, "C deve possuir grau um.");
        check(graph.getDegree("D") == 0, "D deve estar isolado.");

        check(getIds(graph.getLeafNodes()).equals(Arrays.asList("A", "C")), "As folhas devem ser A e C.");
        check(getIds(graph.getIsolatedNodes()).equals(Arrays.asList("D")), "O vértice isolado deve ser D.");
        check(graph.isSimple(), "O caminho deve ser simples.");
        check(graph.isBipartite(), "O caminho com vértice isolado deve ser bipartido.");
        check(!graph.isRegular(), "O grafo não deve ser regular.");
        check(!graph.isComplete(), "O grafo não deve ser completo.");

        show("TESTE 2 - Caminho A-B-C e vértice isolado D", "Folhas = [A, C]; Isolados = [D]; Regular = Não; Completo = Não.", graph, null);
    }

    private static void testLoopAndDuplicateEdge() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B");

        graph.addEdge("A", "A", 0.0);
        graph.addEdge("A", "B", 5.5);

        check(graph.hasLoops(), "O grafo deve possuir laço.");
        check(getIds(graph.getLoopNodes()).equals(Arrays.asList("A")), "O laço deve estar em A.");
        check(graph.getDegree("A") == 3, "O laço deve contribuir com dois para o grau de A.");
        check(graph.getNumEdges() == 2, "O laço deve contar como uma única aresta.");
        check(graph.getWeight("A", "A") == 0.0, "Peso zero deve ser permitido.");
        check(!graph.isSimple(), "Um grafo com laço não deve ser simples.");
        check(!graph.isBipartite(), "Um grafo com laço não deve ser bipartido.");
        check(!graph.isComplete(), "Um grafo com laço não deve ser completo.");

        show("TESTE 3 - Grafo ponderado com laço", "Laço em A; Grau de A = 3; Tamanho = 2; Simples = Não (multigrafo); Bipartido = Não.", graph, null);

        try {
            graph.addEdge("B", "A", 9.0);
            throw new AssertionError("A tentativa de adicionar uma multiaresta deveria ser rejeitada.");
        } catch (IllegalArgumentException exception) {
            check(exception.getMessage().equals("Representação por Matriz de Adjacências não permite multiarestas / multigrafo"), "A mensagem de rejeição da multiaresta está incorreta.");

            System.out.println("\nTentativa de adicionar B-A novamente:");
            System.out.println(exception.getMessage());
        }

        check(graph.getNumEdges() == 2, "A tentativa rejeitada não deve alterar a quantidade de arestas.");
        check(graph.getWeight("A", "B") == 5.5, "A tentativa rejeitada não deve alterar o peso original.");
    }

    private static void testBipartiteGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, false, "A", "B", "C", "D");

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");
        graph.addEdge("D", "A");

        check(graph.isSimple(), "O ciclo de quatro vértices deve ser simples.");
        check(graph.isRegular(), "O ciclo de quatro vértices deve ser regular.");
        check(graph.isBipartite(), "O ciclo de quatro vértices deve ser bipartido.");
        check(!graph.isComplete(), "O ciclo de quatro vértices não deve ser completo.");

        show("TESTE 4 - Ciclo A-B-C-D-A", "Bipartido = Sim; partições possíveis: [A, C] e [B, D]; Regular = Sim.", graph, null);
    }

    private static void testDirectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(true, false, "A", "B", "C", "D", "E");

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");

        check(!graph.hasEdge("B", "A"), "A inserção de A para B não deve criar B para A.");
        check(getIds(graph.getSources()).equals(Arrays.asList("A")), "A fonte deve ser A.");
        check(getIds(graph.getSinks()).equals(Arrays.asList("D")), "O sumidouro deve ser D.");
        check(!graph.isSource("E") && !graph.isSink("E"), "O isolado E não deve ser fonte nem sumidouro.");

        check(graph.getInDegree("B") == 1, "B deve possuir grau de entrada um.");
        check(graph.getOutDegree("B") == 1, "B deve possuir grau de saída um.");
        check(getIds(graph.getPredecessors("B")).equals(Arrays.asList("A")), "O antecessor de B deve ser A.");
        check(getIds(graph.getNeighbors("B")).equals(Arrays.asList("C")), "O sucessor de B deve ser C.");

        check(getIds(graph.getDirectTransitiveClosure("B")).equals(Arrays.asList("C", "D")), "O fecho transitivo direto de B deve ser [C, D].");

        check(getIds(graph.getInverseTransitiveClosure("B")).equals(Arrays.asList("A")), "O fecho transitivo inverso de B deve ser [A].");

        check(graph.getGraphInformation("B").contains("Não se aplica nesta entrega"), "O relatório deve informar que a bipartição não se aplica ao dígrafo.");

        show("TESTE 5 - Dígrafo A->B->C->D e isolado E", "Fonte = A; Sumidouro = D; Antecessor de B = A; Sucessor = C; Fecho direto = [C, D]; Inverso = [A].", graph, "B");
    }

    private static void testCompleteDirectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(true, false, "A", "B", "C");

        for (Node<Double> source : graph.getNodes()) {
            for (Node<Double> destination : graph.getNodes()) {
                if (!source.getId().equals(destination.getId())) {
                    graph.addEdge(source.getId(), destination.getId());
                }
            }
        }

        check(graph.getNumEdges() == 6, "O dígrafo completo deve possuir seis arcos.");
        check(graph.isSimple(), "O dígrafo completo deve ser simples.");
        check(graph.isRegular(), "O dígrafo completo deve ser regular.");
        check(graph.isComplete(), "O dígrafo deve ser completo.");
        check(graph.getSources().isEmpty(), "Não deve haver fontes.");
        check(graph.getSinks().isEmpty(), "Não deve haver sumidouros.");

        for (Node<Double> node : graph.getNodes()) {
            check(graph.getInDegree(node.getId()) == 2, "Cada vértice deve possuir entrada dois.");
            check(graph.getOutDegree(node.getId()) == 2, "Cada vértice deve possuir saída dois.");
        }

        check(getIds(graph.getDirectTransitiveClosure("A")).equals(Arrays.asList("A", "B", "C")), "O fecho direto deve incluir A porque há um ciclo de retorno.");

        check(getIds(graph.getInverseTransitiveClosure("A")).equals(Arrays.asList("A", "B", "C")), "O fecho inverso deve incluir A porque há um ciclo de retorno.");

        show("TESTE 6 - Dígrafo completo em três vértices", "Simples = Sim; Regular = Sim; Completo = Sim; seis arcos; entrada e saída dois em cada vértice.", graph, "A");
    }

    private static void testRemoval() {
        AdjMatrixGraph<Double> graph = createGraph(true, true, "A", "B", "C");

        graph.addEdge("A", "B", 2.0);
        graph.addEdge("B", "A", 3.0);
        graph.addEdge("B", "B", 4.0);
        graph.addEdge("B", "C", 5.0);
        graph.addEdge("A", "C", -1.0);

        check(graph.getInDegree("B") == 2, "O laço deve contar uma vez na entrada.");
        check(graph.getOutDegree("B") == 3, "O laço deve contar uma vez na saída.");

        graph.removeEdge("A", "B");

        check(!graph.hasEdge("A", "B"), "O arco A para B deveria ter sido removido.");
        check(graph.hasEdge("B", "A"), "O arco inverso deve permanecer.");
        check(graph.getNumEdges() == 4, "Devem restar quatro arcos.");

        graph.removeNode("B");

        check(graph.getOrder() == 2, "Devem restar dois vértices.");
        check(graph.getNumEdges() == 1, "Deve restar apenas o arco A para C.");
        check(graph.hasEdge("A", "C"), "O arco A para C deve permanecer.");
        check(graph.getWeight("A", "C") == -1.0, "O peso do arco restante deve ser preservado.");
        check(getIds(graph.getNodes()).equals(Arrays.asList("A", "C")), "Os vértices restantes devem ser A e C.");

        show("TESTE 7 - Remoção de arco e de vértice com laço", "Após remover A->B e o vértice B: ordem = 2; tamanho = 1; permanece A->C com peso -1.", graph, "A");
    }
}
