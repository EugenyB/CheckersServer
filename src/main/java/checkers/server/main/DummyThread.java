package checkers.server.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static checkers.client.main.GameConstants.SORRY;

public class DummyThread extends Thread {
    private ServerSocket ss;

    public DummyThread(ServerSocket serverSocket) {
        ss = serverSocket;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = ss.accept();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(SORRY);
                out.close();
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
