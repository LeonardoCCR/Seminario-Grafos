package grafos.model;

import java.util.Objects;

public class Node<T> {
    private final String id;
    private T value;

    public Node(String id, T value) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("O ID do vértice não pode ser vazio.");
        }

        this.id = id.trim();
        this.value = value;
    }

    public String getId() {
        return this.id;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Node<?> node = (Node<?>) obj;
        return Objects.equals(this.id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return "Vértice [ID: " + this.id + ", Valor: " + this.value + "]";
    }
}