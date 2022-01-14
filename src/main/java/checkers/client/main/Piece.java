package checkers.client.main;

import javafx.scene.paint.Color;
import lombok.Data;

import java.util.Objects;

import static checkers.GameConstants.COLORS;

@Data
public class Piece {
    private int row;
    private int column;
    private Color color;
    private boolean selected;

    public Piece(int row, int column, int colorNum) {
        this.row = row;
        this.column = column;
        this.color = COLORS[colorNum];
        selected = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return row == piece.row && column == piece.column && Objects.equals(color, piece.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column, color);
    }
}
