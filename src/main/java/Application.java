
import grafos.model.AdjMatrixGraph;
import grafos.model.Node;
import grafos.model.OutputBellmanFord;
import grafos.model.FloydWarshall;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            System.out.println("2 - Abrir Grafo (.dot)");
            System.out.println("0 - Sair");

            switch (read("Escolha uma opção: ")) {
                case "1":
                    createGraph();
                    break;
                case "2":
                    openGraph();
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
            System.out.println("1.8 - Salvar Grafo em Disco (.dot)");
            System.out.println("1.9 - Gerar Imagem do Grafo");
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
                    //Manter e descomentar quando implementarmos nas proximas entregas
                    case "1.8":
                        saveGraph();
                        break;
                    case "1.9":
                        imageMenu();
                        break;
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

    private void saveGraph() {
        String path = read("Caminho do arquivo .dot para salvar: ");
        if (!this.running) return;
        if (!path.toLowerCase().endsWith(".dot")) path += ".dot";
        try {
            this.graph.saveToDot(path);
            System.out.println("Grafo salvo em: " + path);
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("Erro ao salvar grafo: " + exception.getMessage());
        }
    }

    private void openGraph() {
        String path = read("Caminho do arquivo .dot para abrir: ");
        if (!this.running) return;
        if (this.graph != null) {
            String answer = read("Substituir o grafo atual e perder seus dados? (s/n): ");
            if (!this.running || !answer.equalsIgnoreCase("s")) return;
        }
        try {
            AdjMatrixGraph<String> loaded = AdjMatrixGraph.loadFromDot(path);
            this.graph = loaded;
            System.out.println("Grafo carregado com sucesso: " + path);
            graphMenu();
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("Erro ao abrir grafo: " + exception.getMessage());
        }
    }

    private void imageMenu() {
        while (this.running) {
            System.out.println("\n=== GERAR IMAGEM DO GRAFO ===");
            System.out.println("1.9.1 - Gerar Imagem do Grafo (Normal)");
            System.out.println("1.9.2 - Gerar Imagens de CFCs e Grafo Reduzido (Malgrange)");
            System.out.println("1.9.3 - Gerar Imagem da Busca (BFS ou DFS)");
            System.out.println("0 - Voltar ao menu do grafo");

            try {
                switch (read("Escolha uma opção: ")) {
                    case "1.9.1":
                        generateNormalGraphImage();
                        break;
                    case "1.9.2":
                        generateMalgrangeGraphImages();
                        break;
                    case "1.9.3":
                        generateSearchImage();
                        break;
                    case "0":
                        return;
                    default:
                        invalidOption();
                }
            } catch (IllegalArgumentException | IOException exception) {
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
            System.out.println("1.7.2 - Kruskal (árvore geradora mínima)");
            System.out.println("1.7.3 - Prim (árvore geradora mínima)");
            System.out.println("1.7.4 - Malgrange (componentes fortemente conexas)");
            System.out.println("1.7.5 - Busca em largura (BFS)");
            System.out.println("1.7.6 - Busca em profundidade (DFS)");
            System.out.println("1.7.7 - Bellman Ford (problema das rotas com custos variáveis)");
            System.out.println("1.7.8 - Floyd-Warshall (caminhos mínimos entre todos os pares)");
            System.out.println("0 - Voltar ao menu do grafo");

            try {
                switch (read("Escolha uma opção: ")) {
                    case "1.7.1":
                        printShortestPath("dijkstra");
                        break;
                    case "1.7.2":
                        printKruskal();
                        break;
                    case "1.7.3":
                        printPrim();
                        break;
                    case "1.7.4":
                        printMalgrange();
                        break;
                    case "1.7.5":
                        printSearch(false);
                        break;
                    case "1.7.6":
                        printSearch(true);
                        break;
                    case "1.7.7":
                        printShortestPath("bellmanford");
                        break;
                    case "1.7.8":
                        printFloydWarshall();
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

    private void printShortestPath(String algorithm) {
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

        if (algorithm.equals("dijkstra")) {
            System.out.println(this.graph.dijkstra(this.graph, src, dest));
        } else if (algorithm.equals("bellmanford")) {
            OutputBellmanFord<String> outputBellmanFord = new OutputBellmanFord<>();

            String msg = outputBellmanFord.minimumCost(this.graph, src) + "\n"
                    + outputBellmanFord.getShortestPath(this.graph, src, dest);
            System.out.println(msg);
        }
    }

    private void printFloydWarshall() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        System.out.println(FloydWarshall.floydWarshall(this.graph));
    }

    private void printPrim() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        System.out.println(this.graph.prim());
    }

    private void printMalgrange() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        System.out.println(this.graph.malgrange());
    }

    //As duas buscas pedem a mesma coisa (o vértice inicial), então usei um método só para as duas.
    private void printSearch(boolean depthFirst) {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        System.out.println("\nVértices disponíveis:");

        for (Node<String> node : this.graph.getNodes()) {
            System.out.println(node);
        }

        String id = read("ID do vértice inicial: ");

        if (!this.running) {
            return;
        }

        System.out.println(depthFirst ? this.graph.dfs(id) : this.graph.bfs(id));
    }

    private void printKruskal() {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        System.out.println(this.graph.kruskal());
    }

    private void generateNormalGraphImage() throws IOException {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        // Sugerir o caminho padrão dentro da pasta que com o nome Generaed-Graph-Images
        String path = read("Informe o path(caminho) de saída (ex: Generated-Graph-Images/grafo_normal.png): ");
        if (!this.running) {
            return;
        }

        if (path.isEmpty()) {
            path = "Generated-Graph-Images/grafo_normal.png";
        } else if (!path.endsWith(".png")) {
            path += ".png";
        }

        this.graph.saveToPng(path);
        System.out.println("Imagem do grafo gerada com sucesso: " + path);
    }

    //Gera a imagem da busca: numera os vértices na ordem de visita e pinta de vermelho
    //as arestas que a busca percorreu, sem tirar o resto do grafo do desenho.
    private void generateSearchImage() throws IOException {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        String answer = read("Qual busca? (1 - Largura/BFS, 2 - Profundidade/DFS): ");

        if (!this.running) {
            return;
        }

        if (!answer.equals("1") && !answer.equals("2")) {
            invalidOption();
            return;
        }

        boolean depthFirst = answer.equals("2");

        System.out.println("\nVértices disponíveis:");

        for (Node<String> node : this.graph.getNodes()) {
            System.out.println(node);
        }

        String id = read("ID do vértice inicial: ");

        if (!this.running) {
            return;
        }

        String path = read("Informe o path(caminho) de saída (ex: Generated-Graph-Images/busca.png): ");

        if (!this.running) {
            return;
        }

        if (path.isEmpty()) {
            path = "Generated-Graph-Images/busca_" + (depthFirst ? "dfs" : "bfs") + ".png";
        } else if (!path.endsWith(".png")) {
            path += ".png";
        }

        this.graph.saveSearchToPng(id, depthFirst, path);
        System.out.println("Imagem da busca gerada com sucesso: " + path);
    }

    private void generateMalgrangeGraphImages() throws IOException {
        if (this.graph.getNumNodes() == 0) {
            System.out.println("O grafo não possui vértices.");
            return;
        }

        if (!this.graph.isDirected()) {
            System.out.println("Aviso: A análise de Componentes Fortemente Conexas (Malgrange) aplica-se a grafos orientados.");
        }

        List<List<Node<String>>> cfcs = this.graph.getStronglyConnectedComponents();

        List<String> paletaCores = Arrays.asList("lightblue", "lightgreen", "lightpink", "lightyellow", "orange", "plum");
        Map<String, String> coresPorVertice = new HashMap<>();

        for (int i = 0; i < cfcs.size(); i++) {
            String cor = paletaCores.get(i % paletaCores.size());
            for (Node<String> node : cfcs.get(i)) {
                coresPorVertice.put(node.getId(), cor);
            }
        }

        // Criei a pasta Generated-Graph-Images >> Salvar as duas imagens do grafo , talvez criar mais pastas ?
        String caminhoDoGrafoOriginal = "Generated-Graph-Images/grafo_original_cfcs.png";
        String caminhoDoGrafoReduzido = "Generated-Graph-Images/grafo_reduzido.png";

        this.graph.saveColoredToPng(coresPorVertice, caminhoDoGrafoOriginal);
        this.graph.saveReducedGraphToPng(cfcs, caminhoDoGrafoReduzido);

        System.out.println("Imagens de Malgrange geradas com sucesso:");
        System.out.println(" - Grafo original com CFCs coloridas: " + caminhoDoGrafoOriginal);
        System.out.println(" - Grafo reduzido Gr(S, W): " + caminhoDoGrafoReduzido);
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
