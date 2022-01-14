package checkers.client.main.model.moves;

import checkers.client.main.controller.Connection;
import checkers.client.main.model.Game;
import checkers.client.main.util.Pair;

/**
 * Indicates that this player in move process
 */
public class ProcessMove extends State {
    @Override
    public State select(Pair p) {
        return null;
    }

    @Override
    public void processPair(Game game, Pair p, Connection connection) {
        game.makeMove(p, connection);
    }
}
