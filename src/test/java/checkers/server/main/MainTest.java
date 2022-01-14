package checkers.server.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.print.attribute.HashAttributeSet;

import java.util.Set;

import static checkers.GameConstants.H;
import static checkers.GameConstants.W;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    Main main;

    @BeforeEach
    void setUp() {
        main = new Main();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testMakeMove() {
        int[][] field = main.fillField(6);
        main.setField(field);
        String str = "Simple(13,9,12,10)";
        assertEquals(2,field[13][9]);
        assertEquals(0,field[12][10]);
        field = main.makeMove(str);
        assertEquals(0,field[13][9]);
        assertEquals(2,field[12][10]);
        str = "Jump(2,10,4,12)";
        assertEquals(1, field[2][10]);
        assertEquals(0, field[4][12]);
        field = main.makeMove(str);
        assertEquals(0, field[2][10]);
        assertEquals(1, field[4][12]);
    }

    @Test
    void testFirstWinGame() {
        int[][] field = main.fillField(6);
        assertFalse(main.firstWinGame(field));
        field[16][12] = 1;
        field[15][11] = field[15][13] = 1;
        assertFalse(main.firstWinGame(field));
        field[14][10] = field[14][12] = field[14][14] = 1;
        field[13][9] = field[13][11] = field[13][13] = field[13][15] = 1;
        assertTrue(main.firstWinGame(field));
    }

    @Test
    void testSecondWinGame() {
        int[][] field = main.fillField(6);
        assertFalse(main.secondWinGame(field));
        field[0][12] = 2;
        field[1][11] = field[1][13] = 2;
        assertFalse(main.secondWinGame(field));
        field[2][10] = field[2][12] = field[2][14] = 2;
        field[3][9] = field[3][11] = field[3][13] = field[3][15] = 2;
        assertTrue(main.secondWinGame(field));
    }

    @Test
    void testThirdWinGame() {
        int[][] field = main.fillField(6);
        assertFalse(main.thirdWinGame(field));
        field[7][21] = 3;
        field[6][20] = field[6][22] = 3;
        assertFalse(main.thirdWinGame(field));
        field[5][19] = field[5][21] = field[5][23] = 3;
        field[4][18] = field[4][20] = field[4][22] = field[4][24] = 3;
        assertTrue(main.thirdWinGame(field));
    }

    @Test
    void testFourthWinGame() {
        int[][] field = main.fillField(6);
        assertFalse(main.fourthWinGame(field));
        field[7][3] = 4;
        field[6][2] = field[6][4] = 4;
        field[5][1] = field[5][3] = field[5][5] = 4;
        assertFalse(main.fourthWinGame(field));
        field[4][0] = field[4][2] = field[4][4] = field[4][6] = 4;
        assertTrue(main.fourthWinGame(field));
    }

    @Test @Disabled
    void fifthWinGame() {
    }

    @Test @Disabled
    void sixthWinGame() {
    }

    @Test
    void testFillField() {
        int[][] field = main.fillField(2);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                assertTrue(Set.of(0,1,2,9).contains(field[i][j]));
            }
        }

        field = main.fillField(3);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                // 0 1 3 4 9
                assertTrue(Set.of(0,1,3,4,9).contains(field[i][j]));
            }
        }

        field = main.fillField(4);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                // 0 1 2 4 5 9
                assertTrue(Set.of(0,1,2,4,5,9).contains(field[i][j]));
            }
        }

        field = main.fillField(6);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                assertTrue(Set.of(0,1,2,3,4,5,6,9).contains(field[i][j]));
            }
        }
    }

    @Test
    void testFillFieldForPlayer1() {
        int[][] field = new int[H][W];
        main.fillFieldForPlayer1(field);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                assertTrue(field[i][j] == 0 || field[i][j] == 1);
            }
        }
        assertEquals(1, field[0][12]);
        assertEquals(1, field[1][11]);
        assertEquals(1, field[1][13]);
        assertEquals(1, field[2][10]);
        assertEquals(1, field[2][12]);
        assertEquals(1, field[2][14]);
        assertEquals(1, field[3][9]);
        assertEquals(1, field[3][11]);
        assertEquals(1, field[3][13]);
        assertEquals(1, field[3][15]);
    }

    @Test
    void testFillFieldForPlayer2() {
        int[][] field = new int[H][W];
        main.fillFieldForPlayer2(field);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                assertTrue(field[i][j] == 0 || field[i][j] == 2);
            }
        }
        assertEquals(2, field[16][12]);
        assertEquals(2, field[15][11]);
        assertEquals(2, field[15][13]);
        assertEquals(2, field[14][10]);
        assertEquals(2, field[14][12]);
        assertEquals(2, field[14][14]);
        assertEquals(2, field[13][9]);
        assertEquals(2, field[13][11]);
        assertEquals(2, field[13][13]);
        assertEquals(2, field[13][15]);
    }

    @Test
    void testFillFieldForPlayer3() {
        int[][] field = new int[H][W];
        main.fillFieldForPlayer3(field);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                assertTrue(field[i][j] == 0 || field[i][j] == 3);
            }
        }
        assertEquals(3, field[9][3]);
        assertEquals(3, field[10][2]);
        assertEquals(3, field[10][4]);
        assertEquals(3, field[11][1]);
        assertEquals(3, field[11][3]);
        assertEquals(3, field[11][5]);
        assertEquals(3, field[12][0]);
        assertEquals(3, field[12][2]);
        assertEquals(3, field[12][4]);
        assertEquals(3, field[12][6]);
        assertEquals(0, field[6][6]);
    }

    @Test @Disabled
    void testFillFieldForPlayer4() {
    }

    @Test @Disabled
    void testFillFieldForPlayer5() {
    }

    @Test @Disabled
    void testFillFieldForPlayer6() {
    }

    @Test
    void testFillEmptyPart() {
        int[][] field = new int[H][W];
        //main.fillFieldForPlayer2(field);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                field[i][j] = 9;
            }
        }
        main.fillEmptyPart(field);
        assertEquals(0, field[16][12]);
        assertEquals(0, field[15][11]);
        assertEquals(0, field[15][13]);
        assertEquals(0, field[14][10]);
        assertEquals(0, field[14][12]);
        assertEquals(0, field[14][14]);
        assertEquals(0, field[13][9]);
        assertEquals(0, field[13][11]);
        assertEquals(0, field[13][13]);
        assertEquals(0, field[13][15]);
    }
}