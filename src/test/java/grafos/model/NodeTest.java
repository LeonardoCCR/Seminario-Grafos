package grafos.model;

import org.junit.jupiter.api.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
class NodeTest {

    @Test
    void deveCriarNoComIdEValor() {
        Node<Integer> node = new Node<>("A", 10);

        assertEquals("A", node.getId());
        assertEquals(Integer.valueOf(10), node.getValue());
    }

    @Test
    void deveRemoverEspacosNasExtremidadesDoId() {
        Node<Integer> node = new Node<>("  A  ", 10);

        assertEquals("A", node.getId());
    }

    //void deveRejeitarIdNulo

    @Test
    void deveRejeitarIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Node<Integer>(null, 10));
    }
}