package checkers.client.main.model.moves;

import checkers.client.main.controller.Connection;
import checkers.client.main.model.Game;
import checkers.client.main.util.Pair;

/**
 * State of game
 */
public abstract class State {
    /**
     * Select piece
     * @param p pair coordinates of piece
     * @return new state of game
     */
    public abstract State select(Pair p);

    /**
     * Process pair selection
     * @param game parent game object
     * @param p pair coordinates of piece
     * @param connection object that perform communications
     */
    public abstract void processPair(Game game, Pair p, Connection connection);
}
