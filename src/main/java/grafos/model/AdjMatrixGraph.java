package grafos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjMatrixGraph<T> {

    //lembrar de reaproveitar as cponstantes declaradas

    private static final double WITH_EDGE = 1.0;
    private static final Double WITHOUT_EDGE = null;

    private boolean directed;
    private boolean weighted;
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

        if (this.adjacencyMatrix[srcIndex][destIndex] == WITHOUT_EDGE) {
            this.adjacencyMatrix[srcIndex][destIndex] = weight;

            if (!this.directed) {
                this.adjacencyMatrix[destIndex][srcIndex] = weight;
            }

            ++this.numEdges;
        }
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
        int index = getNodeIndex(id);
        int count = 0;

        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[index][i] != WITHOUT_EDGE) {
                ++count;
            }
        }

        return count;
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

    public void printDegrees() {
        if (this.numNodes == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        for (Node<T> node : this.nodes) {
            String id = node.getId();

            if (this.directed) {
                System.out.println("Vértice " + id + ": grau de entrada " + getInDegree(id) + ", grau de saída " + getOutDegree(id));
            } else {
                System.out.println("Vértice " + id + ": grau " + getDegree(id));
            }
        }
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
}