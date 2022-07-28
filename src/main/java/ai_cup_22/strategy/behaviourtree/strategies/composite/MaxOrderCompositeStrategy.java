package ai_cup_22.strategy.behaviourtree.strategies.composite;

import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.behaviourtree.strategies.NullStrategy;
import ai_cup_22.strategy.behaviourtree.Strategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                .filter(s -> s.getOrder() > MIN_ORDER)
                .max(Comparator.comparingDouble(Strategy::getOrder))
                .orElseGet(NullStrategy::new);
    }

    @Override
    public String toString() {
        var winnerStrategy = getStrategy();
        var loosedStrategies = strategies.stream()
                .filter(s -> s != winnerStrategy)
                .collect(Collectors.toList());

        return Strategy.toString(this, winnerStrategy, loosedStrategies);
    }
}
