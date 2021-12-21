package checkers.client.main.model.moves;

import checkers.client.main.util.Pair;

public abstract class State {
    public abstract State select(Pair p);
}
