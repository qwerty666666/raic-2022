package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.models.Unit;

public class RotateAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        order.setTargetDirection(
                unit.getDirection().rotate(Math.PI / 4).toVec2()
        );
    }
}
