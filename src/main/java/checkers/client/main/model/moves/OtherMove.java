package checkers.client.main.model.moves;

import checkers.client.main.controller.Connection;
import checkers.client.main.model.Game;
import checkers.client.main.util.Pair;

public class OtherMove extends State {
    @Override
    public State select(Pair p) {
        return this;
    }

    @Override
    public void processPair(Game game, Pair p, Connection connection) {
        // do nothing
    }
}
