package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.basic.TakeShieldPotionAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.models.Unit;

public class TakeShieldPotionStrategy implements Strategy {
    private final Unit unit;
    private final FightStrategy fightStrategy;

    public TakeShieldPotionStrategy(Unit unit, FightStrategy fightStrategy) {
        this.unit = unit;
        this.fightStrategy = fightStrategy;
    }

    @Override
    public double getOrder() {
        return shouldTakePotion() ? MAX_ORDER : MIN_ORDER;
    }

    @Override
    public Action getAction() {
        return new TakeShieldPotionAction();
    }

    private boolean shouldTakePotion() {
        if (!unit.canDoNewAction()) {
            return false;
        }

        if (unit.getShieldPotions() == 0) {
            return false;
        }

        if (unit.getShield() > 150) {
            return false;
        }

        return fightStrategy.isOnSafeDistance();
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
