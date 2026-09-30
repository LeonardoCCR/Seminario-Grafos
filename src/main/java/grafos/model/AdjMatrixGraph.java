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

    //Deixei a validação aqui e não no menu, porque quem sabe se o grafo serve é o próprio grafo
    public String dijkstra(String src, String dest) {
        getNodeIndex(src);
        getNodeIndex(dest);

        if (!this.weighted) {
            throw new IllegalArgumentException("O algoritmo de Dijkstra exige um grafo ponderado.");
        }

        if (hasNegativeWeights()) {
            throw new IllegalArgumentException("O grafo possui pesos negativos, então o algoritmo de Dijkstra não pode ser aplicado.");
        }

        return "O algoritmo de Dijkstra ainda será implementado.";
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
        return value ? "True" : "False";
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
        result.append("\n\n1.6.3 — Simples - ").append(formatBoolean(isSimple()));
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
