package checkers.client.main.model;

import checkers.client.main.model.moves.MoveType;
import checkers.client.main.util.Pair;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static checkers.GameConstants.H;
import static checkers.GameConstants.W;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testIsMoveValid() {
        game.setPlayerColor(Color.BLUE);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                builder.append('0');
            }
        }
        game.fillField(builder.toString());
        int[][] field = game.getField();
        field[9][11] = 1;

        MoveType result = game.isMoveValid(9, 11, 8, 10);
        assertEquals(MoveType.SIMPLE, result);

        field[8][10] = 3;
        result = game.isMoveValid(9, 11, 8, 10);
        assertEquals(MoveType.IMPOSSIBLE, result);

        result = game.isMoveValid(9,11,11,9);
        assertEquals(MoveType.IMPOSSIBLE, result);

        field[10][10] = 2;
        result = game.isMoveValid(9,11,11,9);
        assertEquals(MoveType.JUMP, result);

        result = game.isMoveValid(10,10,11,11);
        assertEquals(MoveType.IMPOSSIBLE, result);
    }

    @Test
    void testCreateField() {
        // 0xff0000ff
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                builder.append('0');
            }
        }
        builder.append(Color.RED);
        builder.setCharAt(9 * W + 11, '1');
        builder.setCharAt(8 * W + 8, '2');
        game.createField(builder.toString());

        Color color = game.getPlayerColor();
        assertEquals(Color.RED, color);

        assertEquals(1, game.getField()[9][11]);
        assertEquals(2, game.getField()[8][8]);
    }

    @Test
    void testIsOccupied() {
        assertTrue(game.isOccupied(1));
        assertTrue(game.isOccupied(2));
        assertTrue(game.isOccupied(3));
        assertTrue(game.isOccupied(4));
        assertTrue(game.isOccupied(5));
        assertTrue(game.isOccupied(6));
        assertFalse(game.isOccupied(0));
        assertFalse(game.isOccupied(9));
    }

    @Test
    void testFindPiece() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                builder.append('0');
            }
        }
        builder.append(Color.RED);
        builder.setCharAt(9 * W + 11, '1');
        builder.setCharAt(8 * W + 8, '2');
        game.createField(builder.toString());

        Optional<Piece> piece = game.findPiece(new Pair(9, 11));
        assertTrue(piece.isPresent());
        assertEquals(new Piece(9,11, 1), piece.get());

        assertTrue(game.findPiece(new Pair(8,8)).isPresent());
        assertEquals(new Piece(8,8,2), game.findPiece(new Pair(8,8)).get());

        Optional<Piece> piece2 = game.findPiece(new Pair(7, 7));
        assertTrue(piece2.isEmpty());
    }

    @Test
    void testMovePiece() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                builder.append('0');
            }
        }
        builder.append(Color.RED);
        builder.setCharAt(9 * W + 11, '1');
        builder.setCharAt(8 * W + 8, '2');
        game.createField(builder.toString());
        Piece piece = game.getPieces().get(0);
        game.setSelectedPiece(piece);

        game.movePiece(new Pair(9,9));
        assertEquals(0,game.getField()[8][8]);
        assertEquals(2, game.getField()[9][9]);
        assertEquals(1, game.getField()[9][11]);
    }

    @Test
    void testCreateMoveLineForSimple() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                builder.append('0');
            }
        }
        builder.append(Color.RED);
        builder.setCharAt(9 * W + 11, '1');
        builder.setCharAt(8 * W + 8, '2');
        game.createField(builder.toString());
        Piece piece = game.getPieces().get(0);
        game.setSelectedPiece(piece);
        String line = game.createMoveLine(new Pair(8, 6));
        assertEquals("Simple(8,8,8,6)", line);
    }

    @Test
    void testCreateMoveLineForJump() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                builder.append('0');
            }
        }
        builder.append(Color.RED);
        builder.setCharAt(9 * W + 9, '1');
        builder.setCharAt(8 * W + 8, '2');
        game.createField(builder.toString());
        Piece piece = game.getPieces().get(0);
        game.setSelectedPiece(piece);
        String line = game.createMoveLine(new Pair(10, 10));
        assertEquals("Jump(8,8,10,10)", line);
    }

    @Test
    void testCreateMoveLineForImpossible() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                builder.append('0');
            }
        }
        builder.append(Color.RED);
        builder.setCharAt(9 * W + 9, '1');
        builder.setCharAt(8 * W + 8, '2');
        game.createField(builder.toString());
        Piece piece = game.getPieces().get(0);
        game.setSelectedPiece(piece);
        String line = game.createMoveLine(new Pair(11, 11));
        assertEquals("", line);
    }
}