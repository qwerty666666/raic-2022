package ai_cup_22.strategy.actions.basic;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;

public class LookToAction implements Action {
    private final Position target;

    public LookToAction(Unit target) {
        this(target.getPosition());
    }

    public LookToAction(Position target) {
        this.target = target;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        var velocity = new Vector(unit.getPosition(), target);

        order.setTargetDirection(velocity.toVec2());
    }
}
