package checkers.client.main.model.moves;

import checkers.client.main.util.Pair;

public class StartMove extends State {
    @Override
    public State select(Pair p) {
        // todo set piece selected
        return new ProcessMove();
    }
}
