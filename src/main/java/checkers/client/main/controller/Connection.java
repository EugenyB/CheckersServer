package checkers.client.main.controller;

import checkers.client.main.model.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static checkers.client.main.GameConstants.*;

public class Connection implements Runnable {

    private final BufferedReader in;
    private final PrintWriter out;
    private final Game game;

    public Connection(BufferedReader in, PrintWriter out, Game game) {
        this.in = in;
        this.out = out;
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String line = in.readLine();
                if (line.startsWith(FIELD)) {
                    game.updateField(line.substring(FIELD.length()));
                } else if (line.startsWith(MOVE)) {
                    game.processChangeMove(line.substring(MOVE.length()));
                } else if (line.startsWith(WINNER)) {
                    game.processGameOver(line.substring(WINNER.length()));
                    in.close();
                    out.close();
                    break;
                }
                Controller.getInstance().draw();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String line) {
        out.println(line);
    }
}
