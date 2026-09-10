package grafos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjMatrixGraph<T> {

    //lembrar de reaproveitar as cponstantes declaradas

    private static final int WITH_EDGE = 1;
    private static final int WITHOUT_EDGE = 0;

    private  int numNodes;
    private int numEdges;
    private int[][] adjacencyMatrix;
    private final Map<String, Integer> nodeIndexes;
    private final List<Node<T>> nodes;

    public AdjMatrixGraph(List<Node<T>> nodes) {
        if (nodes == null) {
            throw new IllegalArgumentException("A lista de vértices não pode ser nula.");
        }

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
        this.adjacencyMatrix = new int[this.numNodes][this.numNodes];
    }

    public void addEdge(String src, String dest) {
        int srcIndex = getNodeIndex(src);
        int destIndex = getNodeIndex(dest);

        if (this.adjacencyMatrix[srcIndex][destIndex] == WITHOUT_EDGE) {
            this.adjacencyMatrix[srcIndex][destIndex] = WITH_EDGE;
            this.adjacencyMatrix[destIndex][srcIndex] = WITH_EDGE;
            ++this.numEdges;
        }
    }

    public void removeEdge(String src, String dest) {
        int srcIndex = getNodeIndex(src);
        int destIndex = getNodeIndex(dest);

        if (this.adjacencyMatrix[srcIndex][destIndex] == WITH_EDGE) {
            this.adjacencyMatrix[srcIndex][destIndex] = WITHOUT_EDGE;
            this.adjacencyMatrix[destIndex][srcIndex] = WITHOUT_EDGE;
            --this.numEdges;
        }
    }

    public boolean hasEdge(String src, String dest) {
        int srcIndex = getNodeIndex(src);
        int destIndex = getNodeIndex(dest);

        return this.adjacencyMatrix[srcIndex][destIndex] == WITH_EDGE;
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
            if (this.adjacencyMatrix[index][i] == WITH_EDGE) {
                neighbors.add(this.nodes.get(i));
            }
        }

        return neighbors;
    }

    public int countNeighbors(String id) {
        int index = getNodeIndex(id);
        int count = 0;

        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[index][i] == WITH_EDGE) {
                ++count;
            }
        }

        return count;
    }


    //  reotornar a ordem do grafo
    public int getOrder() {
        return this.numNodes;
    }

    // Calcula o grau do vértice; -> getDegree(String id)
    // Considerei que cada laço contribui com dois. verificar depois
    public int getDegree(String id) {
        int index = getNodeIndex(id);
        int degree = 0;

        for (int i = 0; i < this.numNodes; i++) {
            if (this.adjacencyMatrix[index][i] == WITH_EDGE) {
                degree += i == index ? 2 : 1;
            }
        }

        return degree;
    }

    // Exibe o grau de cada vértice do grafo.
    public void printDegrees() {
        if (this.numNodes == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        for (Node<T> node : this.nodes) {
            System.out.println("Vértice " + node.getId() + ": grau " + getDegree(node.getId()));
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

        String format = "%" + width + "s";
        System.out.printf(format, "");

        for (Node<T> node : this.nodes) {
            System.out.printf(format, node.getId());
        }
        System.out.println();

        for (int i = 0; i < this.numNodes; i++) {
            System.out.printf(format, this.nodes.get(i).getId());

            for (int j = 0; j < this.numNodes; j++) {
                System.out.printf(format, this.adjacencyMatrix[i][j]);
            }
            System.out.println();
        }
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

        int[][] expandedMatrix = new int[this.numNodes + 1][this.numNodes + 1];

        for (int i = 0; i < this.numNodes; i++) {
            System.arraycopy(this.adjacencyMatrix[i], 0, expandedMatrix[i], 0, this.numNodes);
        }

        this.nodeIndexes.put(node.getId(), this.numNodes);
        this.nodes.add(node);
        this.adjacencyMatrix = expandedMatrix;
        ++this.numNodes;
    }
}