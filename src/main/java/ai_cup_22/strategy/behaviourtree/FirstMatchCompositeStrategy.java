package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.actions.Action;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class FirstMatchCompositeStrategy implements Strategy {
    private final Map<Strategy, Supplier<Boolean>> strategies = new LinkedHashMap<>();

    public FirstMatchCompositeStrategy add(Supplier<Boolean> cond, Strategy strategy) {
        strategies.put(strategy, cond);
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
        return strategies.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(Entry::getKey)
                .findFirst()
                .orElseGet(NullStrategy::new);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " { " + getStrategy().toString() + " } ";
    }
}
