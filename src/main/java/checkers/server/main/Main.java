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

import static checkers.GameConstants.*;

public class Main {

    private static Main instance;

    private int numOfPlayers;
    private volatile int[][] field;
    private Color[] colors;
    private int playerToMove;
    private MoveType lastMove = MoveType.IMPOSSIBLE;
    private int gameWinner = 0;

    private ServerSocket serverSocket;

    private int connectedPlayers = 0;
    private List<Player> players = new ArrayList<>();

    public static void main(String[] args) {
        new Main().run();
    }

    public static Main getInstance() {
        return instance;
    }

    /**
     * Running server App
     */
    private void run() {
        instance = this;
        initialize();
        processGame();
    }

    /**
     * Read config of server
     * Reads parameters from property file
     * Configuring colors for players
     * Start Server
     * Waiting for clients
     * After connect of all clients - starting game process
     */
    void initialize() {
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
            setField(fillField(numOfPlayers));
            System.out.println("Started. Waiting for players...");
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Waiting while players connected
     * Main server loop
     * Broadcasting moves
     */
    private void processGame() {
        while (connectedPlayers<numOfPlayers) {
            waitForPlayer();
            //connectedPlayers++;
        }

        // running thread for connections, that can't be a player
        new DummyThread(serverSocket).start();

        System.out.println("Starting!");
        for (Player player : players) {
            new Thread(player).start();
        }

        Random random = new Random();
        playerToMove = random.nextInt(players.size());

        boolean gameInProgress = true;
        while (gameInProgress) {
            broadcastMove();
            synchronized (this) {
                try {
                    wait();
                    gameInProgress = processMove();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        broadcastGameOver();
    }

    /**
     * Sending to clients Game Over Signal. And Color of Winner
     */
    private synchronized void broadcastGameOver() {
        for (Player player : players) {
            player.updateField(field);
            player.sendMessage(WINNER + colors[gameWinner-1]);
        }
    }

    /**
     * process sent player move
     * @param str - text line with move from client
     */
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

    /**
     * perfoms player's move in field
     * @param str - encoded string about player's move
     */
    public int[][] makeMove(String str) {
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
        return field;
    }

    /**
     * Processing last move
     * @return true if game in process and false if game is over
     */
    public boolean processMove() {
        int winner = checkIfOneWinGame();
        if (winner == 0) {
            if (lastMove == MoveType.SIMPLE) playerToMove++;
            if (playerToMove >= players.size()) playerToMove = 0;
            return true;
        } else {
            gameWinner = winner;
            return false;
        }
    }

    /**
     * Find winner's num
     * @return number from 1 to 6 - number of winner, or 0 if game is not over
     */
    private int checkIfOneWinGame() {
        if (firstWinGame(field)) return 1;
        if (secondWinGame(field)) return 2;
        if (thirdWinGame(field)) return 3;
        if (fourthWinGame(field)) return 4;
        if (fifthWinGame(field)) return 5;
        if (sixthWinGame(field)) return 6;
        return 0;
    }

    boolean firstWinGame(int[][] field) {
        return field[16][12] == 1 &&
                field[15][11] == 1 && field[15][13] == 1 &&
                field[14][10] == 1 && field[14][12] == 1 && field[14][14] == 1 &&
                field[13][9] == 1 && field[13][11] == 1 && field[13][13] == 1 && field[13][15] == 1;
    }

    boolean secondWinGame(int[][] field) {
        return field[0][12] == 2 &&
                field[1][11] == 2 && field[1][13] == 2 &&
                field[2][10] == 2 && field[2][12] == 2 && field[2][14] == 2 &&
                field[3][9] == 2 && field[3][11] == 2 && field[3][13] == 2 && field[3][15] == 2;
    }

    boolean thirdWinGame(int[][] field) {
        return field[7][21] == 3 &&
                field[6][20] == 3 && field[6][22] == 3 &&
                field[5][19] == 3 && field[5][21] == 3 && field[5][23] == 3 &&
                field[4][18] == 3 && field[4][20] == 3 && field[4][22] == 3 && field[4][24] == 3;
    }

    boolean fourthWinGame(int[][] field) {
        return field[7][3] == 4 &&
                field[6][2] == 4 && field[6][4] == 4 &&
                field[5][1] == 4 && field[5][3] == 4 && field[5][5] == 4 &&
                field[4][0] == 4 && field[4][2] == 4 && field[4][4] == 4 && field[4][6] == 4;
    }

    boolean fifthWinGame(int[][] field) {
        return field[9][21] == 5 &&
                field[10][20] == 5 && field[10][22] == 5 &&
                field[11][19] == 5 && field[11][21] == 5 && field[11][23] == 5 &&
                field[12][18] == 5 && field[12][20] == 5 && field[12][22] == 5 && field[12][24] == 5;
    }

    boolean sixthWinGame(int[][] field) {
        return field[9][3] == 6 &&
                field[10][2] == 6 && field[10][4] == 6 &&
                field[11][1] == 6 && field[11][3] == 6 && field[11][5] == 6 &&
                field[12][0] == 6 && field[12][2] == 6 && field[12][4] == 6 && field[12][6] == 6;
    }

    /**
     * Broadcast moves to clients. As many instances of thread - this method is synchronized
     */
    private synchronized void broadcastMove() {
        for (Player player : players) {
            player.sendMessage(MOVE+colors[playerToMove]);
            player.updateField(field);
        }
    }

    /**
     * Waiting for player connect after start of server
     */
    private void waitForPlayer() {
        try {
            Socket socket = serverSocket.accept();
            Player player = new Player(socket, colors[connectedPlayers++]);
            player.sendMessage(WELCOME);
            player.sendField(field);
            players.add(player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int[][] fillField(int numOfPlayers) {
        int[][] field = new int[H][W];
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
        field[6][0] = field[6][24] =
                field[7][1] = field[7][23] =
                field[8][0] = field[8][2] =
                field[8][22] = field[8][24] =
                field[9][1] = field[9][23] =
                field[10][0] = field[10][24] = 9;

        switch (numOfPlayers) {
            case 2: fillForTwo(field); break;
            case 3: fillForThree(field); break;
            case 4: fillForFour(field); break;
            case 6: fillForSix(field); break;
        }
        return field;
    }

    private void fillForSix(int[][] field) {
        fillFieldForPlayer1(field);
        fillFieldForPlayer2(field);

        fillFieldForPlayer3(field);
        fillFieldForPlayer4(field);

        fillFieldForPlayer5(field);
        fillFieldForPlayer6(field);
    }

    private void fillForFour(int[][] field) {
        fillFieldForPlayer1(field);
        fillFieldForPlayer2(field);

        fillFieldForPlayer4(field);
        fillFieldForPlayer5(field);
    }

    private void fillForThree(int[][] field) {
        fillFieldForPlayer1(field);

        fillFieldForPlayer3(field);
        fillFieldForPlayer4(field);

        fillEmptyPart(field);
    }

    private void fillForTwo(int[][] field) {
        fillFieldForPlayer1(field);
        fillFieldForPlayer2(field);
    }

    public void fillFieldForPlayer1(int[][] field) {
        field[0][12] = 1;
        field[1][11] = field[1][13] = 1;
        field[2][10] = field[2][12] = field[2][14] = 1;
        field[3][9] = field[3][11] = field[3][13] = field[3][15] = 1;
    }

    public void fillFieldForPlayer2(int[][] field) {
        field[16][12] = 2;
        field[15][11] = field[15][13] = 2;
        field[14][10] = field[14][12] = field[14][14] = 2;
        field[13][9] = field[13][11] = field[13][13] = field[13][15] = 2;
    }

    public void fillFieldForPlayer3(int[][] field) {
        field[9][3] = 3;
        field[10][2] = field[10][4] = 3;
        field[11][1] = field[11][3] = field[11][5] = 3;
        field[12][0] = field[12][2] = field[12][4] = field[12][6] = 3;
    }

    public void fillFieldForPlayer4(int[][] field) {
        field[9][21] = 4;
        field[10][20] = field[10][22] = 4;
        field[11][19] = field[11][21] = field[11][23] = 4;
        field[12][18] = field[12][20] = field[12][22] = field[12][24] = 4;
    }

    public void fillFieldForPlayer5(int[][] field) {
        field[7][3] = 5;
        field[6][2] = field[6][4] = 5;
        field[5][1] = field[5][3] = field[5][5] = 5;
        field[4][0] = field[4][2] = field[4][4] = field[4][6] = 5;
    }

    public void fillFieldForPlayer6(int[][] field) {
        field[7][21] = 6;
        field[6][20] = field[6][22] = 6;
        field[5][19] = field[5][21] = field[5][23] = 6;
        field[4][18] = field[4][20] = field[4][22] = field[4][24] = 6;
    }

    public void fillEmptyPart(int[][] field) {
        field[16][12] = 0;
        field[15][11] = field[15][13] = 0;
        field[14][10] = field[14][12] = field[14][14] = 0;
        field[13][9] = field[13][11] = field[13][13] = field[13][15] = 0;
    }

    /**
     * send info that player was gone.
     * if all players gone - exit program
     * @param player number of gone player
     */
    public synchronized void sendExit(Player player) {
        players.remove(player);
        if (players.isEmpty()) {
            System.exit(0);
        }
    }

    int[][] getField() {
        return field;
    }

    public void setField(int[][] field) {
        this.field = field;
    }
}
