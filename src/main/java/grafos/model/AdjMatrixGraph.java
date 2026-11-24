package grafos.model;

import java.util.*;
import java.util.regex.*;
import java.nio.file.*;

public class AdjMatrixGraph<T> {

    //lembrar de reaproveitar as cponstantes declaradas

    private static final double WITH_EDGE = 1.0;
    private static final Double WITHOUT_EDGE = null;

    private final boolean directed;
    private final boolean weighted;
    private int numNodes;
    private int numEdges;
    private Double[][] adjacencyMatrix;
    private final Map<String, Integer> nodeIndexes;
    private final List<Node<T>> nodes;

    public AdjMatrixGraph(List<Node<T>> nodes) {
        this(nodes, false, false);
    }

    public AdjMatrixGraph(List<Node<T>> nodes, boolean directed) {
        this(nodes, directed, false);
    }

    public AdjMatrixGraph(List<Node<T>> nodes, boolean directed, boolean weighted) {
        if (nodes == null) {
            throw new IllegalArgumentException("A lista de vértices não pode ser nula.");
        }

        this.directed = directed;
        this.weighted = weighted;
        this.nodes = new ArrayList<>();
        this.nodeIndexes = new HashMap<>();

        for (Node<T> node : nodes) {
            if (node == null) {
                throw new IllegalArgumentException("A lista não pode ter vértices nulos.");
            }

            if (this.nodeIndexes.containsKey(node.getId())) {
                throw new IllegalArgumentException("Id de vértice repetido: " + node.getId());
            }

            this.nodeIndexes.put(node.getId(), this.nodes.size());
            this.nodes.add(node);
        }

        this.numNodes = this.nodes.size();
        this.adjacencyMatrix = new Double[this.numNodes][this.numNodes];
    }

    public void addEdge(String src, String dest) {
        if (this.weighted) {
            throw new IllegalArgumentException("Informe o peso da aresta para um grafo ponderado.");
        }

        insertEdge(src, dest, WITH_EDGE);
    }

    public void addEdge(String src, String dest, double weight) {
        if (!this.weighted) {
            throw new IllegalArgumentException("Não é permitido informar peso em um grafo não ponderado.");
        }

        if (Double.isNaN(weight) || Double.isInfinite(weight)) {
            throw new IllegalArgumentException("O peso deve ser um número finito.");
        }

        insertEdge(src, dest, weight);
    }

    private void insertEdge(String src, String dest, double weight) {
        int srcIndex = getNodeIndex(src);
        int destIndex = getNodeIndex(dest);

        if (this.adjacencyMatrix[srcIndex][destIndex] != WITHOUT_EDGE) {
            throw new IllegalArgumentException("Representação por Matriz de Adjacências não permite multiarestas / multigrafo");
        }

        this.adjacencyMatrix[srcIndex][destIndex] = weight;

        if (!this.directed) {
            this.adjacencyMatrix[destIndex][srcIndex] = weight;
        }

        ++this.numEdges;
    }

    public void removeEdge(String src, String dest) {
        int srcIndex = getNodeIndex(src);
        int destIndex = getNodeIndex(dest);

        if (this.adjacencyMatrix[srcIndex][destIndex] != WITHOUT_EDGE) {
            this.adjacencyMatrix[srcIndex][destIndex] = WITHOUT_EDGE;

            if (!this.directed) {
                this.adjacencyMatrix[destIndex][srcIndex] = WITHOUT_EDGE;
            }

            --this.numEdges;
        }
    }

