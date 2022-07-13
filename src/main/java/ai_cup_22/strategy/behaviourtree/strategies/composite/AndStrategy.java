package ai_cup_22.strategy.behaviourtree.strategies.composite;

import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AndStrategy implements Strategy {
    private final List<Strategy> strategies = new ArrayList<>();

    public AndStrategy add(Strategy s) {
        strategies.add(s);
        return this;
    }

    @Override
    public double getOrder() {
        return strategies.stream()
                .mapToDouble(Strategy::getOrder)
                .max()
                .orElse(MIN_ORDER);
    }

    @Override
    public Action getAction() {
        return new CompositeAction()
                .add(strategies.stream()
                        .filter(s -> s.getOrder() > MIN_ORDER)
                        .map(Strategy::getAction)
                        .collect(Collectors.toList())
                );
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        sb.append(Strategy.toString(this) + " {\n");

        sb.append(strategies.stream()
                .map(s -> StringUtils.pad(s.toString(), 4))
                .collect(Collectors.joining("\n"))
        );

        sb.append("\n } \n");

        return sb.toString();
    }
}
