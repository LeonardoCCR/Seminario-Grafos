package grafos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FloydWarshallTest {

    private AdjMatrixGraph<String> graph;

    @BeforeEach
    void setUp() {
        graph = new AdjMatrixGraph<>(
         List.of(new Node<>("A", "A"), new Node<>("B", "B"), new Node<>("C", "C")), true, true);
    }

    @Test
    @DisplayName("Deve encontrar o menor caminho com pesos positivos")
    void deveEncontrarMenorCaminhoComPesosPositivos() {
        graph.addEdge("A", "B", 10.0);
        graph.addEdge("A", "C", 3.0);
        graph.addEdge("C", "B", 2.0);
        String resultado = FloydWarshall.floydWarshall(graph);
        assertTrue(resultado.contains("A -> B\n" + "Caminho: A --> C --> B\n" + "Custo total: 5.0\n"));
    }

    @Test
    @DisplayName("Deve encontrar o menor caminho com pesos negativos")
    void deveEncontrarMenorCaminhoComPesoNegativo() {
        graph.addEdge("A", "B", 2.0);
        graph.addEdge("A", "C", 5.0);
        graph.addEdge("C", "B", -4.0);
        String resultado = FloydWarshall.floydWarshall(graph);
        assertTrue(resultado.contains("A -> B\n" + "Caminho: A --> C --> B\n" + "Custo total: 1.0\n"));
        assertTrue(resultado.contains("Nenhum ciclo negativo encontrado."));
    }

    @Test
    @DisplayName("Deve detectar ciclos negativos")
    void deveDetectarCicloNegativo() {
        graph.addEdge("A", "B", 2.0);
        graph.addEdge("B", "C", -5.0);
        graph.addEdge("C", "A", 1.0);
        String resultado = FloydWarshall.floydWarshall(graph);
        assertTrue(resultado.contains("Foram identificados ciclos negativos"));
        assertTrue(resultado.contains("A -> B\n" + "Caminho: indefinido\n" + "Custo total: indefinido"));
    }

    @Test
    @DisplayName("Deve identificar caminhos inexistentes")
    void deveIdentificarCaminhoInexistente() {
        graph.addEdge("A", "B", 2.0);
        String resultado = FloydWarshall.floydWarshall(graph);
        assertTrue(resultado.contains("A -> C\n" + "Caminho: inexistente\n" + "Custo total: infinito"));
    }

    @Test
    @DisplayName("Deve identificar grafo vazio")
    void deveIdentificarGrafoVazio() {
        AdjMatrixGraph<String> grafoVazio = new AdjMatrixGraph<>(List.of(), true, true);
        String resultado = FloydWarshall.floydWarshall(grafoVazio);
        assertTrue(resultado.contains("O grafo não possui vértices."));
    }

    @Test
    @DisplayName("Deve lançar exceção para grafos nulos")
    void deveLancarExcecaoParaGrafoNulo() {
        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> FloydWarshall.floydWarshall(null));
        assertEquals("O grafo não pode ser nulo.", excecao.getMessage());
    }
}