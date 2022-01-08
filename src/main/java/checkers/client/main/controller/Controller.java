package checkers.client.main.controller;

import checkers.client.main.Piece;
import checkers.client.main.model.Game;
import checkers.client.main.model.moves.*;
import checkers.client.main.util.GameParameters;
import checkers.client.main.util.Pair;
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
import java.util.Properties;

import static checkers.client.main.GameConstants.IMPOSSIBLE;
import static checkers.client.main.GameConstants.SORRY;

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
            String line = game.readWelcome(in.readLine());
            if (SORRY.equals(line)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Come again later!");
                alert.setTitle("Sorry");
                alert.setHeaderText("Game already started!");
                alert.showAndWait();
                System.exit(2);
            }
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please, restart and reconnect");
            alert.setTitle("Connection refused");
            alert.setHeaderText("Server not found, or wrong configured");
            alert.showAndWait();
            System.exit(1);
            //e.printStackTrace();
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
        game.processPair(p, connection);
//        game.getPlayerState().processPair(p, game, connection);
        draw();
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

    public void showWin() {
        Platform.runLater(()->{
            moveIndicator.setText("You are the Winner!");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Congrats!");
            alert.setHeaderText(null);
            alert.setX(GameParameters.getInstance().getX());
            alert.setY(GameParameters.getInstance().getY());
            alert.showAndWait();
        });
    }

    public void showLose() {
        Platform.runLater(()-> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Sorry you are loose");
            alert.setHeaderText(null);
            alert.setX(GameParameters.getInstance().getX());
            alert.setY(GameParameters.getInstance().getY());
            alert.showAndWait();
        });
    }
}
