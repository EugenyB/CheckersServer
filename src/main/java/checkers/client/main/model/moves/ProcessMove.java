package checkers.client.main.model.moves;

import checkers.client.main.controller.Connection;
import checkers.client.main.model.Game;
import checkers.client.main.util.Pair;

public class ProcessMove extends State {
    @Override
    public State select(Pair p) {
        // todo if (move finished) return new OtherMove(); else return this;
        return null;
    }

    @Override
    public void processPair(Game game, Pair p, Connection connection) {
        game.makeMove(p, connection);
    }
}
