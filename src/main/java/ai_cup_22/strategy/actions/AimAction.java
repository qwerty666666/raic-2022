package ai_cup_22.strategy.actions;

import ai_cup_22.model.ActionOrder.Aim;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.models.Unit;

public class AimAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        order.setAction(new Aim(false));
    }
}
