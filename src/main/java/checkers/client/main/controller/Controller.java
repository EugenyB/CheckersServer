package checkers.client.main.controller;

import checkers.client.main.CheckersClientApp;
import checkers.client.main.Piece;
import checkers.client.main.model.Game;
import checkers.client.main.model.moves.*;
import checkers.client.main.util.Pair;
import checkers.server.main.Player;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.Properties;

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

    private static Controller instance;

    @FXML private Canvas canvas;
    @FXML private Pane pane;
    @FXML private Label moveIndicator;

    public Controller() {
    }

    public static Controller getInstance() {
        return instance;
    }

    public void initialize() {
        instance = this;
        try {
            connectToServer();
            game = new Game();
            game.createField(in.readLine());

            canvas.widthProperty().bind(pane.widthProperty());
            canvas.heightProperty().bind(pane.heightProperty());
            canvas.widthProperty().addListener(((observable, oldValue, newValue) -> draw()));
            canvas.heightProperty().addListener(((observable, oldValue, newValue) -> draw()));

            connection = new Connection(in, out, game);
//            new Thread(connection).start();
            Thread thread = new Thread(connection);
            thread.setDaemon(true);
            thread.start();
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
        try (BufferedReader reader = new BufferedReader(new FileReader("checkers.props"))){
            Properties props = new Properties();
            props.load(reader);
            String host = props.getProperty("server", "localhost");
            String port = props.getProperty("serverport", "12345");
            Socket socket = new Socket(host, Integer.parseInt(port));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Piece piece : game.getPieces()) {
            double y = piece.getRow() * SIZE * Math.sqrt(3) / 2.0;
            double x = piece.getColumn() * SIZE / 2.0;
            gc.setFill(piece.getColor());
            gc.fillOval(x, y, SIZE, SIZE);
            if (piece.equals(game.getSelectedPiece())) {
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
        if (game.getPlayerState().getClass() == StartMove.class) {
            if (p != null) {
                Optional<Piece> f = game.findPiece(p);
                if (f.isEmpty()) {
                    System.out.println("none");
                } else {
                    if (f.get().getColor().equals(game.getPlayerColor())) {
                        System.out.println("ok");
                        Piece piece = f.get();
                        select(piece);
                        game.setPlayerState(game.getPlayerState().select(p));
                        //connection.send(String.format("Start(%d,%d)", p.getI(), p.getJ()));
                        draw();
                    } else {
                        System.out.println("err");
                    }
                }
            }
        } else if (game.getPlayerState().getClass() == ProcessMove.class) {
            if (p!=null) {
                MoveType type = game.isMoveValid(p);
                //System.out.println(valid);
                if (type == MoveType.SIMPLE) {
                    Piece sp = game.getSelectedPiece();
                    String moveLine = String.format("Simple(%d,%d,%d,%d)", sp.getRow(), sp.getColumn(), p.getI(), p.getJ());
                    game.simpleMovePiece(p);
                    connection.send(moveLine);
                }
                else if (type == MoveType.JUMP) {
                    Piece sp = game.getSelectedPiece();
                    String moveLine = String.format("Jump(%d,%d,%d,%d)", sp.getRow(), sp.getColumn(), p.getI(), p.getJ());
                    game.jumpMovePiece(p);
                    connection.send(moveLine);
                } else if (type == MoveType.END) {
                    //Piece sp = game.getSelectedPiece();
                    game.endMove(p);
                    connection.send("End");
                }
                draw();
            }
        }
    }

    private void select(Piece piece) {
        for (Piece p : game.getPieces()) {
            p.setSelected(p.equals(piece));
        }
        game.setSelectedPiece(piece);
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

    public void setMoveState(boolean state) {
        Platform.runLater(()->moveIndicator.setText(state ? "Your turn!" : " "));
    }
}
