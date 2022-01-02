package checkers.server.main;

import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;

public class Player implements Runnable {
    private Socket socket;
    private Color color;
    private PrintWriter out;
    private BufferedReader in;
    private boolean finished = false;

    public Player(Socket socket, Color color) {
        this.socket = socket;
        this.color = color;
        System.out.println(color);
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        out.println("color=" + color);
        while (!finished) {
            process();
        }
        out.println("exiting");
        System.out.println("exiting");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process() {
        try {
            String str = in.readLine();
            System.out.println(str);
            Main.getInstance().sendPlayerMove(str);
            if (str.isBlank()) finished = true;
        } catch (IOException e) {
            finished = true;
            e.printStackTrace();
        }
    }

    public void sendField(int[][] field) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                buf.append(field[i][j]);
            }
        }
        buf.append(color);
        out.println(buf);
    }

    public void updateField(int[][] field) {
        StringBuilder buf = new StringBuilder("Field:");
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                buf.append(field[i][j]);
            }
        }
        out.println(buf);
    }

    public synchronized void sendMessage(String s) {
        out.println(s);
    }
}
