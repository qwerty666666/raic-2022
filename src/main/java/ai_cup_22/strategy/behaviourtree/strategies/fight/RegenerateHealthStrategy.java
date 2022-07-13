package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Unit;

public class RegenerateHealthStrategy implements Strategy {
    private final Unit unit;
    private final RetreatStrategy retreatStrategy;

    public RegenerateHealthStrategy(Unit unit, RetreatStrategy retreatStrategy) {
        this.unit = unit;
        this.retreatStrategy = retreatStrategy;
    }

    @Override
    public double getOrder() {
        if (unit.isRegeneratingHealth() || unit.getHealth() < unit.getMaxHealth() / 2) {
            return MAX_ORDER;
        }
        return MIN_ORDER;
    }

    @Override
    public Action getAction() {
        return retreatStrategy.getAction();
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
