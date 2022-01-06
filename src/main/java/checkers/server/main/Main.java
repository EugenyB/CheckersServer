package checkers.server.main;

import checkers.client.main.model.moves.MoveType;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static checkers.client.main.GameConstants.*;

public class Main {

    private static Main instance;

    private int numOfPlayers;
    private volatile int[][] field;
    private Color[] colors;
    private int playerToMove;
    private MoveType lastMove = MoveType.IMPOSSIBLE;

    private ServerSocket serverSocket;

    final static int H = 17;
    final static int W = 25;

    private int connectedPlayers = 0;
    private List<Player> players = new ArrayList<>();

    public static void main(String[] args) {
        new Main().run();
    }

    public static Main getInstance() {
        return instance;
    }

    private void run() {
        instance = this;
        initialize();
    }

    private void initialize() {
        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("checkers.props"))) {
            props.load(reader);
            numOfPlayers = Integer.parseInt(props.getProperty("numofplayers"));
            if (numOfPlayers < 2 || numOfPlayers > 6 || numOfPlayers == 5) {
                throw new IllegalArgumentException("Wrong number of players");
            }
            colors = new Color[numOfPlayers];
            for (int i = 0; i < numOfPlayers; i++) {
                if (numOfPlayers == 3) {
                    colors[i] = COLORS_3[i+1];
                } else if (numOfPlayers == 4) {
                    colors[i] = COLORS_4[i+1];
                } else  if (numOfPlayers == 6) {
                    colors[i] = COLORS_6[i+1];
                }
                else colors[i] = COLORS[i+1]; // as COLORS[0] - is color of empty cell
            }
            int port = Integer.parseInt(props.getProperty("serverport"));
            serverSocket = new ServerSocket(port);
            fillField();
            System.out.println("Started. Waiting for players...");
            processGame();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void processGame() {
        while (connectedPlayers<numOfPlayers) {
            waitForPlayer();
            //connectedPlayers++;
        }
        System.out.println("Starting!");
        for (Player player : players) {
            //player.sendField(field);
            new Thread(player).start();
        }
        Random random = new Random();
        // todo in final version uncomment this
        //playerToMove = random.nextInt(players.size());
        playerToMove = 0;
        while (true) {
            broadcastMove();
            synchronized (this) {
                try {
                    wait();
                    processMove();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void sendPlayerMove(String str) {
        if (str.startsWith("Simple")) {
            makeMove(str);
            lastMove = MoveType.SIMPLE;
        } else if (str.startsWith("Jump")) {
            makeMove(str);
            lastMove = MoveType.JUMP;
        } else if (str.startsWith("End")) {
            lastMove = MoveType.SIMPLE;
        }
        notify();
    }

    private void makeMove(String str) {
        int posBegin = str.indexOf('(') + 1;
        String line = str.substring(posBegin, str.length()-1);
        System.out.println(line);
        int[] ints = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
        int fromRow = ints[0];
        int fromColumn = ints[1];
        int toRow = ints[2];
        int toColumn = ints[3];
        field[toRow][toColumn] = field[fromRow][fromColumn];
        field[fromRow][fromColumn] = 0;
    }

    public void processMove() {
        // todo change with program logic
        int winner = checkIfOneWinGame();
        if (winner == 0) {
            if (lastMove == MoveType.SIMPLE) playerToMove++;
            if (playerToMove >= players.size()) playerToMove = 0;
        } else {
            gameOver(winner);
        }
    }

    private void gameOver(int winner) {
        // todo player with num==winner is winner - send to all and finish
    }

    private int checkIfOneWinGame() {
        return 0;
    }

    private void broadcastMove() {
        for (Player player : players) {
            player.sendMessage("Move:"+colors[playerToMove]);
            player.updateField(field);
        }
    }

    private void waitForPlayer() {
        try {
            Socket socket = serverSocket.accept();
            Player player = new Player(socket, colors[connectedPlayers++]);
            player.sendField(field);
            players.add(player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillField() {
        field = new int[H][W];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                field[i][j] = 9; // wrong
            }
        }
        for (int i = 4; i <= 12; i++) {
            for (int j = 0; j <= 24; j++) {
                if ((i+j)%2==0) field[i][j] = 0;
            }
        }
        field[6][0] = field[6][24]
                = field[7][1] = field[7][23]
                = field[8][0] = field[8][2]
                = field[8][22] = field[8][24]
                = field[9][1] = field[9][23]
                = field[10][0] = field[10][24] = 9;

        switch (numOfPlayers) {
            case 2: fillForTwo(); break;
            case 3: fillForThree(); break;
            case 4: fillForFour(); break;
            case 6: fillForSix(); break;
        }
    }

    private void fillForSix() {
        field[0][12] = 1;
        field[1][11] = field[1][13] = 1;
        field[2][10] = field[2][12] = field[2][14] = 1;
        field[3][9] = field[3][11] = field[3][13] = field[3][15] = 1;

        field[16][12] = 2;
        field[15][11] = field[15][13] = 2;
        field[14][10] = field[14][12] = field[14][14] = 2;
        field[13][9] = field[13][11] = field[13][13] = field[13][15] = 2;

        field[9][3] = 3;
        field[10][2] = field[10][4] = 3;
        field[11][1] = field[11][3] = field[11][5] = 3;
        field[12][0] = field[12][2] = field[12][4] = field[12][6] = 3;

        field[9][21] = 4;
        field[10][20] = field[10][22] = 4;
        field[11][19] = field[11][21] = field[11][23] = 4;
        field[12][18] = field[12][20] = field[12][22] = field[12][24] = 4;

        field[7][3] = 5;
        field[6][2] = field[6][4] = 5;
        field[5][1] = field[5][3] = field[5][5] = 5;
        field[4][0] = field[4][2] = field[4][4] = field[4][6] = 5;

        field[7][21] = 6;
        field[6][20] = field[6][22] = 6;
        field[5][19] = field[5][21] = field[5][23] = 6;
        field[4][18] = field[4][20] = field[4][22] = field[4][24] = 6;
    }

    private void fillForFour() {
        field[0][12] = 1;
        field[1][11] = field[1][13] = 1;
        field[2][10] = field[2][12] = field[2][14] = 1;
        field[3][9] = field[3][11] = field[3][13] = field[3][15] = 1;

        field[16][12] = 2;
        field[15][11] = field[15][13] = 2;
        field[14][10] = field[14][12] = field[14][14] = 2;
        field[13][9] = field[13][11] = field[13][13] = field[13][15] = 2;

        field[7][3] = 5;
        field[6][2] = field[6][4] = 5;
        field[5][1] = field[5][3] = field[5][5] = 5;
        field[4][0] = field[4][2] = field[4][4] = field[4][6] = 5;

        field[9][21] = 4;
        field[10][20] = field[10][22] = 4;
        field[11][19] = field[11][21] = field[11][23] = 4;
        field[12][18] = field[12][20] = field[12][22] = field[12][24] = 4;
    }

    private void fillForThree() {
        field[0][12] = 1;
        field[1][11] = field[1][13] = 1;
        field[2][10] = field[2][12] = field[2][14] = 1;
        field[3][9] = field[3][11] = field[3][13] = field[3][15] = 1;

        field[9][3] = 3;
        field[10][2] = field[10][4] = 3;
        field[11][1] = field[11][3] = field[11][5] = 3;
        field[12][0] = field[12][2] = field[12][4] = field[12][6] = 3;

        field[9][21] = 4;
        field[10][20] = field[10][22] = 4;
        field[11][19] = field[11][21] = field[11][23] = 4;
        field[12][18] = field[12][20] = field[12][22] = field[12][24] = 4;

        field[16][12] = 0;
        field[15][11] = field[15][13] = 0;
        field[14][10] = field[14][12] = field[14][14] = 0;
        field[13][9] = field[13][11] = field[13][13] = field[13][15] = 0;
    }

    private void fillForTwo() {
        field[0][12] = 1;
        field[1][11] = field[1][13] = 1;
        field[2][10] = field[2][12] = field[2][14] = 1;
        field[3][9] = field[3][11] = field[3][13] = field[3][15] = 1;

        field[16][12] = 2;
        field[15][11] = field[15][13] = 2;
        field[14][10] = field[14][12] = field[14][14] = 2;
        field[13][9] = field[13][11] = field[13][13] = field[13][15] = 2;
    }

    public synchronized void sendExit(Player player) {
        players.remove(player);
    }
}