    public void removeNode(String id) {
        int index = getNodeIndex(id);
        int removedEdges = 0;

        // conto as arestas que somem junto com o vértice antes de mexer na matriz
        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[index][i] != WITHOUT_EDGE) {
                ++removedEdges;
            }
        }

        // no orientado a coluna também conta, menos o laço que já foi contado na linha
        if (this.directed) {
            for (int i = 0; i < this.numNodes; i++) {
                if (i != index && this.adjacencyMatrix[i][index] != WITHOUT_EDGE) {
                    ++removedEdges;
                }
            }
        }

        Double[][] reducedMatrix = new Double[this.numNodes - 1][this.numNodes - 1];
        int reducedRow = 0;

        for (int i = 0; i < this.numNodes; i++) {
            if (i == index) {
                continue;
            }

            int reducedColumn = 0;

            for (int j = 0; j < this.numNodes; j++) {
                if (j == index) {
                    continue;
                }

                reducedMatrix[reducedRow][reducedColumn] = this.adjacencyMatrix[i][j];
                ++reducedColumn;
            }

            ++reducedRow;
        }

        this.nodes.remove(index);
        this.adjacencyMatrix = reducedMatrix;
        this.numEdges -= removedEdges;
        --this.numNodes;

        // os vértices andam de posição quando um sai, então refaço o mapa de índices
        this.nodeIndexes.clear();

        for (int i = 0; i < this.numNodes; i++) {
            this.nodeIndexes.put(this.nodes.get(i).getId(), i);
        }
    }

    public boolean hasEdge(String src, String dest) {
        int srcIndex = getNodeIndex(src);
        int destIndex = getNodeIndex(dest);

        return this.adjacencyMatrix[srcIndex][destIndex] != WITHOUT_EDGE;
    }

    public double getWeight(String src, String dest) {
        if (!this.weighted) {
            throw new IllegalArgumentException("O grafo não é ponderado.");
        }

        int srcIndex = getNodeIndex(src);
        int destIndex = getNodeIndex(dest);

        if (this.adjacencyMatrix[srcIndex][destIndex] == WITHOUT_EDGE) {
            throw new IllegalArgumentException("A aresta informada não existe.");
        }

        return this.adjacencyMatrix[srcIndex][destIndex];
    }

    public Node<T> getNode(String id) {
        return this.nodes.get(getNodeIndex(id));
    }

    public Node<T> getNode(int index) {
        return this.nodes.get(index);
    }

    public List<Node<T>> getNodes() {
        return Collections.unmodifiableList(this.nodes);
    }

    public List<Node<T>> getNeighbors(String id) {
        int index = getNodeIndex(id);
        List<Node<T>> neighbors = new ArrayList<>();

        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[index][i] != WITHOUT_EDGE) {
                neighbors.add(this.nodes.get(i));
            }
        }

        return neighbors;
    }

    // Retorna os predecessores: origens das arestas que chegam ao vértice.

    public List<Node<T>> getPredecessors(String id) {
        int index = getNodeIndex(id);
        List<Node<T>> predecessors = new ArrayList<>();

        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[i][index] != WITHOUT_EDGE) {
                predecessors.add(this.nodes.get(i));
            }
        }

        return predecessors;
    }

    public boolean isDirected() {
        return this.directed;
    }

    public boolean isWeighted() {
        return this.weighted;
    }

    public int countNeighbors(String id) {
        return getOutDegree(id);
    }

    //  reotorna a ordem do grafo
    public int getOrder() {
        return this.numNodes;
    }

    public int getOutDegree(String id) {
        int index = getNodeIndex(id);
        int degree = 0;

        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[index][i] != WITHOUT_EDGE) {
                ++degree;
            }
        }

        return degree;
    }

    public int getInDegree(String id) {
        int index = getNodeIndex(id);
        int degree = 0;

        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[i][index] != WITHOUT_EDGE) {
                ++degree;
            }
        }

        return degree;
    }

    /* Calcula o grau do vértice;
     Considerei que cada laço contribui com dois.
     Verificar depois nos slides*/
    public int getDegree(String id) {
        if (this.directed) {
            return getInDegree(id) + getOutDegree(id);
        }

        int index = getNodeIndex(id);
        int degree = 0;

        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[index][i] != WITHOUT_EDGE) {
                degree += i == index ? 2 : 1;
            }
        }

        return degree;
    }

    public int getNumNodes() {
        return this.numNodes;
    }

    public int getNumEdges() {
        return this.numEdges;
    }

    public void printGraph() {
        int width = 2;

        for (Node<T> node : this.nodes) {
            width = Math.max(width, node.getId().length() + 1);
        }

        for (int i = 0; i < this.numNodes; i++) {
            for (int j = 0; j < this.numNodes; j++) {
                width = Math.max(width, getMatrixDisplayValue(i, j).length() + 1);
            }
        }

        String format = "%" + width + "s";
        System.out.printf(format, "");

        for (Node<T> node : this.nodes) {
            System.out.printf(format, node.getId());
        }

        System.out.println();

        for (int i = 0; i < this.numNodes; i++) {
            System.out.printf(format, this.nodes.get(i).getId());

            for (int j = 0; j < this.numNodes; j++) {
                System.out.printf(format, getMatrixDisplayValue(i, j));
            }

            System.out.println();
        }
    }

    private String getMatrixDisplayValue(int row, int column) {
        Double value = this.adjacencyMatrix[row][column];

        if (value == WITHOUT_EDGE) {
            return this.weighted ? "-" : "0";
        }

        return this.weighted ? value.toString() : "1";
    }

    protected int getNodeIndex(String id) {
        Integer index = this.nodeIndexes.get(id);

        if (index == null) {
            throw new IllegalArgumentException("Vértice não encontrado: " + id);
        }

        return index;
    }

    public void addNode(Node<T> node) {
        if (node == null) {
            throw new IllegalArgumentException("O vértice não pode ser nulo.");
        }

        if (this.nodeIndexes.containsKey(node.getId())) {
            throw new IllegalArgumentException("ID de vértice repetido: " + node.getId());
        }

        Double[][] expandedMatrix = new Double[this.numNodes + 1][this.numNodes + 1];

        for (int i = 0; i < this.numNodes; i++) {
            System.arraycopy(this.adjacencyMatrix[i], 0, expandedMatrix[i], 0, this.numNodes);
        }

        this.nodeIndexes.put(node.getId(), this.numNodes);
        this.nodes.add(node);
        this.adjacencyMatrix = expandedMatrix;
        ++this.numNodes;
    }

    public boolean hasLoops() {
        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[i][i] != WITHOUT_EDGE) {
                return true;
            }
        }
        return false;
    }

    public boolean isSimple() {
        return !hasLoops();
    }

    // Dijkstra só vale para pesos não negativos, então preciso conferir antes de rodar
    public boolean hasNegativeWeights() {
        if (!this.weighted) {
            return false;
        }

        for (int i = 0; i < this.numNodes; i++) {
            for (int j = 0; j < this.numNodes; j++) {
                if (this.adjacencyMatrix[i][j] != WITHOUT_EDGE && this.adjacencyMatrix[i][j] < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isRegular() {
        if (this.numNodes == 0) {
            return true;
        }
        String first = this.nodes.get(0).getId();
        int degree = getDegree(first);
        int inDegree = getInDegree(first);
        int outDegree = getOutDegree(first);

        for (Node<T> node : this.nodes) {
            String id = node.getId();
            if (this.directed) {
                if (getInDegree(id) != inDegree || getOutDegree(id) != outDegree) {
                    return false;
                }
            } else if (getDegree(id) != degree) {
                return false;
            }
        }
        return true;
    }

    public boolean isComplete() {
        if (!isSimple()) {
            return false;
        }
        for (int i = 0; i < this.numNodes; i++) {
            for (int j = 0; j < this.numNodes; j++) {
                if (i != j && this.adjacencyMatrix[i][j] == WITHOUT_EDGE) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBipartite() {
        if (this.directed) {
            throw new IllegalArgumentException("Não se aplica nesta entrega.");
        }
        int[] colors = new int[this.numNodes];
        Arrays.fill(colors, -1);
        Queue<Integer> queue = new ArrayDeque<>();

        for (int start = 0; start < this.numNodes; start++) {
            if (colors[start] != -1) {
                continue;
            }
            colors[start] = 0;
            queue.add(start);

            while (!queue.isEmpty()) {
                int current = queue.remove();
                for (int next = 0; next < this.numNodes; next++) {
                    if (this.adjacencyMatrix[current][next] == WITHOUT_EDGE) {
                        continue;
                    }
                    if (colors[next] == -1) {
                        colors[next] = 1 - colors[current];
                        queue.add(next);
                    } else if (colors[next] == colors[current]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<Node<T>> getLoopNodes() {
        List<Node<T>> result = new ArrayList<>();
        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[i][i] != WITHOUT_EDGE) {
                result.add(this.nodes.get(i));
            }
        }
        return result;
    }

    public List<Node<T>> getLeafNodes() {
        if (this.directed) {
            throw new IllegalArgumentException("A classificação de folhas se aplica apenas a grafos não orientados nesta entrega.");
        }
        List<Node<T>> result = new ArrayList<>();
        for (Node<T> node : this.nodes) {
            if (getDegree(node.getId()) == 1) {
                result.add(node);
            }
        }
        return result;
    }

    public List<Node<T>> getIsolatedNodes() {
        List<Node<T>> result = new ArrayList<>();
        for (Node<T> node : this.nodes) {
            if (getDegree(node.getId()) == 0) {
                result.add(node);
            }
        }
        return result;
    }

    private void requireDirected() {
        if (!this.directed) {
            throw new IllegalArgumentException("Esta operação exige um grafo orientado.");
        }
    }

    public boolean isSource(String id) {
        requireDirected();
        return getInDegree(id) == 0 && getOutDegree(id) > 0;
    }

    public boolean isSink(String id) {
        requireDirected();
        return getOutDegree(id) == 0 && getInDegree(id) > 0;
    }

    public List<Node<T>> getSources() {
        requireDirected();
        List<Node<T>> result = new ArrayList<>();
        for (Node<T> node : this.nodes) {
            if (isSource(node.getId())) {
                result.add(node);
            }
        }
        return result;
    }

    public List<Node<T>> getSinks() {
        requireDirected();
        List<Node<T>> result = new ArrayList<>();
        for (Node<T> node : this.nodes) {
            if (isSink(node.getId())) {
                result.add(node);
            }
        }
        return result;
    }

    public List<Node<T>> getDirectTransitiveClosure(String id) {
        return getTransitiveClosure(id, false);
    }

    public List<Node<T>> getInverseTransitiveClosure(String id) {
        return getTransitiveClosure(id, true);
    }

    private List<Node<T>> getTransitiveClosure(String id, boolean inverse) {
        requireDirected();
        int start = getNodeIndex(id);
        boolean[] reached = new boolean[this.numNodes];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            int current = queue.remove();
            for (int next = 0; next < this.numNodes; next++) {
                boolean connected = inverse ? this.adjacencyMatrix[next][current] != WITHOUT_EDGE : this.adjacencyMatrix[current][next] != WITHOUT_EDGE;
                if (connected && !reached[next]) {
                    reached[next] = true;
                    queue.add(next);
                }
            }
        }

        List<Node<T>> result = new ArrayList<>();
        for (int i = 0; i < this.numNodes; i++) {
            if (reached[i]) {
                result.add(this.nodes.get(i));
            }
        }
        return result;
    }

    // Valida o grafo recebido e calcula os menores caminhos sem alterar a matriz original.
    public String dijkstra(AdjMatrixGraph<T> graph, String src, String dest) {
        if (graph == null) {
            throw new IllegalArgumentException("O grafo não pode ser nulo.");
        }

        int sourceIndex = graph.getNodeIndex(src);
        int destinationIndex = graph.getNodeIndex(dest);

        if (!graph.isWeighted()) {
            throw new IllegalArgumentException("O algoritmo de Dijkstra exige um grafo ponderado.");
        }

        if (graph.hasNegativeWeights()) {
            throw new IllegalArgumentException("O grafo possui pesos negativos, então o algoritmo de Dijkstra não pode ser aplicado.");
        }

        Double[][] distancesMatrix = createDistanceMatrix(graph);
        int order = graph.getOrder();
        Double[] distances = new Double[order];
        boolean[] closed = new boolean[order];
        int[] predecessors = new int[order];

        Arrays.fill(distances, Double.POSITIVE_INFINITY);
        Arrays.fill(predecessors, -1);
        distances[sourceIndex] = 0.0;

        StringBuilder result = new StringBuilder("\n=== DIJKSTRA ===\n");
        result.append("Origem: ").append(src)
                .append("\nDestino: ").append(dest)
                .append("\nDesempate: ordem de inserção dos vértices.\n")
                .append("\n=== REGISTRO DA EXECUÇÃO ===\n")
                .append("Distâncias desde a origem, após atualizar os vizinhos em cada passo.\n");

        appendDijkstraStep(result, graph, 0, -1, distances, closed);
        int step = 1;

        while (true) {
            int current = selectClosestOpenVertex(distances, closed);

            // Não existem mais vértices abertos alcançáveis pela origem.
            if (current == -1) {
                break;
            }

            closed[current] = true;

            for (int next = 0; next < order; next++) {
                double weight = distancesMatrix[current][next];

                if (closed[next] || Double.isInfinite(weight)) {
                    continue;
                }

                double candidateDistance = distances[current] + weight;

                if (Double.isInfinite(candidateDistance)) {
                    throw new IllegalArgumentException("A soma dos custos ultrapassou o limite de double. Não foi possível concluir o cálculo.");
                }

                if (candidateDistance < distances[next]) {
                    distances[next] = candidateDistance;
                    predecessors[next] = current;
                }
            }

            appendDijkstraStep(result, graph, step, current, distances, closed);
            step++;
        }

        result.append("\n=== RESULTADO ===\n")
                .append("Origem: ").append(src)
                .append("\nDestino: ").append(dest);

        if (Double.isInfinite(distances[destinationIndex])) {
            result.append("\nCaminho: inexistente")
                    .append("\nCusto total: infinito ")
                    .append("\nNão existe caminho de ").append(src).append(" até ").append(dest).append(".\n");
            return result.toString();
        }

        result.append("\nCaminho: ").append(reconstructDijkstraPath(graph, destinationIndex, predecessors))
                .append("\nCusto total: ").append(distances[destinationIndex]).append("\n");
        return result.toString();
    }

    // Em caso de empate, mantém o primeiro vértice na ordem de inserção.
    private int selectClosestOpenVertex(Double[] distances, boolean[] closed) {
        int selected = -1;
        double shortestDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < distances.length; i++) {
            if (!closed[i] && distances[i] < shortestDistance) {
                selected = i;
                shortestDistance = distances[i];
            }
        }

        return selected;
    }

    // Registra o fechamento, todas as distâncias e os vértices ainda abertos.
    private void appendDijkstraStep(StringBuilder result, AdjMatrixGraph<T> graph,
                                    int step, int current, Double[] distances, boolean[] closed) {
        result.append("\nPasso ").append(step)
                .append(" | Fechado: ").append(current == -1 ? "nenhum (inicialização)" : graph.nodes.get(current).getId())
                .append("\nDistâncias: ");

        List<Node<T>> openNodes = new ArrayList<>();

        for (int i = 0; i < distances.length; i++) {
            if (i > 0) {
                result.append(" | ");
            }

            result.append(graph.nodes.get(i).getId()).append(" = ")
                    .append(Double.isInfinite(distances[i]) ? "infinito" : distances[i].toString());

            if (!closed[i]) {
                openNodes.add(graph.nodes.get(i));
            }
        }

        result.append("\nAbertos: ").append(formatNodes(openNodes)).append("\n");
    }

    // Percorre os predecessores do destino à origem e inverte a sequência.
    private String reconstructDijkstraPath(AdjMatrixGraph<T> graph, int destinationIndex, int[] predecessors) {
        List<String> path = new ArrayList<>();

        for (int current = destinationIndex; current != -1; current = predecessors[current]) {
            path.add(graph.nodes.get(current).getId());
        }

        Collections.reverse(path);
        return String.join(" --> ", path);
    }

    private String formatNodes(List<Node<T>> selected) {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < selected.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(selected.get(i).getId());
        }
        return result.append("]").toString();
    }

    protected Double[][] createDistanceMatrix(AdjMatrixGraph<T> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("O grafo não pode ser nulo.");
        }

        int order = graph.getOrder();
        Double[][] distances = new Double[order][order];

        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                if (i == j) {
                    distances[i][j] = 0.0;
                    continue;
                }

                Double weight = graph.adjacencyMatrix[i][j];

                if (weight != WITHOUT_EDGE) {
                    distances[i][j] = graph.isWeighted() ? weight : WITH_EDGE;
                } else {
                    distances[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }

        return distances;
    }

    //Prim vai crescendo a árvore a partir de um vértice, sempre pegando a aresta
    //mais barata que sai de quem já entrou para quem ainda está de fora.
    //Se o grafo for desconexo eu recomeço em cada componente, aí o resultado vira uma floresta.
    public String prim() {
        if (this.directed) {
            throw new IllegalArgumentException("A árvore geradora mínima não se aplica a grafos orientados.");
        }

        if (!this.weighted) {
            throw new IllegalArgumentException("A árvore geradora mínima exige um grafo ponderado.");
        }

        StringBuilder result = new StringBuilder("\n=== PRIM ===\n");
        result.append("Desempate: ordem de inserção dos vértices.\n");

        if (this.numNodes == 0) {
            result.append("\nO grafo não possui vértices.\n");
            return result.toString();
        }

        boolean[] inTree = new boolean[this.numNodes];
        StringBuilder components = new StringBuilder();
        double totalWeight = 0.0;
        int numComponents = 0;
        int numTreeEdges = 0;

        for (int start = 0; start < this.numNodes; start++) {
            if (inTree[start]) {
                continue;
            }

            ++numComponents;
            List<Integer> current = new ArrayList<>();
            current.add(start);
            inTree[start] = true;

            components.append("\nComponente ").append(numComponents)
                    .append(" (a partir de ").append(this.nodes.get(start).getId()).append("):\n");
            int componentEdges = 0;

            while (true) {
                int bestSrc = -1;
                int bestDest = -1;
                double bestWeight = 0.0;

                for (int i : current) {
                    for (int j = 0; j < this.numNodes; j++) {
                        // pulo o laço e quem já está na árvore
                        if (i == j || inTree[j] || this.adjacencyMatrix[i][j] == WITHOUT_EDGE) {
                            continue;
                        }

                        double weight = this.adjacencyMatrix[i][j];

                        if (bestSrc == -1 || weight < bestWeight) {
                            bestSrc = i;
                            bestDest = j;
                            bestWeight = weight;
                        }
                    }
                }

                // acabaram as arestas que saem deste componente
                if (bestSrc == -1) {
                    break;
                }

                inTree[bestDest] = true;
                current.add(bestDest);
                totalWeight += bestWeight;
                ++numTreeEdges;
                ++componentEdges;

                components.append(this.nodes.get(bestSrc).getId()).append(" -- ")
                        .append(this.nodes.get(bestDest).getId())
                        .append(" (peso ").append(bestWeight).append(")\n");
            }

            if (componentEdges == 0) {
                components.append("Nenhuma aresta (vértice isolado).\n");
            }
        }

        result.append("\n=== RESULTADO ===\n");
        result.append(numComponents == 1 ? "Árvore geradora mínima" : "Floresta geradora mínima")
                .append(" com ").append(numComponents)
                .append(numComponents == 1 ? " componente." : " componentes.").append("\n");
        result.append(components);
        result.append("\nTotal de arestas: ").append(numTreeEdges)
                .append("\nPeso total: ").append(totalWeight).append("\n");
        return result.toString();
    }


    public String kruskal() {
        if (this.directed) {
            throw new IllegalArgumentException("A árvore geradora mínima não se aplica a grafos orientados.");
        }

        if (!this.weighted) {
            throw new IllegalArgumentException("A árvore geradora mínima exige um grafo ponderado.");
        }

        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < this.numNodes; i++) {
            for (int j = i + 1; j < this.numNodes; j++) {
                if (this.adjacencyMatrix[i][j] != WITHOUT_EDGE) {
                    edges.add(new int[]{i, j});
                }
            }
        }

        edges.sort(Comparator.comparingDouble(edge -> this.adjacencyMatrix[edge[0]][edge[1]]));

        int[] parent = new int[this.numNodes];
        for (int i = 0; i < this.numNodes; i++) {
            parent[i] = i;
        }

        List<int[]> selectedEdges = new ArrayList<>();
        double totalWeight = 0.0;

        for (int[] edge : edges) {
            int sourceRoot = findKruskalRoot(parent, edge[0]);
            int destinationRoot = findKruskalRoot(parent, edge[1]);

            if (sourceRoot == destinationRoot) {
                continue;
            }

            parent[sourceRoot] = destinationRoot;
            selectedEdges.add(edge);
            totalWeight += this.adjacencyMatrix[edge[0]][edge[1]];
        }

        Map<Integer, List<Integer>> components = new LinkedHashMap<>();
        for (int i = 0; i < this.numNodes; i++) {
            int root = findKruskalRoot(parent, i);
            components.computeIfAbsent(root, key -> new ArrayList<>()).add(i);
        }

        int numComponents = components.size();
        StringBuilder result = new StringBuilder("\n=== KRUSKAL ===\n");
        result.append("Desempate: ordem de inserção dos vértices.\n")
                .append("\n=== RESULTADO ===\n")
                .append(numComponents == 1 ? "Árvore geradora mínima" : "Floresta geradora mínima")
                .append(" com ").append(numComponents)
                .append(numComponents == 1 ? " componente.\n" : " componentes.\n");

        int componentNumber = 0;
        for (List<Integer> component : components.values()) {
            ++componentNumber;
            result.append("\nComponente ").append(componentNumber)
                    .append(" (a partir de ").append(this.nodes.get(component.get(0)).getId()).append("):\n");
            int componentEdges = 0;
            for (int[] edge : selectedEdges) {
                if (findKruskalRoot(parent, edge[0]) == findKruskalRoot(parent, component.get(0))) {
                    result.append(this.nodes.get(edge[0]).getId()).append(" -- ")
                            .append(this.nodes.get(edge[1]).getId())
                            .append(" (peso ").append(this.adjacencyMatrix[edge[0]][edge[1]]).append(")\n");
                    ++componentEdges;
                }
            }
            if (componentEdges == 0) {
                result.append("Nenhuma aresta (vértice isolado).\n");
            }
        }

        result.append("\nTotal de arestas: ").append(selectedEdges.size())
                .append("\nPeso total: ").append(totalWeight).append("\n");
        return result.toString();
    }

    private int findKruskalRoot(int[] parent, int vertex) {
        if (parent[vertex] != vertex) {
            parent[vertex] = findKruskalRoot(parent, parent[vertex]);
        }
        return parent[vertex];
    }

    //Malgrange: pego um vértice, calculo o fecho direto e o inverso dele e cruzo os dois.
    //Quem está nos dois volta para o vértice e é alcançado por ele, então está no mesmo ciclo.
    //O próprio vértice entra sempre, porque os fechos daqui só incluem o início quando existe ciclo.
    //Deixei este método devolvendo as componentes para a parte da imagem poder pintar cada uma.
    public List<List<Node<T>>> getStronglyConnectedComponents() {
        if (!this.directed) {
            throw new IllegalArgumentException("O algoritmo de Malgrange não se aplica a grafos não orientados.");
        }

        boolean[] classified = new boolean[this.numNodes];
        List<List<Node<T>>> components = new ArrayList<>();

        for (int start = 0; start < this.numNodes; start++) {
            if (classified[start]) {
                continue;
            }

            List<Node<T>> component = new ArrayList<>();

            for (int i : intersectClosures(start, classified)) {
                classified[i] = true;
                component.add(this.nodes.get(i));
            }

            components.add(component);
        }

        return components;
    }

    private List<Integer> intersectClosures(int start, boolean[] classified) {
        String startId = this.nodes.get(start).getId();
        boolean[] inDirect = markClosure(getDirectTransitiveClosure(startId));
        boolean[] inInverse = markClosure(getInverseTransitiveClosure(startId));

        List<Integer> component = new ArrayList<>();

        for (int i = 0; i < this.numNodes; i++) {
            if (i == start || (inDirect[i] && inInverse[i] && !classified[i])) {
                component.add(i);
            }
        }

        return component;
    }

    private boolean[] markClosure(List<Node<T>> closure) {
        boolean[] marked = new boolean[this.numNodes];

        for (Node<T> node : closure) {
            marked[getNodeIndex(node.getId())] = true;
        }

        return marked;
    }

    public String malgrange() {
        if (!this.directed) {
            throw new IllegalArgumentException("O algoritmo de Malgrange não se aplica a grafos não orientados.");
        }

        StringBuilder result = new StringBuilder("\n=== MALGRANGE ===\n");
        result.append("Componentes fortemente conexas pela interseção do fecho direto com o inverso.\n");

        if (this.numNodes == 0) {
            result.append("\nO grafo não possui vértices.\n");
            return result.toString();
        }

        boolean[] classified = new boolean[this.numNodes];
        List<List<Integer>> components = new ArrayList<>();

        result.append("\n=== REGISTRO DA EXECUÇÃO ===\n");
        int iteration = 1;

        for (int start = 0; start < this.numNodes; start++) {
            if (classified[start]) {
                continue;
            }

            String startId = this.nodes.get(start).getId();
            List<Integer> component = intersectClosures(start, classified);

            result.append("\nIteração ").append(iteration).append(" | Vértice escolhido: ").append(startId)
                    .append("\nFecho direto (sucessores)  : ").append(formatNodes(getDirectTransitiveClosure(startId)))
                    .append("\nFecho inverso (antecessores): ").append(formatNodes(getInverseTransitiveClosure(startId)))
                    .append("\nComponente (interseção + o próprio vértice): ").append(formatIndexes(component));

            for (int i : component) {
                classified[i] = true;
            }

            components.add(component);
            result.append("\nRestam para processar: ").append(formatRemaining(classified)).append("\n");
            ++iteration;
        }

        result.append("\n=== RESULTADO ===\n");
        result.append("Total de componentes fortemente conexas: ").append(components.size()).append("\n");

        for (int i = 0; i < components.size(); i++) {
            result.append("S").append(i + 1).append(" = ").append(formatIndexes(components.get(i))).append("\n");
        }

        result.append(buildReducedGraph(components));
        return result.toString();
    }

    //Grafo reduzido: cada componente vira um super-vértice e só guardo os arcos que saem
    //de uma componente para outra. Os arcos de dentro da componente somem.
    private String buildReducedGraph(List<List<Integer>> components) {
        int[] componentOf = new int[this.numNodes];

        for (int c = 0; c < components.size(); c++) {
            for (int i : components.get(c)) {
                componentOf[i] = c;
            }
        }

        StringBuilder arcs = new StringBuilder();
        boolean[][] used = new boolean[components.size()][components.size()];
        int numArcs = 0;

        for (int i = 0; i < this.numNodes; i++) {
            for (int j = 0; j < this.numNodes; j++) {
                if (this.adjacencyMatrix[i][j] == WITHOUT_EDGE) {
                    continue;
                }

                int from = componentOf[i];
                int to = componentOf[j];

                if (from != to && !used[from][to]) {
                    used[from][to] = true;
                    ++numArcs;
                    arcs.append("S").append(from + 1).append(" -> S").append(to + 1)
                            .append("   (").append(formatIndexes(components.get(from)))
                            .append(" -> ").append(formatIndexes(components.get(to))).append(")\n");
                }
            }
        }

        StringBuilder result = new StringBuilder("\n=== GRAFO REDUZIDO Gr(S, W) ===\n");
        result.append("Super-vértices: ").append(components.size())
                .append(" | Arcos: ").append(numArcs).append("\n");

        if (numArcs == 0) {
            result.append("Nenhum arco entre componentes.\n");
        } else {
            result.append(arcs);
        }

        return result.toString();
    }

    private String formatIndexes(List<Integer> indexes) {
        StringBuilder result = new StringBuilder("{");

        for (int i = 0; i < indexes.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(this.nodes.get(indexes.get(i)).getId());
        }

        return result.append("}").toString();
    }

    private String formatRemaining(boolean[] classified) {
        List<Node<T>> remaining = new ArrayList<>();

        for (int i = 0; i < this.numNodes; i++) {
            if (!classified[i]) {
                remaining.add(this.nodes.get(i));
            }
        }

        return remaining.isEmpty() ? "[] (todos já classificados)" : formatNodes(remaining);
    }

    //BFS: parto do vértice inicial e uso uma fila, então visito primeiro todos os vizinhos
    //dele, depois os vizinhos dos vizinhos, e assim por diante.
    //Guardo quem descobriu cada vértice (parent) para conseguir montar a árvore de busca.
    public String bfs(String startId) {
        int start = getNodeIndex(startId);
        List<Integer> order = new ArrayList<>();
        int[] parent = breadthFirstSearch(start, order);
        return formatSearchResult("BUSCA EM LARGURA (BFS)", startId, order, parent);
    }

    private int[] breadthFirstSearch(int start, List<Integer> order) {
        int[] parent = new int[this.numNodes];
        boolean[] visited = new boolean[this.numNodes];
        Arrays.fill(parent, -1);

        Queue<Integer> queue = new ArrayDeque<>();
        visited[start] = true;
        queue.add(start);

        while (!queue.isEmpty()) {
            int current = queue.remove();
            order.add(current);

            for (int next = 0; next < this.numNodes; next++) {
                // só sigo o arco no sentido certo; no não orientado a matriz já é espelhada
                if (!visited[next] && this.adjacencyMatrix[current][next] != WITHOUT_EDGE) {
                    visited[next] = true;
                    parent[next] = current;
                    queue.add(next);
                }
            }
        }

        return parent;
    }

    //DFS: em vez de fila, vou o mais fundo possível por um caminho e só volto quando acabam os vizinhos.
    //Fiz recursivo porque fica mais fácil de ler: visito o vértice e já desço no primeiro vizinho livre.
    public String dfs(String startId) {
        int start = getNodeIndex(startId);
        List<Integer> order = new ArrayList<>();
        int[] parent = depthFirstSearch(start, order);
        return formatSearchResult("BUSCA EM PROFUNDIDADE (DFS)", startId, order, parent);
    }

    private int[] depthFirstSearch(int start, List<Integer> order) {
        int[] parent = new int[this.numNodes];
        boolean[] visited = new boolean[this.numNodes];
        Arrays.fill(parent, -1);
        visitDepthFirst(start, visited, parent, order);
        return parent;
    }

    private void visitDepthFirst(int current, boolean[] visited, int[] parent, List<Integer> order) {
        visited[current] = true;
        order.add(current);

        for (int next = 0; next < this.numNodes; next++) {
            if (!visited[next] && this.adjacencyMatrix[current][next] != WITHOUT_EDGE) {
                parent[next] = current;
                visitDepthFirst(next, visited, parent, order);
            }
        }
    }

    //Deixei público para a parte da imagem conseguir destacar as arestas que a busca percorreu.
    //Cada item é {origem, destino}, na ordem em que a busca descobriu o vértice.
    public List<String[]> getSearchTreeEdges(String startId, boolean depthFirst) {
        int start = getNodeIndex(startId);
        List<Integer> order = new ArrayList<>();
        int[] parent = depthFirst ? depthFirstSearch(start, order) : breadthFirstSearch(start, order);
        List<String[]> edges = new ArrayList<>();

        for (int v : order) {
            if (parent[v] != -1) {
                edges.add(new String[]{this.nodes.get(parent[v]).getId(), this.nodes.get(v).getId()});
            }
        }

        return edges;
    }

    private String formatSearchResult(String title, String startId, List<Integer> order, int[] parent) {
        StringBuilder result = new StringBuilder("\n=== ").append(title).append(" ===\n");
        result.append("Vértice inicial: ").append(startId)
                .append("\nDesempate: ordem de inserção dos vértices.\n");

        result.append("\n=== RESULTADO ===\nOrdem de visita: ");

        for (int i = 0; i < order.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(i + 1).append("º ").append(this.nodes.get(order.get(i)).getId());
        }

        String arrow = this.directed ? " -> " : " -- ";
        StringBuilder treeEdges = new StringBuilder();

        for (int v : order) {
            if (parent[v] != -1) {
                if (treeEdges.length() > 0) {
                    treeEdges.append(", ");
                }
                treeEdges.append(this.nodes.get(parent[v]).getId()).append(arrow).append(this.nodes.get(v).getId());
            }
        }

        result.append("\nArestas da árvore de busca: ")
                .append(treeEdges.length() == 0 ? "nenhuma" : treeEdges.toString());

        boolean[] reached = new boolean[this.numNodes];
        for (int v : order) {
            reached[v] = true;
        }

        List<Node<T>> unreached = new ArrayList<>();
        for (int i = 0; i < this.numNodes; i++) {
            if (!reached[i]) {
                unreached.add(this.nodes.get(i));
            }
        }

        result.append("\nVértices não alcançados a partir de ").append(startId).append(": ")
                .append(unreached.isEmpty() ? "nenhum, todos foram alcançados" : formatNodes(unreached))
                .append("\n");
        return result.toString();
    }

    public String getDegreesInformation() {
        StringBuilder result = new StringBuilder();
        if (this.numNodes == 0) {
            result.append("O grafo não possui vértices.\n");
        }
        for (Node<T> node : this.nodes) {
            String id = node.getId();
            result.append("Vértice ").append(id);
            if (this.directed) {
                result.append(": grau de entrada ").append(getInDegree(id))
                        .append(", grau de saída ").append(getOutDegree(id));
            } else {
                result.append(": grau ").append(getDegree(id));
            }
            result.append("\n");
            if (this.directed) {
                result.append("  Sucessores: ").append(formatNodes(getNeighbors(id)))
                        .append("; Antecessores: ").append(formatNodes(getPredecessors(id))).append("\n");
            }
        }

        result.append("Laços: ");
        List<Node<T>> loopNodes = getLoopNodes();
        if (loopNodes.isEmpty()) {
            result.append("[]");
        } else {
            for (int i = 0; i < loopNodes.size(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                String id = loopNodes.get(i).getId();
                result.append(id).append(" → ").append(id);
            }
        }
        result.append("\nVértices isolados: ").append(formatNodes(getIsolatedNodes()));
        if (this.directed) {
            result.append("\nFontes: ").append(formatNodes(getSources()));
            result.append("\nSumidouros: ").append(formatNodes(getSinks()));
        } else {
            result.append("\nVértices folha ou terminais: ").append(formatNodes(getLeafNodes()));
        }
        return result.toString();
    }

    private String formatBoolean(boolean value) {
        return value ? "Sim" : "Não";
    }

    public String getGraphInformation() {
        return getGraphInformation(null);
    }

    public String getGraphInformation(String closureNodeId) {
        if (this.directed && closureNodeId != null) {
            getNodeIndex(closureNodeId);
        }
        StringBuilder result = new StringBuilder("\n=== INFORMAÇÕES DO GRAFO ===\n");
        result.append("\n1.6.1 — Ordem: ").append(getOrder())
                .append("\nTamanho: ").append(this.numEdges)
                .append(this.directed ? " arcos" : " arestas")
                .append("\nTipo: ").append(this.directed ? "Orientado" : "Não orientado")
                .append(this.weighted ? " e ponderado" : " e não ponderado");
        result.append("\n\n1.6.2 — Grau dos Vértices:\n").append(getDegreesInformation());
        //Pelos slides, simples é não ter laço nem aresta múltipla, e multigrafo é ter aresta múltipla.
        //Como a matriz não guarda duas arestas no mesmo par, aqui o grafo só deixa de ser simples por causa de laço.
        //obs: Lembrar de conferir então depois sobre o kruskal <<<<
        result.append("\n\n1.6.3 — Simples - ").append(isSimple() ? "Sim" : "Não -> pois possui laço")
                .append("\n1.6.3 — Multigrafo - Não -> a matriz de adjacências não permite arestas múltiplas");
        result.append("\n1.6.4 — Regular - ").append(formatBoolean(isRegular()));
        result.append("\n1.6.5 — Completo - ").append(formatBoolean(isComplete()));
        result.append("\n1.6.6 — Bipartido - ")
                .append(this.directed ? "Não se aplica nesta entrega" : formatBoolean(isBipartite()));
        result.append("\n\n1.6.7 — Fontes e sumidouros:\n");
        if (this.directed) {
            result.append("Fontes: ").append(formatNodes(getSources()))
                    .append("\nSumidouros: ").append(formatNodes(getSinks()));
        } else {
            result.append("Não se aplica a grafos não orientados.");
        }
        result.append("\n\n1.6.8 — Fecho Transitivo Direto e Inverso:\n");
        if (!this.directed) {
            result.append("Não se aplica a grafos não orientados.");
        } else if (this.numNodes == 0) {
            result.append("O grafo não possui vértices.");
        } else if (closureNodeId == null) {
            result.append("Informe um vértice para consultar os fechos transitivos.");
        } else {
            result.append("Vértice: ").append(closureNodeId)
                    .append("\nDireto: ").append(formatNodes(getDirectTransitiveClosure(closureNodeId)))
                    .append("\nInverso: ").append(formatNodes(getInverseTransitiveClosure(closureNodeId)));
        }
        return result.toString();
    }

    //Para gerar a representação DOT,
    public String toDotString(Map<String, String> nodeColors) {
        StringBuilder dot = new StringBuilder();
        //mais fácil com string buikder

        dot.append(this.directed ? "digraph G {\n" : "graph G {\n");
        dot.append("    graph [weighted=").append(this.weighted).append("];\n");

        // Adicionar os nós, vértices isolados tbm
        for (Node<T> node : this.nodes) {
            String id = node.getId();
            dot.append("    ").append(dotQuote(id));

            if (nodeColors != null && nodeColors.containsKey(id)) {
                dot.append(" [style=filled, fillcolor=").append(nodeColors.get(id)).append(", value=\"").append(dotEscape(String.valueOf(node.getValue()))).append("\"]");
            }
            if (nodeColors == null || !nodeColors.containsKey(id)) {
                dot.append(" [value=\"").append(dotEscape(String.valueOf(node.getValue()))).append("\"]");
            }
            dot.append(";\n");
        }

        // Adiciona as arestas/arcos
        boolean[][] visited = new boolean[this.numNodes][this.numNodes];
        String edgeOperator = this.directed ? " -> " : " -- ";

        for (int i = 0; i < this.numNodes; i++) {
            for (int j = 0; j < this.numNodes; j++) {
                if (this.adjacencyMatrix[i][j] == WITHOUT_EDGE) {
                    continue;
                }

                // não precisa visitar duas vezes a mesma posição nos orientados
                if (!this.directed && visited[j][i]) {
                    continue;
                }

                visited[i][j] = true;

                String srcId = this.nodes.get(i).getId();
                String destId = this.nodes.get(j).getId();
                double weight = this.adjacencyMatrix[i][j];

                dot.append("    ").append(dotQuote(srcId)).append(edgeOperator).append(dotQuote(destId));

                if (this.weighted) {
                    dot.append(" [label=\"").append(weight).append("\"]");
                }
                dot.append(";\n");
            }
        }

        dot.append("}\n");
        return dot.toString();
    }


    public String toDotString() {
        return toDotString(null);
    }

    //limpa os possíveis caracteres que podem dar problemas nos ids dos grafos
    private static String dotEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }


    private static String dotQuote(String value) {
        return "\"" + dotEscape(value) + "\"";
    }

    //usando nio
    public void saveToDot(String filePath) throws java.io.IOException {
        java.nio.file.Path path = java.nio.file.Path.of(filePath);
        java.nio.file.Files.writeString(path, toDotString(), java.nio.charset.StandardCharsets.UTF_8);
    }

    // Lê o subconjunto Dot

    public static AdjMatrixGraph<String> loadFromDot(String filePath) throws java.io.IOException {
        String content = Files.readString(java.nio.file.Path.of(filePath), java.nio.charset.StandardCharsets.UTF_8);
        Matcher header = Pattern.compile("(?s)^\\s*(digraph|graph)\\s+(?:[A-Za-z_][A-Za-z_0-9]*|\\\"(?:\\\\.|[^\\\"\\\\])*\\\")?\\s*\\{").matcher(content);

        if (!header.find()) throw new IllegalArgumentException("Cabeçalho DOT inválido.");
        boolean directed = header.group(1).equals("digraph");

        int end = content.lastIndexOf('}');
        if (end < header.end() || !content.substring(end + 1).trim().isEmpty()) throw new IllegalArgumentException("Estrutura DOT inválida.");
        String body = content.substring(header.end(), end);
        Pattern token = Pattern.compile("\\s*(\\\"(?:\\\\.|[^\\\"\\\\])*\\\"|[A-Za-z_][A-Za-z_0-9]*|[0-9]+|->|--|\\[|\\]|=|,|;)");
        ArrayList<String> tokens = new ArrayList<>();
        int position = 0;
        while (position < body.length()) {
            Matcher matcher = token.matcher(body);
            matcher.region(position, body.length());
            if (!matcher.lookingAt()) {
                if (body.substring(position).trim().isEmpty()) break;
                throw new IllegalArgumentException("Conteúdo DOT inválido na posição " + position + ".");
            }
            tokens.add(matcher.group(1));
            position = matcher.end();
        }
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        List<String[]> edges = new ArrayList<>();
        boolean weighted = false;
        int i = 0;
        while (i < tokens.size()) {
            String src = dotUnquote(tokens.get(i++));
            if (src.equals(";") || src.equals("[") || src.equals("]") || src.equals(",") || src.equals("=")) throw new IllegalArgumentException("Declaração DOT inválida.");
            if (!src.equals("graph")) values.putIfAbsent(src, src);
            String dest = null;
            if (i < tokens.size() && (tokens.get(i).equals("->") || tokens.get(i).equals("--"))) {
                String op = tokens.get(i++);
                if (!op.equals(directed ? "->" : "--") || i == tokens.size()) throw new IllegalArgumentException("Operador de aresta incompatível com o grafo.");
                dest = dotUnquote(tokens.get(i++));
                values.putIfAbsent(dest, dest);
            }
            String label = null;
            String value = null;
            String weightedFlag = null;
            if (i < tokens.size() && tokens.get(i).equals("[")) {
                i++;
                while (i < tokens.size() && !tokens.get(i).equals("]")) {
                    if (i + 2 >= tokens.size() || !tokens.get(i + 1).equals("=")) throw new IllegalArgumentException("Atributo DOT inválido.");
                    String key = tokens.get(i);
                    String attribute = dotUnquote(tokens.get(i + 2));
                    if (key.equals("label")) label = attribute;
                    if (key.equals("value")) value = attribute;
                    if (key.equals("weighted")) weightedFlag = attribute;
                    i += 3;
                    if (i < tokens.size() && tokens.get(i).equals(",")) i++;
                    else if (i < tokens.size() && !tokens.get(i).equals("]")) throw new IllegalArgumentException("Separador de atributos inválido.");
                }
                if (i == tokens.size()) throw new IllegalArgumentException("Atributos DOT não terminados.");
                i++;
            }
            if (i >= tokens.size() || !tokens.get(i).equals(";")) throw new IllegalArgumentException("Falta ';' em declaração DOT.");
            i++;
            if (dest == null) {
                if (src.equals("graph")) {
                    if (weightedFlag == null || (!weightedFlag.equals("true") && !weightedFlag.equals("false"))) throw new IllegalArgumentException("Atributo weighted inválido.");
                    weighted = Boolean.parseBoolean(weightedFlag);
                } else if (value != null) values.put(src, value);
            } else {
                if (label != null) {
                    try { Double.parseDouble(label); } catch (NumberFormatException ex) { throw new IllegalArgumentException("Peso inválido: " + label); }
                    weighted = true;
                }
                edges.add(new String[]{src, dest, label});
            }
        }
        List<Node<String>> nodes = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) nodes.add(new Node<>(entry.getKey(), entry.getValue()));
        AdjMatrixGraph<String> result = new AdjMatrixGraph<>(nodes, directed, weighted);
        for (String[] edge : edges) {
            if (weighted) {
                if (edge[2] == null) throw new IllegalArgumentException("Aresta sem peso em grafo ponderado.");
                result.addEdge(edge[0], edge[1], Double.parseDouble(edge[2]));
            } else result.addEdge(edge[0], edge[1]);
        }
        return result;
    }

    private static String dotUnquote(String token) {
        if (!token.startsWith("\"")) return token;
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < token.length() - 1; i++) {
            char c = token.charAt(i);
            if (c == '\\' && i + 1 < token.length() - 1) {
                char next = token.charAt(++i);
                result.append(next == 'n' ? '\n' : next == 'r' ? '\r' : next);
            } else result.append(c);
        }
        return result.toString();
    }

    //Função para salvar o grafo >> passar o path, (salva a imagem em PNG)
    public void saveToPng(String filePath) throws java.io.IOException {
        br.com.davesmartins.api.Graph.createStringDotToPng(toDotString(), filePath);
    }

    //Dar cor para os vertices
    public void saveColoredToPng(Map<String, String> nodeColors, String filePath) throws java.io.IOException {
        br.com.davesmartins.api.Graph.createStringDotToPng(toDotString(nodeColors), filePath);
    }

    //Monta o DOT da busca: numero a ordem de visita no vértice e pinto de vermelho as arestas
    //que a busca usou. As outras arestas continuam no desenho, só que sem destaque.
    public String toDotStringWithSearch(String startId, boolean depthFirst) {
        int start = getNodeIndex(startId);
        List<Integer> order = new ArrayList<>();
        int[] parent = depthFirst ? depthFirstSearch(start, order) : breadthFirstSearch(start, order);

        int[] position = new int[this.numNodes];
        Arrays.fill(position, -1);

        for (int i = 0; i < order.size(); i++) {
            position[order.get(i)] = i + 1;
        }

        boolean[][] treeEdge = new boolean[this.numNodes][this.numNodes];

        for (int v : order) {
            if (parent[v] != -1) {
                treeEdge[parent[v]][v] = true;

                if (!this.directed) {
                    treeEdge[v][parent[v]] = true;
                }
            }
        }

        StringBuilder dot = new StringBuilder(this.directed ? "digraph G {\n" : "graph G {\n");
        dot.append("    label=\"").append(depthFirst ? "DFS" : "BFS")
                .append(" a partir de ").append(dotEscape(startId)).append("\";\n");
        dot.append("    labelloc=t;\n");

        for (int i = 0; i < this.numNodes; i++) {
            String id = this.nodes.get(i).getId();
            dot.append("    ").append(dotQuote(id)).append(" [style=filled, fillcolor=");

            if (position[i] == -1) {
                //a biblioteca de imagem não entende "lightgray", só a grafia "lightgrey"
                dot.append("lightgrey, label=\"").append(dotEscape(id)).append("\\n(não alcançado)\"]");
            } else {
                dot.append("lightblue, label=\"").append(dotEscape(id))
                        .append("\\n").append(position[i]).append("º\"]");
            }

            dot.append(";\n");
        }

        boolean[][] drawn = new boolean[this.numNodes][this.numNodes];
        String edgeOperator = this.directed ? " -> " : " -- ";

        for (int i = 0; i < this.numNodes; i++) {
            for (int j = 0; j < this.numNodes; j++) {
                if (this.adjacencyMatrix[i][j] == WITHOUT_EDGE) {
                    continue;
                }

                // no não orientado a matriz é espelhada, então desenho a aresta uma vez só
                if (!this.directed && drawn[j][i]) {
                    continue;
                }

                drawn[i][j] = true;
                dot.append("    ").append(dotQuote(this.nodes.get(i).getId()))
                        .append(edgeOperator).append(dotQuote(this.nodes.get(j).getId())).append(" [");

                if (this.weighted) {
                    dot.append("label=\"").append(this.adjacencyMatrix[i][j]).append("\", ");
                }

                if (treeEdge[i][j]) {
                    dot.append("color=red, penwidth=2.5]");
                } else {
                    dot.append("color=gray]");
                }

                dot.append(";\n");
            }
        }

        dot.append("}\n");
        return dot.toString();
    }

    public void saveSearchToPng(String startId, boolean depthFirst, String filePath) throws java.io.IOException {
        br.com.davesmartins.api.Graph.createStringDotToPng(toDotStringWithSearch(startId, depthFirst), filePath);
    }

    //Função para gerar o grafo reduzido, retorna uma PNG do grafo
    public void saveReducedGraphToPng(List<List<Node<T>>> components, String filePath) throws java.io.IOException {
        StringBuilder dot = new StringBuilder("digraph G {\n");

        // Mapeia cada vértice original para o índice da sua componente
        Map<String, String> superNodeLabels = new HashMap<>();
        int cIndex = 1;
        for (List<Node<T>> comp : components) {
            String superNodeId = "S" + cIndex;
            StringBuilder label = new StringBuilder("{");
            for (int i = 0; i < comp.size(); i++) {
                if (i > 0) label.append(", ");
                label.append(comp.get(i).getId());
            }
            label.append("}");

            superNodeLabels.put(superNodeId, label.toString());
            dot.append("    ").append(superNodeId).append(" [label=\"").append(superNodeId).append(": ").append(label).append("\", style=filled, fillcolor=lightgrey];\n");
            cIndex++;
        }

        // >> Para pegar os arcos arcos entre as componentes
        int[] componentOf = new int[this.numNodes];
        for (int c = 0; c < components.size(); c++) {
            for (Node<T> node : components.get(c)) {
                componentOf[getNodeIndex(node.getId())] = c + 1;
            }
        }

        boolean[][] usedArcs = new boolean[components.size() + 1][components.size() + 1];
        for (int i = 0; i < this.numNodes; i++) {
            for (int j = 0; j < this.numNodes; j++) {
                if (this.adjacencyMatrix[i][j] == WITHOUT_EDGE) continue;
                int fromComp = componentOf[i];
                int toComp = componentOf[j];

                if (fromComp != toComp && !usedArcs[fromComp][toComp]) {
                    usedArcs[fromComp][toComp] = true;
                    dot.append("    S").append(fromComp).append(" -> S").append(toComp).append(";\n");
                }
            }
        }

        dot.append("}\n");
        br.com.davesmartins.api.Graph.createStringDotToPng(dot.toString(), filePath);
    }
}

