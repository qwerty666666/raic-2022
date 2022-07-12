package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.basic.TakeShieldPotionAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Unit;

public class TakeShieldPotionStrategy implements Strategy {
    private final Unit unit;
    private final double shieldPotionHealth = World.getInstance().getConstants().getShieldPerPotion();

    public TakeShieldPotionStrategy(Unit unit) {
        this.unit = unit;
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
        if (unit.getShieldPotions() == 0) {
            return false;
        }

        // two hits from  .. , or 4 hits from ..
        if (unit.getShield() > 160) {
            return false;
        }

        return World.getInstance().getEnemyUnits().values().stream()
                .allMatch(enemy -> enemy.getDistanceTo(unit) >= getMinDistToEnemyToSafelyRetreatForDisabledTime(enemy));
    }

    private double getMinDistToEnemyToSafelyRetreatForDisabledTime(Unit enemy) {
        var speedDiff = (enemy.getMaxForwardSpeedPerTick() - unit.getMaxBackwardSpeedPreTick());
        var retreatDistance = speedDiff * unit.getTicksToNewActionBeAvailable();

        return enemy.getThreatenDistanceFor(unit) + retreatDistance;
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
