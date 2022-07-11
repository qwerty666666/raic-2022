package ai_cup_22.strategy.behaviourtree;


import ai_cup_22.strategy.actions.Action;

public interface Strategy {
    double MIN_ORDER = 0;
    double MAX_ORDER = 1;

    default double getOrder() {
        return MIN_ORDER;
    }

    Action getAction();
}
