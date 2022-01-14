package checkers.server.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static checkers.GameConstants.SORRY;

/**
 * Utility thread for players that are late
 */
public class DummyThread extends Thread {
    private ServerSocket ss;

    public DummyThread(ServerSocket serverSocket) {
        ss = serverSocket;
        setDaemon(true);
    }

    /**
     * run method for dummy thread.
     * After player connected - send "sorry" to him, and disconnect
     */
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
