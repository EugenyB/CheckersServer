package checkers.client.main.model;

import checkers.client.main.Piece;
import checkers.client.main.controller.Connection;
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

import static checkers.GameConstants.*;

/**
 * Main class of Client App - contains game model
 */
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

    /**
     * Checks if move is valid
     * @param startRow start row of move
     * @param startCol start column of move
     * @param endRow end row of move
     * @param endCol end column of move
     * @return type of Move - corresponding value of MoveType enum
     */
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

    /**
     * Check if move is valid. If position is valid - performs check with coordinates.
     * @param p position of selection
     * @return type of Move - corresponding value of MoveType enum
     */
    public MoveType isMoveValid(Pair p) {
        if (selectedPiece == null) return MoveType.IMPOSSIBLE;
        if (p == null) return MoveType.IMPOSSIBLE;
        MoveType move = isMoveValid(selectedPiece.getRow(), selectedPiece.getColumn(), p.getI(), p.getJ());
        if (move != MoveType.IMPOSSIBLE) lastValidMove = move;
        return move;
    }

    /**
     * Creating field from encoded string, received from server.
     * Sets Player's pieces color.
     * @param s encoded string
     */
    public void createField(String s) {
        fillField(s);
        //updateField(s);
        // Color 0xff0000ff
        String tail = s.substring(H * W + 2, H * W + 8);
        field[0][0] = Integer.parseInt(tail, 16);
        int col = field[0][0];
        playerColor = Color.rgb(col / 0x100 / 0x100, col / 0x100 % 0x100, col % 0x100); // ff 00 ff 0x100
    }

    /**
     * Fill field from encoded string
     * @param s encoded string
     */
    public void fillField(String s) {
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
        playerState = new OtherMove();
    }

    /**
     * checks if cell is occupied or free
     * @param cellValue value in cell
     * @return true if cell is occupied
     */
    boolean isOccupied(int cellValue) {
        return cellValue > 0 && cellValue < IMPOSSIBLE;
    }

    /**
     * Finds piece with pair coordinates
     * @param p pair of cell
     * @return Optional, that contain found piece or empty Optional
     */
    public Optional<Piece> findPiece(Pair p) {
        return pieces.stream().filter(f -> f.getRow() == p.getI() && f.getColumn() == p.getJ()).findFirst();
    }

    /**
     * Process last move of player and changes it's state
     * @param colorTxt color of moving player
     */
    public void processChangeMove(String colorTxt) {
        int col = Integer.parseInt(colorTxt.substring(2, 8), 16);
        Color colorToMove = Color.rgb(col / 0x100 / 0x100, col / 0x100 % 0x100, col % 0x100);
        if (colorToMove.equals(playerColor)) {
            if (lastValidMove != MoveType.JUMP) {
                playerState = new StartMove();
            }
            Controller.getInstance().setMoveState(true);
        } else {
            playerState = new OtherMove();
            Controller.getInstance().setMoveState(false);
        }
    }

    /**
     * Handles game over info: Win or Loose
     * @param colorTxt - color of winner in text format
     */
    public void processGameOver(String colorTxt) {
        int col = Integer.parseInt(colorTxt.substring(2, 8), 16);
        Color winnerColor = Color.rgb(col / 0x100 / 0x100, col / 0x100 % 0x100, col % 0x100);
        if (winnerColor.equals(playerColor)) {
            Controller.getInstance().showWin();
        } else {
            Controller.getInstance().showLose();
        }
    }

    /**
     * Updates field with encoded string from server
     * @param s encoded string with field
     */
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

    /**
     * Performs simple move without jump
     * @param p new cell for selected piece
     */
    public void simpleMovePiece(Pair p) {
        movePiece(p);
        endMove(p);
    }

    /**
     * Performs jump move
     * @param p new cell for selected piece
     */
    public void jumpMovePiece(Pair p) {
        movePiece(p);
        selectedPiece.setSelected(true);
    }

    /**
     * moving selected piece
     * @param p new position for selected piece
     */
    private void movePiece(Pair p) {
        int color = field[selectedPiece.getRow()][selectedPiece.getColumn()];
        field[selectedPiece.getRow()][selectedPiece.getColumn()] = 0;
        selectedPiece.setRow(p.getI());
        selectedPiece.setColumn(p.getJ());
        field[selectedPiece.getRow()][selectedPiece.getColumn()] = color;
        selectedPiece.setSelected(false);
    }

    /**
     * ends move and sends move order to another player
     * @param p finish position of moved piece
     */
    public void endMove(Pair p) {
        selectedPiece = null;
        lastValidMove = MoveType.END;
    }

    /**
     * Reads Welcome message and trims it
     * @param line welcome message
     * @return
     */
    public String readWelcome(String line) {
        return line.trim().toLowerCase();
    }

    /**
     * Performs move and sends to server info about it
     * @param p new position of selection
     * @param connection object that perform communications
     */
    public void makeMove(Pair p, Connection connection) {
        if (p !=null) {
            MoveType type = isMoveValid(p);
            //System.out.println(valid);
            if (type == MoveType.SIMPLE) {
                Piece sp = getSelectedPiece();
                String moveLine = String.format("Simple(%d,%d,%d,%d)", sp.getRow(), sp.getColumn(), p.getI(), p.getJ());
                simpleMovePiece(p);
                connection.send(moveLine);
            }
            else if (type == MoveType.JUMP) {
                Piece sp = getSelectedPiece();
                String moveLine = String.format("Jump(%d,%d,%d,%d)", sp.getRow(), sp.getColumn(), p.getI(), p.getJ());
                jumpMovePiece(p);
                connection.send(moveLine);
            } else if (type == MoveType.END) {
                endMove(p);
                connection.send("End");
            }
        }
    }

    /**
     * Starts move
     * @param p position of selected cell
     */
    public void startMove(Pair p) {
        if (p != null) {
            Optional<Piece> f = findPiece(p);
            if (f.isEmpty()) {
                System.out.println("none");
            } else {
                if (f.get().getColor().equals(getPlayerColor())) {
                    System.out.println("ok");
                    Piece piece = f.get();
                    select(piece);
                    setPlayerState(getPlayerState().select(p));
                } else {
                    System.out.println("err");
                }
            }
        }
    }

    /**
     * Selects Piece
     * @param piece piece that will be selected
     */
    private void select(Piece piece) {
        for (Piece p : getPieces()) {
            p.setSelected(p.equals(piece));
        }
        setSelectedPiece(piece);
    }

    /**
     * Handle pair with player state
     * @param p pair of cell's coordinates
     * @param connection object that perform communications
     */
    public void processPair(Pair p, Connection connection) {
        playerState.processPair(this, p, connection);
    }
}
