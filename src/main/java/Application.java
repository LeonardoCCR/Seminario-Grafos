import grafos.model.AdjMatrixGraph;
import grafos.model.Node;

import java.util.ArrayList;
import java.util.Scanner;

public class Application {
    private final Scanner scanner;
    private AdjMatrixGraph<String> graph;
    private boolean running;

    public Application() {
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public static void main(String[] args) {
        Application application = new Application();

        try {
            application.run();
        } finally {
            application.scanner.close();
        }
    }

    public void run() {
        while (this.running) {
            System.out.println("\n=== GRAFOS ===");
            System.out.println("1 - Criar Grafo");
            System.out.println("0 - Sair");

            switch (read("Escolha uma opção: ")) {
                case "1":
                    createGraph();
                    break;
                case "0":
                    this.running = false;
                    break;
                default:
                    invalidOption();
            }
        }

        System.out.println("Programa encerrado.");
    }

    //Refatoramos, para se adequar com a possibilidade de grafos orientados
    private void createGraph() {
        if (this.graph != null) {
            String answer = read("Substituir o grafo atual e perder seus dados? (s/n): ");

            if (!this.running) {
                return;
            }

            if (!answer.equalsIgnoreCase("s")) {
                graphMenu();
                return;
            }
        }

        if (!this.running) {
            return;
        }

        boolean directed = false;
        boolean weighted = false;

        while (this.running) {
            String answer = read("O grafo será orientado? (s/n): ");

            if (!this.running) {
                return;
            }

            if (!answer.equalsIgnoreCase("s") && !answer.equalsIgnoreCase("n")) {
                invalidOption();
                continue;
            }

            directed = answer.equalsIgnoreCase("s");
            break;
        }

        while (this.running) {
            String answer = read("O grafo será ponderado? (s/n): ");

            if (!this.running) {
                return;
            }

            if (!answer.equalsIgnoreCase("s") && !answer.equalsIgnoreCase("n")) {
                invalidOption();
                continue;
            }

            weighted = answer.equalsIgnoreCase("s");
            break;
        }

        if (!this.running) {
            return;
        }

        this.graph = new AdjMatrixGraph<String>(new ArrayList<Node<String>>(), directed, weighted);

        //Se quiser depois pode alterar, mas por enquanto deixei ternário mesmo
        System.out.println("Grafo " + (directed ? "orientado" : "não orientado")
                + " e " + (weighted ? "ponderado" : "não ponderado") + " criado.");

        graphMenu();
    }

    private void graphMenu() {
        while (this.running) {
            System.out.println("\n=== MENU DO GRAFO ===");
            System.out.println("1.1 - Adicionar Vértice");
            System.out.println("1.2 - Adicionar Aresta");
            System.out.println("1.3 - Remover Vértice");
            System.out.println("1.4 - Remover Aresta");
            System.out.println("1.5 - Imprimir Grafo");
            System.out.println("1.6 - Informações do Grafo");
            System.out.println("1.7 - Algoritmos");
            // System.out.println("1.8 - Salvar Grafo em Disco");
            // System.out.println("1.9 - Gerar Imagem do Grafo");
            System.out.println("1.10 - Sair");

            try {
                switch (read("Escolha uma opção: ")) {
                    case "1.1":
                        addNode();
                        break;
                    case "1.2":
                        addEdge();
                        break;
                    case "1.3":
                        removeNode();
                        break;
                    case "1.4":
                        removeEdge();
                        break;
                    case "1.5":
                        this.graph.printGraph();
                        break;
                    case "1.6":
                        printInformation();
                        break;
                    case "1.7":
                        algorithmsMenu();
                        break;
                    //Manter e desmcomentar quando implementarmos nas proximas entregas
                    // case "1.8":
                    //     break;
                    // case "1.9":
                    //     break;
                    case "1.10":
                        return;
                    default:
                        invalidOption();
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Erro: " + exception.getMessage());
            }
        }
    }

    private void addNode() {
        String id = read("ID do vértice: ");

        if (!this.running) {
            return;
        }

        String value = read("Valor do vértice: ");

        if (!this.running) {
            return;
        }

        this.graph.addNode(new Node<>(id, value));
        System.out.println("Vértice adicionado.");
    }

    private void addEdge() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("Adicione vértices primeiro.");
            return;
        }

        System.out.println("\nVértices disponíveis:");

        for (Node<String> node : this.graph.getNodes()) {
            System.out.println(node);
        }

        String src = read("ID do vértice de origem: ");

        if (!this.running) {
            return;
        }

        String dest = read("ID do vértice de destino: ");

        if (!this.running) {
            return;
        }

        if (this.graph.isWeighted()) {
            String input = read("Peso da aresta: ");

            if (!this.running) {
                return;
            }

            double weight;

            try {
                weight = Double.parseDouble(input.replace(',', '.'));
            } catch (NumberFormatException exception) {
                System.out.println("Peso inválido. Informe um número.");
                return;
            }

            this.graph.addEdge(src, dest, weight);
        } else {
            this.graph.addEdge(src, dest);
        }

        System.out.println("Aresta adicionada.");
    }

