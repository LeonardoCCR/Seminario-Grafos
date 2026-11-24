package grafos.model;

public class Edge<T> {

    private final Node<T> sourceNode;
    private final Node<T> destinationNode;
    private final Double weight;

    public Edge(Node<T> sourceNode, Node<T> destinationNode, Double weight) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.weight = weight;
    }

    public Node<T> getSourceNode() {
        return sourceNode;
    }

    public Node<T> getDestinationNode() {
        return destinationNode;
    }

    public Double getWeight() {
        return weight;
    }
}
