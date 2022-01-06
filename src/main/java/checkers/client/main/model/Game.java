package checkers.client.main.model;

import checkers.client.main.Piece;
import checkers.client.main.controller.Controller;
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
    private Piece selectedPiece = null;

    private MoveType lastValidMove = MoveType.IMPOSSIBLE;

    public MoveType isMoveValid(int startRow, int startCol, int endRow, int endCol) {
        // начальная клетка должна быть с фигурой цвета игрока, если нет - уходим...
        if (!COLORS[field[startRow][startCol]].equals(playerColor)) return MoveType.IMPOSSIBLE;
        // если конечная клетка совпадает с начальной и предыдущий ход Jump, то это завершение прыжка
        if (startRow == endRow && startCol == endCol && lastValidMove == MoveType.JUMP) return MoveType.END;
        // конечная клетка должна быть свободна, если нет - уходим...
        if (field[endRow][endCol]!=0) return MoveType.IMPOSSIBLE;
        // простой ход без прыжка
        if (Math.abs(endRow-startRow)==1 && Math.abs(endCol - startCol) == 1 && lastValidMove != MoveType.JUMP) return MoveType.SIMPLE;
        if (Math.abs(endCol-startCol)==2 && endRow==startRow && lastValidMove != MoveType.JUMP) return MoveType.SIMPLE;
        // прыжок
        if (lastValidMove!=MoveType.SIMPLE) {
            int midRow = (endRow + startRow) / 2;
            int midCol = (endCol + startCol) / 2;
            if (Math.abs(endRow - startRow) == 2 && Math.abs(endCol - startCol) == 2 && isOccupied(field[midRow][midCol]))
                return MoveType.JUMP;
            if (Math.abs(endCol - startCol) == 4 && endRow == startRow && isOccupied(field[midRow][midCol]))
                return MoveType.JUMP;
        }
        return MoveType.IMPOSSIBLE;
    }

    public MoveType isMoveValid(Pair p) {
        if (selectedPiece == null) return MoveType.IMPOSSIBLE;
        if (p == null) return MoveType.IMPOSSIBLE;
        MoveType move = isMoveValid(selectedPiece.getRow(), selectedPiece.getColumn(), p.getI(), p.getJ());
        if (move != MoveType.IMPOSSIBLE) lastValidMove = move;
        return move;
    }

    public void createField(String s) {
            field = new int[H][W];
            int k = 0;
            pieces.clear();
            for (int i = 0; i < H; i++) {
                for (int j = 0; j < W; j++) {
                    field[i][j] = s.charAt(k++) - '0';
                    if (field[i][j] > 0 && field[i][j] < 9) {
                        pieces.add(new Piece(i, j, field[i][j]));
                    }
                }
            }
        //updateField(s);
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
            Controller.getInstance().setMoveState(true);
        } else {
            playerState = new OtherMove();
            Controller.getInstance().setMoveState(false);
        }
    }

    public void processGameOver(String line) {
        // todo process game over
    }

    public void updateField(String s) {
        int k = 0;
        pieces.clear();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                field[i][j] = s.charAt(k++) - '0';
                if (field[i][j] > 0 && field[i][j] < 9) {
                    pieces.add(new Piece(i, j, field[i][j]));
                }
            }
        }
    }

    public void simpleMovePiece(Pair p) {
        movePiece(p);
        endMove(p);
    }

    public void jumpMovePiece(Pair p) {
        movePiece(p);
    }

    private void movePiece(Pair p) {
        int color = field[selectedPiece.getRow()][selectedPiece.getColumn()];
        field[selectedPiece.getRow()][selectedPiece.getColumn()] = 0;
        selectedPiece.setRow(p.getI());
        selectedPiece.setColumn(p.getJ());
        field[selectedPiece.getRow()][selectedPiece.getColumn()] = color;
        selectedPiece.setSelected(false);
    }

    public void endMove(Pair p) {
        selectedPiece = null;
        lastValidMove = MoveType.END;
    }
}
