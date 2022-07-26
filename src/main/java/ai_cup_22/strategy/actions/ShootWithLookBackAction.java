package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.models.Unit;

public class ShootWithLookBackAction implements Action {
    private final ShootAction shootAction;

    public ShootWithLookBackAction(Unit me, Unit targetEnemy) {
        shootAction = new ShootAction(me, targetEnemy);
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        shootAction.apply(unit, order);

        if (!shootAction.isShouldStartAimingToEnemy()) {
            new LookBackAction().apply(unit, order);
        }
    }
}
