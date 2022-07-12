package ai_cup_22.strategy.behaviourtree;


import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.utils.StringUtils;
import java.util.Collections;
import java.util.List;

public interface Strategy {
    double MIN_ORDER = 0;
    double MAX_ORDER = 1;

    default double getOrder() {
        return MIN_ORDER;
    }

    Action getAction();

    static String toString(Strategy strategy) {
        return toString(strategy, null);
    }

    static String toString(Strategy strategy, Strategy child) {
        return toString(strategy, child, Collections.emptyList());
    }

    static String toString(Strategy strategy, Strategy child, List<Strategy> loosedStrategies) {
        StringBuilder sb = new StringBuilder();

        sb.append(strategy.getClass().getSimpleName() + " (" + strategy.getOrder() + ") ");

        if (child != null) {
            sb.append(" {\n");

            loosedStrategies.forEach(loosedStrategy -> {
                sb.append(StringUtils.pad("--" + toString(loosedStrategy), 4) + "\n");
            });

            sb.append(StringUtils.pad(child.toString(), 4));

            sb.append("\n } \n");
        }

        return sb.toString();
    }
}
