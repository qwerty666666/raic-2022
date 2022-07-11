package ai_cup_22.strategy.actions;

import ai_cup_22.model.ActionOrder.Aim;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.models.Unit;

public class ShootAction implements Action {
    private final Unit target;

    public ShootAction(Unit target) {
        this.target = target;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        var shoot = unit.getShootingSegment().contains(target.getPosition()) && unit.canShoot(target);

        order.setAction(new Aim(shoot));

        new LookToAction(target).apply(unit, order);
    }
}
