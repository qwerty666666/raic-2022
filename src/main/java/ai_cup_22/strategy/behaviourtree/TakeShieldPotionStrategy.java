package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.basic.TakeShieldPotionAction;
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

        if ((unit.getMaxShield() - unit.getShield()) / 2 < shieldPotionHealth) {
            return false;
        }

        return getDistanceToNearestEnemy() > 30;
    }

    private double getDistanceToNearestEnemy() {
        return World.getInstance().getEnemyUnits().values().stream()
                .mapToDouble(enemy -> enemy.getDistanceTo(unit))
                .min()
                .orElse(Double.MAX_VALUE);
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
