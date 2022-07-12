package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;

public class RotateAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        new LookToAction(unit.getPosition().move(unit.getDirection().rotate(Math.PI / 4)))
                .apply(unit, order);
    }
}
