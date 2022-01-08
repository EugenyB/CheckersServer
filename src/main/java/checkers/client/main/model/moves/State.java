package checkers.client.main.model.moves;

import checkers.client.main.controller.Connection;
import checkers.client.main.model.Game;
import checkers.client.main.util.Pair;

public abstract class State {
    public abstract State select(Pair p);

    public abstract void processPair(Game game, Pair p, Connection connection);
}
