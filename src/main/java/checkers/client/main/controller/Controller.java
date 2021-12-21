package checkers.client.main.controller;

import checkers.client.main.Piece;
import checkers.client.main.model.Game;
import checkers.client.main.model.moves.OtherMove;
import checkers.client.main.model.moves.State;
import checkers.client.main.util.Pair;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

import static checkers.client.main.GameConstants.IMPOSSIBLE;

public class Controller {
    public final static int SIZE = 40;
    int W = 25;
    int H = 17;
    private Color myColor;
    private State state;

    private Game game;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Connection connection;

    @FXML private Canvas canvas;
    @FXML private Pane pane;

    public Controller() {
    }

    public void initialize() {
        try {
            connectToServer();
            game = new Game();
            game.createField(in.readLine());

            canvas.widthProperty().bind(pane.widthProperty());
            canvas.heightProperty().bind(pane.heightProperty());
            canvas.widthProperty().addListener(((observable, oldValue, newValue) -> draw()));
            canvas.heightProperty().addListener(((observable, oldValue, newValue) -> draw()));

            connection = new Connection(in, out, game);
            new Thread(connection).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printField(int[][] field) {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                System.out.print(field[i][j]);
            }
            System.out.println();
        }
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (Piece piece : game.getPieces()) {
            double y = piece.getRow() * SIZE * Math.sqrt(3) / 2.0;
            double x = piece.getColumn() * SIZE / 2.0;
            gc.setFill(piece.getColor());
            gc.fillOval(x, y, SIZE, SIZE);
            if (piece.isSelected()) {
                // draw selection
                gc.strokeOval(x + SIZE / 4.0, y + SIZE / 4.0, SIZE / 2.0, SIZE / 2.0);
            }
        }
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                if ((i+j)%2 == 0 && game.getField()[i][j] < IMPOSSIBLE) {
                    double y = i * SIZE * Math.sqrt(3) / 2.0;
                    double x = j * SIZE / 2.0;
                    gc.strokeOval(x, y, SIZE, SIZE);
                }
            }
        }
        gc.setFill(game.getPlayerColor());
        gc.fillRect(0, H * SIZE * Math.sqrt(3) / 2 + SIZE, canvas.getWidth(), SIZE);
    }

    public void processMove(MouseEvent e) {
        if (game.getPlayerState().getClass() == OtherMove.class) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("info");
            alert.setContentText("It's not your turn!");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        double x = e.getX();
        double y = e.getY();
        Pair p = findCell(x,y);
        // todo checkCell
        if (p != null) {
            Optional<Piece> f = game.findPiece(p);
            if (f.isEmpty()) {
                System.out.println("none");
            } else {
                if (f.get().getColor().equals(game.getPlayerColor())) {
                    System.out.println("ok");
                    Piece piece = f.get();
                    select(piece);
                    draw();
                } else {
                    System.out.println("err");
                }
            }
        }
    }

    private void select(Piece piece) {
        for (Piece p : game.getPieces()) {
            p.setSelected(p.equals(piece));
        }
    }

    private Pair findCell(double x, double y) {
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                if ((i+j)%2 == 0 && game.getField()[i][j] < IMPOSSIBLE) {
                    double yc = i * SIZE * Math.sqrt(3) / 2.0 + SIZE / 2.0;
                    double xc = j * SIZE / 2.0 + SIZE / 2.0;
                    if (Math.hypot(x-xc, y-yc) < SIZE / 2.0) return new Pair(i,j);
                }
            }
        }
        return null;
    }
}
