package grafos.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private int getNodeIndex(String id) {
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

    private Double[][] createDistanceMatrix(AdjMatrixGraph<T> graph) {
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
        result.append("\n\n1.6.3 — Simples - ").append(isSimple() ? "Sim" : "Não -> pois é um multigrafo");
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
}
