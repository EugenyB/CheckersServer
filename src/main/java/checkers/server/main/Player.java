package checkers.server.main;

import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;

/**
 * Thread for communication with player
 */
public class Player implements Runnable {
    private Socket socket;
    private Color color;
    private PrintWriter out;
    private BufferedReader in;
    private boolean finished = false;

    /**
     * Constructor for Player
     * @param socket socket object for communication with client
     * @param color game color of player's pieces
     */
    public Player(Socket socket, Color color) {
        this.socket = socket;
        this.color = color;
        System.out.println(color);
        createInOut(socket);
    }

    public Player() {
    }

    /**
     * Creating in/out reader and writer for communication with client
     * @param socket socket of connection
     */
    private void createInOut(Socket socket) {
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run method of thread
     * Performs communication with player and Main server object
     */
    @Override
    public void run() {
        out.println("color=" + color);
        while (!finished) {
            process();
        }
        out.println("exiting");
        Main.getInstance().sendExit(this);
        System.out.println("exiting");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * One step of communication
     */
    private void process() {
        try {
            String str = in.readLine();
            System.out.println(str);
            Main.getInstance().sendPlayerMove(str);
            if (str.isBlank()) finished = true;
        } catch (IOException e) {
            finished = true;
            // todo deal with exit of player
            if (e.getMessage().contains("Connection reset")) {
                System.out.println("Player is gone");
            }
        }
    }

    /**
     * Create StringBuilder with field info
     * @param field game field
     * @return string with encoded game field
     */
    public StringBuilder createFieldInfo(int[][] field) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                buf.append(field[i][j]);
            }
        }
        return buf;
    }

    /**
     * Sends field from server to client
     * @param field game field
     * as "tail" of field info, send color of player's pieces
     */
    public void sendField(int[][] field) {
        StringBuilder buf = createFieldInfo(field);
        buf.append(color);
        out.println(buf);
    }

    /**
     * Sends update info about field
     * @param field game field
     */
    public void updateField(int[][] field) {
        StringBuilder buf = createFieldInfo(field);
        out.println(buf);
    }

    /**
     * Sends message for client
     * @param s message
     */
    public synchronized void sendMessage(String s) {
        out.println(s);
    }
}
