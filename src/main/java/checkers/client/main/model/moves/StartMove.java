package checkers.client.main.model.moves;

import checkers.client.main.controller.Connection;
import checkers.client.main.model.Game;
import checkers.client.main.util.Pair;

/**
 * Indicates that this player starting move now
 */
public class StartMove extends State {
    @Override
    public State select(Pair p) {
        return new ProcessMove();
    }

    @Override
    public void processPair(Game game, Pair p, Connection connection) {
        game.startMove(p);
    }
}
