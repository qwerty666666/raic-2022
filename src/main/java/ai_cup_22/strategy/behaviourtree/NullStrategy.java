package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.NullAction;

public class NullStrategy implements Strategy {
    @Override
    public double getOrder() {
        return MIN_ORDER;
    }

    @Override
    public Action getAction() {
        return new NullAction();
    }

    @Override
    public String toString() {
        return "NullStrategy";
    }
}
