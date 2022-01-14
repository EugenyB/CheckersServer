package checkers.server.main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
    }

    @Test
    void testCreateFieldInfo() {
        int[][] f = {{1,2,3},{0,4,5},{6,9,8}};
        StringBuilder fieldInfo = player.createFieldInfo(f);
        assertEquals("123045698", fieldInfo.toString());
    }
}