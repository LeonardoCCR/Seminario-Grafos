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
        testDijkstraUndirectedGraph();
        testDijkstraDirectedGraph();
        testPrimConnectedGraph();
        testPrimTiedWeights();
        testPrimLoopAndParallelEdge();
        testPrimDisconnectedGraph();
        testPrimDirectedGraph();
        testPrimSingleNodeGraph();
        testKruskalConnectedGraph();
        testKruskalTiedWeights();
        testKruskalLoopAndParallelEdge();
        testKruskalDisconnectedGraph();
        testKruskalDirectedGraph();
        testKruskalSingleNodeGraph();

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

        show("TESTE 3 - Grafo ponderado com laço", "Laço em A; Grau de A = 3; Tamanho = 2; Simples = Não (por causa do laço); Bipartido = Não.", graph, null);

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

    // Valida somente a seção final para não confundir o custo com as distâncias dos passos.
    private static void checkPrimResult(String report, int expectedEdges, double expectedWeight) {
        int resultIndex = report.indexOf("\n=== RESULTADO ===\n");
        check(resultIndex >= 0, "O relatório deve conter a seção de resultado.");
        String result = report.substring(resultIndex);

        check(result.contains("\nTotal de arestas: " + expectedEdges + "\n"),
                "Total de arestas esperado: " + expectedEdges + ". Resultado recebido: " + result);

        String weightPrefix = "\nPeso total: ";
        int weightIndex = result.indexOf(weightPrefix);
        check(weightIndex >= 0, "O resultado deve informar o peso total.");
        int start = weightIndex + weightPrefix.length();
        int end = result.indexOf('\n', start);
        check(end >= 0, "A linha do peso total deve estar completa.");
        double actualWeight = Double.parseDouble(result.substring(start, end).trim());

        // Uma tolerância evita falhas causadas pela representação binária de decimais.
        check(Math.abs(actualWeight - expectedWeight) < 0.000000001,
                "Peso esperado: " + expectedWeight + "; recebido: " + actualWeight);
    }

    private static void showPrim(String title, String expected, String report) {
        System.out.println("\n==================================================");
        System.out.println(title);
        System.out.println("Esperado: " + expected);
        System.out.println(report);
    }

    private static void testPrimConnectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C", "D");
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("A", "C", 5.0);
        graph.addEdge("C", "D", 3.0);
        graph.addEdge("B", "D", 10.0);

        String report = graph.prim();
        checkPrimResult(report, 3, 6.0);

        // As arestas caras precisam ficar de fora da árvore.
        check(report.contains("A -- B (peso 1.0)"), "A aresta de peso 1.0 deve entrar na árvore.");
        check(!report.contains("(peso 10.0)"), "A aresta de peso 10.0 não deve entrar na árvore.");
        check(report.contains("Árvore geradora mínima com 1 componente."),
                "Um grafo conexo deve gerar uma única árvore.");

        check(graph.getOrder() == 4 && graph.getNumEdges() == 5,
                "Prim não deve alterar a quantidade de vértices ou arestas.");
        check(graph.getWeight("B", "D") == 10.0, "Prim deve preservar os pesos originais.");

        showPrim("TESTE 10 - Prim em grafo conexo com peso conhecido",
                "três arestas; peso total = 6.0; grafo original preservado.", report);
    }

    private static void testPrimTiedWeights() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C");
        graph.addEdge("A", "B", 2.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("A", "C", 2.0);

        // Com todos os pesos iguais o desempate muda as arestas, mas nunca o peso total.
        String report = graph.prim();
        checkPrimResult(report, 2, 4.0);

        showPrim("TESTE 11 - Prim com empate de pesos",
                "duas arestas quaisquer; peso total = 4.0 independente do desempate.", report);
    }

    private static void testPrimLoopAndParallelEdge() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C");
        graph.addEdge("A", "A", 99.0);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);

        // A matriz guarda um único peso por par, então a segunda aresta é recusada.
        try {
            graph.addEdge("A", "B", 50.0);
            check(false, "A matriz de adjacências não deveria aceitar uma segunda aresta entre A e B.");
        } catch (IllegalArgumentException exception) {
            check(exception.getMessage().contains("não permite multiarestas"),
                    "A mensagem deve explicar que a representação não permite multiarestas.");
        }

        String report = graph.prim();
        checkPrimResult(report, 2, 3.0);
        check(!report.contains("(peso 99.0)"), "O laço não pode entrar na árvore geradora mínima.");

        showPrim("TESTE 12 - Prim com laço e tentativa de aresta paralela",
                "laço de peso 99.0 ignorado; aresta paralela recusada pela matriz; peso total = 3.0.", report);
    }

    private static void testPrimDisconnectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C", "D", "E");
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 4.0);

        // Sem ligação entre os componentes o resultado é uma floresta, não uma árvore.
        String report = graph.prim();
        checkPrimResult(report, 2, 5.0);
        check(report.contains("Floresta geradora mínima com 3 componentes."),
                "Os três componentes devem ser identificados.");
        check(report.contains("Nenhuma aresta (vértice isolado)."),
                "O vértice isolado deve aparecer como um componente sem arestas.");

        showPrim("TESTE 13 - Prim em grafo desconexo",
                "floresta com três componentes; duas arestas; peso total = 5.0; E isolado.", report);
    }

    private static void testPrimDirectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(true, true, "A", "B");
        graph.addEdge("A", "B", 1.0);

        try {
            graph.prim();
            check(false, "Prim não deveria aceitar um grafo orientado.");
        } catch (IllegalArgumentException exception) {
            check(exception.getMessage().contains("não se aplica a grafos orientados"),
                    "A mensagem deve informar que a árvore geradora mínima não se aplica a dígrafos.");
        }

        System.out.println("\n==================================================");
        System.out.println("TESTE 14 - Prim em grafo orientado");
        System.out.println("Esperado: o sistema informa que a funcionalidade não se aplica.");
        System.out.println("A árvore geradora mínima não se aplica a grafos orientados.");
    }

    private static void testPrimSingleNodeGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A");

        String report = graph.prim();
        checkPrimResult(report, 0, 0.0);

        showPrim("TESTE 15 - Prim em grafo com um único vértice",
                "nenhuma aresta; peso total = 0.0.", report);
    }

    private static void checkDijkstraResult(String report, String path, double expectedCost) {
        int resultIndex = report.indexOf("\n=== RESULTADO ===\n");
        check(resultIndex >= 0, "O relatório deve conter a seção de resultado.");
        String result = report.substring(resultIndex);

        check(result.contains("\nCaminho: " + path + "\n"),
                "Caminho esperado: " + path + ". Resultado recebido: " + result);

        String costPrefix = "\nCusto total: ";
        int costIndex = result.indexOf(costPrefix);
        check(costIndex >= 0, "O resultado deve informar o custo total.");
        int start = costIndex + costPrefix.length();
        int end = result.indexOf('\n', start);
        check(end >= 0, "A linha do custo total deve estar completa.");
        double actualCost = Double.parseDouble(result.substring(start, end).trim());

        // Uma tolerância evita falhas causadas pela representação binária de decimais.
        check(Math.abs(actualCost - expectedCost) < 0.000000001,
                "Custo esperado: " + expectedCost + "; recebido: " + actualCost);
    }

    private static void showDijkstra(String title, String expected, String report) {
        System.out.println("\n==================================================");
        System.out.println(title);
        System.out.println("Esperado: " + expected);
        System.out.println(report);
    }

    private static void testDijkstraUndirectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C", "D");
        graph.addEdge("A", "B", 4.0);
        graph.addEdge("A", "C", 1.0);
        graph.addEdge("C", "B", 2.0);
        graph.addEdge("B", "D", 1.0);
        graph.addEdge("C", "D", 7.0);
        graph.addEdge("A", "D", 10.0);

        String report = graph.dijkstra(graph, "A", "D");
        checkDijkstraResult(report, "A --> C --> B --> D", 4.0);
        checkDijkstraResult(graph.dijkstra(graph, "D", "A"), "D --> B --> C --> A", 4.0);

        // O algoritmo deve melhorar a distância inicial de B, de 4 para 3.
        check(report.contains("Passo 2 | Fechado: C\nDistâncias: A = 0.0 | B = 3.0 | C = 1.0 | D = 8.0\n"),
                "O segundo passo deve atualizar B para 3.0 e D para 8.0.");
        check(report.contains("Passo 4 | Fechado: D"), "D deve ser fechado no quarto passo.");
        check(graph.getOrder() == 4 && graph.getNumEdges() == 6,
                "Dijkstra não deve alterar a quantidade de vértices ou arestas.");
        check(graph.getWeight("A", "B") == 4.0 && graph.getWeight("A", "D") == 10.0,
                "Dijkstra deve preservar os pesos originais.");
        check(!graph.hasEdge("A", "A") && !graph.hasEdge("D", "D"),
                "A diagonal da matriz auxiliar não deve criar laços no grafo original.");

        showDijkstra("TESTE 8 - Dijkstra em grafo não orientado",
                "A --> C --> B --> D; custo = 4.0; grafo original preservado.", report);
    }

    private static void testDijkstraDirectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(true, true, "A", "B", "C", "D", "E");
        graph.addEdge("A", "B", 0.0);
        graph.addEdge("B", "C", 0.1);
        graph.addEdge("C", "D", 0.2);
        graph.addEdge("A", "D", 0.5);

        // O caminho com mais arcos deve vencer o arco direto pelo menor custo.
        String report = graph.dijkstra(graph, "A", "D");
        checkDijkstraResult(report, "A --> B --> C --> D", 0.3);
        checkDijkstraResult(graph.dijkstra(graph, "A", "B"), "A --> B", 0.0);
        checkDijkstraResult(graph.dijkstra(graph, "A", "A"), "A", 0.0);

        // A orientação impede voltar de D até A.
        String reverseReport = graph.dijkstra(graph, "D", "A");
        check(reverseReport.contains("\nCaminho: inexistente\n"),
                "Os arcos não devem permitir um caminho de D até A.");
        check(reverseReport.contains("\nCusto total: infinito"),
                "O custo do caminho inexistente deve ser infinito.");

        // E está isolado e deve permanecer inalcançável.
        String isolatedReport = graph.dijkstra(graph, "A", "E");
        check(isolatedReport.contains("\nCaminho: inexistente\n"), "E deve ser inalcançável.");
        check(isolatedReport.contains("\nCusto total: infinito"), "O custo até E deve ser infinito.");
        check(isolatedReport.contains("Não existe caminho de A até E."),
                "O relatório deve identificar a origem e o destino sem caminho.");
        check(!isolatedReport.contains("| Fechado: E\n"),
                "O vértice inalcançável deve permanecer aberto.");

        check(graph.getOrder() == 5 && graph.getNumEdges() == 4,
                "Dijkstra não deve alterar a quantidade de vértices ou arcos.");
        check(!graph.hasEdge("B", "A") && !graph.hasEdge("D", "A"),
                "Dijkstra não deve criar arcos inversos.");
        check(graph.getWeight("A", "B") == 0.0 && graph.getWeight("A", "D") == 0.5,
                "Dijkstra deve preservar os pesos originais.");

        showDijkstra("TESTE 9 - Dijkstra em grafo orientado",
                "A --> B --> C --> D; custo aproximadamente 0.3; peso zero permitido; "
                        + "A até A custa zero; D até A e A até E não possuem caminho.", report);
    }

    private static void testKruskalConnectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C", "D");
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("A", "C", 5.0);
        graph.addEdge("C", "D", 3.0);
        graph.addEdge("B", "D", 10.0);

        String report = graph.kruskal();
        checkPrimResult(report, 3, 6.0);
        checkPrimResult(graph.prim(), 3, 6.0);
        check(report.contains("A -- B (peso 1.0)"), "Kruskal deve escolher A-B.");
        check(report.contains("B -- C (peso 2.0)"), "Kruskal deve escolher B-C.");
        check(report.contains("C -- D (peso 3.0)"), "Kruskal deve escolher C-D.");
        check(!report.contains("(peso 10.0)"), "Kruskal não deve escolher a aresta cara.");
        check(graph.getNumEdges() == 5 && graph.getWeight("B", "D") == 10.0,
                "Kruskal deve preservar o grafo original.");
        showPrim("TESTE 16 - Kruskal em grafo conexo", "três arestas; peso 6.0; mesmo peso de Prim.", report);
    }

    private static void testKruskalTiedWeights() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C", "D");
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 1.0);
        graph.addEdge("B", "D", 1.0);
        graph.addEdge("C", "D", 1.0);

        String report = graph.kruskal();
        checkPrimResult(report, 3, 3.0);
        checkPrimResult(graph.prim(), 3, 3.0);
        showPrim("TESTE 17 - Kruskal com empate", "três arestas; peso 3.0 em ambos os algoritmos.", report);
    }

    private static void testKruskalLoopAndParallelEdge() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C");
        graph.addEdge("A", "A", -100.0);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);

        try {
            graph.addEdge("A", "B", 50.0);
            throw new AssertionError("Arestas paralelas devem continuar proibidas.");
        } catch (IllegalArgumentException exception) {
            check(exception.getMessage().contains("não permite multiarestas"),
                    "A mensagem deve explicar a restrição de multiarestas.");
        }

        String report = graph.kruskal();
        checkPrimResult(report, 2, 3.0);
        checkPrimResult(graph.prim(), 2, 3.0);
        check(!report.contains("(peso -100.0)"), "Kruskal deve ignorar o laço.");
        check(graph.getNumEdges() == 3, "A tentativa de aresta paralela não deve alterar o grafo.");
        showPrim("TESTE 18 - Kruskal com laço e aresta paralela recusada",
                "laço ignorado; paralela recusada; peso 3.0.", report);
    }

    private static void testKruskalDisconnectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A", "B", "C", "D", "E");
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 4.0);

        String report = graph.kruskal();
        checkPrimResult(report, 2, 5.0);
        checkPrimResult(graph.prim(), 2, 5.0);
        check(report.contains("Floresta geradora mínima com 3 componentes."),
                "Kruskal deve identificar os três componentes.");
        check(report.contains("Nenhuma aresta (vértice isolado)."),
                "Kruskal deve exibir o vértice isolado.");
        showPrim("TESTE 19 - Kruskal em grafo desconexo", "três componentes; peso 5.0.", report);
    }

    private static void testKruskalDirectedGraph() {
        AdjMatrixGraph<Double> graph = createGraph(true, true, "A", "B");
        graph.addEdge("A", "B", 1.0);

        try {
            graph.kruskal();
            throw new AssertionError("Kruskal não deveria aceitar um grafo orientado.");
        } catch (IllegalArgumentException exception) {
            check(exception.getMessage().contains("não se aplica a grafos orientados"),
                    "Kruskal deve informar que a funcionalidade não se aplica.");
        }
        System.out.println("\nTESTE 20 - Kruskal em grafo orientado: rejeição correta.");
    }

    private static void testKruskalSingleNodeGraph() {
        AdjMatrixGraph<Double> graph = createGraph(false, true, "A");
        String report = graph.kruskal();
        checkPrimResult(report, 0, 0.0);
        checkPrimResult(graph.prim(), 0, 0.0);
        check(report.contains("Nenhuma aresta (vértice isolado)."),
                "A AGM de um único vértice deve ser vazia.");
        showPrim("TESTE 21 - Kruskal com um vértice", "zero arestas; peso 0.0.", report);
    }

}
