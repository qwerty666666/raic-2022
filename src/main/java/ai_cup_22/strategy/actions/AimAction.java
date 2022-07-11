package ai_cup_22.strategy.actions;

import ai_cup_22.model.ActionOrder.Aim;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.models.Unit;

public class AimAction implements Action {
    private boolean shoot;

    public AimAction() {
        this(false);
    }

    public AimAction(boolean shoot) {
        this.shoot = shoot;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        if (unit.getRemainingCoolDownTicks() <= unit.getTicksToFullAim()) {
            order.setAction(new Aim(shoot));
        }
    }
}
