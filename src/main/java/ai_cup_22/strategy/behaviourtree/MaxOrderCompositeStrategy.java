package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.actions.Action;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MaxOrderCompositeStrategy implements Strategy {
    private final List<Strategy> strategies = new ArrayList<>();

    public MaxOrderCompositeStrategy add(Strategy strategy) {
        strategies.add(strategy);
        return this;
    }

    @Override
    public double getOrder() {
        return getStrategy().getOrder();
    }

    @Override
    public Action getAction() {
        return getStrategy().getAction();
    }

    private Strategy getStrategy() {
        return strategies.stream()
                .max(Comparator.comparingDouble(Strategy::getOrder))
                .orElseGet(NullStrategy::new);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + getOrder() + ") { " + getStrategy().toString() + " } ";
    }
}
