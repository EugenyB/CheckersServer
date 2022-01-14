package checkers.client.main.controller;

import checkers.client.main.model.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static checkers.GameConstants.*;

/**
 * Connection with server
 */
public class Connection implements Runnable {

    private final BufferedReader in;
    private final PrintWriter out;
    private final Game game;

    /**
     * Creates connection with server and sets I/O and Game objects
     * @param in reader for read info from server
     * @param out writer to post info to server
     * @param game game object for process game with
     */
    public Connection(BufferedReader in, PrintWriter out, Game game) {
        this.in = in;
        this.out = out;
        this.game = game;
    }

    /**
     * Main method of runnable object
     * loop with server communications - reading messages from server
     */
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

    /**
     * Sending messages to server
     * @param line - text of message
     */
    public void send(String line) {
        out.println(line);
    }
}
