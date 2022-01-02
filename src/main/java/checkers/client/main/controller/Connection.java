package checkers.client.main.controller;

import checkers.client.main.model.Game;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Connection implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private Game game;

    public Connection(BufferedReader in, PrintWriter out, Game game) {
        this.in = in;
        this.out = out;
        this.game = game;
    }

    @Override
    public void run() {
        try {
            String line;
            while (isGameProceed(line = in.readLine())) {
                if (line.startsWith("Field:")) {
                    game.updateField(line.substring("Field:".length()));
                } else if (line.startsWith("Move:")) {
                    game.processChangeMove(line.substring(5));
                } else if (line.startsWith("Turn:")) {
                    game.processTurn(line.substring(5));
                } else if (line.startsWith("End")) {
                    game.processGameOver();
                }
//                Platform.runLater(()->Controller.getInstance().draw());
                Controller.getInstance().draw();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isGameProceed(String line) {
        return true;
    }

    public void send(String line) {
        out.println(line);
    }
}
