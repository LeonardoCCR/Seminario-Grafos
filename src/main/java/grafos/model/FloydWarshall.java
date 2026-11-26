
package grafos.model;

import java.util.*;

public class FloydWarshall {

    public static <T> String floydWarshall(AdjMatrixGraph<T> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("O grafo não pode ser nulo.");
        }

        int order = graph.getOrder();
        List<Node<T>> nodes = graph.getNodes();

        StringBuilder result = new StringBuilder("\n=== FLOYD-WARSHALL ===\n");

        result.append("Tipo: ")
                .append(graph.isDirected() ? "Orientado" : "Não orientado")
                .append(graph.isWeighted() ? " e ponderado" : " e não ponderado")
                .append("\nDesempate: ordem de inserção dos vértices.\n");

        if (order == 0) {
            result.append("\nO grafo não possui vértices.\n");
            return result.toString();
        }

        Double[][] distances = createDistanceMatrix(graph);
        int[][] next = new int[order][order];

        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {

                if (i == j) {
                    next[i][j] = i;
                } else if (!Double.isInfinite(distances[i][j])) {
                    next[i][j] = j;
                } else {
                    next[i][j] = -1;
                }
            }
        }

        result.append("\n=== MATRIZ INICIAL DE DISTÂNCIAS ===\n");
        appendDistanceMatrix(result, nodes, distances);

        // Considera cada vértice como possível intermediário.
        for (int k = 0; k < order; k++) {

            for (int i = 0; i < order; i++) {

                for (int j = 0; j < order; j++) {

                    if (Double.isInfinite(distances[i][k])
                            || Double.isInfinite(distances[k][j])) {
                        continue;
                    }

                    double candidateDistance = distances[i][k] + distances[k][j];

                    if (Double.isInfinite(candidateDistance)) {
                        throw new IllegalArgumentException(
                                "A soma dos custos ultrapassou o limite de double. Não foi possível concluir o cálculo."
                        );
                    }

                    if (candidateDistance < distances[i][j]) {
                        distances[i][j] = candidateDistance;
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        // Identifica os pares de vértices afetados por ciclos negativos.
        boolean[][] affectedByNegativeCycle = new boolean[order][order];
        List<String> negativeCycles = new ArrayList<>();

        for (int k = 0; k < order; k++) {

            if (distances[k][k] < 0) {
                negativeCycles.add(nodes.get(k).getId());

                for (int i = 0; i < order; i++) {
                    for (int j = 0; j < order; j++) {

                        if (!Double.isInfinite(distances[i][k])
                                && !Double.isInfinite(distances[k][j])) {

                            affectedByNegativeCycle[i][j] = true;
                        }
                    }
                }
            }
        }

        result.append("\n=== MATRIZ FINAL DE DISTÂNCIAS ===\n");
        appendDistanceMatrix(result, nodes, distances);

        result.append("\n=== DETECÇÃO DE CICLOS NEGATIVOS ===\n");

        if (negativeCycles.isEmpty()) {
            result.append("Nenhum ciclo negativo encontrado.\n");
        } else {
            result.append("Foram identificados ciclos negativos envolvendo os vértices: ")
                    .append(String.join(", ", negativeCycles))
                    .append("\n");
        }

        result.append("\n=== RESULTADO: MENORES CAMINHOS ===\n");

        for (int i = 0; i < order; i++) {

            for (int j = 0; j < order; j++) {

                String source = nodes.get(i).getId();
                String destination = nodes.get(j).getId();

                result.append("\n")
                        .append(source).append(" -> ").append(destination);

                if (affectedByNegativeCycle[i][j]) {
                    result.append("\nCaminho: indefinido")
                            .append("\nCusto total: indefinido")
                            .append("\nMotivo: existe um ciclo negativo alcançável entre os vértices.\n");

                } else if (Double.isInfinite(distances[i][j])) {
                    result.append("\nCaminho: inexistente")
                            .append("\nCusto total: infinito")
                            .append("\nNão existe caminho entre os vértices.\n");

                } else {
                    result.append("\nCaminho: ")
                            .append(reconstructPath(nodes, next, i, j))
                            .append("\nCusto total: ")
                            .append(distances[i][j])
                            .append("\n");
                }
            }
        }

        return result.toString();
    }

    // Monta a matriz inicial com os pesos das arestas.
    private static <T> Double[][] createDistanceMatrix(AdjMatrixGraph<T> graph) {

        int order = graph.getOrder();
        List<Node<T>> nodes = graph.getNodes();

        Double[][] distances = new Double[order][order];

        for (int i = 0; i < order; i++) {

            for (int j = 0; j < order; j++) {

                String source = nodes.get(i).getId();
                String destination = nodes.get(j).getId();

                if (graph.hasEdge(source, destination)) {

                    double weight = graph.isWeighted()
                            ? graph.getWeight(source, destination)
                            : 1.0;

                    distances[i][j] = i == j
                            ? Math.min(0.0, weight)
                            : weight;

                } else {
                    distances[i][j] = i == j
                            ? 0.0
                            : Double.POSITIVE_INFINITY;
                }
            }
        }

        return distances;
    }

    // Exibe uma matriz de distâncias utilizando os IDs dos vértices.
    private static <T> void appendDistanceMatrix(
            StringBuilder result,
            List<Node<T>> nodes,
            Double[][] distances) {

        int order = nodes.size();
        int width = 12;

        for (Node<T> node : nodes) {
            width = Math.max(width, node.getId().length() + 2);
        }

        String format = "%" + width + "s";

        result.append(String.format(format, ""));

        for (Node<T> node : nodes) {
            result.append(String.format(format, node.getId()));
        }

        result.append("\n");

        for (int i = 0; i < order; i++) {

            result.append(String.format(format, nodes.get(i).getId()));

            for (int j = 0; j < order; j++) {

                String value = Double.isInfinite(distances[i][j])
                        ? "INF"
                        : distances[i][j].toString();

                result.append(String.format(format, value));
            }

            result.append("\n");
        }
    }

    // Reconstrói o caminho utilizando a matriz next.
    private static <T> String reconstructPath(
            List<Node<T>> nodes,
            int[][] next,
            int source,
            int destination) {

        if (next[source][destination] == -1) {
            return "inexistente";
        }

        List<String> path = new ArrayList<>();
        int current = source;

        path.add(nodes.get(current).getId());

        int steps = 0;

        while (current != destination) {

            if (steps >= nodes.size()) {
                throw new IllegalStateException(
                        "Não foi possível reconstruir o caminho. Verifique a existência de ciclos negativos."
                );
            }

            current = next[current][destination];

            if (current == -1) {
                throw new IllegalStateException(
                        "A matriz de caminhos possui uma referência inválida."
                );
            }

            path.add(nodes.get(current).getId());
            steps++;
        }

        return String.join(" --> ", path);
    }
}
