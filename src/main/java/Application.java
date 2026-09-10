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
            System.out.println("1 - Criar grafo");

            if (this.graph != null) {
                System.out.println("2 - Continuar no grafo atual");
            }

            System.out.println("0 - Sair");

            switch (read("Escolha uma opção: ")) {
                case "1":
                    createGraph();
                    break;
                case "2":
                    if (this.graph == null) {
                        System.out.println("Crie um grafo primeiro.");
                    } else {
                        graphMenu();
                    }
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

    //Cria um grafo vazio.
    private void createGraph() {
        if (this.graph != null) {
            String answer = read("Substituir o grafo atual e perder seus dados? (s/n): ");

            if (!answer.equalsIgnoreCase("s")) {
                return;
            }
        }

        if (!this.running) {
            return;
        }

        this.graph = new AdjMatrixGraph<String>(new ArrayList<Node<String>>());
        System.out.println("Grafo criado.");
        graphMenu();
    }

    private void graphMenu() {
        while (this.running) {
            System.out.println("\n=== MENU DO GRAFO ===");
            System.out.println("1 - Adicionar vértice");
            System.out.println("2 - Adicionar aresta");
            System.out.println("6 - Informações do grafo");
            System.out.println("0 - Voltar ao menu principal");

            try {
                switch (read("Escolha uma opção: ")) {
                    case "1":
                        addNode();
                        break;
                    case "2":
                        addEdge();
                        break;
                    case "6":
                        informationMenu();
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

    // Adiciona uma aresta não direcionada e sem peso.
    private void addEdge() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("Adicione vértices primeiro.");
            return;
        }

        System.out.println("\nVértices disponíveis:");

        for (Node<String> node : this.graph.getNodes()) {
            System.out.println(node);
        }

        String src = read("ID do primeiro vértice: ");

        if (!this.running) {
            return;
        }

        String dest = read("ID do segundo vértice: ");

        if (!this.running) {
            return;
        }

        if (this.graph.hasEdge(src, dest)) {
            System.out.println("Essa aresta já existe.");
            return;
        }

        this.graph.addEdge(src, dest);
        System.out.println("Aresta adicionada.");
    }

    private void informationMenu() {
        while (this.running) {
            System.out.println("\n=== INFORMAÇÕES DO GRAFO ===");
            System.out.println("1 - Ordem do grafo");
            System.out.println("2 - Grau dos vértices");
            System.out.println("0 - Voltar ao menu do grafo");

            switch (read("Escolha uma opção: ")) {
                case "1":
                    printOrder();
                    break;
                case "2":
                    printDegrees();
                    break;
                case "0":
                    return;
                default:
                    invalidOption();
            }
        }
    }

    //Exibe a quantidade de vértices do grafo.
    private void printOrder() {
        System.out.println("Ordem do grafo: " + this.graph.getOrder());
    }

    //Exibe o grau de cada vértice.
    private void printDegrees() {
        this.graph.printDegrees();
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