    private void removeNode() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        System.out.println("\nVértices disponíveis:");

        for (Node<String> node : this.graph.getNodes()) {
            System.out.println(node);
        }

        String id = read("ID do vértice: ");

        if (!this.running) {
            return;
        }

        this.graph.removeNode(id);
        System.out.println("Vértice removido.");
    }

    private void removeEdge() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        System.out.println("\nVértices disponíveis:");

        for (Node<String> node : this.graph.getNodes()) {
            System.out.println(node);
        }

        String src = read("ID do vértice de origem: ");

        if (!this.running) {
            return;
        }

        String dest = read("ID do vértice de destino: ");

        if (!this.running) {
            return;
        }

        if (!this.graph.hasEdge(src, dest)) {
            System.out.println("Essa aresta não existe.");
            return;
        }

        this.graph.removeEdge(src, dest);
        System.out.println("Aresta removida.");
    }

    private void algorithmsMenu() {
        while (this.running) {
            System.out.println("\n=== ALGORITMOS ===");
            System.out.println("1.7.1 - Dijkstra (caminho mínimo)");
            System.out.println("0 - Voltar ao menu do grafo");

            try {
                switch (read("Escolha uma opção: ")) {
                    case "1.7.1":
                        printShortestPath();
                        break;
                    case "0":
                        return;
                    default:
                        invalidOption();
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Erro: " + exception.getMessage());
            }
        }
    }

    private void printShortestPath() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        System.out.println("\nVértices disponíveis:");

        for (Node<String> node : this.graph.getNodes()) {
            System.out.println(node);
        }

        String src = read("ID do vértice de origem: ");

        if (!this.running) {
            return;
        }

        String dest = read("ID do vértice de destino: ");

        if (!this.running) {
            return;
        }

        System.out.println(this.graph.dijkstra(this.graph, src, dest));
    }

    private void printInformation() {
        String id = null;

        if (this.graph.isDirected() && this.graph.getNumNodes() > 0) {
            System.out.println("\nVértices disponíveis:");

            for (Node<String> node : this.graph.getNodes()) {
                System.out.println(node);
            }

            id = read("ID do vértice para o item 1.6.8 - Fecho Transitivo Direto e Inverso: ");

            if (!this.running) {
                return;
            }
        }

        System.out.println(this.graph.getGraphInformation(id));
    }

    private String read(String message) {
        System.out.print(message);

        if (!this.scanner.hasNextLine()) {
            this.running = false;
            return "";
        }

        return this.scanner.nextLine().trim();
    }

    private void invalidOption() {
        if (this.running) {
            System.out.println("Opção inválida. Tente novamente.");
        }
    }

}
