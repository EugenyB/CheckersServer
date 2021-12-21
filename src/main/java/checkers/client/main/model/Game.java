package checkers.client.main.model;

import checkers.client.main.Piece;
import checkers.client.main.model.moves.MoveType;
import checkers.client.main.model.moves.OtherMove;
import checkers.client.main.model.moves.StartMove;
import checkers.client.main.model.moves.State;
import checkers.client.main.util.Pair;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static checkers.client.main.GameConstants.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    private int[][] field;
    private List<Piece> pieces = new ArrayList<>();
    private Color playerColor;
    private State playerState;


    public MoveType isMoveValid(int startRow, int startCol, int endRow, int endCol) {
        // начальная клетка должна быть с фигурой цвета игрока, если нет - уходим...
        if (!COLORS[field[startRow][startCol]].equals(playerColor)) return MoveType.IMPOSSIBLE;
        // конечная клетка должна быть свободна, если нет - уходим...
        if (field[endRow][endCol]!=0) return MoveType.IMPOSSIBLE;
        // простой ход без прыжка
        if (Math.abs(endRow-startRow)==1 && Math.abs(endCol - startCol) == 1) return MoveType.SIMPLE;
        if (Math.abs(endCol-startCol)==2 && endRow==startRow) return MoveType.SIMPLE;
        // прыжок
        int midRow = endRow-startRow;
        int midCol = endCol-startCol;
        if (Math.abs(endRow-startRow)==2 && Math.abs(endCol - startCol) == 2 && isOccupied(field[midRow][midCol])) return MoveType.JUMP;
        if (Math.abs(endCol-startCol)==4 && endRow==startRow && isOccupied(field[midRow][midCol])) return MoveType.JUMP;
        return MoveType.IMPOSSIBLE;
    }

    public void createField(String s) {
            field = new int[H][W];
            int k = 0;
            for (int i = 0; i < H; i++) {
                for (int j = 0; j < W; j++) {
                    field[i][j] = s.charAt(k++) - '0';
                    if (field[i][j] > 0 && field[i][j] < 9) {
                        pieces.add(new Piece(i, j, field[i][j]));
                    }
                }
            }
            // Color 0xff0000ff
            String tail = s.substring(H*W+2, H*W+8);
            field[0][0] = Integer.parseInt(tail,16);
            int col = field[0][0];
            playerColor = Color.rgb(col / 0x100 / 0x100, col / 0x100 % 0x100, col % 0x100); // ff 00 ff 0x100
        playerState = new OtherMove();
    }

    private boolean isOccupied(int cellValue) {
        return cellValue > 0 && cellValue < IMPOSSIBLE;
    }

    public Optional<Piece> findPiece(Pair p) {
        return pieces.stream().filter(f -> f.getRow() == p.getI() && f.getColumn() == p.getJ()).findFirst();
    }

    public void processChangeMove(String colorTxt) {
        int col = Integer.parseInt(colorTxt.substring(2, 8), 16);
        Color colorToMove = Color.rgb(col / 0x100 / 0x100, col / 0x100 % 0x100, col % 0x100);
        if (colorToMove.equals(playerColor)) {
            playerState = new StartMove();
        } else {
            playerState = new OtherMove();
        }
    }

    public void processTurn(String turn) {
        // todo create process turn
    }

    public void processGameOver() {
        // todo process game over
    }

    public void updateField(String line) {
        // todo process update field (as in create field)
    }
}
