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

    //Refatoramos, para se adequar com a possibilidade de grafos orientados
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
            System.out.println("1 - Adicionar vértice");
            System.out.println("2 - Adicionar aresta");
            System.out.println("3 - Remover vértice");
            System.out.println("4 - Remover aresta");
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
                    case "3":
                        removeNode();
                        break;
                    case "4":
                        removeEdge();
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

        if (this.graph.hasEdge(src, dest)) {
            System.out.println("Essa aresta já existe.");
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

        if (!this.graph.hasEdge(src, dest)) {
            System.out.println("Essa aresta não existe.");
            return;
        }

        this.graph.removeEdge(src, dest);
        System.out.println("Aresta removida.");
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

